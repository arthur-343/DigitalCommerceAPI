package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.events.ProductDeletedEvent;
import com.arthur.digitalcommerce.events.ProductUpdatedEvent;
import com.arthur.digitalcommerce.exceptions.APIException;
import com.arthur.digitalcommerce.exceptions.ResourceNotFoundException;
import com.arthur.digitalcommerce.model.Cart;
import com.arthur.digitalcommerce.model.CartItem;
import com.arthur.digitalcommerce.model.Product;
import com.arthur.digitalcommerce.payload.CartDTO;
import com.arthur.digitalcommerce.payload.ProductDTO;
import com.arthur.digitalcommerce.repository.CartItemRepository;
import com.arthur.digitalcommerce.repository.CartRepository;
import com.arthur.digitalcommerce.repository.ProductRepository;
import com.arthur.digitalcommerce.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;
    private final AuthUtil authUtil;

    // ... addProductToCart e getCartForCurrentUser permanecem os mesmos ...

    @Override
    @Transactional
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = getOrCreateCartForCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + quantity;
            if (product.getQuantityInStock() < newQuantity) {
                throw new APIException("Not enough stock for " + product.getProductName() + ". Available: " + product.getQuantityInStock());
            }
            cartItem.setQuantity(newQuantity);

        } else {
            if (product.getQuantityInStock() < quantity) {
                throw new APIException("Not enough stock for " + product.getProductName());
            }

            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
            cartItem.setQuantity(quantity);
            cartItem.setDiscount(BigDecimal.ZERO);

            cart.getCartItems().add(cartItem);
        }

        recalculateCartTotal(cart);
        cartRepository.save(cart);

        return mapToDTO(cart);
    }


    public CartDTO getCartForCurrentUser(){
        Cart cart = getOrCreateCartForCurrentUser();
        recalculateCartTotal(cart);
        cartRepository.save(cart);

        return mapToDTO(cart);
    }

    // ======================================================= //
    // MÉTODOS DE EXCLUSÃO "BRUTOS" E REUTILIZÁVEIS            //
    // ======================================================= //

    /**
     * NOVO HELPER "BRUTO" E SEGURO
     * Deleta um único item do carrinho usando a estratégia explícita (manual).
     * É "burro" (recebe o carrinho) para funcionar em qualquer contexto.
     */
    @Transactional
    private boolean removeProductFromCartExplicitly(Cart cart, Long productId) {
        // 1. Encontra o item
        CartItem cartItemToRemove = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId)
                .orElse(null);

        if (cartItemToRemove != null) {
            // 2. Remove da lista em memória
            cart.getCartItems().remove(cartItemToRemove);
            // 3. Deleta do banco EXPLICITAMENTE
            cartItemRepository.delete(cartItemToRemove);

            recalculateCartTotal(cart);
            cartRepository.save(cart);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        Cart cart = getOrCreateCartForCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (quantity <= 0) {
            // Chama o novo helper "bruto"
            removeProductFromCartExplicitly(cart, productId);
            return mapToDTO(cart);
        }

        if (product.getQuantityInStock() < quantity) {
            throw new APIException("Not enough stock. Available: " + product.getQuantityInStock());
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId)
                .orElseThrow(() -> new APIException("Product not found in cart."));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        recalculateCartTotal(cart);
        cartRepository.save(cart);

        return mapToDTO(cart);
    }

    @Override
    @Transactional
    public String deleteProductFromCart(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Cart cart = getOrCreateCartForCurrentUser();

        // Chama o novo helper "bruto"
        boolean removed = removeProductFromCartExplicitly(cart, productId);

        if (removed) {
            return "Product '" + product.getProductName() + "' successfully removed from cart!";
        } else {
            return "Product '" + product.getProductName() + "' not found in cart.";
        }
    }

    @EventListener
    @Transactional
    public void handleProductDelete(ProductDeletedEvent event) {
        Long productId = event.getProductId();
        List<Cart> cartsToUpdate = cartRepository.findCartsByProductId(productId);

        for (Cart cart : cartsToUpdate) {
            // Chama o novo helper "bruto" para cada carrinho
            removeProductFromCartExplicitly(cart, productId);
        }
    }

    /**
     * MÉTODO BRUTO PARA O WEBHOOK (que já funcionou)
     */
    @Override
    @Transactional
    public void clearCartByUserEmail(String email) {
        cartRepository.findByUserEmail(email).ifPresent(cart -> {
            if (!cart.getCartItems().isEmpty()) {

                // 1. Ordem bruta (DELETE * FROM cart_item WHERE cart_id = ?)
                cartItemRepository.deleteByCart(cart);

                // 2. Limpa a lista em memória
                cart.getCartItems().clear();

                cart.setTotalPrice(BigDecimal.ZERO);
                cartRepository.save(cart);
            }
        });
    }

    // ======================================================= //
    // MÉTODOS AUXILIARES (NENHUMA MUDANÇA ABAIXO)             //
    // ======================================================= //

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            throw new APIException("No cart exists");
        }
        return carts.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @EventListener
    @Transactional
    public void handleProductUpdate(ProductUpdatedEvent event) {
        Product updatedProduct = event.getProduct();
        List<Cart> cartsToUpdate = cartRepository.findCartsByProductId(updatedProduct.getProductId());
        for (Cart cart : cartsToUpdate) {
            recalculateCartTotal(cart);
            cartRepository.save(cart);
        }
    }

    private Cart getOrCreateCartForCurrentUser() {
        String email = authUtil.loggedInEmail();
        return cartRepository.findByUserEmail(email).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setTotalPrice(BigDecimal.ZERO);
            newCart.setUser(authUtil.loggedInUser());
            return cartRepository.save(newCart);
        });
    }

    public void validateCartForCheckout(Cart cart) {
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new APIException("Cannot proceed to checkout with an empty cart.");
        }
        for (CartItem item : cart.getCartItems()) {
            Product product = productRepository.findById(item.getProduct().getProductId())
                    .orElseThrow(() -> new APIException("Product '" + item.getProduct().getProductName() +
                            "' is no longer available. Please remove it from your cart."));
            if (product.getQuantityInStock() <= 0) {
                throw new APIException("Sorry, '" + product.getProductName() +
                        "' is now out of stock. Please remove it from your cart to proceed.");
            } else if (item.getQuantity() > product.getQuantityInStock()) {
                throw new APIException("Cannot proceed to checkout. Product '" + product.getProductName() +
                        "' has insufficient stock. Available: " + product.getQuantityInStock() +
                        ", in your cart: " + item.getQuantity());
            }
        }
    }

    private void recalculateCartTotal(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            BigDecimal currentBasePrice;
            if (product.isSpecialPriceActive() && product.getSpecialPrice() != null) {
                currentBasePrice = product.getSpecialPrice();
            } else {
                currentBasePrice = product.getPrice();
            }
            BigDecimal finalUnitPrice = currentBasePrice;
            BigDecimal discountPercent = item.getDiscount();
            if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountMultiplier = discountPercent.divide(new BigDecimal("100"));
                BigDecimal discountAmount = currentBasePrice.multiply(discountMultiplier);
                finalUnitPrice = currentBasePrice.subtract(discountAmount);
            }
            BigDecimal subtotal = finalUnitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
        }
        cart.setTotalPrice(total);
    }

    private CartDTO mapToDTO(Cart cart) {
        recalculateCartTotal(cart);
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<ProductDTO> productDTOs = cart.getCartItems().stream().map(item -> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            productDTO.setCartQuantity(item.getQuantity());
            if (productDTO.getCartQuantity() > productDTO.getQuantityInStock()) {
                if (productDTO.getQuantityInStock() > 0) {
                    productDTO.setWarningMessage("Warning! Only " + productDTO.getQuantityInStock() + " units available in stock.");
                } else {
                    productDTO.setWarningMessage("Product out of stock! Please remove it from the cart to continue.");
                }
            }
            return productDTO;
        }).collect(Collectors.toList());
        cartDTO.setProducts(productDTOs);
        return cartDTO;
    }
}