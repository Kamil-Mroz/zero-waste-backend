package com.kamilpm.zero_waste.service;

import java.util.List;

import com.kamilpm.zero_waste.domain.dto.BlogDto;
import com.kamilpm.zero_waste.domain.request.BlogRequest;

public interface BlogService {
  BlogDto createBlog(BlogRequest blog);

  List<BlogDto> getBlogs();

  List<BlogDto> getOwnBlogs();
}
