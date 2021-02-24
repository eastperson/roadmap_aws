package com.roadmap.repository;

import com.roadmap.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<Node,Long> {
}
