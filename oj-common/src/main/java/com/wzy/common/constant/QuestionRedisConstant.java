package com.wzy.common.constant;

/**
 * @author YukeSeko
 * @Since 2023/11/27 16:36
 */
public interface QuestionRedisConstant {

    String questionPageKey="oj:questionVoPage";

    String redissonLock = "questionRedissonLock";

    String readOnlyLock = "readOnlyLock";
}
