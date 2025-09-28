package de.dmitrij.patuk.template.nodes;

import de.dmitrij.patuk.template.MyTinyContext;

import java.util.Objects;

public class MyTinyIdentifier implements MyTinyTemplateNode{
    private String name;

    public MyTinyIdentifier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyTinyIdentifier that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String render(MyTinyContext context) {
        return "";
    }

    @Override
    public String prettyPrint(String indent) {
        return indent + "Identifier(" + name + ")";
    }


}
