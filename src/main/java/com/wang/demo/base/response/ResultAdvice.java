package com.wang.demo.base.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


/**
 * @author wangjianhua
 * @Description 统一接口返回值
 * @date 2021/3/17/017 21:09
 **/
@RestControllerAdvice(basePackages = "com.wang.demo")
public class  ResultAdvice implements ResponseBodyAdvice<Object> {
    /**
     * 开启统一接口返回值支持 默认为false  开启为true
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    /**
     * 在写入body前处理json
     * @param object 返回值
     */
    @Override
    public Object beforeBodyWrite(Object object, MethodParameter methodParameter, MediaType mediaType,
    Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if(object instanceof  ResultMessage){
            return object;
        }
        return ResultMessage.success(object);
    }

    /**
     * 该方法用于处理异常 可以自行添加并完善
     * @param ex 异常
     * @return 返回值
     */
    @ExceptionHandler(value = Exception.class)
    public ResultMessage serverInternal(Exception ex) {
        //无访问权限时
        if (ex instanceof AccessDeniedException) {
            return ResultMessage.failure(ResultCode.PERMISSION_NO_ACCESS);
        }
        //请求参数无法解析时
        else if(ex instanceof HttpMessageNotReadableException){
            //记录该异常
            HttpMessageNotReadableException exception = (HttpMessageNotReadableException)ex;
            return ResultMessage.failure(ResultCode.PARAM_TYPE_BIND_ERROR,"HTTP请求参数无法解析"+exception.getMessage());
        }
        //方法参数类型不匹配时
        else if(ex instanceof MethodArgumentNotValidException){
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) ex;
            return ResultMessage.failure(ResultCode.PARAM_TYPE_BIND_ERROR,"方法参数类型不匹配"+exception.getMessage());
        }
        //有未添加的异常暂时将异常打印到data里面  返回异常为 内部接口调用异常
        return ResultMessage.failure(ResultCode.INTERFACE_INNER_INVOKE_ERROR,ex.getMessage());
    }
}
