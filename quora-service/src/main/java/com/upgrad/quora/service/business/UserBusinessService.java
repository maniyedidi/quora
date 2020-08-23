package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.AuthTokenParser;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = SignUpRestrictedException.class)
    public UserEntity signup(UserEntity userEntity) throws SignUpRestrictedException {
        try {
            return userDao.createUser(userEntity);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("users_email_key")) {
                throw new SignUpRestrictedException("SGR-001", "This user has already been registered, try with any other emailId");
            } else {
                throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authenticate(final String username, final String password) throws AuthenticationFailedException {
        UserEntity userEntity = userDao.getUserByUserName(username);
        if (userEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This username does not exist");
        }
        final String encryptedPassword = passwordCryptographyProvider.encrypt(password, userEntity.getSalt());
        if (encryptedPassword.equals(userEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            UserAuthEntity userAuthEntity = new UserAuthEntity();
            userAuthEntity.setUuid(UUID.randomUUID().toString());
            userAuthEntity.setUser(userEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            userAuthEntity.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
            userAuthEntity.setLoginAt(now);
            userAuthEntity.setExpiresAt(expiresAt);
            userDao.createAuthToken(userAuthEntity);
            userDao.updateUser(userEntity);
            return userAuthEntity;
        } else {
            throw new AuthenticationFailedException("ATH-002", "Password failed");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signout(final String authorization) throws SignOutRestrictedException {
        String accessToken = AuthTokenParser.parseAuthToken(authorization);
        UserAuthEntity userAuthEntity = userDao.getUserAuthByToekn(accessToken);
        if (userAuthEntity == null || userAuthEntity.getExpiresAt().isBefore(ZonedDateTime.now()) || userAuthEntity.getLogoutAt() != null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        } else {
            userAuthEntity.setLogoutAt(ZonedDateTime.now());
            userDao.updateUserAuth(userAuthEntity);
        }
        return userAuthEntity;
    }
}
