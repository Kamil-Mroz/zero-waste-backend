package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.BlogDto;
import com.kamilpm.zero_waste.domain.request.BlogRequest;
import com.kamilpm.zero_waste.service.BlogService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(path = "/api/v{version}/blogs", version = "1")
@RequiredArgsConstructor
public class BlogController {

  private final BlogService blogService;

  @PostMapping
  public ResponseEntity<BlogDto> createBlog(@RequestBody BlogRequest blog) {

    BlogDto createdBlog = blogService.createBlog(blog);

    return new ResponseEntity<BlogDto>(createdBlog, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<BlogDto>> getBlogs() {

    List<BlogDto> blogs = blogService.getBlogs();
    return ResponseEntity.ok(blogs);
  }

  @GetMapping("/own")
  public ResponseEntity<List<BlogDto>> getOwnBlogs() {

    List<BlogDto> blogs = blogService.getOwnBlogs();
    return ResponseEntity.ok(blogs);
  }


}
