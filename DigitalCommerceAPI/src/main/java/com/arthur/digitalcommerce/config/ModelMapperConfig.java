package com.arthur.digitalcommerce.config;

import com.arthur.digitalcommerce.model.Product;
import com.arthur.digitalcommerce.payload.ProductDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // 1. Configuração para atualizações parciais (JÁ ESTAVA CORRETO)
        // Diz ao ModelMapper para pular qualquer campo que seja nulo na origem (DTO).
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);

        // 2. Regra para mapear DTO -> Entidade (JÁ ESTAVA CORRETO)
        // Ignora a categoria, pois ela é tratada manualmente no service para evitar erros.
        modelMapper.typeMap(ProductDTO.class, Product.class)
                .addMappings(mapper -> mapper.skip(Product::setCategory));

        // 3. REGRA ADICIONADA: Mapear Entidade -> DTO (A CORREÇÃO)
        // Ensina como obter o 'categoryId' a partir do objeto 'category' da entidade.
        modelMapper.typeMap(Product.class, ProductDTO.class)
                .addMappings(mapper -> mapper.map(
                        src -> src.getCategory().getCategoryId(), // Pega o ID da categoria aninhada
                        ProductDTO::setCategoryId              // E coloca no campo 'categoryId' do DTO
                ));

        return modelMapper;
    }
}