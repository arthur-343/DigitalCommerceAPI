package com.arthur.digitalcommerce.repository;

import com.arthur.digitalcommerce.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

class CartItemRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User createValidUser(String email, String cpf, String username) {
        User user = new User(username, email, "Str0ngP@ss123", cpf);
        return entityManager.persistAndFlush(user);
    }

    private Category createValidCategory(String name) {
        Category category = new Category();
        category.setCategoryName(name);
        return entityManager.persistAndFlush(category);
    }

    private Product createValidProduct(User seller, Category category) {
        Product product = new Product();
        product.setProductName("Test Product CI");
        product.setDescription("Test product CI description");
        product.setQuantityInStock(100);
        product.setPrice(new BigDecimal("150.00"));
        product.setUser(seller);
        product.setCategory(category);
        return entityManager.persistAndFlush(product);
    }

    private Cart createValidCart(User customer) {
        Cart cart = new Cart();
        cart.setUser(customer);
        cart.setTotalPrice(BigDecimal.ZERO);
        return entityManager.persistAndFlush(cart);
    }

    private CartItem createFullCartSetup() {
        User seller = createValidUser("seller.ci@email.com", "11122233355", "seller_ci");
        User customer = createValidUser("customer.ci@email.com", "55566677799", "customer_ci");
        Category category = createValidCategory("Clothing");
        Product product = createValidProduct(seller, category);
        Cart cart = createValidCart(customer);

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(5);
        cartItem.setDiscount(BigDecimal.TEN);

        CartItem savedCartItem = entityManager.persistAndFlush(cartItem);
        return savedCartItem;
    }

    @Test
    void testFindCartItemByProductIdAndCartId() {
        CartItem savedItem = createFullCartSetup();
        Long cartId = savedItem.getCart().getCartId();
        Long productId = savedItem.getProduct().getProductId();

        Optional<CartItem> found = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        Optional<CartItem> notFound_wrongCart = cartItemRepository.findCartItemByProductIdAndCartId(999L, productId);
        Optional<CartItem> notFound_wrongProduct = cartItemRepository.findCartItemByProductIdAndCartId(cartId, 999L);

        assertThat(found).isPresent();
        assertThat(found.get().getCartItemId()).isEqualTo(savedItem.getCartItemId());
        assertThat(found.get().getQuantity()).isEqualTo(5);

        assertThat(notFound_wrongCart).isNotPresent();
        assertThat(notFound_wrongProduct).isNotPresent();
    }

    @Test
    void testDeleteCartItemByProductIdAndCartId() {
        CartItem savedItem = createFullCartSetup();
        Long cartId = savedItem.getCart().getCartId();
        Long productId = savedItem.getProduct().getProductId();

        Optional<CartItem> beforeDelete = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        assertThat(beforeDelete).isPresent();

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        entityManager.flush();
        entityManager.clear();

        Optional<CartItem> afterDelete = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        assertThat(afterDelete).isNotPresent();
    }



}