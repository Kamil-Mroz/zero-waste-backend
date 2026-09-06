package com.kamilpm.zero_waste.blog.api;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.blog.entity.Blog;
import com.kamilpm.zero_waste.blog.repository.BlogRepository;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.user.api.UserBlogApi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogReportApi {
  private final BlogRepository blogRepository;
  private final UserBlogApi userBlogApi;

  public void blogExists(UUID subjectId, UUID userId) {
    Blog blog = blogRepository.findById(subjectId).orElseThrow(() -> new EntityNotFoundException("Blog not found"));

    if (Objects.equals(blog.getAuthorId(), userId))
      throw new ForbiddenException("You can not report yourself");

    if (userBlogApi.isUserDemo(userId))
      throw new ForbiddenException("Unable to interact with demo users");

    if (Objects.equals(blog.getModerationStatus(), ModerationStatus.HIDDEN))
      throw new ForbiddenException("Unable to report a hidden blog");

  }

  public boolean isBlogAuthor(UUID blogId, UUID userId) {
    return blogRepository.existsByIdAndAuthorId(blogId, userId);
  }

  public void deleteBlogById(UUID blogId) {
    blogRepository.deleteById(blogId);
  }

  public void hideBlog(UUID adminId, UUID subjectId) {

    Blog blog = blogRepository.findByIdAndModerationStatus(subjectId, ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Blog not found"));
    blog.setModeratedAt(Instant.now());
    blog.setModeratedBy(adminId);
    blog.setModerationStatus(ModerationStatus.HIDDEN);
    blogRepository.save(blog);

  }

}
