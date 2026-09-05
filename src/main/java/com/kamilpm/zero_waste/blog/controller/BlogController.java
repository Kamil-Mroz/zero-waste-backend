package com.kamilpm.zero_waste.blog.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.blog.dto.BlogDto;
import com.kamilpm.zero_waste.blog.dto.BlogRequest;
import com.kamilpm.zero_waste.blog.service.BlogService;
import com.kamilpm.zero_waste.common.annotation.RateLimit;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(path = "/api/v{version}/blogs", version = "1")
@RequiredArgsConstructor
public class BlogController {

  private final BlogService blogService;

  @RateLimit(action = "create-blog", limit = 10, window = 10, unit = ChronoUnit.MINUTES)
  @PostMapping
  public ResponseEntity<BlogDto> createBlog(@Valid @RequestBody BlogRequest blog) {
    BlogDto createdBlog = blogService.createBlog(blog);
    return new ResponseEntity<BlogDto>(createdBlog, HttpStatus.CREATED);
  }

  @RateLimit(action = "update-blog", limit = 20, window = 1, unit = ChronoUnit.MINUTES)
  @PutMapping("/{id}")
  public ResponseEntity<BlogDto> updateBlog(@PathVariable(name = "id") UUID id, @Valid @RequestBody BlogRequest blog) {
    BlogDto updatedBlog = blogService.updateBlog(id, blog);
    return new ResponseEntity<BlogDto>(updatedBlog, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<BlogDto>> getBlogs() {
    List<BlogDto> blogs = blogService.getBlogs();
    return ResponseEntity.ok(blogs);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BlogDto> getBlog(@PathVariable(name = "id") UUID id) {
    BlogDto blog = blogService.getBlog(id);
    return ResponseEntity.ok(blog);
  }

  // @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'DEMO')")
  @GetMapping("/own")
  public ResponseEntity<List<BlogDto>> getOwnBlogs() {
    List<BlogDto> blogs = blogService.getOwnBlogs();
    return ResponseEntity.ok(blogs);
  }

  @RateLimit(action = "delete-blog", limit = 10, window = 10, unit = ChronoUnit.MINUTES)
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBlog(@PathVariable(name = "id") UUID id) {
    blogService.deleteBlog(id);
    return ResponseEntity.noContent().build();

  }

}
