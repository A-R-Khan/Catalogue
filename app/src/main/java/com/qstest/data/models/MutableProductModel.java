package com.qstest.data.models;

public class MutableProductModel extends ProductModel {

    public MutableProductModel(){
        super();
    }
    public MutableProductModel(String id, String name, String desc, String image, String price) {
        super(id, name, desc, image, price);
    }

    public ProductModel makeImmutable() {
        return new ProductModel(getId(), getName(), getDesc(), getImage(), getPrice());
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setPrice(String price) {
        this.price = price;
    }

}
