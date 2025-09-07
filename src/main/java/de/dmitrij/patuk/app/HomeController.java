package de.dmitrij.patuk.app;

import de.dmitrij.patuk.framework.MyTinyInject;

public class HomeController {
    @MyTinyInject
    public HomeController(){
    }

    public void index(){
        System.out.println("home controller");
    }
}
