package com.wzy.question.controller.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzy.common.feign.QuestionFeignClient;
import com.wzy.common.model.entity.Question;
import com.wzy.common.model.entity.QuestionSolve;
import com.wzy.common.model.entity.QuestionSubmit;
import com.wzy.question.service.QuestionService;
import com.wzy.question.service.QuestionSolveService;
import com.wzy.question.service.QuestionSubmitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author YukeSeko
 * @Since 2023/10/9 14:55
 */
@RestController
@RequestMapping("/inner")
public class QuestionInnerController implements QuestionFeignClient {
    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSolveService questionSolveService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @GetMapping("/get/id")
    @Override
    public Question getQuestionById(@RequestParam("questionId") long questionId) {
        return questionService.getById(questionId);
    }

    @GetMapping("/question_submit/get/id")
    @Override
    public QuestionSubmit getQuestionSubmitById(@RequestParam("questionId") long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }

    @PostMapping("/question_submit/update")
    @Override
    public boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }

    @PostMapping("/question_submit/updateAccepted")
    @Override
    public boolean updateQuestionById(long questionId) {
        Question byId = questionService.getById(questionId);
        byId.setAcceptedNum(byId.getAcceptedNum() + 1);
        return questionService.updateById(byId);
    }

    @PostMapping("/question_submit/createQuestionSolve")
    @Override
    public boolean createQuestionSolve(@RequestBody QuestionSolve questionSolve) {
        Long questionId = questionSolve.getQuestionId();
        Long userId = questionSolve.getUserId();
        long count = questionSolveService.count(new QueryWrapper<QuestionSolve>().eq("questionId", questionId).eq("userId", userId));
        if (count > 0){
            return true;
        }else {
            return questionSolveService.save(questionSolve);
        }
    }

}
