package com.wang.demo.component.security.filter.wrapper;

import com.wang.demo.component.utils.EscapeUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 防止xss注入包装器
 * @author wangjianhua
 * @date 2021-03-12 13:57
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 继承父类的构造方法
     * @param request http请求
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 获取请求参数并过滤请求参数xss攻击关键字
     * @param name 参数名
     * @return 字符串数组
     */
    @Override
    public String[] getParameterValues(String name) {
        String []values = super.getParameterValues(name);
        if(!StringUtils.isEmpty(values)){
            int length = values.length;
            String[] escapeValues = new String[length];
            for (int i = 0; i <length ; i++) {
                //先去空格并防止xss攻击
                escapeValues[i] = EscapeUtils.clean(values[i].trim());
            }
            return escapeValues;
        }
        return super.getParameterValues(name);
    }

    /**
     * 此方法用于xss的判断过滤请求
     * @return 输入流
     * @throws IOException IO异常
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        //非json类型，直接返回
        if(!MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(super.getHeader(HttpHeaders.CONTENT_TYPE))){
            return super.getInputStream();
        }
        //为空，直接返回
        String json = IOUtils.toString(super.getInputStream(), StandardCharsets.UTF_8);
        if (StringUtils.isEmpty(json)) {
            return super.getInputStream();
        }
        // xss过滤
        json = EscapeUtils.clean(json).trim();
        final ByteArrayInputStream bis = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream()
        {
            @Override
            public boolean isFinished()
            {
                return true;
            }

            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener)
            {
            }

            @Override
            public int read() throws IOException
            {
                return bis.read();
            }
        };
    }

}
