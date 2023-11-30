package com.wzy.ojcodesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 *
 * @author 王灼宇
 * @Since 2023/10/8 14:28
 */
@Data
public class ExecuteMessage {

    private Integer exitValue;

    private String message;

    private String errorMessage;

    private Long time;

    private Long memory;

}
