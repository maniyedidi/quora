package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDao {
    @PersistenceContext
    private EntityManager entityManager;

    public QuestionEntity getQuestionById(String questionId){
        try {
            return entityManager.createNamedQuery("getQuestionByUuid", QuestionEntity.class).setParameter("uuid", questionId).getSingleResult();
        }catch (Exception e){
            return  null;
        }
    }

    public QuestionEntity createQuestion(QuestionEntity questionEntity){
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    public List<QuestionEntity> getAllQuestions() {
        return entityManager.createNamedQuery("allQuestions", QuestionEntity.class).getResultList();
    }

    public QuestionEntity getQuestionByUUID(String uuid) {
        try {
            return entityManager.createNamedQuery("getQuestionByUuid", QuestionEntity.class).setParameter(
                    "uuid",
                    uuid).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public QuestionEntity updateQuestion(QuestionEntity question) {
        entityManager.merge(question);
        return question;
    }

    public QuestionEntity deleteQuestion(QuestionEntity questionEntity) {
        entityManager.remove(questionEntity);
        return questionEntity;
    }

    public List<QuestionEntity> getAllQuestionsByUser(long userId) {
        return entityManager.createNamedQuery("allQuestionsByUser", QuestionEntity.class)
                .setParameter("user_id", userId).getResultList();
    }


}
