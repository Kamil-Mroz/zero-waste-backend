package com.kamilpm.zero_waste.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.dto.BlogDto;
import com.kamilpm.zero_waste.domain.entity.Blog;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.mapper.BlogMapper;
import com.kamilpm.zero_waste.domain.request.BlogRequest;
import com.kamilpm.zero_waste.repository.BlogRepository;
import com.kamilpm.zero_waste.security.MyUserDetails;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.BlogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

  private final AuthService authService;
  private final BlogRepository blogRepository;
  private final BlogMapper blogMapper;

  @Override
  public BlogDto createBlog(BlogRequest blog) {
    User user = authService.getRequiredAuthenticatedUserEntity();
    Blog newBlog = Blog.builder()
        .author(user)
        .content(blog.getContent())
        .description(blog.getDescription())
        .title(blog.getTitle())
        .build();
    Blog savedBlog = blogRepository.save(newBlog);

    return blogMapper.toDto(savedBlog);
  }

  @Override
  public List<BlogDto> getBlogs() {
    return blogRepository.findAll().stream().map(blogMapper::toDto).toList();
  }

  @Override
  public List<BlogDto> getOwnBlogs() {

    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();
    return blogRepository.findByAuthor_Id(user.getId()).stream().map(blogMapper::toDto).toList();
  }
}
