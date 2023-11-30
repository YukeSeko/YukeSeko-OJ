package com.wzy.question.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.wzy.common.annotation.AuthCheck;
import com.wzy.common.common.BaseResponse;
import com.wzy.common.common.DeleteRequest;
import com.wzy.common.common.ErrorCode;
import com.wzy.common.common.ResultUtils;
import com.wzy.common.constant.QuestionRedisConstant;
import com.wzy.common.constant.UserConstant;
import com.wzy.common.exception.BusinessException;
import com.wzy.common.exception.ThrowUtils;
import com.wzy.common.feign.UserFeignClient;
import com.wzy.common.model.dto.question.*;
import com.wzy.common.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wzy.common.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wzy.common.model.entity.Question;
import com.wzy.common.model.entity.QuestionSubmit;
import com.wzy.common.model.entity.User;
import com.wzy.common.model.vo.PerSonalDataVo;
import com.wzy.common.model.vo.QuestionSubmitVO;
import com.wzy.common.model.vo.QuestionVO;
import com.wzy.question.common.LockUpdateQuestion;
import com.wzy.question.service.QuestionService;
import com.wzy.question.service.QuestionSubmitService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 题目接口
 */
@RestController
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private final static Gson GSON = new Gson();

    private int count = 0;

    @Resource
    private LockUpdateQuestion lockUpdateQuestion;

    // region 增删改查


    @ApiOperation("获取题目答案")
    @GetMapping("/getQuestionAnswer")
    public BaseResponse<String> getQuestionAnswer(Long questionId) {
        String answer = questionService.getQuestionAnswerById(questionId);
        return ResultUtils.success(answer);
    }


    @ApiOperation("获取前端个人数据总览")
    @GetMapping("/getPersonalData")
    public BaseResponse<PerSonalDataVo> getPersonalData(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return questionSubmitService.getPersonalData(loginUser);
    }


    @ApiOperation("创建题目")
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    @ApiOperation("删除题目")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    @ApiOperation("更新（仅管理员）")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = lockUpdateQuestion.lockUpdateQuestion(question);
        return ResultUtils.success(result);
    }

    @ApiOperation("根据 id 获取")
    @GetMapping("/get")
    public BaseResponse<Question> getQuestionById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 不是本人或管理员，不能直接获取所有信息
        if (!question.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(question);
    }

    @ApiOperation("根据 id 获取（脱敏）")
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    @ApiOperation("分页获取列表（封装类）")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        //引入缓存
        String questionVo = redisTemplate.opsForValue().get(QuestionRedisConstant.questionPageKey);
        if (questionVo != null) {
            //缓存命中的话，直接返回
            Page<QuestionVO> bean = JSONUtil.toBean(questionVo, Page.class);
            return ResultUtils.success(bean);
        }
        //未命中缓存，加锁进行，解决缓存击穿问题
        RReadWriteLock lock = redissonClient.getReadWriteLock(QuestionRedisConstant.redissonLock);
        RLock readOnlyLock = redissonClient.getLock(QuestionRedisConstant.readOnlyLock);
        Page<QuestionVO> questionVOPage = null;
        try {
            boolean b = lock.readLock().tryLock(10, 10, TimeUnit.SECONDS);
            //尝试加锁，如果当前线程拿到了读锁，再尝试拿可重入锁，这样可以解决读锁多次查询数据库的问题
            boolean b1 = readOnlyLock.tryLock(10, 10, TimeUnit.SECONDS);
            if (b && b1) {
                //如果两个锁都拿到了
                String question = redisTemplate.opsForValue().get(QuestionRedisConstant.questionPageKey);
                if (question != null) {
                    //拿到锁后，验证缓存是否存在，如果存在，直接返回
                    Page<QuestionVO> bean = JSONUtil.toBean(question, Page.class);
                    questionVOPage = bean;
                }else {
                    //尝试获取锁成功
                    long current = questionQueryRequest.getCurrent();
                    long size = questionQueryRequest.getPageSize();
                    // 限制爬虫
                    ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
                    Page<Question> questionPage = questionService.page(new Page<>(current, size),
                            questionService.getQueryWrapper(questionQueryRequest));
                    questionVOPage = questionService.getQuestionVOPage(questionPage, request);
                    //设置随机过期时间，解决缓存雪崩的问题
                    redisTemplate.opsForValue().set(QuestionRedisConstant.questionPageKey, String.valueOf(JSONUtil.parse(questionVOPage)), RandomUtil.randomInt(1, 5), TimeUnit.MINUTES);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            //解锁
            lock.readLock().unlock();
            readOnlyLock.unlock();
        }
        //没有加入缓存的情况
//        long current = questionQueryRequest.getCurrent();
//        long size = questionQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<Question> questionPage = questionService.page(new Page<>(current, size),
//                questionService.getQueryWrapper(questionQueryRequest));
//        Page<QuestionVO> questionVOPage = questionService.getQuestionVOPage(questionPage, request);
//        //设置随机过期时间，解决缓存雪崩的问题
//        redisTemplate.opsForValue().set(QuestionRedisConstant.questionPageKey, String.valueOf(JSONUtil.parse(questionVOPage)), RandomUtil.randomInt(1, 5), TimeUnit.MINUTES);
        return ResultUtils.success(questionVOPage);
    }

    @ApiOperation("分页获取当前用户创建的资源列表")
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }


    @ApiOperation("分页获取题目列表（仅管理员）")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                           HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionEditRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = lockUpdateQuestion.lockUpdateQuestion(question);
        return ResultUtils.success(result);
    }

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的 id
     */
    @PostMapping("/question_submit/do")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交列表（除了管理员外，普通用户只能看到非答案、提交代码等公开信息）
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/question_submit/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 从数据库中查询原始的题目提交分页信息
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        final User loginUser = userService.getLoginUser(request);
        // 返回脱敏信息
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }


}
