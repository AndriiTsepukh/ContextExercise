package org.bibernate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testclasses.CustomInterface;
import testclasses.ExampleOne;
import testclasses.ExampleTwo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ContextTest {

    AnnotationBasedApplicationContext context;

    @BeforeEach
    void setup() {
        context = new AnnotationBasedApplicationContext("testclasses");
    }

    @Test
    void namedAnnotationTest() {
        var beanName = context.extractBeanName(ExampleOne.class);
        assertEquals("FirstExample", beanName);
    }

    @Test
    void defaultNameTest() {
        var beanName = context.extractBeanName(ExampleTwo.class);
        assertEquals("exampleTwo", beanName);
    }

    @Test
    void findBeanByTypeTest() {
        var beanInstance = context.getBean(ExampleOne.class);
        assertNotNull(beanInstance);
    }

    @Test
    void findBeanByTypeAndNameTest() {
        var beanInstance = context.getBean("FirstExample", ExampleOne.class);
        assertNotNull(beanInstance);
    }

    @Test
    void findAllBeansByTypeTest() {
        var map = context.getAllBeans(CustomInterface.class);
        assertEquals(2, map.size());
    }

}
