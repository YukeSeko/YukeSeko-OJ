package com.wzy.user.common;

import cn.hutool.extra.mail.MailUtil;
import com.wzy.common.common.ErrorCode;
import com.wzy.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author YukeSeko
 * @Since 2023/9/18 15:33
 */
@Component
@Slf4j
public class MailUtils {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private RedisTokenBucket redisTokenBucket;

    @Autowired
    private ThreadPoolExecutor executor;

    //标题
    private static String subject = "YukeSeko在线判题OJ系统";


    /**
     * 尝试获取一个令牌，如果成功了，那么返回true ，失败返回false，表示限流
     * @param email
     * @param code
     * @return
     */
    private boolean sendEmailAuth(String email, String code) {
        if (redisTokenBucket.tryAcquire(email)) {
            // 通过验证验证后，向redis中写入数据
            redisTemplate.opsForValue().set(email, code, 1 , TimeUnit.MINUTES);
            return true;
        } else {
            log.error("send email to " + email + " rejected due to rate limiting");
            return false;
        }
    }

    public void sendAuthCodeEmail(String email) {
        String code = randomCode();
        String content = "尊敬的用户:\n" +
                "你好! 你的验证码为:  " + code + "  (有效期为5分钟)";
        if (!sendEmailAuth(email,code)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"验证码发送太快");
        }else {
            //验证通过，异步多线程实现
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (this){
                        MailUtil.send(email, subject, content, false);
                    }
                }
            });
        }
    }

    /**
     * 随机生成6位数的验证码
     *
     * @return String code
     */
    @NotNull
    private String randomCode() {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }

    /**
     * 删除redis中存储的数据
     * @param email
     */
    public void deleteRedisEmail(String email){
        redisTemplate.delete(email);
    }
}
