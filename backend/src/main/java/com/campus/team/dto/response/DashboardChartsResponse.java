package com.campus.team.dto.response;

import lombok.Data;

@Data
public class DashboardChartsResponse {
    
    private TeamStatusData teamStatus;
    private CompetitionLevelData competitionLevel;
    private SkillTagData skillTag;
    private TeamTypeData teamType;
    
    @Data
    public static class TeamStatusData {
        private Long recruiting;
        private Long full;
        private Long closed;
    }
    
    @Data
    public static class CompetitionLevelData {
        private Long national;
        private Long provincial;
        private Long school;
    }
    
    @Data
    public static class SkillTagData {
        private java.util.List<SkillTagItem> items;
    }
    
    @Data
    public static class SkillTagItem {
        private String name;
        private Long count;
    }
    
    @Data
    public static class TeamTypeData {
        private Long competition;
        private Long project;
    }
}
