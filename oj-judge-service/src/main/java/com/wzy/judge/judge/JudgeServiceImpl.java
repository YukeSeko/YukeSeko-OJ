package com.wzy.judge.judge;

import cn.hutool.json.JSONUtil;

import com.wzy.common.common.ErrorCode;
import com.wzy.common.exception.BusinessException;
import com.wzy.common.feign.QuestionFeignClient;
import com.wzy.common.model.dto.question.JudgeCase;
import com.wzy.common.model.entity.JudgeInfo;
import com.wzy.common.model.entity.Question;
import com.wzy.common.model.entity.QuestionSolve;
import com.wzy.common.model.entity.QuestionSubmit;
import com.wzy.common.model.enums.JudgeInfoMessageEnum;
import com.wzy.common.model.enums.QuestionSubmitStatusEnum;
import com.wzy.judge.judge.codesandbox.CodeSandBox;
import com.wzy.judge.judge.codesandbox.CodeSandboxFactory;
import com.wzy.judge.judge.codesandbox.CodeSandboxProxy;
import com.wzy.judge.judge.codesandbox.model.ExecuteCodeRequest;
import com.wzy.judge.judge.codesandbox.model.ExecuteCodeResponse;
import com.wzy.judge.judge.strategy.DefaultJudgeStrategy;
import com.wzy.judge.judge.strategy.JudgeContext;
import com.wzy.judge.judge.strategy.JudgeStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {


    @Resource
    private QuestionFeignClient questionService;



    @Value("${codesandbox.type:example}")
    private String type;


    /**
     * 判题服务
     *
     * @param questionSubmitId
     * @return
     */
    @Override
//    @Transactional(rollbackFor = Exception.class)
    public QuestionSubmit doJudge(long questionSubmitId) {
        QuestionSubmit questionSubmit = questionService.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2）如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionService.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 4）调用沙箱，获取到执行结果
        CodeSandboxFactory codeSandboxFactory = new CodeSandboxFactory();
        CodeSandBox codeSandBox = codeSandboxFactory.newInstance(type);
        codeSandBox = new CodeSandboxProxy(codeSandBox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).build();
        ExecuteCodeResponse executeCodeResponse = null;
        //修改判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        try {
            executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
        } catch (Exception e) {
            JudgeInfo judgeInfo = new JudgeInfo();
            //远程代码执行异常
            judgeInfo.setMessage(JudgeInfoMessageEnum.REMOTE_ERROR.getValue());
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
            questionService.updateQuestionSubmitById(questionSubmitUpdate);
            throw new BusinessException(ErrorCode.API_REQUEST_TIMEOUT_ERROR);
        }
        //拿到题目的结果输出
        List<String> outputList = executeCodeResponse.getOutputList();
        // 5）根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        //执行判题
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        JudgeInfo judgeInfo = judgeStrategy.doJudge(judgeContext);
        if (judgeInfo.getMessage().equals(JudgeInfoMessageEnum.ACCEPTED.getValue())) {
            //判题成功
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
            // 更新完状态后，需要修改question中的题目通过率
            questionService.updateQuestionById(questionId);
            // 更新当前用户的题目解决情况
            QuestionSolve questionSolve = new QuestionSolve();
            questionSolve.setQuestionId(questionId);
            questionSolve.setUserId(questionSubmit.getUserId());
            questionSolve.setTags(question.getTags());
            questionSolve.setTitle(question.getTitle());
            questionService.createQuestionSolve(questionSolve);
        } else {
            //其他清空均为判题失败
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
        }
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionService.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        return questionService.getQuestionSubmitById(questionId);
    }
}
