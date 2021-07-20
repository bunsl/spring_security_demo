package com.wang.demo.component.mybatis.interceptor;

import com.wang.demo.component.security.jwt.JsonWebToken;
import com.wang.demo.modules.system.user.service.UserService;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.omg.CORBA.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.Object;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 更新操作的拦截器
 * Mybatis拦截器用到责任链模式+动态代理+反射机制
 * @author wangjianhua
 * @date 2021-03-18 11:27
 */
@Intercepts({
        @Signature(
                method = "update",
                type = Executor.class,
                args = {MappedStatement.class, Object.class}
        )
})
public class UpdateInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(UpdateInterceptor.class);


    private UserService userService;

    private JsonWebToken jsonWebToken;
    /**
     * 创建人 创建时间
     */
    private static final String CREATED_BY = "createdBy";
    private static final String CREATED_TIME = "createdTime";
    /**
     * 更新人 更新时间
     */
    private static final String UPDATED_BY = "updatedBy";
    private static final String UPDATED_TIME = "updatedTime";

    /**
     * 构造器注入 json工具类 与 用户服务类
     *
     * @param userService  userService
     * @param jsonWebToken jsonWebToken
     */
    public UpdateInterceptor(UserService userService, JsonWebToken jsonWebToken) {
        this.userService = userService;
        this.jsonWebToken = jsonWebToken;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String name = invocation.getMethod().getName();
        String update = "update";
        String install = "install";
        if (update.equals(name) || install.equals(name)) {
            return invokeUpdate(invocation);
        }
        else {
            return invocation.proceed();
        }

    }

    private Object invokeUpdate(Invocation invocation) throws Exception {
        Object[] args1 = invocation.getArgs();
        List<Object> objList = Arrays.stream(args1).filter(x -> x != null).collect(Collectors.toList());
        MappedStatement ms = null;
        Object args = null;
        //赋值
        for (Object obj : objList) {
            if (obj instanceof MappedStatement) {
                ms = (MappedStatement) obj;
            } else if (obj instanceof Object) {
                args = obj;
            }
        }
        if(ms==null || args ==null){
            return invocation.proceed();
        }
        if(args instanceof MapperMethod.ParamMap){
            MapperMethod.ParamMap<Object> mapObj = (MapperMethod.ParamMap<Object>)args;
            for (Map.Entry<String, Object> obj : mapObj.entrySet()) {
                Object paramObj = mapObj.get(obj.getKey());
                if(paramObj instanceof List){
                    this.objList(paramObj,ms);
                }else {
                    Field [] fields = paramObj.getClass().getSuperclass().getDeclaredFields();
                    this.upFiled(fields,ms,args);
                }
            }
        }
        else if(args instanceof DefaultSqlSession.StrictMap){
            DefaultSqlSession.StrictMap<Object> mapObj =(DefaultSqlSession.StrictMap<Object>)args;
            for (Map.Entry<String, Object> obj : mapObj.entrySet()) {
                Object paramObj = mapObj.get(obj.getKey());
                if(paramObj instanceof List){
                    this.objList(paramObj,ms);
                }
                else {
                    Field[] fields = paramObj.getClass().getSuperclass().getDeclaredFields();
                    this.upFiled(fields,ms,args);
                }
            }
        }else{
            Field[] fields = args.getClass().getSuperclass().getDeclaredFields();
            this.upFiled(fields,ms,args);
        }
        return invocation.proceed();
    }

    /**
     * 如果是集合就进行递归
     *
     * @param paramObj list对象
     * @param ms       MappedStatement
     */
    public void objList(Object paramObj, MappedStatement ms) {
        List<Object> listObj = (List<Object>) paramObj;
        for (Object obj : listObj) {
            if (obj instanceof List) {
                this.objList(obj, ms);
            }
            Object object;
            Field[] declaredFields = obj.getClass().getSuperclass().getDeclaredFields();
            this.upFiled(declaredFields, ms, obj);
        }
    }

    /**
     * 开始执行修改方法
     *
     * @param fields 通过反射获取的字段列表
     * @param ms     MappedStatement
     * @param args   对象参数
     */
    private void upFiled(Field[] fields, MappedStatement ms, Object args) {
        //insert语句 就添加创建时间 创建人 修改时间 修改人
        if (ms.getSqlCommandType() == SqlCommandType.INSERT) {
            this.setAllParams(fields, args, CREATED_BY, jsonWebToken.getAuthentication().getName());
            this.setAllParams(fields, args, CREATED_TIME, new Date());
            this.setAllParams(fields, args, UPDATED_BY, jsonWebToken.getAuthentication().getName());
            this.setAllParams(fields, args, UPDATED_TIME, new Date());
        }
        //update 语句 则添加修改时间 修改人
        if (ms.getSqlCommandType() == SqlCommandType.UPDATE) {
            this.setAllParams(fields, args, UPDATED_BY, jsonWebToken.getAuthentication().getName());
            this.setAllParams(fields, args, UPDATED_TIME, new Date());
        }
    }

    /**
     * 根据传递参数进行修改
     *
     * @param fields   反射存在的参数
     * @param obj      需要改变的对象
     * @param valueKey 变更的字段
     * @param valObj   变更参数类型
     */
    private void setAllParams(Field[] fields, Object obj, String valueKey, Object valObj) {
        for (int i = 0; i < fields.length; i++) {
            if (valueKey.toLowerCase().equals(fields[i].getName().toLowerCase())) {
                try {
                    if (valObj instanceof Date) {
                        fields[i].setAccessible(true);
                        fields[i].set(obj, new Date());
                        fields[i].setAccessible(false);
                    }
                    if (valObj instanceof String) {
                        String name = jsonWebToken.getAuthentication().getName();
//                        可以用id做修改人 为展示方便这里使用name
//                        int id = userService.findIdByUserName(name);
                        fields[i].setAccessible(true);
//                        fields[i].set(obj, id);
                        fields[i].set(obj,name);
                        fields[i].setAccessible(false);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
