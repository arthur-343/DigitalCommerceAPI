package com.arthur.digitalcommerce.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal; // IMPORT NECESSÁRIO
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = createCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem newCartItem = new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setProductPrice(product.getSpecialPrice());
        // CORREÇÃO: Removido pois 'discount' não existe mais em Product
        // newCartItem.setDiscount(product.getDiscount());
        newCartItem.setDiscount(BigDecimal.ZERO); // Ou defina um valor padrão

        cartItemRepository.save(newCartItem);

        // A quantidade do produto em estoque deve ser atualizada em um serviço de 'estoque' separado, não aqui.
        // product.setQuantity(product.getQuantity());

        // CORREÇÃO: Cálculo com BigDecimal
        BigDecimal quantityBD = BigDecimal.valueOf(quantity);
        BigDecimal subtotal = product.getSpecialPrice().multiply(quantityBD);
        cart.setTotalPrice(cart.getTotalPrice().add(subtotal));

        cartRepository.save(cart);

        return mapCartToDTO(cart);
    }

    // Método auxiliar para evitar repetição de código
    private CartDTO mapCartToDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<ProductDTO> productDTOs = cart.getCartItems().stream().map(item -> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            productDTO.setQuantity(item.getQuantity());
            return productDTO;
        }).collect(Collectors.toList());
        cartDTO.setProducts(productDTOs);
        return cartDTO;
    }


    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty()) {
            throw new APIException("No cart exists");
        }

        return carts.stream().map(this::mapCartToDTO).collect(Collectors.toList());
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        return mapCartToDTO(cart);
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String emailId = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }
        Long cartId = cart.getCartId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        cartItem.setQuantity(quantity);

        // Recalcula o total do carrinho do zero para garantir consistência
        recalculateCartTotal(cart);

        cartItemRepository.save(cartItem);
        cartRepository.save(cart);

        return mapCartToDTO(cart);
    }

    private void recalculateCartTotal(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            BigDecimal subtotal = item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
        }
        cart.setTotalPrice(total);
    }


    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart != null) {
            return userCart;
        }

        Cart cart = new Cart();
        // CORREÇÃO: Iniciar com BigDecimal.ZERO
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setUser(authUtil.loggedInUser());

        return cartRepository.save(cart);
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        // CORREÇÃO: Cálculo com BigDecimal
        BigDecimal quantityBD = BigDecimal.valueOf(cartItem.getQuantity());
        BigDecimal subtotal = cartItem.getProductPrice().multiply(quantityBD);
        cart.setTotalPrice(cart.getTotalPrice().subtract(subtotal));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        // CORREÇÃO: Lógica de recálculo com BigDecimal
        BigDecimal oldSubtotal = cartItem.getProductPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal cartPriceWithoutOldItem = cart.getTotalPrice().subtract(oldSubtotal);

        cartItem.setProductPrice(product.getSpecialPrice());

        BigDecimal newSubtotal = cartItem.getProductPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        cart.setTotalPrice(cartPriceWithoutOldItem.add(newSubtotal));

        cartItemRepository.save(cartItem);
    }


    // NOVA IMPLEMENTAÇÃO PARA O WEBHOOK
    @Override
    @Transactional
    public void clearCartByUserEmail(String email) {
        // Usamos o método do repositório para encontrar o carrinho pelo e-mail
        cartRepository.findByUserEmail(email).ifPresent(cart -> {
            if (!cart.getCartItems().isEmpty()) {
                // A lógica de limpeza é a mesma do método acima
                cart.getCartItems().clear();
                cart.setTotalPrice(BigDecimal.ZERO);
                cartRepository.save(cart);
            }
        });
    }
}
