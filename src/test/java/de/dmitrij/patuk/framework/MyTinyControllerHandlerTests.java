package de.dmitrij.patuk.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MyTinyController(route = "/test")
class TestController {
    @MyTinyGet(route = "hello")
    public String hello() {
        return "Hello";
    }

    public String notMapped() {
        return "No";
    }
}

public class MyTinyControllerHandlerTests {
    MyTinyHttpServer server;
    MyTinyClassProvider provider;
    MyTinyControllerHandler handler;
    MyTinyRequestParamHandler requestParamHandler;
    MyTinyViewRenderer viewRenderer;

    @BeforeEach
    void setup() {
        server = mock(MyTinyHttpServer.class);
        provider = mock(MyTinyClassProvider.class);
        requestParamHandler = mock(MyTinyRequestParamHandler.class);
        viewRenderer = mock(MyTinyViewRenderer.class);
        handler = new MyTinyControllerHandler(server, provider, requestParamHandler, viewRenderer);
    }

    @Test
    void registersControllerWithGetMethod() throws Exception {
        doReturn(new TestController()).when(provider).getBeanClass(TestController.class);
        handler.registerController(TestController.class);
        verify(server).bindContext(eq("/test/hello"), any());
    }

    @Test
    void doesNotRegisterIfNotAnnotated() {
        class NotAController {
        }
        handler.registerController(NotAController.class);
        verify(server, never()).bindContext(anyString(), any());
    }

    @Test
    void doesNotRegisterMethodWithoutGetAnnotation() {
        class ControllerNoGet {
            @MyTinyController(route = "/noGet")
            public class Inner {
            }

            public String notMapped() {
                return "No";
            }
        }
        handler.registerController(ControllerNoGet.class);
        verify(server, never()).bindContext(anyString(), any());
    }
}
