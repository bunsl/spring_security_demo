package com.wang.demo.component.security.filter.wrapper;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 重写sql注入包装器中的输入流方法，可以判断请求体
 * @author wangjianhua
 * @date 2021-03-16 14:55
 */
public class SqlHttpServletRequestWrapper extends HttpServletRequestWrapper {
    HttpServletRequest orgRequest = null;
    /**
     * 保存body数据
     */
    private final byte[] body;

    public SqlHttpServletRequestWrapper(HttpServletRequest request) throws IOException{
        super(request);
        orgRequest = request;
        body = StreamUtils.copyToByteArray(request.getInputStream());
    }

    // 重写几个HttpServletRequestWrapper中的方法
    /**
     * 重写获取流方法，用作body判断 否则会在过滤后丢失body
     * @return BufferedReader
     * @throws IOException IOException
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * 重写获取流方法 避免冲突
     * @return ServletInputStream
     * @throws IOException IOException
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener arg0) {
            }
        };
    }
}
