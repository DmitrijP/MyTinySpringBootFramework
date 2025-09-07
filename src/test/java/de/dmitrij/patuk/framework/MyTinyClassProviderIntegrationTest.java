package de.dmitrij.patuk.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyTinyClassProviderIntegrationTest {
    MyTinyClassProvider provider;

    @BeforeEach
    void setup() {
        var p = new MyTinyPropertiesProvider(new MyTinyPropertiesScanner("application.properties"));
        provider = new MyTinyClassProvider(new MyTinyApplicationContext(p), p);
    }

    @Test
    void createsNoArgBeanWithRealDeps() {
        NoArgBean bean = provider.getBeanClass(NoArgBean.class);
        assertNotNull(bean);
    }
}
