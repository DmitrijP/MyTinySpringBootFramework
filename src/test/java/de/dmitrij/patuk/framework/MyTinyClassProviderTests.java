package de.dmitrij.patuk.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



// Test classes
class NoArgBean {
    @MyTinyInject
    public NoArgBean() {
    }
}

class PropertyBean {
    @MyTinyInject
    public PropertyBean(String value) {
    }
}

class ContextBean {
    @MyTinyInject
    public ContextBean(NoArgBean bean) {
    }
}

class MultiDepBean {
    @MyTinyInject
    public MultiDepBean(NoArgBean bean, String value) {
    }
}

public class MyTinyClassProviderTests {
    MyTinyApplicationContext context;
    MyTinyPropertiesProvider propertiesProvider;
    MyTinyClassProvider provider;

    @BeforeEach
    void setup() {
        context = mock(MyTinyApplicationContext.class);
        propertiesProvider = mock(MyTinyPropertiesProvider.class);
        provider = new MyTinyClassProvider(context, propertiesProvider);
    }

    @Test
    void createsNoArgBean() {
        NoArgBean bean = provider.getBeanClass(NoArgBean.class);
        assertNotNull(bean);
    }

    @Test
    void injectsPropertyBean() {
        when(propertiesProvider.canProvide(any())).thenReturn(true);
        when(propertiesProvider.provide(any())).thenReturn("test");
        PropertyBean bean = provider.getBeanClass(PropertyBean.class);
        assertNotNull(bean);
    }

    @Test
    void injectsContextBean() {
        when(propertiesProvider.canProvide(any())).thenReturn(false);
        when(context.isBeanPresent(NoArgBean.class)).thenReturn(true);
        when(context.getBean(NoArgBean.class)).thenReturn(new NoArgBean());
        ContextBean bean = provider.getBeanClass(ContextBean.class);
        assertNotNull(bean);
    }

    @Test
    void injectsMultiDepBeanWithContextAndProperty() {
        when(propertiesProvider.canProvide(any())).thenAnswer(invocation -> {
            var param = invocation.getArgument(0);
            try {
                java.lang.reflect.Parameter parameter = (java.lang.reflect.Parameter) param;
                return parameter.getType().equals(String.class);
            } catch (ClassCastException e) {
                return false;
            }
        });
        when(propertiesProvider.provide(any())).thenReturn("multi");
        when(context.isBeanPresent(NoArgBean.class)).thenReturn(true);
        when(context.getBean(NoArgBean.class)).thenReturn(new NoArgBean());
        MultiDepBean bean = provider.getBeanClass(MultiDepBean.class);
        assertNotNull(bean);
    }

    @Test
    void throwsIfNoInjectConstructor() {
        class NoInject {
        }
        Exception ex = assertThrows(RuntimeException.class, () -> provider.getBeanClass(NoInject.class));
        assertTrue(ex.getMessage().contains("bean"));
    }

    @Test
    void throwsIfDependencyNotFound() {
        when(propertiesProvider.canProvide(any())).thenReturn(false);
        when(context.isBeanPresent(NoArgBean.class)).thenReturn(false);
        Exception ex = assertThrows(RuntimeException.class, () -> provider.getBeanClass(ContextBean.class));
        assertTrue(ex.getMessage().contains("bean"));
    }

    @Test
    void throwsIfOneOfMultipleDependenciesNotFound() {
        when(propertiesProvider.canProvide(any())).thenAnswer(invocation -> {
            var param = invocation.getArgument(0);
            try {
                java.lang.reflect.Parameter parameter = (java.lang.reflect.Parameter) param;
                return parameter.getType().equals(String.class);
            } catch (ClassCastException e) {
                return false;
            }
        });
        when(propertiesProvider.provide(any())).thenReturn(null); // Simulate missing String property
        when(context.isBeanPresent(NoArgBean.class)).thenReturn(true);
        when(context.getBean(NoArgBean.class)).thenReturn(new NoArgBean());
        Exception ex = assertThrows(RuntimeException.class, () -> provider.getBeanClass(MultiDepBean.class));
        assertTrue(ex.getMessage().contains("bean"));
    }
}
