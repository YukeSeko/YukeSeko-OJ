package com.wzy.question.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.common.common.BaseResponse;
import com.wzy.common.common.ErrorCode;
import com.wzy.common.common.ResultUtils;
import com.wzy.common.constant.CommonConstant;
import com.wzy.common.constant.RabbitMqConstant;
import com.wzy.common.exception.BusinessException;
import com.wzy.common.feign.UserFeignClient;
import com.wzy.common.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wzy.common.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wzy.common.model.entity.*;
import com.wzy.common.model.enums.JudgeInfoMessageEnum;
import com.wzy.common.model.enums.QuestionSubmitLanguageEnum;
import com.wzy.common.model.enums.QuestionSubmitStatusEnum;
import com.wzy.common.model.vo.PerSonalDataVo;
import com.wzy.common.model.vo.QuestionSubmitVO;
import com.wzy.common.model.vo.QuestionVO;
import com.wzy.common.model.vo.UserVO;
import com.wzy.common.utils.SqlUtils;
import com.wzy.question.common.LockUpdateQuestion;
import com.wzy.question.mapper.QuestionSubmitMapper;
import com.wzy.question.rabbitmq.MessageProducer;
import com.wzy.question.service.QuestionService;
import com.wzy.question.service.QuestionSolveService;
import com.wzy.question.service.QuestionSubmitService;
import jodd.bean.BeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userService;

    @Resource
    private MessageProducer messageProducer;

    @Resource
    private QuestionSolveService questionSolveService;

    @Resource
    private LockUpdateQuestion lockUpdateQuestion;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        question.setSubmitNum(question.getSubmitNum() + 1);
        boolean b = lockUpdateQuestion.lockUpdateQuestion(question);
        if (!save || !b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        // 执行判题服务：向mq中发送消息
        messageProducer.sendMessage(RabbitMqConstant.exchange, RabbitMqConstant.routing_key, String.valueOf(questionSubmitId));
//        CompletableFuture.runAsync(() -> {
//            // 向mq中发送消息
//            judgeService.doJudge(questionSubmitId);
//        });
        return questionSubmitId;
    }


    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 不同）提交的代码
        long userId = loginUser.getId();
        // 处理脱敏
        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    /**
     * 分页获取题目封装
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
//        if (CollectionUtils.isEmpty(questionSubmitList)) {
//            return questionSubmitVOPage;
//        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(loginUser, userVO);
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(m -> {
                    QuestionSubmitVO questionSubmitVO = getQuestionSubmitVO(m, loginUser);
                    questionSubmitVO.setUserVO(userVO);
                    String judgeInfo = m.getJudgeInfo();
                    JudgeInfo bean = JSONUtil.toBean(judgeInfo, JudgeInfo.class);
                    String message = bean.getMessage();
                    if (message != null) {
                        questionSubmitVO.setJudgeInfo(Objects.requireNonNull(JudgeInfoMessageEnum.getEnumByValue(message)).getText());
                        questionSubmitVO.setDetailsInfo(bean);
                    } else {
                        questionSubmitVO.setJudgeInfo("暂无判题信息");
                        questionSubmitVO.setDetailsInfo(null);
                    }
                    //获取判题信息，设置到对象中去
                    Long questionId = m.getQuestionId();
                    Question question = questionService.getById(questionId);
                    QuestionVO questionSubmitVO1 = new QuestionVO();
                    BeanUtils.copyProperties(question, questionSubmitVO1);
                    questionSubmitVO.setQuestionVO(questionSubmitVO1);
                    return questionSubmitVO;
                }).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }


    /**
     * 获取个人数据总览
     *
     * @param loginUser
     * @return
     */
    @Override
    public BaseResponse<PerSonalDataVo> getPersonalData(User loginUser) {
        Long id = loginUser.getId();
        //1、获取用户提交了多少次
        long commitCount = this.count(new QueryWrapper<QuestionSubmit>().eq("userId", id));
        //2、获取该用户已经提交通过的题目数
        long questionSolveCount = questionSolveService.count(new QueryWrapper<QuestionSolve>().eq("userId", id));
        //3、获取总题量
        long questionCount = questionService.count();
        HashMap<String, Long> hashMap = new HashMap<>();
        hashMap.put("commitCount", commitCount);
        hashMap.put("questionSolveCount", questionSolveCount);
        hashMap.put("questionCount", questionCount);
        String jsonStr = JSONUtil.toJsonStr(hashMap);
        PerSonalDataVo bean = JSONUtil.toBean(jsonStr, PerSonalDataVo.class);
        return ResultUtils.success(bean);
    }
}




