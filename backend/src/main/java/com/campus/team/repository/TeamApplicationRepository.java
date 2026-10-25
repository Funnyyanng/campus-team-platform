package com.campus.team.repository;

import com.campus.team.entity.TeamApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {
    
    List<TeamApplication> findByTeamId(Long teamId);
    
    List<TeamApplication> findByApplicantId(Long applicantId);
    
    List<TeamApplication> findByApplicantIdAndStatus(Long applicantId, String status);
    
    boolean existsByTeamIdAndApplicantIdAndStatus(Long teamId, Long applicantId, String status);
    
    long countByApplicantIdAndStatus(Long applicantId, String status);
    
    long countByTeamIdAndStatus(Long teamId, String status);
}
