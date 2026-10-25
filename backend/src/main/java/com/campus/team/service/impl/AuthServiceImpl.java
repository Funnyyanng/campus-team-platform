package com.campus.team.service.impl;

import com.campus.team.dto.request.LoginRequest;
import com.campus.team.dto.request.RegisterRequest;
import com.campus.team.dto.request.SendCodeRequest;
import com.campus.team.dto.response.LoginResponse;
import com.campus.team.dto.response.UserInfoResponse;
import com.campus.team.entity.User;
import com.campus.team.exception.BusinessException;
import com.campus.team.repository.UserRepository;
import com.campus.team.service.IAuthService;
import com.campus.team.utils.JwtUtil;
import com.campus.team.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // @Autowired
    // private JavaMailSender mailSender;
    
    @Value("${email.whitelist}")
    private String emailWhitelist;
    
    @Value("${email.code-expiration}")
    private Long codeExpiration;
    
    // @Value("${spring.mail.username}")
    // private String fromEmail;
    
    private static final String CODE_PREFIX = "code:";
    
    @Override
    public void sendCode(SendCodeRequest request) {
        String email = request.getEmail();
        
        // 验证邮箱白名单
        if (!email.endsWith(".edu.cn")) {
            throw new BusinessException("仅支持高校邮箱注册（@*.edu.cn）");
        }
        
        // 检查是否已注册
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("该邮箱已注册");
        }
        
        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(999999));
        
        // 存入Redis
        redisUtil.set(CODE_PREFIX + email, code, codeExpiration, TimeUnit.SECONDS);
        
        // 开发环境：直接在控制台打印验证码
        log.info("===========================================");
        log.info("【验证码】邮箱: {} 的验证码是: {}", email, code);
        log.info("【验证码】有效期: {} 秒", codeExpiration);
        log.info("===========================================");
        
        // 生产环境：发送邮件（暂时注释）
        /*
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【校园组队平台】注册验证码");
            message.setText("您的验证码是：" + code + "\n有效期5分钟，请勿泄露给他人。");
            mailSender.send(message);
            log.info("验证码已发送至: {}", email);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new BusinessException("邮件发送失败，请稍后重试");
        }
        */
    }
    
    @Override
    public void register(RegisterRequest request) {
        String email = request.getEmail();
        
        // 验证邮箱白名单
        if (!email.endsWith(".edu.cn")) {
            throw new BusinessException("仅支持高校邮箱注册（@*.edu.cn）");
        }
        
        // 检查是否已注册
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("该邮箱已注册");
        }
        
        // 验证验证码
        String cacheCode = redisUtil.get(CODE_PREFIX + email);
        if (cacheCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!cacheCode.equals(request.getCode())) {
            throw new BusinessException("验证码错误");
        }
        
        // 创建用户
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname("用户_" + email.substring(0, email.indexOf("@")));
        user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + email);
        user.setRole("USER");
        user.setAllowInvite(true);
        user.setStatus("NORMAL");
        
        userRepository.save(user);
        
        // 删除验证码
        redisUtil.delete(CODE_PREFIX + email);
        
        log.info("用户注册成功: {}", email);
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();
        
        // 查询用户
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("邮箱或密码错误"));
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("邮箱或密码错误");
        }
        
        // 检查账号状态
        if ("BANNED".equals(user.getStatus())) {
            throw new BusinessException("账号已被封禁");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        // 构建用户信息
        UserInfoResponse userInfo = new UserInfoResponse();
        BeanUtils.copyProperties(user, userInfo);
        
        log.info("用户登录成功: {}", email);
        return new LoginResponse(token, userInfo);
    }
}
