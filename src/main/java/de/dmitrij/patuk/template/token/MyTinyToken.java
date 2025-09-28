package de.dmitrij.patuk.template.token;

import java.util.Objects;

public class MyTinyToken {
    private final String token;
    private final MyTinyTokenType type;

    public MyTinyToken(String token, MyTinyTokenType type) {
        this.token = token;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public MyTinyTokenType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyTinyToken that)) return false;
        return Objects.equals(token, that.token) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, type);
    }

    @Override
    public String toString() {
        return type.name() + "(\"" + token + "\")";
    }
}
