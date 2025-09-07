package de.dmitrij.patuk.framework;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class MyTinyControllerHandler {
    private final MyTinyHttpServer server;
    private final MyTinyClassProvider classProvider;
    private final MyTinyRequestParamHandler requestQueryHandler;

    public MyTinyControllerHandler(MyTinyHttpServer server,
                                   MyTinyClassProvider classProvider,
                                   MyTinyRequestParamHandler requestHandler) {
        this.server = server;
        this.classProvider = classProvider;
        this.requestQueryHandler = requestHandler;
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

                server.bindContext(classRoute + "/" + methodRoute, (String query) -> {
                    var queryParams = requestQueryHandler.getRequestParams(query);

                    try {
                        var controllerInstance = classProvider.getBeanClass(controller);
                        var methodParams = method.getParameters();
                        ArrayList<Object> params = new ArrayList<>();
                        for (var param : methodParams) {
                            if(requestQueryHandler.canHandle(param)){
                               params.add(requestQueryHandler.handle(param, queryParams));
                            }
                        }
                        return (String) method.invoke(controllerInstance,  params.toArray());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}
