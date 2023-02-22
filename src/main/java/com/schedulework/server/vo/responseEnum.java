package com.schedulework.server.vo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author:Li Jinming
 * @Description:
 * @date:2023-02-13
 */
@Getter
@ToString
@AllArgsConstructor

public enum responseEnum {
    RJOb_SUCCESS(20001,"successfully regist job"),
    UJOb_SUCCESS(20002,"successfully update job"),
    RWORKFLOW_SUCCESS(20003,"successfully regist workflow"),
    UWORKFLOW_SUCCESS(20004,"successfully update  workflow"),
    EDIT_SUCCESS(2003,"userInfo is null"),
    RJOb_FAILED(30001,"failed add job"),
    RWORKFLOW_FAILED(30003,"failed add workflow"),
    EDIT_FAILED(3002,"userInfo is null"),
    USER_NOT_FOUND(4001,"user not found "),
    BAD_REQUEST(4002,"userInfo is null"),

    LOGIN_PROCESING(5001,"userInfo is null");
//    LOGIN_ERROR (403 , "username authorised fail,format is not right"),
//    USERNAME_EMPTY(403, "username is empty"),
//    TOKEN_VERIFY_FAILED(4001,"token verify failed"),
//    TOKEN_EXPIRED(4002,"token has expired");


    private int statusCode;
    private String statusDescription;
}
