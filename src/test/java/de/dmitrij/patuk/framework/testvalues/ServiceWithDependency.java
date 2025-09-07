package de.dmitrij.patuk.framework.testvalues;

public class ServiceWithDependency {
    private final String value;

    public ServiceWithDependency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
