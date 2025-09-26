package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.payload.ProductDTO;
import com.arthur.digitalcommerce.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductDTO addProduct(ProductDTO productDTO, MultipartFile image) throws IOException;

    // Mantenha este, que é o nome mais claro
    ProductResponse getProductByCategoryId(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    // Mantenha este também
    ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductDTO updateProduct(Long productId, ProductDTO productDTO, MultipartFile image) throws IOException;

    void deleteProduct(Long productId);



}