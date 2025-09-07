package de.dmitrij.patuk.framework;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// Test annotation for scanning
@Retention(RetentionPolicy.RUNTIME)
@interface TestAnnotation {
}

// Test classes for scanning
@TestAnnotation
class AnnotatedClass {

}

class NonAnnotatedClass {
}

public class MyTinyClassScannerTests {
    static MyTinyClassScanner scanner;

    @BeforeAll
    static void setup() {
        scanner = new MyTinyClassScanner();
    }

    @Test
    void findsAnnotatedClassInPackage() {
        List<Class<?>> result = scanner.findAnnotatedClasses("de.dmitrij.patuk.framework", TestAnnotation.class);
        assertTrue(result.contains(AnnotatedClass.class), "Should find AnnotatedClass");
    }

    @Test
    void doesNotFindNonAnnotatedClass() {
        List<Class<?>> result = scanner.findAnnotatedClasses("de.dmitrij.patuk.framework", TestAnnotation.class);
        assertFalse(result.contains(NonAnnotatedClass.class), "Should not find NonAnnotatedClass");
    }

    @Test
    void returnsEmptyListForNonexistentPackage() {
        List<Class<?>> result = scanner.findAnnotatedClasses("non.existent.package", TestAnnotation.class);
        assertTrue(result.isEmpty(), "Should return empty list for non-existent package");
    }
}
