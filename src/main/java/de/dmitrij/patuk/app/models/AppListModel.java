package de.dmitrij.patuk.app.models;

import java.util.List;

public class AppListModel {
    private List<AppModel> apps;
    private  String title;
    private  String description;


    public AppListModel(List<AppModel> apps, String title, String description) {
        this.apps = apps;
        this.title = title;
        this.description = description;
    }

    public List<AppModel> getApps() {
        return apps;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
