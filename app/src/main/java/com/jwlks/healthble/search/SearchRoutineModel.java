package com.jwlks.healthble.search;

import android.graphics.drawable.Drawable;

public class SearchRoutineModel {
    private Drawable iconDrawable ;
    private String mTitle ;
    private String mCounter ;
    private String mTimer ;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        mTitle = title ;
    }
    public void setCounter(String counter) {
        mCounter = counter ;
    }
    public void setTimer(String timer) {
        mCounter = timer ;
    }

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.mTitle ;
    }
    public String getCounter() {
        return this.mCounter ;
    }
    public String getTimer() {
        return this.mTimer ;
    }
}
