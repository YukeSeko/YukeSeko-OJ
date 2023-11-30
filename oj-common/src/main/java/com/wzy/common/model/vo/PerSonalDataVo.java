package com.wzy.common.model.vo;

import lombok.Data;

/**
 * @author YukeSeko
 * @Since 2023/10/25 17:18
 */
@Data
public class PerSonalDataVo {
    //用户提交了多少次
    private Long commitCount;
    //用户已经提交通过的题目数
    private Long questionSolveCount;
    //获取总题量
    private Long questionCount;

}
