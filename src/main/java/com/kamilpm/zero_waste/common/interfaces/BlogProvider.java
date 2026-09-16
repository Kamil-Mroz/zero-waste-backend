package com.kamilpm.zero_waste.common.interfaces;

import java.util.UUID;

public interface BlogProvider {

  public void blogExists(UUID subjectId, UUID userId);

  public boolean isBlogAuthor(UUID blogId, UUID userId);

  public void deleteBlogById(UUID blogId);

  public void hideBlog(UUID adminId, UUID subjectId);

}
