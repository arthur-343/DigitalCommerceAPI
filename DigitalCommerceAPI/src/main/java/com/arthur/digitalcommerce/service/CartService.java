package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.payload.CartDTO;
import java.util.List;

public interface CartService {

    CartDTO addProductToCart(Long productId, Integer quantity);

    CartDTO getCartForCurrentUser();

    CartDTO updateProductQuantityInCart(Long productId, Integer quantity);

    String deleteProductFromCart(Long productId);

    void clearCartByUserEmail(String email);

    List<CartDTO> getAllCarts();

    // O método getCart(String email, Long cartId) foi removido em favor do getCartForCurrentUser()
    // para manter a lógica de negócio centralizada e o controller limpo.
}

