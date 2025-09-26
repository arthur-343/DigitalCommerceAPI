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

        // --- CONFIGURAÇÃO CHAVE PARA ATUALIZAÇÕES PARCIAIS ---
        // Diz ao ModelMapper para pular qualquer campo que seja nulo na origem (DTO).
        // Isso é o que permite que a atualização parcial funcione com uma única linha.
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);

        // --- CONFIGURAÇÃO DE SEGURANÇA PARA ATUALIZAÇÕES ---
        // Adiciona uma regra global para o mapeamento de ProductDTO para Product.
        // Diz para SEMPRE ignorar a tentativa de definir a categoria diretamente.
        // Isso previne o erro "identifier of an instance was altered".
        modelMapper.typeMap(ProductDTO.class, Product.class)
                .addMappings(mapper -> mapper.skip(Product::setCategory));

        return modelMapper;
    }
}
