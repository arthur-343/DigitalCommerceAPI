package com.arthur.digitalcommerce.payload;

import com.arthur.digitalcommerce.model.Cart;
import com.arthur.digitalcommerce.model.Product;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {

    private Long CartItemId;

    private CartDTO cart;

    private ProductDTO productDTO;

    private Integer quantity;


    private BigDecimal discount;


}
