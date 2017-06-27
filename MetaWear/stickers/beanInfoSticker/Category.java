package com.alesp.feedbackapp.MetaWear.stickers.beanInfoSticker;

/**
 * Created by claudio on 26/04/16.
 */
public class Category {
    private String name;
    private String model;
    private String manipulation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManipulation() {
        return manipulation;
    }

    public void setManipulation(String manipulation) {
        this.manipulation = manipulation;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", model='" + model + '\'' +
                ", manipulation='" + manipulation + '\'' +
                '}';
    }
}
