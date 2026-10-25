package com.campus.team.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementResponse {
    
    private Long id;
    private String title;
    private String content;
    private Long publisherId;
    private String publisherName;
    private Integer recipientCount;
    private LocalDateTime createTime;
}
