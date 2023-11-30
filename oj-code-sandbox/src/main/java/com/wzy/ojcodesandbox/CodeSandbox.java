package com.wzy.ojcodesandbox;

import com.wzy.ojcodesandbox.model.ExecuteCodeRequest;
import com.wzy.ojcodesandbox.model.ExecuteCodeResponse;

/**
 *代码沙箱接口定义
 * @author 王灼宇
 * @Since 2023/10/8 14:28
 */
public interface CodeSandbox {
    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
