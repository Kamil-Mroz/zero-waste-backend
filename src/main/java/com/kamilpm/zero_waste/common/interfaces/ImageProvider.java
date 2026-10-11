package com.kamilpm.zero_waste.common.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.ImageData;

public interface ImageProvider {

  public List<ImageData> attachUploadedImages(UUID item, List<UUID> uploadIds);

  void deleteImages(Collection<UUID> ids);

  public List<ImageData> getAllImagesByIds(Collection<UUID> ids);

  public List<ImageData> getImagesByItemId(UUID id);

  public Map<UUID, ImageData> getImagesByIds(Collection<UUID> ids);
}
