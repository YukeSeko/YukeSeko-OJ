package com.wzy.question.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzy.common.model.entity.Question;
import org.apache.ibatis.annotations.Param;

public interface QuestionMapper extends BaseMapper<Question> {

    String getQuestionAnswerById(@Param("questionId") Long questionId);
}




