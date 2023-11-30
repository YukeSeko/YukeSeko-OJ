package com.wzy.judge.judge.codesandbox.impl;



import com.wzy.common.model.entity.JudgeInfo;
import com.wzy.common.model.enums.JudgeInfoMessageEnum;
import com.wzy.common.model.enums.QuestionSubmitStatusEnum;
import com.wzy.judge.judge.codesandbox.CodeSandBox;
import com.wzy.judge.judge.codesandbox.model.ExecuteCodeRequest;
import com.wzy.judge.judge.codesandbox.model.ExecuteCodeResponse;

import java.util.List;

/**
 * 示例代码沙箱（仅为了跑通业务流程）
 * @author YukeSeko
 * @Since 2023/9/25 15:47
 */
public class ExampleCodeSandbox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("测试执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;

    }
}
