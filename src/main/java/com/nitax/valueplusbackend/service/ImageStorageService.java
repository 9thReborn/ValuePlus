package com.nitax.valueplusbackend.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

  String saveFile(MultipartFile multipartFile) throws IOException;
}
