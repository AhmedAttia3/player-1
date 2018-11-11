package com.iptv.player.data.model;

public enum Type {
    LIVE(0), VOD(1), SERIES(2);

    final int id;

    Type(int id) {
        this.id = id;
    }
}
