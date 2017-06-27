package com.alesp.feedbackapp.MetaWear.stickers.beanInfoSticker;

import java.util.List;

/**
 * Created by claudio on 26/04/16.
 */
public class BeanInfoSticker{
    private List<Sticker> sticker;
    private List<Category> category;

    public List<Sticker> getSticker() {
        return sticker;
    }

    public void setSticker(List<Sticker> sticker) {
        this.sticker = sticker;
    }

    public List<Category> getCategory() {
        return category;
    }

    public void setCategory(List<Category> category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "InfoSticker{" +
                "sticker=" + sticker +
                ", category=" + category +
                '}';
    }
}
