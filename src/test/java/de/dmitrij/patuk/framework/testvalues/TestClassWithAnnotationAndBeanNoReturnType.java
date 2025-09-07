package de.dmitrij.patuk.framework.testvalues;

import de.dmitrij.patuk.framework.MyTinyBean;
import de.dmitrij.patuk.framework.MyTinyConfiguration;

@MyTinyConfiguration
public class TestClassWithAnnotationAndBeanNoReturnType {
    @MyTinyBean
    public void provideService() {

    }
}
