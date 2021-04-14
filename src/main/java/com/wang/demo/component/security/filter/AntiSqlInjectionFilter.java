package com.wang.demo.component.security.filter;

import com.wang.demo.component.security.filter.wrapper.SqlHttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sql注入过滤器
 * @author wangjianhua
 * @date 2021-03-12 16:32
 */
public class AntiSqlInjectionFilter implements Filter {
    /**
     *排除链接
     */
    public List<String> excludes = new ArrayList<>();
    /**
     * sql注入开关
     */
    public boolean enabled = false;
    /**
     * 编译正则 sqlValidate()方法中有更加完整的字段防注入 可以补全下面的正则
     */
    private static final Pattern SQL_PATTERN =
            Pattern.compile("\\b(select|update|and|or|delete|insert|trancate|" +
                      "char|substr|ascii|declare|exec|count|master|into|drop|" +
                      "execute" + ")\\b");

    private final Logger logger = LoggerFactory.getLogger(AntiSqlInjectionFilter.class);
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String tempExcludes = filterConfig.getInitParameter("excludes");
        String tempEnabled = filterConfig.getInitParameter("enabled");
        if(!StringUtils.isEmpty(tempExcludes)){
            String[] url = tempExcludes.split(",");
            for (int i = 0; url != null && i < url.length; i++) {
                excludes.add(url[i]);
            }
        }
        if(!StringUtils.isEmpty(tempEnabled)){
            enabled = Boolean.parseBoolean(tempEnabled);
        }
    }

    /**
     * 过滤 放行
     * @param servletRequest 请求
     * @param servletResponse 响应
     * @param filterChain 过滤链
     * @throws IOException IOException
     * @throws ServletException ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        SqlHttpServletRequestWrapper sqlRequest =null;
        //如果有不需要过滤的接口，放行
        if(handleExcludeURL(req,res)){
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        Enumeration<String> parameterNames = req.getParameterNames();
        StringBuilder sql = new StringBuilder();
        while (parameterNames.hasMoreElements()) {
            //得到参数名
            String name = parameterNames.nextElement();
            //得到参数对应值
            String[] value = req.getParameterValues(name);
            for (int i = 0; i < value.length; i++) {
                sql.append(value[i]);
            }
        }
        String get = "GET";
        String post = "POST";
        //还有别的restful风格的验证也可以加入  视项目情况而定
        //有sql关键字，跳转到sql注入警告  ==>GET方式注入
        if(get.equals(req.getMethod())){
//            if (sqlValidate(sql.toString())) {
            if (sqlValidateMatch(sql.toString())) {
                //打印sql注入相关信息
                logger.warn("sql注入的uri===>"+req.getRequestURI());
                logger.warn("sql注入方式"+req.getMethod());
                res.setContentType("application/json;charset=utf-8");
                res.getWriter().print("请不要尝试sql注入");
            }
            else{
                filterChain.doFilter(servletRequest, res);
            }
        }
        //有sql关键字，跳转到sql注入警告  ==>POST方式注入
        else if(post.equals(req.getMethod())){
            sqlRequest = new SqlHttpServletRequestWrapper((HttpServletRequest) servletRequest);
            String bodyStr = getBodyString(sqlRequest.getReader());
            //这样可以判断body 不需要判断的可以添加excludes
//            if(bodyStr.contains("administrator")){
//                filterChain.doFilter(sqlRequest,servletResponse);
//            }
//            else if(sqlValidate(bodyStr)){
//            if(sqlValidate(bodyStr)){
            if(sqlValidateMatch(bodyStr)){
                //打印sql注入相关信息
                logger.warn("sql注入的uri===>"+req.getRequestURI());
                logger.warn("sql注入方式"+req.getMethod());
                res.setContentType("application/json;charset=utf-8");
                res.getWriter().print("请不要尝试sql注入");
            }
            else {
                filterChain.doFilter(sqlRequest,servletResponse);
            }
        }
        else {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    @Override
    public void destroy() {

    }

    /**
     * 效验过滤掉的sql关键字，可以手动添加
     * @param str 参数中的字符串
     * @return 是否有sql注入关键字
     */
    protected static boolean sqlValidate(String str) {
        //统一转为小写
        str = str.toLowerCase();
        //这是sql注入的关键字 可以加 |再跟关键字进行补充 比如 |or
        String badStr = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|chr|mid|master|truncate|" +
                "char|declare|sitename|net user|xp_cmdshell|;|or|+|,|like'|and|exec|execute|insert|create|drop|" +
                "table|from|grant|use|group_concat|column_name|" +
                "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" +
                "chr|mid|master|truncate|char|declare|or|;|--|+|,|like|//|/|%|#";
        //分隔并进行判断
        System.out.println(badStr+"badStr");
        String[] badStrs = badStr.split("\\|");
        for (int i = 0; i < badStrs.length; i++) {
            if (str.contains(" "+badStrs[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 启用正则表达式匹配sql注入 拦截更加智能
      * @param str 字符串
     * @return 是否有sql注入风险
     */
    protected static boolean sqlValidateMatch(String str){
        return containsSqlInjection(str);
    }


    /**
     * 字符串读取http请求的body
     * 此方法取出请求体的参数 防止post等方式的sql注入
     * 此方法会引起流重复调用报错 不建议使用
     * @param request request
     * @return 返回所有的请求体参数
     * @throws IOException IOException
     */
    @Deprecated
    String charReader(HttpServletRequest request) throws IOException {
        BufferedReader br = request.getReader();
        String str;
        StringBuilder wholeStr = new StringBuilder();
        while((str = br.readLine()) != null){
            wholeStr.append(str);
        }
        return wholeStr.toString();

    }

    /**
     *获取request请求body中参数
     * @param br 流
     * @return body参数
     */
    public static String getBodyString(BufferedReader br) {
        String inputLine;
        StringBuilder str = new StringBuilder();
        try {
            while ((inputLine = br.readLine()) != null) {
                str.append(inputLine);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
        return str.toString();

    }

    /**
     * 该方法用于排除是否有需要过滤的白名单
     * @param request 请求
     * @param response 响应
     * @return
     */
    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response)
    {
        if (!enabled)
        {
            return true;
        }
        if (excludes == null || excludes.isEmpty())
        {
            return false;
        }
        String url = request.getServletPath();
        for (String pattern : excludes)
        {
            Pattern p = Pattern.compile("^" + pattern);
            Matcher m = p.matcher(url);
            if (m.find())
            {
                return true;
            }
        }
        return false;
    }
    /**
     * 使用正则判断是否含有sql注入，返回true表示含有
     * @param str 传入的字符串
     * @return
     */
    public static boolean containsSqlInjection(String str){
        Matcher matcher=SQL_PATTERN.matcher(str.toLowerCase());
        return matcher.find();
    }
}
