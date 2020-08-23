package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.AuthTokenParser;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class QuestionBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private QuestionDao questionDao;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = InvalidQuestionException.class)
    public QuestionEntity createQuestion(String authorization, QuestionEntity questionEntity) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByToekn(AuthTokenParser.parseAuthToken((authorization)));
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out");
        }

        questionEntity.setDate(ZonedDateTime.now());
        questionEntity.setUser(userAuthEntity.getUser());
        return questionDao.createQuestion(questionEntity);
    }

    public List<QuestionEntity> getAllQuestions(String accessToken) throws AuthorizationFailedException {
        UserAuthEntity userAuth = userDao.getUserAuthByToekn(accessToken);
        if (userAuth == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuth.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get"
                    + " all questions");
        }
        return questionDao.getAllQuestions();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestion(String accessToken, String uuid) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuth = userDao.getUserAuthByToekn(accessToken);
        if (userAuth == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuth.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to "
                    + "delete a question");
        }

        QuestionEntity question = questionDao.getQuestionByUUID(uuid);
        if (question == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
        UserEntity authUser = userAuth.getUser();
        if (authUser.getRole().equals("nonadmin") && !authUser.getUserName()
                .equals(question.getUser().getUserName())) {
            throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can "
                    + "delete the question");
        }
        QuestionEntity deletedQuestion = questionDao.deleteQuestion(question);
        return deletedQuestion;
    }

    public List<QuestionEntity> getAllQuestionsByUser(String accessToken, String userId)
            throws AuthorizationFailedException, UserNotFoundException {
        UserAuthEntity userAuth = userDao.getUserAuthByToekn(accessToken);
        if (userAuth == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuth.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get "
                    + "all questions posted by a specific user");
        }
        UserEntity userEntity = userDao.getUserByUuid(userId);
        if (userEntity == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid whose question details "
                    + "are to be seen does not exist");
        }
        return questionDao.getAllQuestionsByUser(userEntity.getId());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity updateQuestion(String accessToken, String questionId, String content) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuth = userDao.getUserAuthByToekn(accessToken);
        if (userAuth == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuth.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit" + " the question");
        }
        QuestionEntity question = questionDao.getQuestionByUUID(questionId);
        if (question == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
        if (!question.getUser().getUserName().equals(userAuth.getUser().getUserName())) {
            throw new AuthorizationFailedException("ATHR-003", "Only the question owner can edit the " + "question");
        }
        question.setContent(content);
        return questionDao.updateQuestion(question);
    }

}
