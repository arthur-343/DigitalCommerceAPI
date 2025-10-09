package com.arthur.digitalcommerce.payload;

import com.arthur.digitalcommerce.model.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long productId;
    private String productName;
    private String description;
    private String image;
    private Integer quantityInStock;
    private Integer cartQuantity;
    private String warningMessage;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal specialPrice;
    private boolean specialPriceActive;
    private Long categoryId;



}
