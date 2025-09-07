package de.dmitrij.patuk.framework;

import java.io.IOException;
import java.util.Properties;

public class MyTinyPropertiesScanner {
    private final Properties properties = new Properties();

    public MyTinyPropertiesScanner(String fileName) {
        // get the current class and its classloader, get the resources directory and the file from that directory
        try (var fis = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (fis != null) {
                properties.load(fis);
                System.out.println("Loaded properties from " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file", e);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}
