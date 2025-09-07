package de.dmitrij.patuk.app;

import de.dmitrij.patuk.framework.MyTinyInject;

public class AppController {
    private final AppService appService;

    @MyTinyInject
    public AppController(AppService appService) {
        this.appService = appService;
    }

    public void index(){
        System.out.println("app controller: " + appService.call());
    }
}
