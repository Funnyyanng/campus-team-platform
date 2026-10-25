package com.campus.team.repository;

import com.campus.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    List<Team> findByStatus(String status);
    
    List<Team> findByCompetitionIdAndStatus(Long competitionId, String status);
    
    List<Team> findByLeaderId(Long leaderId);
    
    boolean existsByCompetitionIdAndLeaderId(Long competitionId, Long leaderId);
    
    long countByCompetitionIdAndStatus(Long competitionId, String status);
    
    // 课题模式查询方法
    List<Team> findByType(String type);
    
    List<Team> findByTypeAndStatus(String type, String status);
    
    // 统计方法
    long countByStatus(String status);
    
    long countByType(String type);
}
