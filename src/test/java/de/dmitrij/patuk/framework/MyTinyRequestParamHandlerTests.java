package de.dmitrij.patuk.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DummyController {
    public void stringParam(@MyTinyRequestParam(name = "foo") String foo) {}
    public void intParam(@MyTinyRequestParam(name = "num") int num) {}
    public void boolParam(@MyTinyRequestParam(name = "flag") boolean flag) {}
    public void doubleParam(@MyTinyRequestParam(name = "d") double d) {}
    public void noAnnotation(String notHandled) {}
}

public class MyTinyRequestParamHandlerTests {
    MyTinyRequestParamHandler handler;
    DummyController dummy;

    @BeforeEach
    void setup() {
        handler = new MyTinyRequestParamHandler();
        dummy = new DummyController();
    }

    @Test
    void parsesQueryString() {
        Map<String, String> params = handler.getRequestParams("foo=bar&num=42");
        assertEquals("bar", params.get("foo"));
        assertEquals("42", params.get("num"));
    }

    @Test
    void parsesEmptyOrNullQuery() {
        assertTrue(handler.getRequestParams(null).isEmpty());
        assertTrue(handler.getRequestParams("").isEmpty());
    }

    @Test
    void parsesMalformedQuery() {
        Map<String, String> params = handler.getRequestParams("foo=bar&bad&x=1");
        assertEquals("bar", params.get("foo"));
        assertEquals("1", params.get("x"));
        assertFalse(params.containsKey("bad"));
    }

    @Test
    void handlesStringParam() throws Exception {
        Parameter p = DummyController.class.getMethod("stringParam", String.class).getParameters()[0];
        String result = handler.handle(p, Map.of("foo", "hello"));
        assertEquals("hello", result);
    }

    @Test
    void handlesIntParam() throws Exception {
        Parameter p = DummyController.class.getMethod("intParam", int.class).getParameters()[0];
        int result = handler.handle(p, Map.of("num", "123"));
        assertEquals(123, result);
    }

    @Test
    void handlesBooleanParam() throws Exception {
        Parameter p = DummyController.class.getMethod("boolParam", boolean.class).getParameters()[0];
        boolean result = handler.handle(p, Map.of("flag", "true"));
        assertTrue(result);
    }

    @Test
    void handlesDoubleParam() throws Exception {
        Parameter p = DummyController.class.getMethod("doubleParam", double.class).getParameters()[0];
        double result = handler.handle(p, Map.of("d", "3.14"));
        assertEquals(3.14, result, 0.0001);
    }

    @Test
    void throwsIfParamNotAnnotated() throws Exception {
        Parameter p = DummyController.class.getMethod("noAnnotation", String.class).getParameters()[0];
        Exception ex = assertThrows(RuntimeException.class, () -> handler.handle(p, Map.of("notHandled", "x")));
        assertTrue(ex.getMessage().contains("not annotated"));
    }

    @Test
    void throwsIfParamMissing() throws Exception {
        Parameter p = DummyController.class.getMethod("stringParam", String.class).getParameters()[0];
        Exception ex = assertThrows(RuntimeException.class, () -> handler.handle(p, Map.of()));
        assertTrue(ex.getMessage().contains("not inside request parameters"));
    }

    @Test
    void throwsIfTypeNotSupported() throws Exception {
        class CustomType {}
        class CustomController {
            public void custom(@MyTinyRequestParam(name = "c") CustomType c) {}
        }
        Parameter p = CustomController.class.getMethod("custom", CustomType.class).getParameters()[0];
        Exception ex = assertThrows(RuntimeException.class, () -> handler.handle(p, Map.of("c", "x")));
        assertTrue(ex.getMessage().contains("not supported"));
    }
}
