package com.iptv.player.data.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Category {

    @Id(assignable = true)
    private Long id;
    private String name;
    private int number;
    private boolean locked;

    public Category() {
    }

    public Category(Long id, String name, int number, boolean locked) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.locked = locked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
