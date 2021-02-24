package com.roadmap.repository;

import com.roadmap.model.Roadmap;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    boolean existsByPath(String path);

    Roadmap findByPath(String path);

    @EntityGraph(value = "Roadmap.withMembers", type = EntityGraph.EntityGraphType.LOAD)
    Roadmap findWithMembersByPath(String path);

    @EntityGraph(value = "Roadmap.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Roadmap findWithAllByPath(String path);

    @EntityGraph(value = "Roadmap.withOwner", type = EntityGraph.EntityGraphType.FETCH)
    Roadmap findRoadmapWithOwnerByPath(String path);
}
