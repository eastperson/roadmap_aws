package com.roadmap.repository;

import com.roadmap.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TagRepository extends JpaRepository<Tag,Long> {
    boolean existsByTitle(String title);

    Tag findByTitle(String title);
}
