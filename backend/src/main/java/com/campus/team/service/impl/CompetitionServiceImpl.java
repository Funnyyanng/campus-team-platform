package com.campus.team.service.impl;

import com.campus.team.common.PageResult;
import com.campus.team.dto.request.CompetitionQueryRequest;
import com.campus.team.dto.request.CompetitionRequest;
import com.campus.team.dto.response.CompetitionResponse;
import com.campus.team.entity.Competition;
import com.campus.team.entity.User;
import com.campus.team.exception.BusinessException;
import com.campus.team.repository.CompetitionRepository;
import com.campus.team.repository.UserRepository;
import com.campus.team.repository.TeamRepository;
import com.campus.team.service.ICompetitionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Slf4j
@Service
public class CompetitionServiceImpl implements ICompetitionService {
    
    @Autowired
    private CompetitionRepository competitionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Override
    public PageResult<CompetitionResponse> getCompetitionList(CompetitionQueryRequest queryRequest) {
        // 构建查询条件
        Specification<Competition> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 关键词搜索
            if (queryRequest.getKeyword() != null && !queryRequest.getKeyword().isEmpty()) {
                String keyword = "%" + queryRequest.getKeyword() + "%";
                predicates.add(cb.or(
                    cb.like(root.get("name"), keyword),
                    cb.like(root.get("organizer"), keyword)
                ));
            }
            
            // 级别筛选
            if (queryRequest.getLevel() != null && !queryRequest.getLevel().isEmpty()) {
                predicates.add(cb.equal(root.get("level"), queryRequest.getLevel()));
            }
            
            // 状态筛选
            if (queryRequest.getStatus() != null && !queryRequest.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), queryRequest.getStatus()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 分页查询
        Pageable pageable = PageRequest.of(
            queryRequest.getPage() - 1,
            queryRequest.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createTime")
        );
        
        Page<Competition> competitionPage = competitionRepository.findAll(spec, pageable);
        
        // 转换为响应对象
        List<CompetitionResponse> records = competitionPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PageResult<>(
            records,
            competitionPage.getTotalElements(),
            queryRequest.getPage(),
            queryRequest.getPageSize()
        );
    }
    
    @Override
    public List<CompetitionResponse> getActiveCompetitions() {
        // 查询所有进行中的竞赛
        List<Competition> competitions = competitionRepository.findByStatusOrderByCreateTimeDesc("ACTIVE");
        
        return competitions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public CompetitionResponse getCompetitionById(Long id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("竞赛不存在"));
        return convertToResponse(competition);
    }
    
    @Override
    @Transactional
    public void createCompetition(Long adminId, CompetitionRequest request) {
        // 验证管理员权限
        verifyAdmin(adminId);
        
        // 检查竞赛名称是否已存在
        if (competitionRepository.existsByName(request.getName())) {
            throw new BusinessException("竞赛名称已存在");
        }
        
        Competition competition = new Competition();
        BeanUtils.copyProperties(request, competition);
        
        // 验证队伍人数
        if (request.getMaxTeamSize() < request.getMinTeamSize()) {
            throw new BusinessException("最大队伍人数不能小于最小队伍人数");
        }
        
        // 转换关键词为JSON
        if (request.getKeywords() != null) {
            try {
                competition.setKeywords(objectMapper.writeValueAsString(request.getKeywords()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("关键词格式错误");
            }
        }
        
        if (competition.getStatus() == null) {
            competition.setStatus("ACTIVE");
        }
        competitionRepository.save(competition);
        
        log.info("管理员 {} 创建了竞赛: {}", adminId, request.getName());
    }
    
    @Override
    @Transactional
    public void updateCompetition(Long adminId, Long id, CompetitionRequest request) {
        // 验证管理员权限
        verifyAdmin(adminId);
        
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("竞赛不存在"));
        
        // 检查名称是否与其他竞赛重复
        if (!competition.getName().equals(request.getName()) 
                && competitionRepository.existsByName(request.getName())) {
            throw new BusinessException("竞赛名称已存在");
        }
        
        BeanUtils.copyProperties(request, competition);
        
        // 验证队伍人数
        if (request.getMaxTeamSize() < request.getMinTeamSize()) {
            throw new BusinessException("最大队伍人数不能小于最小队伍人数");
        }
        
        // 转换关键词为JSON
        if (request.getKeywords() != null) {
            try {
                competition.setKeywords(objectMapper.writeValueAsString(request.getKeywords()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("关键词格式错误");
            }
        }
        
        competitionRepository.save(competition);
        
        log.info("管理员 {} 更新了竞赛: {}", adminId, id);
    }
    
    @Override
    @Transactional
    public void deleteCompetition(Long adminId, Long id) {
        // 验证管理员权限
        verifyAdmin(adminId);
        
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("竞赛不存在"));
        
        competitionRepository.delete(competition);
        
        log.info("管理员 {} 删除了竞赛: {}", adminId, id);
    }
    
    private void verifyAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException("无权限操作");
        }
    }
    
    private CompetitionResponse convertToResponse(Competition competition) {
        CompetitionResponse response = new CompetitionResponse();
        BeanUtils.copyProperties(competition, response);
        
        // 解析关键词JSON
        if (competition.getKeywords() != null) {
            try {
                List<String> keywords = objectMapper.readValue(
                    competition.getKeywords(), 
                    new TypeReference<List<String>>() {}
                );
                response.setKeywords(keywords);
            } catch (JsonProcessingException e) {
                response.setKeywords(new ArrayList<>());
            }
        }
        
        // 统计招募中的队伍数量
        long recruitingCount = teamRepository.countByCompetitionIdAndStatus(
            competition.getId(), 
            "RECRUITING"
        );
        response.setRecruitingTeamCount(recruitingCount);
        
        return response;
    }
}
