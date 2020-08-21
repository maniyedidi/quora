package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommonService {

    @Autowired
    UserDao userDao;

    public UserEntity userProfile(final String authorization, final String userId) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByToekn(authorization);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
        }
        UserEntity userEntity = userDao.getUserByUuid(userId);
        if (userEntity == null) {
            throw new AuthorizationFailedException("USR-001", "User with entered uuid does not exist");
        }
        return userEntity;
    }

}
