package com.wzy.judge.judge.codesandbox;


import com.wzy.judge.judge.codesandbox.model.ExecuteCodeRequest;
import com.wzy.judge.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 * @author YukeSeko
 * @Since 2023/9/25 15:37
 */
public interface CodeSandBox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
