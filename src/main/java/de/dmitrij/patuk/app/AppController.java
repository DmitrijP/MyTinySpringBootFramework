package de.dmitrij.patuk.app;

import de.dmitrij.patuk.framework.*;

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
