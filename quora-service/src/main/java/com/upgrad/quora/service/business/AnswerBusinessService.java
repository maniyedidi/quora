package com.upgrad.quora.service.business;


import com.upgrad.quora.service.common.AuthTokenParser;
import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class AnswerBusinessService {

    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private QuestionDao questionDao;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = InvalidQuestionException.class)
    public AnswerEntity createAnswer(String authorization, String questionId, AnswerEntity answerEntity) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByToekn(AuthTokenParser.parseAuthToken(authorization));
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out");
        }
        QuestionEntity questionEntity = questionDao.getQuestionById(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        answerEntity.setQuestion(questionEntity);
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setUser(userAuthEntity.getUser());
        return answerDao.createAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = InvalidQuestionException.class)
    public AnswerEntity EditAnswer(String authorization, String answerId, String updatedAnswer) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByToekn(AuthTokenParser.parseAuthToken(authorization));
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out");
        }
        AnswerEntity answerEntity = answerDao.getAnswerByUUID(answerId);
        if (answerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "The question entered is invalid");
        } else if (!answerEntity.getUser().getUuid().equals(userAuthEntity.getUser().getUuid())) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
        }
        answerEntity.setAnswer(updatedAnswer);
        return answerDao.EditAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(String accessToken, String uuid) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthEntity userAuth = userDao.getUserAuthByToekn(accessToken);
        if (userAuth == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuth.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete an answer ");
        }
        AnswerEntity answerEntity = answerDao.getAnswerByUUID(uuid);
        if (answerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "'Entered answer uuid does not exist");
        }
        UserEntity authUser = userAuth.getUser();
        if (authUser.getRole().equals("nonadmin") && !authUser.getUserName().equals(answerEntity.getUser().getUserName())) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
        }
        answerDao.deleteAnswer(answerEntity);
        return answerEntity;
    }

    public List<AnswerEntity> getAllAnswersToQuestion(String accessToken, String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuth = userDao.getUserAuthByToekn(AuthTokenParser.parseAuthToken(accessToken));
        if (userAuth == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuth.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete an answer ");
        }
        QuestionEntity questionEntity = questionDao.getQuestionByUUID(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
        }
        return answerDao.getAllAnswersByQuestionId(questionEntity);
    }
}
