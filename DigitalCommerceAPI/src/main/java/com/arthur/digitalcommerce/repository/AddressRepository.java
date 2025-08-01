package com.arthur.digitalcommerce.repository;

import com.arthur.digitalcommerce.model.Address;
import com.arthur.digitalcommerce.model.Cart;
import com.arthur.digitalcommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("SELECT a FROM Address a WHERE a.user.email = :email")
    List<Address> findByUserEmail(@Param("email") String email);

    Optional<Address> findById(Long addressId);

    @Query("SELECT a FROM Address a WHERE a.id = :id AND a.user.email = :email")
    Optional<Address> findByIdAndUserEmail(@Param("id") Long id, @Param("email") String email);
}
