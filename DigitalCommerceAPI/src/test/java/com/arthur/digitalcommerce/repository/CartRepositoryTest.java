package com.arthur.digitalcommerce.repository;

import com.arthur.digitalcommerce.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CartRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User createValidUser(String email, String cpf, String username) {
        User user = new User(
                username,
                email,
                "Str0ngP@ss123",
                cpf
        );
        return entityManager.persistAndFlush(user);
    }

    private Category createValidCategory(String name) {
        Category category = new Category();
        category.setCategoryName(name);
        return entityManager.persistAndFlush(category);
    }

    private Product createValidProduct(User seller, Category category) {
        Product product = new Product();
        product.setProductName("Test Product");
        product.setDescription("Test product description");
        product.setQuantityInStock(100);
        product.setPrice(new BigDecimal("99.90"));
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

    private Product createFullCartSetup() {
        User seller = createValidUser("seller@email.com", "11122233344", "seller");
        User customer = createValidUser("customer@email.com", "55566677788", "customer");
        Category category = createValidCategory("Electronics");
        Product product = createValidProduct(seller, category);
        Cart cart = createValidCart(customer);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        entityManager.persistAndFlush(cartItem);

        return product;
    }

    @Test
    void testFindCartByEmail_And_FindByUserEmail() {
        User customer = createValidUser("customer.cart@email.com", "12345678901", "cartcustomer");
        createValidCart(customer);

        Cart foundByEmail = cartRepository.findCartByEmail("customer.cart@email.com");
        Optional<Cart> foundByUserEmail = cartRepository.findByUserEmail("customer.cart@email.com");

        assertThat(foundByEmail).isNotNull();
        assertThat(foundByEmail.getUser().getEmail()).isEqualTo("customer.cart@email.com");

        assertThat(foundByUserEmail).isPresent();
        assertThat(foundByUserEmail.get().getUser().getEmail()).isEqualTo("customer.cart@email.com");
    }

    @Test
    void testFindByUser() {
        User customer = createValidUser("customer.user@email.com", "10987654321", "usercustomer");
        createValidCart(customer);

        Optional<Cart> found = cartRepository.findByUser(customer);

        assertThat(found).isPresent();
        assertThat(found.get().getUser()).isEqualTo(customer);
    }

    @Test
    void testFindCartByEmailAndCartId() {
        User customer = createValidUser("customer.id@email.com", "11111111111", "idcustomer");
        Cart savedCart = createValidCart(customer);

        Cart found = cartRepository.findCartByEmailAndCartId(
                "customer.id@email.com", savedCart.getCartId()
        );
        Cart notFound_wrongEmail = cartRepository.findCartByEmailAndCartId(
                "wrong@email.com", savedCart.getCartId()
        );
        Cart notFound_wrongId = cartRepository.findCartByEmailAndCartId(
                "customer.id@email.com", 999L
        );

        assertThat(found).isNotNull();
        assertThat(found.getCartId()).isEqualTo(savedCart.getCartId());

        assertThat(notFound_wrongEmail).isNull();
        assertThat(notFound_wrongId).isNull();
    }

    @Test
    void testFindCartsByProductId() {
        Product savedProduct = createFullCartSetup();

        List<Cart> carts = cartRepository.findCartsByProductId(savedProduct.getProductId());
        List<Cart> carts_notFound = cartRepository.findCartsByProductId(999L);

        assertThat(carts).isNotNull().hasSize(1);
        assertThat(carts.get(0).getUser().getEmail()).isEqualTo("customer@email.com");

        assertThat(carts.get(0).getCartItems()).hasSize(1);
        assertThat(carts.get(0).getCartItems().get(0).getProduct().getProductId())
                .isEqualTo(savedProduct.getProductId());

        assertThat(carts_notFound).isNotNull().isEmpty();
    }
    @Test
    void testDeleteCart_ShouldAlsoDeleteOrphanedCartItems() {
        // --- Given ---
        // 1. Create a full cart setup
        Product product = createFullCartSetup(); // This creates a cart with 1 item
        Long cartId = cartRepository.findByUserEmail("customer@email.com").get().getCartId();

        // 2. Verify the item exists
        assertThat(cartItemRepository.findAll()).hasSize(1);

        Cart cart = cartRepository.findById(cartId).get();

        // --- When ---
        // 3. Delete the parent Cart
        cartRepository.delete(cart);
        entityManager.flush(); // Force the delete operation
        entityManager.clear(); // Clear the cache to force a fresh read

        // --- Then ---
        // 4. Verify the Cart is gone AND its CartItems are gone
        assertThat(cartRepository.findById(cartId)).isNotPresent();
        assertThat(cartItemRepository.findAll()).isEmpty();
    }
}