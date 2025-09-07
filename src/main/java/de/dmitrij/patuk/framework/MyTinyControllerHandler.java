package de.dmitrij.patuk.framework;

import java.lang.reflect.InvocationTargetException;

public class MyTinyControllerHandler {
    private final MyTinyHttpServer server;
    private final MyTinyClassProvider classProvider;

    public MyTinyControllerHandler(MyTinyHttpServer server, MyTinyClassProvider classProvider) {
        this.server = server;
        this.classProvider = classProvider;
    }

    public void registerController(Class<?> controller) {
        System.out.printf("Registering controller: %s\n", controller.getSimpleName());
        if (!controller.isAnnotationPresent(MyTinyController.class)) {
            return;
        }
        var classAnnotation = controller.getAnnotation(MyTinyController.class);
        var classRoute = classAnnotation.route();
        System.out.printf("Registering class route: %s\n", classRoute);
        var methods = controller.getDeclaredMethods();
        for (var method : methods) {
            if (method.isAnnotationPresent(MyTinyGet.class)) {
                var methodAnnotation = method.getAnnotation(MyTinyGet.class);
                var methodRoute = methodAnnotation.route();
                System.out.printf("Registering method route: %s\n", methodRoute);

                server.bindContext(classRoute + "/" + methodRoute, () -> {
                    try {
                        var controllerInstance = classProvider.getBeanClass(controller);
                        return (String) controller.getDeclaredMethod(method.getName()).invoke(controllerInstance);
                    } catch (IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}
