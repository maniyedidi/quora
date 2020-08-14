package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = SignUpRestrictedException.class)
    public UserEntity signup(UserEntity userEntity)  throws SignUpRestrictedException{
        try {
            return userDao.createUser(userEntity);
        }catch (DataIntegrityViolationException e) {
            System.out.println("Message >>>> " +  e.getMessage());
            if (e.getMessage().contains("users_email_key")) {
                throw new SignUpRestrictedException("SGR-001", "This user has already been registered, try with any other emailId");
            } else {
                throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
            }
        }
    }

    public UserEntity signin(final String uuid){
            return  userDao.getUser(uuid);
    }
}
