package com.iptv.player.data.model;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class Media {

    @Id
    private long id;
    @Unique
    private Long streamId;
    @Convert(converter = Converters.TypeConverter.class, dbType = Integer.class)
    private Type type;
    private Long categoryId;
    private Long subCategoryId;
    private String epgId;
    private String name;
    private String image;
    private Integer number;
    private boolean favorite;

    public Media() {
    }

    public Media(long id, Long streamId, Type type, Long categoryId, Long subCategoryId,
                 String epgId, String name, String image, Integer number, boolean favorite) {
        this.id = id;
        this.streamId = streamId;
        this.type = type;
        this.categoryId = categoryId;
        this.subCategoryId = subCategoryId;
        this.epgId = epgId;
        this.name = name;
        this.image = image;
        this.number = number;
        this.favorite = favorite;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getEpgId() {
        return epgId;
    }

    public void setEpgId(String epgId) {
        this.epgId = epgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
