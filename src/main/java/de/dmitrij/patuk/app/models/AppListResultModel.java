package de.dmitrij.patuk.app.models;

import java.util.List;

public class AppListResultModel {
    private List<AppListModel> lists;

    public AppListResultModel(List<AppListModel> lists) {
        this.lists = lists;
    }

    public List<AppListModel> getLists() {
        return lists;
    }
}
