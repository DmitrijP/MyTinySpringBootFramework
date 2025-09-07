package de.dmitrij.patuk.app;

import de.dmitrij.patuk.framework.MyTinyController;
import de.dmitrij.patuk.framework.MyTinyGet;
import de.dmitrij.patuk.framework.MyTinyInject;

@MyTinyController(route = "/app")
public class AppController {
    private final AppService appService;

    @MyTinyInject
    public AppController(AppService appService) {
        this.appService = appService;
    }

    @MyTinyGet(route = "index")
    public String index(){
        return wrapInHtml("app controller", appService.call());
    }

    private String wrapInHtml(String controller, String serviceResult) {
        return String.format("<html><body><h1>Welcome To</h1><p>%s</p><p>%s</p></body></html>", controller, serviceResult);
    }
}
