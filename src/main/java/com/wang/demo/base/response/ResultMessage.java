package com.wang.demo.base.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author wangjianhua
 * @Description 统一返回json
 * @date 2021/3/17/017 21:25
 **/
@Getter
@Setter
public class ResultMessage {

    private Integer code;

    private String message;

    private Object data;

    public ResultMessage(){

    }

    public ResultMessage(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public void setResultCode(ResultCode code){
        this.code = code.code();
        this.message = code.message();
    }

    public static ResultMessage success(){
        ResultMessage resultMessage =   new ResultMessage();
        resultMessage.setResultCode(ResultCode.SUCCESS);
        return resultMessage;
    }
    public static ResultMessage success(Object data){
        ResultMessage resultMessage =   new ResultMessage();
        resultMessage.setResultCode(ResultCode.SUCCESS);
        resultMessage.setData(data);
        return resultMessage;
    }
    public static ResultMessage success(ResultCode resultCode,Object data){
        ResultMessage resultMessage =   new ResultMessage();
        resultMessage.setResultCode(resultCode);
        resultMessage.setData(data);
        return resultMessage;
    }

    public static  ResultMessage failure(ResultCode resultCode){
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setResultCode(resultCode);
        return resultMessage;
    }
    public static  ResultMessage failure(ResultCode resultCode,Object data){
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setResultCode(resultCode);
        resultMessage.setData(data);
        return resultMessage;
    }

    public static void response(HttpServletResponse response,ResultMessage message){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING,true);
        String json = null;
        try{
            json = objectMapper.writeValueAsString(message);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        PrintWriter out = null;
        try{
            out = response.getWriter();
            assert json != null;
            out.write(json);
            out.flush();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            assert out != null;
            out.close();


        }
    }
}
