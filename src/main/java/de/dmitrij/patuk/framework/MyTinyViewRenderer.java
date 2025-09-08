package de.dmitrij.patuk.framework;

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
