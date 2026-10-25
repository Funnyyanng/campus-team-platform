package com.campus.team.repository;

import com.campus.team.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long>, JpaSpecificationExecutor<Competition> {
    
    boolean existsByName(String name);
    
    List<Competition> findByStatusOrderByCreateTimeDesc(String status);
    
    // 统计方法
    long countByStatus(String status);
    
    long countByLevel(String level);
}
