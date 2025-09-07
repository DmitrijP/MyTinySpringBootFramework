package de.dmitrij.patuk.framework;


import de.dmitrij.patuk.framework.testvalues.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyTinyApplicationContextTests {

    private MyTinyPropertiesProvider propertiesProvider;

    @BeforeEach
    void setUp() {
        propertiesProvider = new MyTinyPropertiesProvider(new MyTinyPropertiesScanner("application.properties"));
    }
    @Test
    void RegisterClassWithoutAnnotationThrowsTest() {
        var context = new MyTinyApplicationContext(propertiesProvider);
        context.registerConfiguration(TestClassWithoutAnnotation.class);
        assertThrows(RuntimeException.class, () -> context.getBean(TestClassWithoutAnnotation.class));
    }

    @Test
    void RegisterClassWithoutAnnotationTest() {
        var context = new MyTinyApplicationContext(propertiesProvider);
        assertThrows(RuntimeException.class, () -> context.registerConfiguration(TestClassWithAnnotation.class));
    }

    @Test
    void RegisterClassWithAnnotationThrowsTest() {
        var context = new MyTinyApplicationContext(propertiesProvider);
        context.registerConfiguration(TestClassWithAnnotationAndBean.class);
        var service = context.getBean(Service.class);
        assertInstanceOf(Service.class, service);
    }

    @Test
    void RegisterClassWithAnnotationTest() {
        var context = new MyTinyApplicationContext(propertiesProvider);
        assertThrows(RuntimeException.class, () -> context.registerConfiguration(TestClassWithAnnotationAndBeanNoReturnType.class));
    }

    @Test
    void RegisterClassWithAnnotationNoReturnTypeThrowsTest() {
        var context = new MyTinyApplicationContext(propertiesProvider);
        context.registerConfiguration(TestClassWithProperties.class);
        var service = context.getBean(ServiceWithDependency.class);
        assertInstanceOf(ServiceWithDependency.class, service);
        assertNotNull(service.getValue());
    }
}
