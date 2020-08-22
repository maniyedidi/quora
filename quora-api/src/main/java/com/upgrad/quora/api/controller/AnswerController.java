package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.api.model.QuestionDeleteResponse;
import com.upgrad.quora.api.model.QuestionDetailsResponse;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.common.AuthTokenParser;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/")
public class AnswerController {
    @Autowired
    private AnswerBusinessService answerBusinessService;
    @Autowired
    private QuestionBusinessService questionBusinessService;


    @RequestMapping(path = "/question/{questionId}/answer/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@RequestHeader("authorization") final String authorization, @PathVariable(name = "questionId") final String questionId, AnswerRequest answerRequest) throws AuthorizationFailedException, InvalidQuestionException {

        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAnswer(answerRequest.getAnswer());
        UserEntity userEntity;
        if (authorization.startsWith("Bearer ")) {
             answerBusinessService.createAnswer(authorization.split("Bearer ")[1],questionId,answerEntity);
        } else {
            throw new AuthorizationFailedException("ATH-004", "Invalid token");
        }

        AnswerResponse answerResponse = new AnswerResponse().id(answerEntity.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    @RequestMapping(path = "/answer/edit/{answerId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> EditAnswer(@RequestHeader("authorization") final String authorization, @PathVariable(name = "answerId") final String answerId, AnswerRequest answerRequest) throws AuthorizationFailedException, AnswerNotFoundException {

        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAnswer(answerRequest.getAnswer());
        UserEntity userEntity;
        if (authorization.startsWith("Bearer ")) {
            answerBusinessService.EditAnswer(authorization.split("Bearer ")[1],answerId,answerEntity);
        } else {
            throw new AuthorizationFailedException("ATH-004", "Invalid token");
        }

        AnswerResponse answerResponse = new AnswerResponse().id(answerEntity.getUuid()).status("ANSWER EDITED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnswerResponse> deleteAnswer(@PathVariable("answerId") String answerId, @RequestHeader("authorization") String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        String accessToken = AuthTokenParser.parseAuthToken(authorization);
        AnswerEntity deletedAnswer = answerBusinessService.deleteAnswer(accessToken, answerId);
        AnswerResponse answerResponse = new AnswerResponse().id(deletedAnswer.getUuid()).status("ANSWER DELETED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerResponse>> getAllAnswersToQuestion(@PathVariable("questionId") String questionId, @RequestHeader("authorization") String authorization) throws AuthorizationFailedException, InvalidQuestionException  {
        List<AnswerResponse> answerList = new ArrayList<AnswerResponse>();
        List<AnswerEntity> answerEntityList = answerBusinessService.getAllAnswersToQuestion(authorization, questionId);
        for (AnswerEntity answerEntity  : answerEntityList) {
            answerList.add(new AnswerResponse().id(answerEntity.getUuid())
                    .answerContent(answerEntity.getAnswer()).questionContent(answerEntity.getQuestion().getContent()));
        }

        return new ResponseEntity<>(answerList, HttpStatus.OK);
    }

    }
}
