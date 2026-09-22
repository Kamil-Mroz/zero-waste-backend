package com.kamilpm.zero_waste.blog.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.blog.dto.BlogDto;
import com.kamilpm.zero_waste.blog.entity.Blog;
import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class BlogMapper {

  public BlogDto toDto(Blog blog, UserSummaryDto user) {
    if (blog == null || user == null) {
      return null;
    }

    return new BlogDto(
        blog.getId(),
        blog.getTitle(),
        blog.getDescription(),
        blog.getContent(),
        user,
        blog.getModerationStatus(),
        blog.getCreatedAt(),
        blog.getUpdatedAt());

  }

  public BlogDto toDto(Blog blog, CurrentUser user) {
    if (blog == null || user == null) {
      return null;
    }

    return new BlogDto(
        blog.getId(),
        blog.getTitle(),
        blog.getDescription(),
        blog.getContent(),
        new UserSummaryDto(user.id(), user.nickname()),
        blog.getModerationStatus(),
        blog.getCreatedAt(),
        blog.getUpdatedAt());

  }
}
