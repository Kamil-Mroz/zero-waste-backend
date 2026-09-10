package com.kamilpm.zero_waste.blog.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.blog.dto.AuthenticatedUser;
import com.kamilpm.zero_waste.blog.dto.BlogDto;
import com.kamilpm.zero_waste.blog.dto.UserSummaryDto;
import com.kamilpm.zero_waste.blog.entity.Blog;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class BlogMapper {

  public BlogDto toDto(Blog blog, AuthenticatedUser user) {
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
