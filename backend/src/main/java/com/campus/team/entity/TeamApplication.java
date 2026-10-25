package com.campus.team.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "team_applications")
public class TeamApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long teamId;
    
    @Column(nullable = false)
    private Long applicantId;
    
    @Column(columnDefinition = "TEXT")
    private String answers;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝
    
    @Column(length = 200)
    private String rejectReason;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
