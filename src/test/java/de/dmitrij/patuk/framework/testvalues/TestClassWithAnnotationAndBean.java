package de.dmitrij.patuk.framework.testvalues;

import de.dmitrij.patuk.framework.MyTinyBean;
import de.dmitrij.patuk.framework.MyTinyConfiguration;

@MyTinyConfiguration
public class TestClassWithAnnotationAndBean {
    @MyTinyBean
    public Service provideService() {
        return new Service();
    }
}
