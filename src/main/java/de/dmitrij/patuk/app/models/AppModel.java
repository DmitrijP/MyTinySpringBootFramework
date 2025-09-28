package de.dmitrij.patuk.app.models;

import java.util.Collection;

public class AppModel {
    private String name;
    private String appRating;
    private String imgUrl;
    private Collection<String> tags;
    private boolean isPremium;
    private String installUrl;

    public AppModel(String name, String appRating, String imgUrl, Collection<String> tags, boolean isPremium, String installUrl) {
        this.name = name;
        this.appRating = appRating;
        this.imgUrl = imgUrl;
        this.tags = tags;
        this.isPremium = isPremium;
        this.installUrl = installUrl;
    }

    public String getName() {
        return name;
    }

    public String getAppRating() {
        return appRating;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public String getInstallUrl() {
        return installUrl;
    }
}
