package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.kamilpm.zero_waste.domain.entity.Image;
import com.kamilpm.zero_waste.domain.entity.Item;

public interface ImageService {
  List<Image> uploadItemImages(Item item, List<MultipartFile> files);

  void deleteImages(UUID itemId, List<UUID> imageIds);

  void deleteImagesByItemId(UUID itemId);

  void deleteImagesFromDisk(List<Image> images);

  Optional<Resource> getImageAsResource(String storedName);

}
