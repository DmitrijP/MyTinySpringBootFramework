package de.dmitrij.patuk.framework;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class MyTinyClassProvider {
    private final MyTinyApplicationContext context;
    private final MyTinyPropertiesProvider propertiesProvider;

    public MyTinyClassProvider(MyTinyApplicationContext context, MyTinyPropertiesProvider propertiesProvider) {
        this.context = context;
        this.propertiesProvider = propertiesProvider;
    }

    public <T> T getBeanClass(Class<T> clazz) {
        var constructors = clazz.getDeclaredConstructors();
        for (var constructor : constructors) {
            if (!constructor.isAnnotationPresent(MyTinyInject.class)) {
                continue;
            }

            var params = constructor.getParameters();
            if (params.length == 0) {
                try {
                    return (T) constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            ArrayList<Object> values = new ArrayList<>();
            for (var param : params) {

                if (propertiesProvider.canProvide(param)) {
                    var bean = propertiesProvider.provide(param);
                    if (bean == null) {
                        throw new RuntimeException(String.format("bean %s not found", param.getName()));
                    }
                    values.add(bean);
                    continue;
                }

                var paramType = param.getType();
                if (context.isBeanPresent(paramType)) {
                    var bean = context.getBean(paramType);
                    if (bean == null) {
                        throw new RuntimeException(String.format("bean %s not found", param.getName()));
                    }
                    values.add(bean);
                    continue;
                }
                throw new RuntimeException(String.format("bean %s not found", param.getName()));
            }
            try {
                var instance = constructor.newInstance(values.toArray());
                return (T) instance;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException(String.format("bean %s not found", clazz.getName()));
    }
}
