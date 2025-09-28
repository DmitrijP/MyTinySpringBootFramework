package de.dmitrij.patuk.app.controller;

import de.dmitrij.patuk.framework.MyTinyController;
import de.dmitrij.patuk.framework.MyTinyGet;
import de.dmitrij.patuk.framework.MyTinyInject;
import de.dmitrij.patuk.framework.MyTinyModelAndView;

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
        return String.format("<html><body style=\"color: white; background-color: black\"><h1>Welcome To</h1><p>%s</p></body></html>", content);
    }

    @MyTinyGet(route = "not-exists")
    public MyTinyModelAndView notExist() {
        return new MyTinyModelAndView("/not_exist", "not_exist");
    }
}
