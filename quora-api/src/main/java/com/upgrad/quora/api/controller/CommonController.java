package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.CommonService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class CommonController {

    @Autowired
    private CommonService commonService;

    @RequestMapping(path = "/userprofile/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> userProfile(@RequestHeader("authorization") final String authorization, @PathVariable(name = "userId") final String userId) throws AuthorizationFailedException {
        UserEntity userEntity = commonService.userProfile(authorization, userId);
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse().firstName(userEntity.getFirstName()).lastName(userEntity.getLastName()).aboutMe(userEntity.getAboutMe())
                .contactNumber(userEntity.getContactNumber()).country(userEntity.getCountry()).dob(userEntity.getDob()).emailAddress(userEntity.getEmailAddress()).userName(userEntity.getUserName());
        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
    }
}
