package com.kamilpm.zero_waste.blog.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;

import com.kamilpm.zero_waste.blog.dto.BlogDto;
import com.kamilpm.zero_waste.blog.entity.Blog;
import com.kamilpm.zero_waste.user.api.UserSummaryMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class BlogMapper {
  private final UserSummaryMapper userSummaryMapper;

  public BlogDto toDto(Blog blog, AuthenticatedUser user) {
    if (blog == null || user == null) {
      return null;
    }

    return new BlogDto(
        blog.getId(),
        blog.getTitle(),
        blog.getDescription(),
        blog.getContent(),
        userSummaryMapper.toDto(user),
        blog.getModerationStatus(),
        blog.getCreatedAt(),
        blog.getUpdatedAt());

  }
}
