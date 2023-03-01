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
//    EDIT_SUCCESS(2003,"userInfo is null"),
    SWORKFLOW_SUCCESS(20005,"start success"),
    UJOB_STATUS_SUCCESS(20006,"update success"),
    FWORKFLOW_SUCCESS(20007,"finish success"),
    RJOb_FAILED(30001,"failed add job"),
    RWORKFLOW_FAILED(30003,"failed add workflow"),
    SWORKFLOW_FAILED(30004,"start failed"),
    UJOB_STATUS_FAILED(30005,"update failed"),
    EDIT_FAILED(3002,"userInfo is null"),
    FWORKFLOW_FAILED(30006,"finish failed"),
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
