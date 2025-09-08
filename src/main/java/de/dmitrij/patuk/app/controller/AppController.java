package de.dmitrij.patuk.app.controller;

import de.dmitrij.patuk.app.services.AppService;
import de.dmitrij.patuk.framework.*;

import java.util.Collection;
import java.util.List;

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

    @MyTinyGet(route = "iterations")
    public MyTinyModelAndView modelAndView(@MyTinyRequestParam(name = "name") String name, @MyTinyRequestParam(name = "age")String age) {
        return new MyTinyModelAndView("/iterations", new Container2(name, age, List.of("Mannheim", "Heidelberg", "Karlsruhe", "Bruchsal")));
    }

    private String wrapInHtml(String controller, String serviceResult) {
        return String.format("<html><body><h1>Welcome To</h1><p>%s</p><p>%s</p></body></html>", controller, serviceResult);
    }

    public class Container2 {
        private final String name;
        private final String age;
        private final Collection<String> values;

        public Container2(String name, String age, Collection<String> values) {
            this.name = name;
            this.age = age;
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public String getAge() {
            return age;
        }

        public Collection<String> getValues() {
            return values;
        }
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
