package com.kamilpm.zero_waste.blog.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.kamilpm.zero_waste.blog.dto.BlogDto;
import com.kamilpm.zero_waste.blog.dto.BlogRequest;
import com.kamilpm.zero_waste.blog.entity.Blog;
import com.kamilpm.zero_waste.blog.mapper.BlogMapper;
import com.kamilpm.zero_waste.blog.repository.BlogRepository;
import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.events.BanEvent;
import com.kamilpm.zero_waste.common.events.RejectReportEvent;
import com.kamilpm.zero_waste.common.events.UnbanEvent;
import com.kamilpm.zero_waste.common.events.UserRoleChangeEvent;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;
import com.kamilpm.zero_waste.common.interfaces.UserProvider;

import jakarta.transaction.Transactional;

@Service
public class BlogService {

  private final CurrentUserProvider currentUser;
  private final BlogRepository blogRepository;
  private final BlogMapper blogMapper;
  private final UserProvider userBlogApi;
  private final ApplicationEventPublisher events;
  private final RedisTemplate<String, BlogDto> blogRedisTemplate;

  public BlogService(CurrentUserProvider currentUser, BlogRepository blogRepository, BlogMapper blogMapper,
      UserProvider userBlogApi, ApplicationEventPublisher events, RedisTemplate<String, BlogDto> blogRedisTemplate) {
    this.currentUser = currentUser;
    this.blogRepository = blogRepository;
    this.blogMapper = blogMapper;
    this.userBlogApi = userBlogApi;
    this.events = events;
    this.blogRedisTemplate = blogRedisTemplate;
  }

  @Caching(evict = {
      @CacheEvict(value = "blogs", cacheManager = "blogCacheManager", allEntries = true),
      @CacheEvict(value = "ownBlogs", cacheManager = "blogCacheManager", allEntries = true),
  })
  public BlogDto createBlog(BlogRequest blog) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    Blog newBlog = new Blog();
    newBlog.setAuthorId(user.id());
    newBlog.setContent(blog.content());
    newBlog.setDescription(blog.description());
    newBlog.setAuthorVisibility(UserVisibility.VISIBLE);
    newBlog.setTitle(blog.title());
    Blog savedBlog = blogRepository.save(newBlog);

    return blogMapper.toDto(savedBlog, user);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "blogs", cacheManager = "blogCacheManager", allEntries = true),
      @CacheEvict(value = "ownBlogs", cacheManager = "blogCacheManager", allEntries = true),
  })
  public BlogDto updateBlog(UUID blogId, BlogRequest blog) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    Blog existingBlog = blogRepository
        .findByIdAndAuthorIdAndModerationStatus(blogId, user.id(), ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Blog not found"));
    existingBlog.setContent(blog.content());
    existingBlog.setDescription(blog.description());
    existingBlog.setTitle(blog.title());
    Blog updatedBlog = blogRepository.save(existingBlog);
    blogRedisTemplate.delete("blog:public:" + blogId);
    return blogMapper.toDto(updatedBlog, user);
  }

  @Cacheable(value = "blogs", cacheManager = "blogCacheManager")
  public List<BlogDto> getBlogs() {
    List<Blog> blogs = blogRepository
        .findVisibleBlogs(ModerationStatus.VISIBLE,
            UserVisibility.VISIBLE);
    List<UUID> authorsIds = blogs.stream().map((blog) -> blog.getAuthorId()).distinct().toList();
    Map<UUID, UserSummaryDto> authors = userBlogApi.getUserSummaryByIds(authorsIds);

    return blogs.stream().map(blog -> blogMapper.toDto(blog, authors.get(blog.getAuthorId()))).toList();
  }

  @Cacheable(value = "ownBlogs", cacheManager = "blogCacheManager", key = "#user.id()")
  public List<BlogDto> getOwnBlogsCached(CurrentUser user) {
    return blogRepository
        .findByAuthorIdOrderByCreatedAtDesc(user.id())
        .stream()
        .map(blog -> blogMapper.toDto(blog, user))
        .toList();
  }

  public List<BlogDto> getOwnBlogs() {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    return getOwnBlogsCached(user);
  }

  public BlogDto getBlog(UUID blogId) {
    String key = "blog:public:" + blogId;
    BlogDto cachedBlog = blogRedisTemplate.opsForValue().get(key);
    if (cachedBlog != null) {
      return cachedBlog;
    }

    Blog blog = blogRepository.findById(blogId)
        .orElseThrow(() -> new EntityNotFoundException("Blog not found"));

    UserSummaryDto author = userBlogApi.findUserSummaryById(blog.getAuthorId());

    boolean publiclyVisible = Objects.equals(
        blog.getModerationStatus(), ModerationStatus.VISIBLE)
        && !userBlogApi.isUserDemo(blog.getAuthorId())
        && Objects.equals(
            blog.getAuthorVisibility(), UserVisibility.VISIBLE);

    if (publiclyVisible) {
      var publicBlog = blogMapper.toDto(blog, author);
      blogRedisTemplate.opsForValue().set(
          key,
          publicBlog,
          Duration.ofHours(1));
      return publicBlog;
    }

    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    if (Objects.equals(user.role(), UserRole.ADMIN)
        || Objects.equals(user.id(), blog.getAuthorId())) {
      return blogMapper.toDto(blog, author);
    }

    throw new EntityNotFoundException("Blog not available");
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "blogs", cacheManager = "blogCacheManager", allEntries = true),
      @CacheEvict(value = "ownBlogs", cacheManager = "blogCacheManager", allEntries = true),
  })
  public void deleteBlog(UUID blogId) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new EntityNotFoundException("Blog not found"));

    boolean isAdmin = user.role() == UserRole.ADMIN;

    if (!Objects.equals(user.id(), blog.getAuthorId()) && !isAdmin) {
      throw new ForbiddenException("You are not the owner of this blog");
    }
    blogRepository.deleteById(blog.getId());

    blogRedisTemplate.delete("blog:public:" + blogId);

    events.publishEvent(new RejectReportEvent(blogId, isAdmin));
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        blogRedisTemplate.delete("blog:public:" + blogId);

      }

    });
  }

  @ApplicationModuleListener
  @Caching(evict = {
      @CacheEvict(value = "blogs", cacheManager = "blogCacheManager", allEntries = true),
      @CacheEvict(value = "ownBlogs", cacheManager = "blogCacheManager", allEntries = true),
  })
  void on(BanEvent event) {
    blogRepository.updateAuthorVisibility(event.ids(), UserVisibility.BANNED);
    evictPublicBlogByAuthor(event.ids());
  }

  @ApplicationModuleListener
  @Caching(evict = {
      @CacheEvict(value = "blogs", cacheManager = "blogCacheManager", allEntries = true),
      @CacheEvict(value = "ownBlogs", cacheManager = "blogCacheManager", allEntries = true),
  })
  void on(UnbanEvent event) {
    blogRepository.updateAuthorVisibility(event.ids(), UserVisibility.VISIBLE);
    evictPublicBlogByAuthor(event.ids());
  }

  @ApplicationModuleListener
  @Caching(evict = {
      @CacheEvict(value = "blogs", cacheManager = "blogCacheManager", allEntries = true),
      @CacheEvict(value = "ownBlogs", cacheManager = "blogCacheManager", allEntries = true),
  })
  void on(UserRoleChangeEvent event) {

    if (event.newRole() == UserRole.DEMO) {
      blogRepository.updateAuthorVisibility(event.userId(), UserVisibility.HIDDEN);
    }

    if (event.oldRole() == UserRole.DEMO
        && event.newRole() != UserRole.DEMO) {
      blogRepository.updateAuthorVisibility(event.userId(), UserVisibility.VISIBLE);
    }
    evictPublicBlogByAuthor(event.userId());
  }

  private void evictPublicBlogByAuthor(List<UUID> ids) {
    List<UUID> blogIds = blogRepository.findByAuthorIdIn(ids);
    blogIds.forEach(id -> blogRedisTemplate.delete("blog:public:" + id));
  }

  private void evictPublicBlogByAuthor(UUID authorId) {
    List<UUID> blogIds = blogRepository.findByAuthorId(authorId);
    blogIds.forEach(id -> blogRedisTemplate.delete("blog:public:" + id));
  }

}
