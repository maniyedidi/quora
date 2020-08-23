package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.common.AuthTokenParser;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
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
public class QuestionController {

    @Autowired
    private QuestionBusinessService questionBusinessService;

    @RequestMapping(path = "/question/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(@RequestHeader("authorization") final String authorization, QuestionRequest questionRequest) throws AuthorizationFailedException, InvalidQuestionException {

        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        questionBusinessService.createQuestion(authorization,questionEntity);
        QuestionResponse questionResponse = new QuestionResponse().id(questionEntity.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") String authorization) throws AuthorizationFailedException {
        String accessToken = AuthTokenParser.parseAuthToken(authorization);
        List<QuestionEntity> qList = questionBusinessService.getAllQuestions(accessToken);
        List<QuestionDetailsResponse> qDetailsList = new ArrayList<>();
        qList.forEach((q) -> {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
            questionDetailsResponse.id(q.getUuid());
            questionDetailsResponse.content(q.getContent());
            qDetailsList.add(questionDetailsResponse);
        });
        return new ResponseEntity<List<QuestionDetailsResponse>>(qDetailsList, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(@PathVariable("questionId") String questionId, @RequestHeader("authorization") String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        String accessToken = AuthTokenParser.parseAuthToken(authorization);
        QuestionEntity deletedQuestion = questionBusinessService.deleteQuestion(accessToken, questionId);
        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse().id(deletedQuestion.getUuid()).status("QUESTION DELETED");
        return new ResponseEntity<>(questionDeleteResponse, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/question/all/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(@PathVariable("userId") String userId, @RequestHeader("authorization") String authorization) throws AuthorizationFailedException, UserNotFoundException {
        String accessToken = AuthTokenParser.parseAuthToken(authorization);
        List<QuestionEntity> qList = questionBusinessService.getAllQuestionsByUser(accessToken, userId);
        List<QuestionDetailsResponse> qDetailsList = new ArrayList<>();
        qList.forEach((q) -> {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
            questionDetailsResponse.content(q.getContent());
            questionDetailsResponse.id(q.getUuid());
            qDetailsList.add(questionDetailsResponse);
        });
        return new ResponseEntity<>(qDetailsList, HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(@PathVariable("questionId") String questionId, final QuestionEditRequest questionEditRequest, @RequestHeader("authorization") String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        String accessToken = AuthTokenParser.parseAuthToken(authorization);
        String content = questionEditRequest.getContent();
        QuestionEntity updatedQuestion = questionBusinessService.updateQuestion(accessToken, questionId, content);
        QuestionEditResponse questionEditResponse = new QuestionEditResponse();
        questionEditResponse.setId(updatedQuestion.getUuid());
        questionEditResponse.setStatus("QUESTION EDITED");
        return new ResponseEntity<>(questionEditResponse, HttpStatus.OK);
    }
}
