package com.jk.blog.repository;

import com.jk.blog.entity.Category;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUser(User user);

    List<Post> findByCategory(Category category);

    @Query("SELECT p FROM Post p WHERE p.postTitle like :patternKey")
    List<Post> searchKeyOnTitle(@Param("patternKey") String searchKey);
}
