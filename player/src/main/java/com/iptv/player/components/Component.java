package com.iptv.player.components;

public class Component<T extends UIView> {

    private T view;
    private Presenter<T> presenter;

    public Component(T view, Presenter<T> presenter) {
        this.view = view;
        this.presenter = presenter;
        this.presenter.setUiView(this.view);
    }

    public T getView() {
        return view;
    }

    public Presenter<T> getPresenter() {
        return presenter;
    }
}
