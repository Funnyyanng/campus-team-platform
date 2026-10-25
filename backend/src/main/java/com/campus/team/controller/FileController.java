package com.campus.team.controller;

import com.campus.team.common.Result;
import com.campus.team.dto.response.UploadResponse;
import com.campus.team.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Tag(name = "文件上传模块")
@RestController
@RequestMapping("/api/file")
public class FileController {
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    @Value("${file.upload.url-prefix}")
    private String urlPrefix;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
    
    @Operation(summary = "上传头像")
    @PostMapping("/upload/avatar")
    public Result<UploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过5MB");
        }
        
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        boolean isAllowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (extension.equals(allowedExt)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new BusinessException("只支持 jpg、jpeg、png、gif 格式的图片");
        }
        
        try {
            // 获取绝对路径
            File uploadDirFile = new File(uploadPath);
            if (!uploadDirFile.isAbsolute()) {
                uploadDirFile = new File(System.getProperty("user.dir"), uploadPath);
            }
            
            // 创建上传目录
            File avatarDir = new File(uploadDirFile, "avatars");
            if (!avatarDir.exists()) {
                boolean created = avatarDir.mkdirs();
                if (!created) {
                    throw new BusinessException("创建上传目录失败");
                }
            }
            
            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + extension;
            File destFile = new File(avatarDir, filename);
            
            // 保存文件
            file.transferTo(destFile);
            
            // 构建访问URL
            String url = urlPrefix + "/avatars/" + filename;
            
            log.info("文件上传成功: {}", url);
            
            UploadResponse response = new UploadResponse();
            response.setUrl(url);
            response.setFilename(filename);
            
            return Result.success(response);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }
}
