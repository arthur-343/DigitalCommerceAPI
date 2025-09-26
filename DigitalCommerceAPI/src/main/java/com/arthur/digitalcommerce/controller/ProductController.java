package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.config.AppConstants;
import com.arthur.digitalcommerce.payload.MessageResponse;
import com.arthur.digitalcommerce.payload.ProductDTO;
import com.arthur.digitalcommerce.payload.ProductResponse;
import com.arthur.digitalcommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * POST (Criar): Retorna 201 Created.
     * Adicionamos o header 'Location' para indicar a URL do novo recurso.
     */
    @PostMapping(value = "/admin/products", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductDTO> addProduct(
            @RequestPart("productDTO") @Valid ProductDTO productDTO,
            @RequestPart("image") MultipartFile image) throws IOException {

        ProductDTO savedProductDTO = productService.addProduct(productDTO, image);

        // Cria a URI para o novo produto criado (ex: /api/public/products/123)
        // Embora não tenhamos um endpoint "GET por ID", esta é a prática correta.
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/public/products/{id}")
                .buildAndExpand(savedProductDTO.getProductId()).toUri();

        // CORRIGIDO: Usa o builder .created() que já define o status 201
        return ResponseEntity.created(location).body(savedProductDTO);
    }

    /**
     * GET (Listar): Retorna 200 OK.
     */
    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);

        // CORRIGIDO: Usa o atalho .ok() para consistência
        return ResponseEntity.ok(productResponse);
    }

    /**
     * GET (Listar por Categoria): Retorna 200 OK.
     */
    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(@PathVariable Long categoryId,
                                                                 @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                 @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                 @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                 @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        ProductResponse productResponse = productService.getProductByCategoryId(categoryId, pageNumber, pageSize, sortBy, sortOrder);

        // CORRIGIDO: Usa o atalho .ok()
        return ResponseEntity.ok(productResponse);
    }

    /**
     * GET (Buscar): Retorna 200 OK.
     */
    @GetMapping("/public/products/search")
    public ResponseEntity<ProductResponse> searchProductsByKeyword(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        ProductResponse productResponse = productService.searchProductByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);

        // CORRIGIDO: Usa o atalho .ok()
        return ResponseEntity.ok(productResponse);
    }

    /**
     * PUT (Atualizar): Retorna 200 OK.
     */
    @PutMapping(value = "/admin/products/{productId}", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long productId,
            @RequestPart("productDTO") @Valid ProductDTO productDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        ProductDTO updatedProductDTO = productService.updateProduct(productId, productDTO, image);

        // CORRIGIDO: Usa o atalho .ok()
        return ResponseEntity.ok(updatedProductDTO);
    }

    /**
     * DELETE (Apagar): Retorna 204 No Content.
     * O corpo da resposta será vazio.
     */
    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);

        // CORRIGIDO: Usa .noContent().build() para retornar 204 com corpo vazio
        return ResponseEntity.noContent().build();
    }
}