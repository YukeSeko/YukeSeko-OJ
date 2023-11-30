package com.wzy.judge.judge.codesandbox.impl;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.wzy.common.common.ErrorCode;
import com.wzy.common.exception.BusinessException;
import com.wzy.judge.judge.codesandbox.CodeSandBox;
import com.wzy.judge.judge.codesandbox.model.ExecuteCodeRequest;
import com.wzy.judge.judge.codesandbox.model.ExecuteCodeResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 *
 * @author YukeSeko
 * @Since 2023/9/25 15:48
 */
public class RemoteCodeSandbox implements CodeSandBox {

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRET = "secretKey";

    private static final String url = "http://122.9.148.119:8090/executeCode";
    /**
     * 远程调用docker执行代码
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = null;
        try {
            responseStr = HttpUtil.createPost(url)
                    .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
                    .body(json)
//                    .timeout(20000)
                    .execute()
                    .body();
        }catch (Exception e){
            // 远程执行代码超时
            throw new RuntimeException();
        }
        if (StringUtils.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error, message = " + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
