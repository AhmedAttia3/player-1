package com.iptv.player.data.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class SubCategory {

    @Id(assignable = true)
    private Long id;
    private Long categoryId;
    private Integer number;
    private String coverImage;
    private String plot;
    private String cast;
    private String director;
    private String genre;
    private String releaseDate;
    private String rating;
    private String backDrop;
    private String youtubeId;
    private String episodeRunTime;

    public SubCategory() {
    }

    public SubCategory(Long id, Long categoryId, Integer number, String coverImage,
                       String plot, String cast, String director, String genre, String releaseDate,
                       String rating, String backDrop, String youtubeId, String episodeRunTime) {
        this.id = id;
        this.categoryId = categoryId;
        this.number = number;
        this.coverImage = coverImage;
        this.plot = plot;
        this.cast = cast;
        this.director = director;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.backDrop = backDrop;
        this.youtubeId = youtubeId;
        this.episodeRunTime = episodeRunTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getBackDrop() {
        return backDrop;
    }

    public void setBackDrop(String backDrop) {
        this.backDrop = backDrop;
    }

    public String getYoutubeId() {
        return youtubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public String getEpisodeRunTime() {
        return episodeRunTime;
    }

    public void setEpisodeRunTime(String episodeRunTime) {
        this.episodeRunTime = episodeRunTime;
    }
}
