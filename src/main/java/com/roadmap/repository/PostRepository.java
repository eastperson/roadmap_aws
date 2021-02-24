package com.roadmap.repository;

import com.roadmap.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long> {

    Post findByNodeId(Long nodeId);
}
