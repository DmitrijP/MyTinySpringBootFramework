package de.dmitrij.patuk.template.nodes;

import de.dmitrij.patuk.template.MyTinyContext;

import java.util.Objects;

public class MyTinyExpressionNode implements MyTinyTemplateNode {
    MyTinyPropertyPath propertyPath;

    public MyTinyExpressionNode(MyTinyPropertyPath propertyPath) {
        this.propertyPath = propertyPath;
    }

    public MyTinyPropertyPath getPropertyPath() {
        return propertyPath;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyTinyExpressionNode that)) return false;
        return Objects.equals(propertyPath, that.propertyPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(propertyPath);
    }

    @Override
    public String render(MyTinyContext context) {
        Object value = context.resolve(propertyPath);
        return value != null ? value.toString() : "";
    }

    @Override
    public String prettyPrint(String indent) {
        return "\n" + indent + "Expression(" + propertyPath.prettyPrint("") + ")";
    }
}
