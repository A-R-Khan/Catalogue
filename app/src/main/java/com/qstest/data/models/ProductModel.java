package com.qstest.data.models;

import java.util.Objects;

public class ProductModel {

    protected String id;
    protected String name;
    protected String desc;
    protected String image;
    protected String price;

    protected ProductModel() {}

    public ProductModel(String id, String name, String desc, String image, String price) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.image = image;
        this.price = price;
    }

    //copy constructor
    public ProductModel(ProductModel productModel) {
        this(productModel.id, productModel.name, productModel.desc, productModel.image, productModel.price);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductModel that = (ProductModel) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(desc, that.desc) && Objects.equals(image, that.image) && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, desc, image, price);
    }
}
