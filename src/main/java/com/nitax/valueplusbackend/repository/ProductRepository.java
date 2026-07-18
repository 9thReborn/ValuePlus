package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  boolean existsByProdId(String toString);

  Optional<Product> findByProdId(String productId);
}
