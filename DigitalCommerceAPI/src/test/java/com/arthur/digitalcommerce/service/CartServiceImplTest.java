package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.events.ProductDeletedEvent;
import com.arthur.digitalcommerce.events.ProductUpdatedEvent;
import com.arthur.digitalcommerce.exceptions.APIException;
import com.arthur.digitalcommerce.exceptions.ResourceNotFoundException;
import com.arthur.digitalcommerce.model.Cart;
import com.arthur.digitalcommerce.model.CartItem;
import com.arthur.digitalcommerce.model.Product;
import com.arthur.digitalcommerce.model.User;
import com.arthur.digitalcommerce.payload.CartDTO;
import com.arthur.digitalcommerce.payload.ProductDTO;
import com.arthur.digitalcommerce.repository.CartItemRepository;
import com.arthur.digitalcommerce.repository.CartRepository;
import com.arthur.digitalcommerce.repository.ProductRepository;
import com.arthur.digitalcommerce.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private Long productId = 1L;
    private Long cartId = 1L;
    private String userEmail = "test@user.com";
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setEmail(userEmail);
        user.setAddresses(new ArrayList<>());

        product = new Product();
        product.setProductId(productId);
        product.setProductName("Test Product");
        product.setQuantityInStock(10);
        product.setPrice(BigDecimal.TEN);

        cart = new Cart();
        cart.setCartId(cartId);
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);

        when(authUtil.loggedInEmail()).thenReturn(userEmail);
        when(authUtil.loggedInUser()).thenReturn(user); // 'lenient()' removido
        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(new CartDTO()); // 'lenient()' removido
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO); // 'lenient()' removido

        productDTO = new ProductDTO();
        productDTO.setProductId(productId);
        productDTO.setProductName("Test Product");
        productDTO.setQuantityInStock(10);

        when(authUtil.loggedInEmail()).thenReturn(userEmail);
        lenient().when(authUtil.loggedInUser()).thenReturn(user);
        lenient().when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(new CartDTO());

        lenient().when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);




    }

    @Test
    void addProductToCart_shouldAddNewItem_whenCartIsEmpty() {
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addProductToCart(productId, 2);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();

        assertEquals(1, savedCart.getCartItems().size());
        assertEquals(2, savedCart.getCartItems().get(0).getQuantity());
    }

    @Test
    void addProductToCart_shouldUpdateQuantity_whenItemAlreadyExists() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addProductToCart(productId, 2);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();

        assertEquals(1, savedCart.getCartItems().size());
        assertEquals(3, savedCart.getCartItems().get(0).getQuantity());
    }

    @Test
    void addProductToCart_shouldThrowException_whenStockIsInsufficient() {
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(APIException.class, () -> cartService.addProductToCart(productId, 20));
    }

    @Test
    void addProductToCart_shouldThrowException_whenProductNotFound() {
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addProductToCart(productId, 1));
    }

    @Test
    void updateProductQuantityInCart_shouldUpdateQuantity_whenQuantityIsValid() {
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(Optional.of(cartItem));

        cartService.updateProductQuantityInCart(productId, 5);

        verify(cartItemRepository, times(1)).save(cartItem);
        assertEquals(5, cartItem.getQuantity());
    }

    @Test
    void updateProductQuantityInCart_shouldDeleteItem_whenQuantityIsZero() {
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(Optional.of(cartItem));
        lenient().when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.updateProductQuantityInCart(productId, 0);

        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    void updateProductQuantityInCart_shouldThrowException_whenItemNotInCart() {
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(Optional.empty());

        assertThrows(APIException.class, () -> cartService.updateProductQuantityInCart(productId, 5));
    }

    @Test
    void updateProductQuantityInCart_shouldThrowException_whenProductNotFound() {
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.updateProductQuantityInCart(productId, 5));
    }

    @Test
    void updateProductQuantityInCart_shouldThrowException_whenStockIsInsufficient() {
        product.setQuantityInStock(3);
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(APIException.class, () -> cartService.updateProductQuantityInCart(productId, 5));
    }

    @Test
    void deleteProductFromCart_shouldSucceed_whenItemIsInCart() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(Optional.of(cartItem));

        String result = cartService.deleteProductFromCart(productId);

        assertEquals("Produto '" + product.getProductName() + "' removido com sucesso do carrinho!", result);
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    void deleteProductFromCart_shouldReturnNotFoundMessage_whenItemIsNotInCart() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(Optional.empty());

        String result = cartService.deleteProductFromCart(productId);

        assertEquals("Produto '" + product.getProductName() + "' não foi encontrado no carrinho.", result);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void clearCartByUserEmail_shouldDeleteAllItems_whenCartExists() {
        cart.getCartItems().add(cartItem);
        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart));

        cartService.clearCartByUserEmail(userEmail);

        verify(cartItemRepository, times(1)).deleteAll(anyList());
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());

        assertTrue(cartCaptor.getValue().getCartItems().isEmpty());
        assertEquals(BigDecimal.ZERO, cartCaptor.getValue().getTotalPrice());
    }

    @Test
    void handleProductUpdate_shouldRecalculateCartTotal() {
        ProductUpdatedEvent event = new ProductUpdatedEvent(product);
        when(cartRepository.findCartsByProductId(productId)).thenReturn(Collections.singletonList(cart));

        cartService.handleProductUpdate(event);

        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void handleProductDelete_shouldRemoveItemFromCarts() {
        ProductDeletedEvent event = new ProductDeletedEvent(productId);
        cart.getCartItems().add(cartItem);

        when(cartRepository.findCartsByProductId(productId)).thenReturn(Collections.singletonList(cart));
        when(cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId)).thenReturn(Optional.of(cartItem));
        lenient().when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.handleProductDelete(event);

        verify(cartItemRepository, times(1)).delete(cartItem);
    }
}