package com.nitax.valueplusbackend.service.impl;

import com.nitax.valueplusbackend.domain.Advertiser;
import com.nitax.valueplusbackend.domain.Product;
import com.nitax.valueplusbackend.dto.request.CreateProductDTO;
import com.nitax.valueplusbackend.repository.ProductRepository;
import com.nitax.valueplusbackend.service.ProductService;
import com.nitax.valueplusbackend.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final UserDetailsService userDetailsService;
  private final AppUtils appUtils;

  @Override
  public Product createProduct(CreateProductDTO createProductDTO) {
    Advertiser advertiser = userDetailsService.getAdvertiserFromSecurityContext();

    Product product = new Product();
    product.setName(createProductDTO.getName());
    product.setPostbackUrl(createProductDTO.getPostbackUrl());
    product.setProdId(appUtils.generateProdId());
    product.setAdvertiser(advertiser);

    return productRepository.save(product);
  }

  @Override
  public Optional<Product> findByProdId(String prodId) {
    return productRepository.findByProdId(prodId);
  }
}
