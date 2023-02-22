package com.schedulework.server.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author:Li Jinming
 * @Description:
 * @date:2023-02-13
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class serverResponse {
    private  long code;
    private String Message;
    private Object object;
    public  static serverResponse getResponse(responseEnum responseEnum, Object object){
        return new serverResponse(responseEnum.getStatusCode(),responseEnum.getStatusDescription(), object);
    }
    public  static serverResponse getResponse(responseEnum responseEnum){
        return new serverResponse(responseEnum.getStatusCode(),responseEnum.getStatusDescription(), null);
    }
}
