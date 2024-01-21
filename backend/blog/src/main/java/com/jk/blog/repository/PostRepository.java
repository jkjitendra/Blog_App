package com.jk.blog.repository;

import com.jk.blog.entity.Category;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUser(User user);

    List<Post> findByCategory(Category category);

    List<Post> findByTitleContainingIgnoreCase(String titlePattern);
}
