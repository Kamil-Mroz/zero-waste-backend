package com.kamilpm.zero_waste.blog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.kamilpm.zero_waste.blog.entity.Blog;
import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.config.PostgresTestConfiguration;
import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.repository.UserRepository;

@DataJpaTest
@Import(PostgresTestConfiguration.class)

public class BlogRepositoryTest {

  @Autowired
  private BlogRepository blogRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  void testFindVisibleBlogs() {
    User author = new User();
    author.setNickname("test-123");
    author.setEmail("test@example.com");
    author.setPassword(null);
    author.setRole(UserRole.WRITER);
    author.setBanActive(false);

    User savedAuthor = userRepository.save(author);

    Blog blog1 = new Blog();
    blog1.setAuthorId(savedAuthor.getId());
    blog1.setContent("This is the content");
    blog1.setDescription("This is the description");
    blog1.setAuthorVisibility(UserVisibility.VISIBLE);
    blog1.setTitle("This is the title");

    Blog blog2 = new Blog();
    blog2.setAuthorId(savedAuthor.getId());
    blog2.setContent("This is the content");
    blog2.setDescription("This is the description");
    blog2.setAuthorVisibility(UserVisibility.BANNED);
    blog2.setTitle("This is the title");

    blogRepository.saveAll(List.of(blog1, blog2));
    List<Blog> blogs = blogRepository.findVisibleBlogs(ModerationStatus.VISIBLE, UserVisibility.VISIBLE);
    assertThat(blogs.size()).isEqualTo(1);

  }

  @Test
  void testUpdateAuthorVisibility() {

    User author = new User();
    author.setNickname("test-123");
    author.setEmail("test@example.com");
    author.setPassword(null);
    author.setRole(UserRole.WRITER);
    author.setBanActive(false);

    User savedAuthor = userRepository.save(author);

    Blog blog1 = new Blog();
    blog1.setAuthorId(savedAuthor.getId());
    blog1.setContent("This is the content");
    blog1.setDescription("This is the description");
    blog1.setAuthorVisibility(UserVisibility.VISIBLE);
    blog1.setTitle("This is the title");

    Blog savedBlog = blogRepository.save(blog1);
    blogRepository.updateAuthorVisibility(List.of(author.getId()), UserVisibility.BANNED);
    Optional<Blog> updatedBlog = blogRepository.findById(savedBlog.getId());
    assertThat(updatedBlog).isPresent();
    assertThat(updatedBlog.get().getAuthorVisibility()).isEqualTo(UserVisibility.BANNED);

  }
}
