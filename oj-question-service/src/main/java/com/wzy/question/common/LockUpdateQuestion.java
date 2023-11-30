package com.wzy.question.common;

import com.wzy.common.constant.QuestionRedisConstant;
import com.wzy.common.model.entity.Question;
import com.wzy.question.service.QuestionService;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author YukeSeko
 * @Since 2023/11/28 17:14
 */
@Component
public class LockUpdateQuestion {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private QuestionService questionService;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 更新问题
     * @param question
     * @return
     */
    public boolean lockUpdateQuestion(Question question){
        //获取同一个锁
        RReadWriteLock lock = redissonClient.getReadWriteLock(QuestionRedisConstant.redissonLock);
        boolean ok = false;
        try {
            //拿到读锁，且同一个锁，在数据库更新时，阻塞其他所有的读和写
            boolean b = lock.writeLock().tryLock(10, 10, TimeUnit.SECONDS);
            if (b){
                //成功拿到读锁，1、先删除redis中的数据
                redisTemplate.delete(QuestionRedisConstant.questionPageKey);
                // 2、更新数据库
                ok = questionService.updateById(question);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.writeLock().unlock();
        }
        return ok;
    }
}
