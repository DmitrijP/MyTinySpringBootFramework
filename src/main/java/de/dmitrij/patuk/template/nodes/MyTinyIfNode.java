package de.dmitrij.patuk.template.nodes;

import de.dmitrij.patuk.template.MyTinyContext;

public class MyTinyIfNode implements MyTinyTemplateNode {
    MyTinyExpressionNode condition;
    MyTinyTemplate thenBranch;
    MyTinyTemplate elseBranch;

    public MyTinyIfNode(MyTinyExpressionNode condition, MyTinyTemplate thenBranch, MyTinyTemplate elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public MyTinyExpressionNode getCondition() {
        return condition;
    }

    public MyTinyTemplate getThenBranch() {
        return thenBranch;
    }

    public MyTinyTemplate getElseBranch() {
        return elseBranch;
    }

    @Override
    public String render(MyTinyContext context) {
        Object cond = condition.render(context);
        boolean truthy = cond != null && !cond.toString().isEmpty() && !cond.equals("false");
        if (truthy) {
            return thenBranch.render(context);
        } else if (elseBranch != null) {
            return elseBranch.render(context);
        }
        return "";
    }


    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("\nIf");
        sb.append(indent).append("\nCondition:");
        sb.append(condition.prettyPrint(indent + "    "));
        sb.append(indent).append("\n  Then:");
        sb.append(thenBranch.prettyPrint(indent + "    "));
        if (elseBranch != null) {
            sb.append(indent).append("\n  Else:");
            sb.append(elseBranch.prettyPrint(indent + "    "));
        }
        return sb.toString();
    }
}
