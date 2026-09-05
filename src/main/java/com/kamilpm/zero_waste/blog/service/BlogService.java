package com.kamilpm.zero_waste.blog.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.api.AuthApi;
import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.blog.dto.BlogDto;
import com.kamilpm.zero_waste.blog.dto.BlogRequest;
import com.kamilpm.zero_waste.blog.entity.Blog;
import com.kamilpm.zero_waste.blog.mapper.BlogMapper;
import com.kamilpm.zero_waste.blog.repository.BlogRepository;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.moderation.api.RejectReportEvent;
import com.kamilpm.zero_waste.user.api.UsersDeletedEvent;
import com.kamilpm.zero_waste.user.api.UserBlogApi;
import com.kamilpm.zero_waste.user.api.UserRole;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogService {

  private final AuthApi authApi;
  private final BlogRepository blogRepository;
  private final BlogMapper blogMapper;
  private final UserBlogApi userBlogApi;
  private final ApplicationEventPublisher events;
  // private final ReportService reportService;

  public BlogDto createBlog(BlogRequest blog) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    Blog newBlog = Blog.builder()
        .authorId(user.id())
        .content(blog.getContent())
        .description(blog.getDescription())
        .title(blog.getTitle())
        .build();
    Blog savedBlog = blogRepository.save(newBlog);

    return blogMapper.toDto(savedBlog, user);
  }

  @Transactional
  public BlogDto updateBlog(UUID blogId, BlogRequest blog) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    Blog existingBlog = blogRepository
        .findByIdAndAuthorIdAndModerationStatus(blogId, user.id(), ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Blog not found"));
    existingBlog.setContent(blog.getContent());
    existingBlog.setDescription(blog.getDescription());
    existingBlog.setTitle(blog.getTitle());
    Blog updatedBlog = blogRepository.save(existingBlog);
    return blogMapper.toDto(updatedBlog, user);
  }

  public List<BlogDto> getBlogs() {
    Set<UUID> excludedAuthorIds = userBlogApi.findExcludedAuthorIdsForPublicContent();
    List<Blog> blogs = blogRepository
        .findVisibleBlogsExcludingAuthors(ModerationStatus.VISIBLE,
            excludedAuthorIds);
    Map<UUID, AuthenticatedUser> authors = userBlogApi
        .getAuthorsByIds(blogs.stream().map((blog) -> blog.getAuthorId()).toList());

    return blogs.stream().map(blog -> blogMapper.toDto(blog, authors.get(blog.getAuthorId()))).toList();
  }

  public List<BlogDto> getOwnBlogs() {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    return blogRepository.findByAuthorIdOrderByCreatedAtDesc(user.id()).stream()
        .map(blog -> blogMapper.toDto(blog, user)).toList();
  }

  public BlogDto getBlog(UUID blogId) {
    Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new EntityNotFoundException("Blog not found"));

    AuthenticatedUser author = userBlogApi.getAuthorById(blog.getAuthorId());

    if (Objects.equals(blog.getModerationStatus(), ModerationStatus.VISIBLE)
        && !userBlogApi.isUserDemo(blog.getAuthorId())) {
      return blogMapper.toDto(blog, author);
    }
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    if (Objects.equals(user.role(), UserRole.ADMIN))
      return blogMapper.toDto(blog, author);

    if (Objects.equals(user.id(), blog.getAuthorId()))
      return blogMapper.toDto(blog, author);

    throw new EntityNotFoundException("Blog not available");

  }

  public void deleteBlog(UUID blogId) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new EntityNotFoundException("Blog not found"));

    boolean isAdmin = user.role() == UserRole.ADMIN;

    if (!Objects.equals(user.id(), blog.getAuthorId()) && !isAdmin) {
      throw new ForbiddenException("You are not the owner of this blog");
    }
    blogRepository.deleteById(blog.getId());

    events.publishEvent(new RejectReportEvent(blogId, isAdmin));
  }

  @ApplicationModuleListener
  void on(UsersDeletedEvent event) {
    blogRepository.deleteByAuthorIdIn(event.ids());
  }
}
