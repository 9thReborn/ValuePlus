package com.nitax.valueplusbackend.service;

import com.nitax.valueplusbackend.domain.Product;
import com.nitax.valueplusbackend.dto.request.CreateProductDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ProductService {

  Product createProduct(CreateProductDTO createProductDTO);

  Optional<Product> findByProdId(String prodId);
}
