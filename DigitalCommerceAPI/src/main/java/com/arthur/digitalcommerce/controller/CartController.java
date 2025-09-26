package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.payload.CartDTO;
import com.arthur.digitalcommerce.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // URL base para todos os endpoints
public class CartController {

    private final CartService cartService;

    // Injeção de dependência via construtor
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Adiciona um produto ao carrinho do usuário logado.
     */
    @PostMapping("/carts/products/{productId}")
    public ResponseEntity<CartDTO> addProductToCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    /**
     * Retorna o carrinho do usuário atualmente logado.
     */
    @GetMapping("/carts/my-cart")
    public ResponseEntity<CartDTO> getMyCart() {
        CartDTO cartDTO = cartService.getCartForCurrentUser();
        if (cartDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cartDTO);
    }

    /**
     * Atualiza a quantidade de um produto no carrinho do usuário.
     */
    @PutMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> updateProductQuantity(
            @PathVariable Long productId,
            @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId, quantity);
        return ResponseEntity.ok(cartDTO);
    }

    /**
     * Remove um produto do carrinho do usuário.
     */
    @DeleteMapping("/carts/products/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(productId);
        return ResponseEntity.ok(status);
    }

    /**
     * [ADMIN] Endpoint para listar todos os carrinhos do sistema.
     */
    @GetMapping("/admin/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTOs, HttpStatus.OK); // Status corrigido para OK
    }
}
