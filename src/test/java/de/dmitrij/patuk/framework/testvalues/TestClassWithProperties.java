package de.dmitrij.patuk.framework.testvalues;

import de.dmitrij.patuk.framework.MyTinyBean;
import de.dmitrij.patuk.framework.MyTinyConfiguration;
import de.dmitrij.patuk.framework.MyTinyValue;

@MyTinyConfiguration
public class TestClassWithProperties {
    @MyTinyBean
    public ServiceWithDependency provideService(@MyTinyValue(name = "my.config-value") String value) {
        return new ServiceWithDependency(value);
    }
}
