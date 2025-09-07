package de.dmitrij.patuk.app;

import de.dmitrij.patuk.framework.MyTinyBean;
import de.dmitrij.patuk.framework.MyTinyConfiguration;

@MyTinyConfiguration
public class AppConfig {
    @MyTinyBean
    public AppService provideAppService() {
        return new AppService();
    }
}
