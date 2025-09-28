package de.dmitrij.patuk.template.nodes;

import de.dmitrij.patuk.template.MyTinyContext;

import java.util.Objects;

public class MyTinyForeachNode implements MyTinyTemplateNode {
    MyTinyIdentifier loopVar;
    MyTinyExpressionNode collectionName;
    MyTinyTemplate children;

    public MyTinyForeachNode(MyTinyIdentifier loopVar, MyTinyExpressionNode collectionName, MyTinyTemplate children) {
        this.loopVar = loopVar;
        this.collectionName = collectionName;
        this.children = children;
    }

    public MyTinyIdentifier getLoopVar() {
        return loopVar;
    }

    public MyTinyExpressionNode getCollectionName() {
        return collectionName;
    }

    public MyTinyTemplate getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyTinyForeachNode that)) return false;
        return Objects.equals(loopVar, that.loopVar) && Objects.equals(collectionName, that.collectionName) && Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loopVar, collectionName, children);
    }

    @Override
    public String render(MyTinyContext context) {
        Object col = context.resolve(collectionName.getPropertyPath());
        if (!(col instanceof Iterable<?> iterable)) return "";
        StringBuilder sb = new StringBuilder();
        for (Object item : iterable) {
            context.pushScope();
            context.set(loopVar.getName(), item);
            sb.append(children.render(context));
            context.popScope();
        }
        return sb.toString();
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n" + indent);
        sb.append(indent).append("Foreach (").append(loopVar.prettyPrint("")).append(" in ").append(collectionName.prettyPrint("")).append(")\n");
        sb.append(children.prettyPrint(indent + "  "));
        return sb.toString();
    }
}
