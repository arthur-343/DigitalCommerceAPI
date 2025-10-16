package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.events.ProductDeletedEvent;
import com.arthur.digitalcommerce.events.ProductUpdatedEvent;
import com.arthur.digitalcommerce.exceptions.APIException;
import com.arthur.digitalcommerce.exceptions.ResourceNotFoundException;
import com.arthur.digitalcommerce.model.Category;
import com.arthur.digitalcommerce.model.Product;
import com.arthur.digitalcommerce.payload.ProductDTO;
import com.arthur.digitalcommerce.payload.ProductResponse;
import com.arthur.digitalcommerce.repository.CategoryRepository;
import com.arthur.digitalcommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private FileService fileService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;
    private ProductDTO productDTO;
    private MockMultipartFile mockImage;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");

        product = new Product();
        product.setProductId(1L);
        product.setProductName("Test Product");
        product.setCategory(category);
        product.setPrice(BigDecimal.valueOf(100));

        productDTO = new ProductDTO();
        productDTO.setProductId(1L);
        productDTO.setProductName("Test Product");
        productDTO.setCategoryId(1L);
        productDTO.setPrice(BigDecimal.valueOf(100));

        mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image content".getBytes());
    }

    @Test
    void addProduct_shouldSucceed() throws IOException {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(fileService.uploadImage(any(), any())).thenReturn("test.jpg");
        when(modelMapper.map(any(ProductDTO.class), eq(Product.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductDTO result = productService.addProduct(productDTO, mockImage);

        assertNotNull(result);
        assertEquals(productDTO.getProductName(), result.getProductName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void addProduct_shouldThrowException_whenCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.addProduct(productDTO, mockImage));
    }

    @Test
    void addProduct_shouldThrowException_whenSpecialPriceIsInvalid() {
        ProductDTO invalidPriceDto = new ProductDTO();
        invalidPriceDto.setCategoryId(1L);
        invalidPriceDto.setPrice(BigDecimal.valueOf(100));
        invalidPriceDto.setSpecialPrice(BigDecimal.valueOf(120));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(APIException.class, () -> productService.addProduct(invalidPriceDto, mockImage));
    }



    @Test
    void updateProduct_shouldSucceed() throws IOException {
        lenient().when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        doNothing().when(eventPublisher).publishEvent(any(ProductUpdatedEvent.class));

        ProductDTO result = productService.updateProduct(1L, productDTO, null);

        assertNotNull(result);
        verify(productRepository, times(1)).save(product);
        verify(eventPublisher, times(1)).publishEvent(any(ProductUpdatedEvent.class));
    }

    @Test
    void updateProduct_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, productDTO, null));
    }

    @Test
    void updateProduct_shouldNullifySpecialPrice_whenItIsGreaterThanPrice() throws IOException {
        Product existingProduct = new Product();
        existingProduct.setProductId(1L);
        existingProduct.setPrice(BigDecimal.valueOf(100));
        ProductDTO updateRequest = new ProductDTO();
        updateRequest.setSpecialPrice(BigDecimal.valueOf(120));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        productService.updateProduct(1L, updateRequest, null);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertNull(savedProduct.getSpecialPrice());
        assertFalse(savedProduct.isSpecialPriceActive());
    }

    @Test
    void updateProduct_shouldUpdateImage_whenNewImageIsProvided() throws IOException {
        product.setImage("old_image.jpg");
        String newImageName = "new_image.jpg";
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(fileService.uploadImage(any(), any())).thenReturn(newImageName);

        productService.updateProduct(1L, new ProductDTO(), mockImage);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertEquals(newImageName, savedProduct.getImage());
        verify(fileService, times(1)).uploadImage(any(), any());
    }

    @Test
    void getAllProducts_shouldReturnProductResponse() {
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductResponse result = productService.getAllProducts(0, 10, "productId", "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProductByCategoryId_shouldReturnProductResponse() {
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryOrderByPriceAsc(any(Category.class), any(Pageable.class))).thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductResponse result = productService.getProductByCategoryId(1L, 0, 10, "price", "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getProductByCategoryId_shouldThrowException_whenCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductByCategoryId(1L, 0, 10, "price", "asc"));
    }

    @Test
    void getProductByCategoryId_shouldThrowException_whenNoProductsFound() {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryOrderByPriceAsc(any(Category.class), any(Pageable.class))).thenReturn(emptyPage);
        assertThrows(APIException.class, () -> productService.getProductByCategoryId(1L, 0, 10, "price", "asc"));
    }

    @Test
    void searchProductByKeyword_shouldReturnProductResponse() {
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findByProductNameLikeIgnoreCase(anyString(), any(Pageable.class))).thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

        ProductResponse result = productService.searchProductByKeyword("Test", 0, 10, "productName", "asc");

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
    }

    @Test
    void searchProductByKeyword_shouldThrowException_whenNoProductsFound() {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
        when(productRepository.findByProductNameLikeIgnoreCase(anyString(), any(Pageable.class))).thenReturn(emptyPage);
        assertThrows(APIException.class, () -> productService.searchProductByKeyword("Unknown", 0, 10, "productName", "asc"));
    }

    @Test
    void deleteProduct_shouldSucceed() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));
        doNothing().when(eventPublisher).publishEvent(any(ProductDeletedEvent.class));

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(product);
        verify(eventPublisher, times(1)).publishEvent(any(ProductDeletedEvent.class));
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(1L));
        verify(productRepository, never()).delete(any());
    }
}