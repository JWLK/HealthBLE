package com.jwlks.healthble.search;

import android.graphics.drawable.Drawable;

public class SearchExerciseModel {
    private Drawable iconDrawable ;
    private String mTitle ;
    private String mDesc ;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        mTitle = title ;
    }
    public void setDesc(String desc) {
        mDesc = desc ;
    }

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.mTitle ;
    }
    public String getDesc() {
        return this.mDesc ;
    }
}
