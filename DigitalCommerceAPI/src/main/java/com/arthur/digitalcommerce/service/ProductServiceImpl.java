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
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${config.paths.image-upload}")
    private String imageUploadPath;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, MultipartFile image) throws IOException {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", productDTO.getCategoryId()));


        if (productDTO.getSpecialPrice() != null && productDTO.getPrice() != null &&
                productDTO.getSpecialPrice().compareTo(productDTO.getPrice()) >= 0) {
            throw new APIException("Special price must be less than the regular price.");
        }

        String imageFileName = fileService.uploadImage(imageUploadPath, image);
        Product product = modelMapper.map(productDTO, Product.class);
        product.setCategory(category);
        product.setImage(imageFileName);
        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }


    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO, MultipartFile image) throws IOException {
        // 1. Encontre a entidade existente que será atualizada.
        Product productToUpdate = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // 2. Use o ModelMapper para aplicar as atualizações parciais.
        // Graças à sua configuração, esta linha já ignora campos nulos no DTO.
        modelMapper.map(productDTO, productToUpdate);

        // 3. Trate manualmente os casos especiais (associações e arquivos).

        // Se um novo ID de categoria foi enviado no DTO, atualize a categoria do produto.
        if (productDTO.getCategoryId() != null) {
            Category newCategory = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", productDTO.getCategoryId()));
            productToUpdate.setCategory(newCategory);
        }

        // Se um novo arquivo de imagem foi enviado, faça o upload e atualize o nome do arquivo.
        if (image != null && !image.isEmpty()) {
            String newImageFileName = fileService.uploadImage(imageUploadPath, image);
            productToUpdate.setImage(newImageFileName);
        }

        // 4. Valide e corrija as regras de negócio do preço especial.
        // Se o preço especial for maior ou igual ao preço normal, ele é invalidado.
        if (productToUpdate.getSpecialPrice() != null && productToUpdate.getPrice() != null &&
                productToUpdate.getSpecialPrice().compareTo(productToUpdate.getPrice()) >= 0) {

            // Em vez de dar erro, o sistema corrige a inconsistência.
            productToUpdate.setSpecialPrice(null);
            productToUpdate.setSpecialPriceActive(false);
        }

        // 5. Salve a entidade atualizada, publique o evento e retorne o DTO.
        Product savedProduct = productRepository.save(productToUpdate);
        eventPublisher.publishEvent(new ProductUpdatedEvent(savedProduct));

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    // ... (outros métodos como delete, buscas, etc. permanecem os mesmos)
    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findAll(pageDetails);
        return createProductResponse(pageProducts);
    }

    @Override
    public ProductResponse getProductByCategoryId(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);

        if (pageProducts.isEmpty()) {
            throw new APIException(category.getCategoryName() + " category does not have any products.");
        }

        return createProductResponse(pageProducts);
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);

        if (pageProducts.isEmpty()) {
            throw new APIException("Products not found with keyword: " + keyword);
        }

        return createProductResponse(pageProducts);
    }

    @Override
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        productRepository.delete(product);
        eventPublisher.publishEvent(new ProductDeletedEvent(productId));
    }



    private ProductResponse createProductResponse(Page<Product> pageProducts) {
        List<ProductDTO> productDTOs = pageProducts.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOs);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }
}

