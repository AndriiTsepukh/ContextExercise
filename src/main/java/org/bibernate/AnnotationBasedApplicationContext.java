package org.bibernate;

import org.bibernate.exceptions.ConfigurationException;
import org.bibernate.exceptions.NoSuchBeanException;
import org.bibernate.exceptions.NoUniqueBeanException;
import org.reflections.Reflections;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AnnotationBasedApplicationContext implements ApplicationContext {
    private String packageName;

    private Map<String, ContextEntry> context = new HashMap<>();

    public AnnotationBasedApplicationContext(String packageName) {
        this.packageName = packageName;
        contextSetup();
    }

    private void contextSetup() {
        if (packageName == null || packageName.isEmpty()) {
            throw new ConfigurationException("Package name to scan are not specified.");
        }
        Reflections reflections = new Reflections(packageName);
        var annotatedClasses = reflections.getTypesAnnotatedWith(Bean.class);
        for (var currentClass: annotatedClasses) {
            var beanName = extractBeanName(currentClass);
            var constructors = currentClass.getDeclaredConstructors();
            if (constructors[0].getParameterCount() > 0)
                throw new ConfigurationException("Classes without empty constructor are not supported. Not supported class: "
                        + currentClass.getSimpleName());
            try {
                var classInstance = constructors[0].newInstance();
                context.put(beanName, new ContextEntry(beanName, currentClass, classInstance));
            } catch (Exception e) {
                throw new ConfigurationException("Error happened during creation of the class " + currentClass.getSimpleName()
                        + " instance: " + e.getMessage());
            }
        }
        setupDependenciesViaAutowire();
    }

    private void setupDependenciesViaAutowire() {
        for (var entry : context.entrySet()) {
            var beanClass = entry.getValue().getObjectClass();
            var beanObject = entry.getValue().getClassInstance();
            var fields = beanClass.getDeclaredFields();
            for (var field : fields) {
                if (field.isAnnotationPresent(Autowire.class)) {
                    var beanName = field.getAnnotation(Autowire.class).value();
                    if(beanName.isEmpty()) beanName = transformToCamelCase(field.getType().getSimpleName());
                    if (!context.containsKey(beanName)) throw new NoSuchBeanException("Can't find bean for autowiring with name: " + beanName);
                    var beanForInjection = context.get(beanName).getClassInstance();
                    field.setAccessible(true);
                    try {
                        field.set(beanObject, beanForInjection);
                    } catch (IllegalAccessException e) {
                        throw new ConfigurationException("Can't setup a field: " + field.getName()
                                + "of Class: " + beanClass.getSimpleName()
                                + " More error details: " + e.getMessage());
                    }
                }
            }
        }
    }

    String extractBeanName(Class<?> clazz) {
        var beanName = clazz.getAnnotation(Bean.class).value();
        var className = clazz.getSimpleName();
        if (beanName.isEmpty()) beanName = transformToCamelCase(className);
        return beanName;
    }

    private String transformToCamelCase(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    @Override
    public <T> T getBean(Class<T> beanType) {
        var beans = context.entrySet().stream()
                .filter(beanTypeFilterPredicate(beanType))
                .map(Map.Entry::getValue)
                .map(ContextEntry::getClassInstance)
                .toList();
        if (beans.size() > 1) throw new NoUniqueBeanException("More then one instance of the bean with type: "
                + beanType.getSimpleName() + " were found.");
        if (beans.size() == 0) throw new NoSuchBeanException("Can't find bean with type: " + beanType.getSimpleName());
        return beanType.cast(beans.get(0));
    }

    @Override
    public <T> T getBean(String name, Class<T> beanType) {
        var beans = context.entrySet().stream()
                .filter(entry -> entry.getKey().equals(name))
                .filter(beanTypeFilterPredicate(beanType))
                .map(Map.Entry::getValue)
                .map(ContextEntry::getClassInstance)
                .toList();
        if (beans.size() == 0) throw new NoSuchBeanException("Can't find bean with type: " + beanType.getSimpleName()
                + " and name: " + name);
        return beanType.cast(beans.get(0));
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        var beans = context.entrySet().stream()
                .filter(beanTypeFilterPredicate(beanType))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> beanType.cast(entry.getValue().getClassInstance())));
        return beans;
    }

    private <T> Predicate<Map.Entry<String, ContextEntry>> beanTypeFilterPredicate(Class<T> beanType) {
        return entry -> beanType.isAssignableFrom(entry.getValue().getObjectClass());
    }
}
