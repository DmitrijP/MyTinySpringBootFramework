package de.dmitrij.patuk.app;

public class AppService {
    private final String importantConfigValue;

    public AppService(String importantConfigValue) {
        this.importantConfigValue = importantConfigValue;
    }

    public String call() {
        return "Hello from AppService! \n" + "The important config value is: " + importantConfigValue + "\n";
    }
}
