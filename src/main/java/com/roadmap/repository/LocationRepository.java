package com.roadmap.repository;

import com.roadmap.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface LocationRepository extends JpaRepository<Location, Long> {
}
