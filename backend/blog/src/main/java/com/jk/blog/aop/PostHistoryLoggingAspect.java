package com.jk.blog.aop;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.blog.dto.post.PostResponseBody;
import com.jk.blog.entity.PostHistory;
import com.jk.blog.repository.PostHistoryRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Aspect
public class PostHistoryLoggingAspect {

    @Autowired
    private PostHistoryRepository postHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("execution(* com.jk.blog.service.PostService.createPost(..)) || execution(* com.jk.blog.service.PostService.updatePost(..)) || " +
              "execution(* com.jk.blog.service.PostService.patchPost(..)) || execution(* com.jk.blog.service.PostService.togglePostVisibility(..)) || " +
              "execution(* com.jk.blog.service.PostService.deactivatePost(..)) || execution(* com.jk.blog.service.PostService.activatePost(..))")
    public void postModificationMethods() {}

    @AfterReturning(pointcut = "postModificationMethods()", returning = "postResponseBody")
    public void logPostHistory(JoinPoint joinPoint, PostResponseBody postResponseBody) throws JsonProcessingException {
        if (postResponseBody != null) {
            Hibernate.initialize(postResponseBody.getComments()); // Ensure comments are loaded
            Hibernate.initialize(postResponseBody.getTagNames()); // Ensure tags are loaded
            String jsonData = objectMapper.writeValueAsString(postResponseBody);
            PostHistory history = new PostHistory();
            history.setPostId(postResponseBody.getPostId());
            history.setPostHistoryJsonData(jsonData);
            history.setHistoryCreatedAt(Instant.now());
            postHistoryRepository.save(history);
        }
    }

}
