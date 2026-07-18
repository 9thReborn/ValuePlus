package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.AppRoles;
import com.nitax.valueplusbackend.utils.enums.Role;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RoleRepository extends JpaRepository<AppRoles, Integer> {
  Optional<AppRoles> findByName(Role name);
}
