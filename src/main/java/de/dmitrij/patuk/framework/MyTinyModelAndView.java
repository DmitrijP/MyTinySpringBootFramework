package de.dmitrij.patuk.framework;

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
