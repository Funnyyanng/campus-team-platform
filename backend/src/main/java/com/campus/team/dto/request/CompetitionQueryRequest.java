package com.campus.team.dto.request;

import lombok.Data;

@Data
public class CompetitionQueryRequest {
    private String keyword;
    private String level;
    private String status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
