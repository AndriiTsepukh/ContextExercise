package org.bibernate;

public class ContextEntry {
    private String beanName;
    private Class<?> objectClass;
    private Object classInstance;

    public ContextEntry(String beanName, Class<?> objectClass, Object classInstance) {
        this.beanName = beanName;
        this.objectClass = objectClass;
        this.classInstance = classInstance;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getObjectClass() {
        return objectClass;
    }

    public Object getClassInstance() {
        return classInstance;
    }
}
