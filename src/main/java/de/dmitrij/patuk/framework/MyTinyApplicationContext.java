package de.dmitrij.patuk.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyTinyApplicationContext {
    private final Map<Class<?>, Object> beans = new HashMap<>();
    private final MyTinyPropertiesProvider propertiesProvider;

    public MyTinyApplicationContext(MyTinyPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    public void registerConfiguration(Class<?> configClass) {
        //We check if the class is annotated with @MyConfiguration
        if (!configClass.isAnnotationPresent(MyTinyConfiguration.class)) {
            return;
        }

        try {
            //We instantiate the Class in order to call the bean methods
            Object configInstance = configClass.getDeclaredConstructor().newInstance();

            //We get all the methods
            var methods = configInstance.getClass().getDeclaredMethods();
            //we need at least one method
            if (methods.length < 1) {
                throw new RuntimeException("No provider methods found for class " + configClass.getName());
            }

            for (var method : methods) {
                if (!method.isAnnotationPresent(MyTinyBean.class)) {
                    continue;
                }

                if (method.getReturnType() == Void.TYPE) {
                    throw new RuntimeException("Missing return type for method " + method.getName());
                }

                ArrayList<Object> values = new ArrayList<>();
                var parameters = method.getParameters();
                for (var parameter : parameters) {
                    if(!propertiesProvider.canProvide(parameter)){
                        continue;
                    }
                    var value = propertiesProvider.provide(parameter);
                    values.add(value);
                }

                var object = method.invoke(configInstance, values.toArray());
                var name = method.getName();

                beans.put(object.getClass(), object);
                System.out.println("Registered bean: " + name + " -> " + object.getClass().getSimpleName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to register configuration " + configClass, e);
        }
    }

    public <T> T getBean(Class<T> beanClass) {
        if(!beans.containsKey(beanClass)) {
            throw new RuntimeException("No bean registered for " + beanClass);
        }
        return (T)beans.get(beanClass);
    }

    public boolean isBeanPresent(Class<?> beanClass) {
        return beans.containsKey(beanClass);
    }
}
