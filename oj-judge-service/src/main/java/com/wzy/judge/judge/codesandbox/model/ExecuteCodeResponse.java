package com.wzy.judge.judge.codesandbox.model;


import com.wzy.common.model.entity.JudgeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Builder： 使用建造者模式，让我们可以通过以下方式进行使用
 *  ExecuteCodeResponse.builder().outputList().message().status().judgeInfo().build()
 *
 * @author YukeSeko
 * @Since 2023/9/25 15:43
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

    private List<String> outputList;

    /**
     * 接口信息
     */
    private String message;

    /**
     * 执行状态
     */
    private Integer status;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;

}
