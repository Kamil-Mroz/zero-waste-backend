package com.kamilpm.zero_waste.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.dto.BlogDto;
import com.kamilpm.zero_waste.domain.entity.Blog;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.mapper.BlogMapper;
import com.kamilpm.zero_waste.domain.request.BlogRequest;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.repository.BlogRepository;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.BlogService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

  private final AuthService authService;
  private final BlogRepository blogRepository;
  private final BlogMapper blogMapper;

  @Override
  public BlogDto createBlog(BlogRequest blog) {
    User user = authService.getRequiredAuthenticatedUser();
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
  @Transactional
  public BlogDto updateBlog(UUID blogId, BlogRequest blog) {
    User user = authService.getRequiredAuthenticatedUser();
    Blog existingBlog = blogRepository.findByIdAndAuthor_Id(blogId, user.getId())
        .orElseThrow(() -> new EntityNotFoundException("Blog not found"));

    existingBlog.setContent(blog.getContent());
    existingBlog.setDescription(blog.getDescription());
    existingBlog.setTitle(blog.getTitle());

    Blog updatedBlog = blogRepository.save(existingBlog);

    return blogMapper.toDto(updatedBlog);
  }

  @Override
  public List<BlogDto> getBlogs() {
    return blogRepository.findAll().stream().map(blogMapper::toDto).toList();
  }

  @Override
  public List<BlogDto> getOwnBlogs() {
    User user = authService.getRequiredAuthenticatedUser();
    return blogRepository.findByAuthor_Id(user.getId()).stream().map(blogMapper::toDto).toList();
  }

  @Override
  public BlogDto getBlog(UUID blogId) {

    return blogMapper
        .toDto(blogRepository.findById(blogId).orElseThrow(() -> new EntityNotFoundException("Blog not found")));
  }

  @Override
  public void deleteBlog(UUID blogId) {
    blogRepository.deleteById(blogId);

  }
}
