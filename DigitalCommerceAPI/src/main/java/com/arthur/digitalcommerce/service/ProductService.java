package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.model.Product;
import com.arthur.digitalcommerce.payload.ProductDTO;
import com.arthur.digitalcommerce.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductDTO addProduct(Long categoryId, ProductDTO productDTO);

    ProductResponse getProductsByName(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductResponse getProductByCategoryId(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductDTO updateProduct(Long productId,ProductDTO productDTO);

    ProductDTO deleteProduct(Long productId);

    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;

    ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
