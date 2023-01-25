package org.bibernate;

import java.util.Map;

public interface ApplicationContext {
    public <T> T getBean(Class<T> beanType);
    public <T> T getBean(String name, Class<T> beanType);
    public <T> Map<String, T> getAllBeans(Class<T> beanType);
}
