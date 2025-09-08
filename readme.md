# My tiny spring boot like framework

## 1. Creation of annotation and entrypoint into the framework

Firs we create our own annotation that marks the entrypoint into our framework. 
```java
//Keep the annotation during runtime
@Retention(RetentionPolicy.RUNTIME)
//only allowed for types/classes
@Target(ElementType.TYPE)
public @interface MyTinyBootApplication {
}
```

Important are the annotations `@Retention` and `@Target`.
- Retention has a parameter called `RetentionPolicy` it governs how long our annotation is kept on the class. Runtime means that is is kept during the runtime of our app and we can access this annotation during that time.
- Target governs what we can apply the annotation on e.g. Type, Method, Parameter...

The class `MyTinyApplication` is launched as early as possible and controls our whole framework.

```java
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
        System.out.println("Application started!");
    }
}
```
At this time it only creates a new instance of the Class it is applied to and prints some messages.

## 2. Reading properties file

A simple class that gets a file name and searches for it in our applications resources directory.
If found it is simply read by the existing Properties class and can be accessed with keys.
```java
public class MyTinyPropertiesScanner {
    private final Properties properties = new Properties();
    public MyTinyPropertiesScanner(String fileName) {
        //getClass() gets the current class
        //getClassLoader().getResourceAsStream() looks in the resource 
        // directory and opens a file
        try (var fis = getClass().getClassLoader()
                .getResourceAsStream(fileName)) {
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
```

## 3. Creating a Dependency Container

We create a container to scan our configuration classes that are marked with `@MyTinyConfiguration` and provide dependenceis via `@MyTinyBean`

The Annotations. Notice the Target of `MyTinyBean` it only targets methods.
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyTinyConfiguration {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyTinyBean {
}
```

Now the dependency container. It stores the instances of classes that are created inside methods annotated with `@MyTinyBean` inside a Map.
```java
public class MyTinyApplicationContext {
    private final Map<Class<?>, Object> beans = new HashMap<>();

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
                //we only care about annotated methods
                if (!method.isAnnotationPresent(MyTinyBean.class)) {
                    continue;
                }

                //our provider methods must have a return type that is not void.
                if (method.getReturnType() == Void.TYPE) {
                    throw new RuntimeException("Missing return type for method " + method.getName());
                }

                var object = method.invoke(configInstance);
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
}
```

## 4. Autoinjection of Properties

We have created a lass in `2. Reading properties files` that is able to read the .properties File and provide us with those properties.
We will now make use of that.

First we need an annotation to mark those parameters that should be provided with values from the properties file.
Notice the `Target` of the annotation and the property `name()` that is able to store a string.
This will be used to name the property key that we want to be provided.
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MyTinyValue {
    String name() default "";
}
```

Now the class that does most of the work. It receives the properties scanned that we build earlier.
Each call to `provide()` expects a Parameter. That parameter must be annotated with `@MyTinyValue`.
We extract the string that is inside the `name()` property of the annotation and use it to get the property from the `properties scanner`.
```java
public class MyTinyPropertiesProvider {
    private final MyTinyPropertiesScanner propertiesScanner;

    public MyTinyPropertiesProvider(MyTinyPropertiesScanner propertiesScanner) {
        this.propertiesScanner = propertiesScanner;
    }

    public String provide(Parameter parameter) {
        if (!parameter.isAnnotationPresent(MyTinyValue.class)) {
            throw new IllegalArgumentException("@MyTinyValue annotation is not present");
        }

        var annotation = parameter.getAnnotation(MyTinyValue.class);
        var propertyName = annotation.name();

        if(propertyName.isEmpty()) {
            throw new IllegalArgumentException("@MyTinyPropertyName annotation is not present");
        }

        return propertiesScanner.get(propertyName);
    }

    public boolean canProvide(Parameter parameter) {
        if (parameter == null){
            return false;
        }
        return parameter.isAnnotationPresent(MyTinyValue.class);
    }
}
```

Now the update to our dependency container. It requires the `PropertiesProvider` we just build.
We use it on each parameter of a method that is annotated with `@MyTinyBean` to get the properties and store them in a list.
This list is then passed to the constructor of our service to be created.
```java

public class MyTinyApplicationContext {
    private final Map<Class<?>, Object> beans = new HashMap<>();

    //new ===========
    private final MyTinyPropertiesProvider propertiesProvider;
    public MyTinyApplicationContext(MyTinyPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }
    //new ===========

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

                //new ===========
                ArrayList<Object> values = new ArrayList<>();
                var parameters = method.getParameters();
                for (var parameter : parameters) {
                    if(!propertiesProvider.canProvide(parameter)){
                        continue;
                    }
                    var value = propertiesProvider.provide(parameter);
                    values.add(value);
                }
                //new ===========

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
}
```
Now the Application class. It only requires a small change.
We inject the provider into our context, the rest should work just as it is.
```java

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

        //new ===========
        var propertiesProvider = new MyTinyPropertiesProvider(propertiesScanner);
        var context = new MyTinyApplicationContext(propertiesProvider);
        //new ===========

        context.registerConfiguration(AppConfig.class);
        var service = context.getBean(AppService.class);
        System.out.println("Starting " + service.call());


        System.out.println("Application started!");
    }
}
```

Now an update to our `AppConfig` and `AppService` classes.
The Config receives the `@MyTinyValue` notice the string `my.config-value` it is the key of our configuration inside the `application.properties` file.
The `AppService` just expects that value and stores it to print it later as an example.
```java
@MyTinyConfiguration
public class AppConfig {
    @MyTinyBean
    public AppService provideAppService(@MyTinyValue(name = "my.config-value") String value) {
        return new AppService(value);
    }
}

public class AppService {
    private final String importantConfigValue;

    public AppService(String importantConfigValue) {
        this.importantConfigValue = importantConfigValue;
    }

    public String call() {
        return "Hello from AppService! \n" + "The important config value is: " + importantConfigValue + "\n";
    }
}
```
The `application.properties` file
```properties
my.boot.application-name:My App Name
my.config-value:The Config Value
```

## 5. Scanning for annotated classes

Next we will need the ability to find all classes with a certain annotation in our package. For example all classes annotated with `@MyTinyConfiguration` in order to automatically register all dependency providers.

This class scans from the base directory of our application every single sub package and collects the Classes annotated with the given annotation.
They are simply returned inside a list for further processing.
```java

public class MyTinyClassScanner {

    public List<Class<?>> findAnnotatedClasses(String packageName, Class annotation) {
        System.out.println("findAnnotatedClasses " + packageName + " for " + annotation);
        var annotatedClasses = new ArrayList<Class<?>>();

        //Turn package names into a path
        var path = packageName.replace('.', '/');

        try {
            //find all directories in our package that we need to scan
            var resources = Thread.currentThread().getContextClassLoader().getResources(path);
            List<File> dirs = new ArrayList<>();
            while (resources.hasMoreElements()) {
                var url = resources.nextElement();
                dirs.add(new File(url.getFile()));
            }

            //scan the found directories for the annotated classes
            for (File dir : dirs) {
                annotatedClasses.addAll(findClasses(dir, packageName, annotation, 0));
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return annotatedClasses;
    }

    private List<Class<?>> findClasses(File dir, String packageName, Class annotation, int level)
            throws ClassNotFoundException {
        System.out.println("called findClasses " + packageName + " for " + annotation + " level " + level);
        List<Class<?>> classes = new ArrayList<>();
        if(dir == null){
            return classes;
        }
        if (!dir.exists()) {
            return classes;
        }
        //get all files in the directory
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                System.out.printf("Scanning directory %s...%n", f.getName());
                //go one directory down and scan again
                classes.addAll(findClasses(f, packageName + "." + f.getName(), annotation, level + 1));
                continue;
            }
            //we have a class
            if (f.getName().endsWith(".class")) {
                //we get the package name and get the class for that name
                String className = packageName + '.' + f.getName().replace(".class", "");
                System.out.println("Getting class for name " + className);
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(annotation)) {
                    System.out.println("Found class " + clazz.getSimpleName() + "with annotation " + annotation);
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }
}
```

Changes inside `MyTinyApplication`

We simply create a new instance of our `MyTinyClassScanner` and let it search for all classes annotated with `@MyTinyConfiguration`.
Next we register all those classes inside our `MyTinyApplicationContext` and simply request our `AppService` as a test.
```java
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
```

## 6. Providing classes on the fly

Now we will build the ability to just ask for a class that has a constructor annotated with `@MyTinyInject`.
This Provider should be able to take the class, look at its constructor, and create all required dependencies to instantiate that class.

Notice the `Target` of the annotation. It can only be applied on the constructor.
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface MyTinyInject {
}
```
Next the Provider itself. It requires the Context and Properties provider as a dependency in order to be able to request for already registered `Beans` and `Properties`.
It will find the constructor annotated with `@MyTinyInject` and request all parameters of that constructor.
We will iterate over all parameters and see if it is inside a `Bean` or a `Property`.
Then we just pass all those objects to the constructor and instantiate the object.

Notice the use of generics `<T>` we do this to take advantage of type safety.
```java
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
```

Changes to `MyTinyApplicationContext`
We have simply added a helper method that lets us check if the required type is present.
```java

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
    
    // new ===================
    public boolean isBeanPresent(Class<?> beanClass) {
        return beans.containsKey(beanClass);
    }
    // new ===================
}
```

The Change to `MyTinyApplication` is also minor.
We create an instance of `MyTinyClassProvider` and request a class from it as a test.
```java
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

        var classScanner = new MyTinyClassScanner();
        var configClasses = classScanner.findAnnotatedClasses(appClass.getPackageName(),
                MyTinyConfiguration.class);
        for(var configClass : configClasses) {
            context.registerConfiguration(configClass);
        }

        //new ====================
        var classProvider = new MyTinyClassProvider(context, propertiesProvider);
        var homeController = classProvider.getBeanClass(HomeController.class);
        homeController.index();
        //new ====================

        var service = context.getBean(AppService.class);
        System.out.println("Starting " + service.call());
        System.out.println("Application started!");
    }
}
```

We have also created a few test controller classes. That way we can test if our provider works.
```java

public class HomeController {
    @MyTinyInject
    public HomeController(){
    }

    public void index(){
        System.out.println("home controller");
    }
}


public class AppController {
    private final AppService appService;

    @MyTinyInject
    public AppController(AppService appService) {
        this.appService = appService;
    }

    public void index(){
        System.out.println("app controller: " + appService.call());
    }
}
```

## 7. Creating the Controller, Server and ServerHandler

Now we want to be able to automatically find all classes annotated with `@MyTinyController` and register them in our server for handling of get requests.

First we declare two annotations `MyTinyController` and `MyTinyGet` to mark controller classes and methods that should handle GET requests.
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyTinyController {
    String route() default "";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyTinyGet {
    String route() default "";
}
```

Next we need a simple server to open a connection and handle requests.
We create a server on a certain port and declare a method `bindContext` that will register our controllers and methods to their paths.
It will call the registered method and return a HTTP 200 response with a string.
We will use an anonymous class implementing `GetResponse` to hold the handlers.
```java 
public class MyTinyHttpServer {
    private final int port;
    private HttpServer server;

    public MyTinyHttpServer(int port) {
        try {
            this.port = port;
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.printf("Server created on port %d %n", port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void bindContext(String path, GetResponse response) {
        System.out.printf("Binding context for path: %s%n", path);
        server.createContext(path, exchange -> {
            System.out.printf("Received request from: %s%n", exchange.getRequestURI());
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(response.handle().getBytes(StandardCharsets.UTF_8));
            exchange.getResponseBody().close();
            exchange.close();
        });
    }

    public void start() {
        server.start();
        System.out.printf("Server created on port %d %n", port);
    }

    public void stop() {
        server.stop(0);
    }

    public interface GetResponse{
        String handle();
    }
}
```

Next the update to `MyTinyApplication`
We instantiate the new classes, use our class scanner to scan for controllers and pass each found controller to the controller handler in order to register its GET methods.
At last we start the server and create a new thread listening on the shutdown signal from the OS in order to gracefully shut down the server.
```java
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

        var classScanner = new MyTinyClassScanner();
        var configClasses = classScanner.findAnnotatedClasses(appClass.getPackageName(),
                MyTinyConfiguration.class);
        for(var configClass : configClasses) {
            context.registerConfiguration(configClass);
        }

        //new ====================
        var classProvider = new MyTinyClassProvider(context, propertiesProvider);
        var server = new MyTinyHttpServer(8080);
        var controllerHandler = new MyTinyControllerHandler(server, classProvider);

        var controllerClasses = classScanner.findAnnotatedClasses(appClass.getPackageName(), MyTinyController.class);
        for(var controllerClass : controllerClasses) {
            controllerHandler.registerController(controllerClass);
        }
        System.out.println("Application started!");
        server.start();

        // gracefully shutdown when a kill signal is sent
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
            System.out.println("Server stopped.");
        }));
        //new ====================
        
    }
}
```

## 8. QueryParam handling

We want to be able to handle query parameters inside our controller methods.
So we first create the annotation as usual.

Notice the `Target` and the `Parameter`of the annotation. It requires a name of the query parameter to be matched against our method parameter.
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MyTinyRequestParam {
    String name() default "";
}
```

Now the Handler
The query looks like this `name=value&name2=value2&whateverkey=value3`, so we first need to parse it into a key value map for easy access.
Our next step is the `handle()` method. It recieves a Parameter from our Controller method and matches it against a key from the query map.
We take the value from the map and cast it to the type of the parameter and simply return it.
```java 
public class MyTinyRequestParamHandler {
    public Map<String, String> getRequestParams(String query) {
        var parsedParams = new HashMap<String, String>();
        if (query == null) {
            return parsedParams;
        }
        if (query.isEmpty()) {
            return parsedParams;
        }
        var queryParams = query.split("&");
        for (var param : queryParams) {
            if (param.isEmpty()) {
                continue;
            }
            var paramParts = param.split("=");
            if (paramParts.length != 2) {
                continue;
            }
            parsedParams.put(paramParts[0], paramParts[1]);
        }
        return parsedParams;
    }

    public <T> T handle(Parameter methodParameter, Map<String, String> requestParams) {
        if (!methodParameter.isAnnotationPresent(MyTinyRequestParam.class)) {
            throw new RuntimeException(
                    "Parameter " + methodParameter.getName() + " is not annotated with @MyTinyRequestParam");
        }
        var annotation = methodParameter.getAnnotation(MyTinyRequestParam.class);
        var name = annotation.name();
        var type = methodParameter.getType();
        if (!requestParams.containsKey(name)) {
            throw new RuntimeException("Parameter " + name + " is not inside request parameters");
        }
        var value = requestParams.get(name);

        if (type.equals(String.class)) {
            return (T) value;
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            return (T) Integer.valueOf(value);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return (T) Long.valueOf(value);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return (T) Double.valueOf(value);
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            return (T) Float.valueOf(value);
        }
        throw new RuntimeException("Type " + type + " is not supported");
    }

    public boolean canHandle(Parameter parameter) {
        return parameter.isAnnotationPresent(MyTinyRequestParam.class);
    }
}
```

Now our HTTPServer, we update `getResponse` interface to receive the query for further processing.
We will simply take the query from the `URI` and pass it to our `ControllerHandler`
```java 

public class MyTinyHttpServer {
    private final int port;
    private HttpServer server;

    public MyTinyHttpServer(int port) {
        try {
            this.port = port;
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.printf("Server created on port %d %n", port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        server.start();
        System.out.printf("Server created on port %d %n", port);
    }

    public void bindContext(String path, GetResponse response) {
        System.out.printf("Binding context for path: %s%n", path);
        server.createContext(path, exchange -> {
            var uri = exchange.getRequestURI();
            System.out.printf("URI: %s%n", uri);
            var query = uri.getQuery();
            System.out.printf("Query: %s%n", query);
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(response.handle(query).getBytes(StandardCharsets.UTF_8));
            exchange.getResponseBody().close();
            exchange.close();
        });
    }

    public void stop() {
        server.stop(0);
    }

    public interface GetResponse{
        String handle(String query);
    }
}
```

Next we update our `MyTinyControllerHandler` to process the query.
It needs our new `MyTinyRequestParamHandler` as a dependency. Now we simply use it inside the matching method to first parse the query and next find all the required values from the query.
Those values are stored in an Object array and passed down to the invoked method of our matching controller.
```java
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
                    //new =============
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
                    //new =============
                        
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}
```

## 9. View rendering

Our next task is to implement a simple view renderer that is able to ingest a HTML template and map an object to that template.
We will first need a few provider that is able to ingest the HTML files.
```java

public class MyTinyViewProvider {
    public String provideTinyView(String viewName) {
        //we hard code the view location to resources/views
        try (var fis = getClass().getClassLoader().getResourceAsStream("/views" + viewName)) {
            return readFromInputStream(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file", e);
        }
    }

    private String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
```

Next the view renderer. It simply look for strings like `{{ PropertyName }}` inside our HTML and replace them with the matching properties from our Model.
```java
public class MyTinyViewRenderer {
    private final MyTinyViewProvider provider;

    public MyTinyViewRenderer(MyTinyViewProvider provider) {
        this.provider = provider;
    }

    public String render(String viewName, Object model) {
        var template = provider.provideTinyView(viewName);
        var fields = model.getClass().getDeclaredFields();
        for (var field : fields) {
            if(!field.canAccess(model)) {
                continue;
            }
            Object value = null;
            try {
                value = field.get(model);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Field was not accessible",e);
            }

            String placeholder = "{{" + field.getName() + "}}";
            template = template.replace(placeholder, value != null ? value.toString() : "");
        }
        return template;
    }
}
```

We will expect a `MyTinyModelAndView` as a return type from our controller methods in order to be able to recognise Methods that require view rendering.
```java
public class MyTinyModelAndView {
    private final String viewName;
    private final Object model;

    public MyTinyModelAndView(String viewName, Object model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public Object getModel() {
        return model;
    }
}
```

This ModelAndView will be used in our `MyTinyControllerHandler`. It receives an additional dependency the `MyTinyViewRenderer`.
We change the Handler to check if the return type of our method is a `String` oder a `MyTinyModelAndView` and use the view renderer to take the HTML file and replace its placeholders according to the model. 

```java

public class MyTinyControllerHandler {
    private final MyTinyHttpServer server;
    private final MyTinyClassProvider classProvider;
    private final MyTinyRequestParamHandler requestQueryHandler;
    private final MyTinyViewRenderer viewRenderer;

    public MyTinyControllerHandler(MyTinyHttpServer server,
                                   MyTinyClassProvider classProvider,
                                   MyTinyRequestParamHandler requestHandler,
                                   MyTinyViewRenderer viewRenderer) {
        this.server = server;
        this.classProvider = classProvider;
        this.requestQueryHandler = requestHandler;
        this.viewRenderer = viewRenderer;
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
                        
                        // new ========
                        var type = method.getReturnType();
                        if(type == String.class) {
                            return (String) method.invoke(controllerInstance,  params.toArray());
                        }
                        if(type == MyTinyModelAndView.class) {
                            var mv = (MyTinyModelAndView) method.invoke(controllerInstance, params.toArray());
                            var viewName = classRoute +  mv.getViewName();
                            return viewRenderer.render(viewName, mv.getModel());
                        }
                        throw new RuntimeException("No suitable rendering method found!");
                        // new ========
                        
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}
```
Don't forget to update `MyTinyApplication` to instantiate the new renderer and pass it to our handler.

```java
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

        var classScanner = new MyTinyClassScanner();
        var configClasses = classScanner.findAnnotatedClasses(appClass.getPackageName(),
                MyTinyConfiguration.class);
        for(var configClass : configClasses) {
            context.registerConfiguration(configClass);
        }

        var classProvider = new MyTinyClassProvider(context, propertiesProvider);
        var server = new MyTinyHttpServer(8080);

        //new ====================
        var viewProvider = new MyTinyViewProvider();
        var viewRenderer = new MyTinyViewRenderer(viewProvider);
        var myRequestParamHandler = new MyTinyRequestParamHandler();
        var controllerHandler = new MyTinyControllerHandler(server, classProvider, myRequestParamHandler, viewRenderer);
        //new ====================

        var controllerClasses = classScanner.findAnnotatedClasses(appClass.getPackageName(), MyTinyController.class);
        for(var controllerClass : controllerClasses) {
            controllerHandler.registerController(controllerClass);
        }
        System.out.println("Application started!");
        server.start();

        // gracefully shutdown when a kill signal is sent
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
            System.out.println("Server stopped.");
        }));
    }
}
```

Now we need to update our controller and create a HTML view.
```java

@MyTinyController(route = "/app")
public class AppController {
    private final AppService appService;

    @MyTinyInject
    public AppController(AppService appService) {
        this.appService = appService;
    }

    @MyTinyGet(route = "index")
    public String index(@MyTinyRequestParam(name = "name") String name) {
        return wrapInHtml("app controller \n" + "called by: " + name, appService.call());
    }

    @MyTinyGet(route = "model-and-view")
    public MyTinyModelAndView modelAndView(@MyTinyRequestParam(name = "name") String name) {
        return new MyTinyModelAndView("/index", new Container("Hello " + name, "This is a body!"));
    }

    private String wrapInHtml(String controller, String serviceResult) {
        return String.format("<html><body><h1>Welcome To</h1><p>%s</p><p>%s</p></body></html>", controller, serviceResult);
    }

    public class Container {
        private final String title;
        private final String body;

        public Container(String title,String body) {
            this.title = title;
            this.body = body;
        }

        public String getBody() {
            return body;
        }

        public String getTitle() {
            return title;
        }
    }
}
```

The HTML View inside `resources/views/app/index.html`
```html 
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<h1>{{ Title }}</h1>
<p>{{ Body }}</p>
</body>
</html>
```