package de.dmitrij.patuk.app;

import de.dmitrij.patuk.framework.MyTinyBean;
import de.dmitrij.patuk.framework.MyTinyConfiguration;
import de.dmitrij.patuk.framework.MyTinyValue;

@MyTinyConfiguration
public class AppConfig {
    @MyTinyBean
    public AppService provideAppService(@MyTinyValue(name = "my.config-value") String value) {
        return new AppService(value);
    }
}
