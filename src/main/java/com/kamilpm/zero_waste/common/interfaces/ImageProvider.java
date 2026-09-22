package com.kamilpm.zero_waste.common.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.kamilpm.zero_waste.common.dto.ImageData;

public interface ImageProvider {

  public List<ImageData> uploadItemImages(UUID itemId, List<MultipartFile> files);

  // public void deleteItemImages(UUID itemId, List<UUID> imageIds);

  // public void deleteImagesByItems(Collection<UUID> itemIds);

  void deleteImages(Collection<UUID> ids);

  public List<ImageData> getAllImagesByIds(Collection<UUID> ids);

  public List<ImageData> getImagesByItemId(UUID id);

  public Map<UUID, ImageData> getImagesByIds(Collection<UUID> ids);
}
