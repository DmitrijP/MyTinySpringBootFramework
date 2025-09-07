package de.dmitrij.patuk.framework;


import de.dmitrij.patuk.framework.testvalues.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MyTinyApplicationContextTests {

    @Test
    void RegisterClassWithoutAnnotationThrowsTest() {
        var context = new MyTinyApplicationContext();
        context.registerConfiguration(TestClassWithoutAnnotation.class);
        assertThrows(RuntimeException.class, () -> context.getBean(TestClassWithoutAnnotation.class));
    }

    @Test
    void RegisterClassWithoutAnnotationTest() {
        var context = new MyTinyApplicationContext();
        assertThrows(RuntimeException.class, () -> context.registerConfiguration(TestClassWithAnnotation.class));
    }

    @Test
    void RegisterClassWithAnnotationThrowsTest() {
        var context = new MyTinyApplicationContext();
        context.registerConfiguration(TestClassWithAnnotationAndBean.class);
        var service = context.getBean(Service.class);
        assertInstanceOf(Service.class, service);
    }

    @Test
    void RegisterClassWithAnnotationTest() {
        var context = new MyTinyApplicationContext();
        assertThrows(RuntimeException.class, () -> context.registerConfiguration(TestClassWithAnnotationAndBeanNoReturnType.class));
    }
}
