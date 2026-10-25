package com.campus.team.repository;

import com.campus.team.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    List<Skill> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
    
    // 统计热门技能标签
    // 注意：如果skills表不存在，请执行 sql/create-skills-table.sql 创建表
    @org.springframework.data.jpa.repository.Query(
        value = "SELECT s.name, COUNT(s.user_id) as count " +
                "FROM skills s " +
                "GROUP BY s.name " +
                "ORDER BY count DESC " +
                "LIMIT ?1",
        nativeQuery = true
    )
    List<Object[]> findTopSkills(int limit);
}
