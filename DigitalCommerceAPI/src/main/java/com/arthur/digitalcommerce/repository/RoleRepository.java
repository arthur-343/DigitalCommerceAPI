package com.arthur.digitalcommerce.repository;

import com.arthur.digitalcommerce.model.AppRole;
import com.arthur.digitalcommerce.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(AppRole appRole);

}
