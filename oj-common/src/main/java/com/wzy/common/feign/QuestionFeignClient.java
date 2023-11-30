package com.wzy.common.feign;


import com.wzy.common.model.entity.Question;
import com.wzy.common.model.entity.QuestionSolve;
import com.wzy.common.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "oj-question-service", path = "/api/question/inner")
public interface QuestionFeignClient {

    @GetMapping("/get/id")
    Question getQuestionById(@RequestParam("questionId") long questionId);

    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionId") long questionSubmitId);

    @PostMapping("/question_submit/update")
    boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);

    /**
     * 更新题目通过率
     * @param questionId
     * @return
     */
    @PostMapping("/question_submit/updateAccepted")
    boolean updateQuestionById(@RequestParam("questionId") long questionId);

    /**
     * 更新用户提交通过的题目
     * @param questionSolve
     * @return
     */
    @PostMapping("/question_submit/createQuestionSolve")
    boolean createQuestionSolve(@RequestBody QuestionSolve questionSolve);
}
