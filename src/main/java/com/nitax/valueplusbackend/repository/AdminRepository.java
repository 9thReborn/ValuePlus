package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AdminRepository extends JpaRepository<Admin, Integer> {
  Optional<Admin> findByEmail(String email);
}
