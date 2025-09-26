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
import org.modelmapper.ModelMapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;
    private final AuthUtil authUtil;

    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository, CartItemRepository cartItemRepository, ModelMapper modelMapper, AuthUtil authUtil) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
    }

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


    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        Cart cart = getOrCreateCartForCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (quantity <= 0) {
            return mapToDTO(deleteItemFromCart(cart, productId));
        }

        // --- ALTERADO AQUI ---
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
        Cart cart = getOrCreateCartForCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        deleteItemFromCart(cart, productId);
        return "Product '" + product.getProductName() + "' successfully removed from the cart!";
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            throw new APIException("No cart exists");
        }
        return carts.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearCartByUserEmail(String email) {
        cartRepository.findByUserEmail(email).ifPresent(cart -> {
            if (!cart.getCartItems().isEmpty()) {
                cartItemRepository.deleteAll(cart.getCartItems());
                cart.getCartItems().clear();
                cart.setTotalPrice(BigDecimal.ZERO);
                cartRepository.save(cart);
            }
        });
    }

    // *** LISTENERS DE EVENTOS E MÉTODOS PRIVADOS ***

    @EventListener
    @Transactional
    public void handleProductUpdate(ProductUpdatedEvent event) {
        Product updatedProduct = event.getProduct();
        // Encontra todos os carrinhos que contêm o produto que foi alterado.
        List<Cart> cartsToUpdate = cartRepository.findCartsByProductId(updatedProduct.getProductId());

        for (Cart cart : cartsToUpdate) {
            // Apenas recalcula o total. O método recalculateCartTotal
            recalculateCartTotal(cart);
            cartRepository.save(cart);
        }
    }


    @EventListener
    @Transactional
    public void handleProductDelete(ProductDeletedEvent event) {
        Long productId = event.getProductId();
        List<Cart> cartsToUpdate = cartRepository.findCartsByProductId(productId);
        for (Cart cart : cartsToUpdate) {
            deleteItemFromCart(cart, productId);
        }
    }

    // ======================================================= //
    // MÉTODOS PRIVADOS AUXILIARES                             //
    // ======================================================= //



    private Cart deleteItemFromCart(Cart cart, Long productId) {
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId)
                .orElse(null);

        if (cartItem != null) {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
            recalculateCartTotal(cart);
            return cartRepository.save(cart);
        }
        return cart;
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
        // Check 0: O carrinho está vazio?
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new APIException("Cannot proceed to checkout with an empty cart.");
        }

        for (CartItem item : cart.getCartItems()) {
            // Para máxima segurança, buscamos a versão mais atual do produto do banco
            Product product = productRepository.findById(item.getProduct().getProductId())
                    .orElseThrow(() -> new APIException("Product '" + item.getProduct().getProductName() +
                            "' is no longer available and has been removed from the store."));



            // Check 2: Estoque
            if (product.getQuantityInStock() <= 0) {
                // Caso 2A: Estoque completamente esgotado.
                throw new APIException("Sorry, '" + product.getProductName() +
                        "' is now out of stock. Please remove it from your cart to proceed.");
            } else if (item.getQuantity() > product.getQuantityInStock()) {
                // Caso 2B: Estoque insuficiente para a quantidade desejada.
                throw new APIException("Cannot proceed to checkout. Product '" + product.getProductName() +
                        "' has insufficient stock. Available: " + product.getQuantityInStock() +
                        ", in your cart: " + item.getQuantity());
            }

            // Check 3: Limite de compra por cliente (Exemplo)
            // final int MAX_QUANTITY_PER_CUSTOMER = 5;
            // if (item.getQuantity() > MAX_QUANTITY_PER_CUSTOMER) {
            //     throw new APIException("You can only purchase a maximum of " + MAX_QUANTITY_PER_CUSTOMER +
            //                            " units of '" + product.getProductName() + "'.");
            // }
        }
    }

    private void recalculateCartTotal(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {

            // --- LÓGICA REVERTIDA PARA O MODELO DINÂMICO ---
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

            // --- LÓGICA DE VERIFICAÇÃO ADICIONADA AQUI ---
            if (productDTO.getCartQuantity() > productDTO.getQuantityInStock()) {
                if (productDTO.getQuantityInStock() > 0) {
                    productDTO.setWarningMessage("Atenção! Apenas " + productDTO.getQuantityInStock() + " unidades disponíveis em estoque.");
                } else {
                    productDTO.setWarningMessage("Produto esgotado! Remova-o do carrinho para continuar.");
                }
            }

            return productDTO;
        }).collect(Collectors.toList());

        cartDTO.setProducts(productDTOs);
        return cartDTO;
    }

}

