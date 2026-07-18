package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.domain.Product;
import com.nitax.valueplusbackend.dto.request.CreateProductDTO;
import com.nitax.valueplusbackend.dto.response.ApiResponse;
import com.nitax.valueplusbackend.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

  private final ProductService productService;

  @Autowired
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @SecurityRequirement(name = "bearerAuth")
  @PostMapping
  public ResponseEntity<ApiResponse<Product>> createProduct(
      @Validated @RequestBody CreateProductDTO createProductDTO) {
    Product createdProduct = productService.createProduct(createProductDTO);

    ApiResponse<Product> response = new ApiResponse<>(true, createdProduct);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }
}
