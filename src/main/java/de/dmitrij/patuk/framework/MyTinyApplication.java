package de.dmitrij.patuk.framework;

import de.dmitrij.patuk.app.AppService;

import java.lang.reflect.InvocationTargetException;

public class MyTinyApplication {
    public static void run(Class<?> appClass, String[] args) {
        if(!appClass.isAnnotationPresent(MyTinyBootApplication.class)) {
            throw new IllegalArgumentException(appClass.getName() +
                    " is not annotated with @MyTinyBootApplication");
        }

        System.out.println("Starting " + appClass.getName());

        try {
            var instance = appClass.getDeclaredConstructor().newInstance();
            System.out.println("Created main application instance: " + instance);
        } catch (NoSuchMethodException | InstantiationException |
                 RuntimeException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
            System.out.printf("Failed to instantiate main application %s\n", appClass.getName());
        }

        var propertiesScanner = new MyTinyPropertiesScanner("application.properties");
        System.out.println(propertiesScanner.get("my.boot.application-name"));

        var propertiesProvider = new MyTinyPropertiesProvider(propertiesScanner);
        var context = new MyTinyApplicationContext(propertiesProvider);

        //new ====================
        var classScanner = new MyTinyClassScanner();
        var configClasses = classScanner.findAnnotatedClasses(appClass.getPackageName(),
                MyTinyConfiguration.class);
        for(var configClass : configClasses) {
            context.registerConfiguration(configClass);
        }
        //new ====================

        var service = context.getBean(AppService.class);
        System.out.println("Starting " + service.call());
        System.out.println("Application started!");
    }
}
