package com.campus.team.repository;

import com.campus.team.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByReceiverIdOrderByCreateTimeDesc(Long receiverId);
    
    List<Message> findByReceiverIdAndTypeOrderByCreateTimeDesc(Long receiverId, String type);
    
    List<Message> findByReceiverIdAndIsReadOrderByCreateTimeDesc(Long receiverId, Boolean isRead);
    
    long countByReceiverIdAndIsRead(Long receiverId, Boolean isRead);
    
    boolean existsBySenderIdAndReceiverIdAndRelatedIdAndType(Long senderId, Long receiverId, Long relatedId, String type);
}
