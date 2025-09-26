package com.arthur.digitalcommerce.events;

import com.arthur.digitalcommerce.model.Product;

public class ProductUpdatedEvent {

    private final Product product;

    public ProductUpdatedEvent(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}