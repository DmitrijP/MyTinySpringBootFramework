package de.dmitrij.patuk.app.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppModelFactory {
    public static List<AppListModel> createAppListModels() {
        List<AppListModel> appListModels = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            List<AppModel> apps = new ArrayList<>();
            for (int j = 1; j <= 10; j++) {
                apps.add(new AppModel(
                        "App " + i + "." + j,
                        String.valueOf(3.5 + (j % 5) * 0.3),
                        "https://picsum.photos/seed/" + i + "_" + j + "/200/200",
                        Arrays.asList("tag" + (j % 3), "tag" + ((j + 1) % 3)),
                        j % 2 == 0,
                        "https://example.com/install/" + i + "." + j));
            }
            appListModels.add(new AppListModel(
                    apps,
                    "Lorem Ipsum Title " + i,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. List " + i
            ));
        }
        return appListModels;
    }
}
