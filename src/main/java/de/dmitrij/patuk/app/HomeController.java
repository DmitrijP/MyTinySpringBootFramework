package de.dmitrij.patuk.app;

import de.dmitrij.patuk.framework.MyTinyController;
import de.dmitrij.patuk.framework.MyTinyGet;
import de.dmitrij.patuk.framework.MyTinyInject;

@MyTinyController(route = "/home")
public class HomeController {
    @MyTinyInject
    public HomeController() {
    }

    @MyTinyGet(route = "index")
    public String index() {
        return wrapInHtml("home controller");
    }

    private String wrapInHtml(String content) {
        return String.format("<html><body><h1>Welcome To</h1><p>%s</p></body></html>", content);
    }
}
