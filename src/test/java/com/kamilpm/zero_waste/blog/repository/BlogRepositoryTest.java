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
    User author = User.builder()
        .nickname("test-123")
        .email("test@example.com")
        .password(null)
        .role(UserRole.WRITER)
        .banActive(false)
        .build();

    User savedAuthor = userRepository.save(author);

    Blog blog1 = Blog.builder()
        .authorId(savedAuthor.getId())
        .content("This is the content")
        .description("This is the description")
        .authorVisibility(UserVisibility.VISIBLE)
        .title("This is the title")
        .build();

    Blog blog2 = Blog.builder()
        .authorId(savedAuthor.getId())
        .content("This is the content")
        .description("This is the description")
        .authorVisibility(UserVisibility.BANNED)
        .title("This is the title")
        .build();

    blogRepository.saveAll(List.of(blog1, blog2));
    List<Blog> blogs = blogRepository.findVisibleBlogs(ModerationStatus.VISIBLE, UserVisibility.VISIBLE);
    assertThat(blogs.size()).isEqualTo(1);

  }

  @Test
  void testUpdateAuthorVisibility() {

    User author = User.builder()
        .nickname("test-123")
        .email("test@example.com")
        .password(null)
        .role(UserRole.WRITER)
        .banActive(false)
        .build();

    User savedAuthor = userRepository.save(author);

    Blog blog1 = Blog.builder()
        .authorId(savedAuthor.getId())
        .content("This is the content")
        .description("This is the description")
        .authorVisibility(UserVisibility.VISIBLE)
        .title("This is the title")
        .build();

    Blog savedBlog = blogRepository.save(blog1);
    blogRepository.updateAuthorVisibility(List.of(author.getId()), UserVisibility.BANNED);
    Optional<Blog> updatedBlog = blogRepository.findById(savedBlog.getId());
    assertThat(updatedBlog).isPresent();
    assertThat(updatedBlog.get().getAuthorVisibility()).isEqualTo(UserVisibility.BANNED);

  }
}
