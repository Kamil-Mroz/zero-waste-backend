package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.dto.BlogDto;
import com.kamilpm.zero_waste.domain.request.BlogRequest;

public interface BlogService {
  BlogDto createBlog(BlogRequest blog);

  BlogDto updateBlog(UUID blogId, BlogRequest blog);

  List<BlogDto> getBlogs();

  BlogDto getBlog(UUID blogId);

  void deleteBlog(UUID blogId);

  List<BlogDto> getOwnBlogs();
}
