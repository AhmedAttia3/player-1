package com.iptv.player.data;

import com.iptv.player.data.model.Category;
import com.iptv.player.data.model.Media;
import com.iptv.player.data.model.SubCategory;

import io.objectbox.Box;

public class DataRepository {

    private static DataRepository Instance = null;

    private Box<Category> categoryBox;
    private Box<SubCategory> subCategoryBox;
    private Box<Media> mediaBox;

    private DataRepository() {
    }

    public static DataRepository getInstance() {
        if (Instance == null) {
            Instance = new DataRepository();
        }

        return Instance;
    }

    public Box<Category> getCategoryBox() {
        return categoryBox;
    }

    public Box<SubCategory> getSubCategoryBox() {
        return subCategoryBox;
    }

    public Box<Media> getMediaBox() {
        return mediaBox;
    }
}
