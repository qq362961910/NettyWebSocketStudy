package com.wxsk.pusher.helper;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ContextHelper implements ApplicationContextAware{

    private static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    public static void setContext(ApplicationContext context) {
        ContextHelper.context = context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static  <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public static  <T> T getBean(Class<T> clazz, Object... args) {
        return context.getBean(clazz, args);
    }
}
