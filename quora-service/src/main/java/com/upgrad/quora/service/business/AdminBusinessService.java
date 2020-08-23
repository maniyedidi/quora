package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.AuthTokenParser;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBusinessService {

    @Autowired
    UserDao userDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity userDelete(final String authorization, final String userId) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByToekn(AuthTokenParser.parseAuthToken((authorization)));
        UserEntity userEntity = userDao.getUserByUuid(userId);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out");
        } else if (userAuthEntity.getUser().getRole().equals("nonadmin")) {
            throw new AuthorizationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
        } else if (userEntity == null) {
            throw new AuthorizationFailedException("USR-001", "User with entered uuid to be deleted does not exist");
        }
        userDao.deleteUserByUuid(userEntity);
        return userEntity;
    }

}
