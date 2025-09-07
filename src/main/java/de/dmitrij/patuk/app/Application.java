package de.dmitrij.patuk.app;

import de.dmitrij.patuk.framework.MyTinyApplication;
import de.dmitrij.patuk.framework.MyTinyBootApplication;

@MyTinyBootApplication
public class Application {
    public static void main(String[] args) {
        MyTinyApplication.run(Application.class, args);
    }
}
