package de.dmitrij.patuk.template.nodes;

import de.dmitrij.patuk.template.MyTinyContext;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MyTinyPropertyPath implements MyTinyTemplateNode {
    List<MyTinyIdentifier> parts;
    public MyTinyPropertyPath(List<MyTinyIdentifier> parts) {
        this.parts = parts;
    }

    public List<MyTinyIdentifier> getParts() {
        return parts;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyTinyPropertyPath that)) return false;
        return Objects.equals(parts, that.parts);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parts);
    }

    @Override
    public String render(MyTinyContext context) {
        return "";
    }

    @Override
    public String prettyPrint(String indent) {
        return indent + "PropertyPath" +
                parts.stream()
                        .map(p -> p.prettyPrint(indent + "  "))
                        .collect(Collectors.joining());
    }
}
