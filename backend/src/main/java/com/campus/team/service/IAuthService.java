package com.campus.team.service;

import com.campus.team.dto.request.LoginRequest;
import com.campus.team.dto.request.RegisterRequest;
import com.campus.team.dto.request.SendCodeRequest;
import com.campus.team.dto.response.LoginResponse;

public interface IAuthService {
    
    void sendCode(SendCodeRequest request);
    
    void register(RegisterRequest request);
    
    LoginResponse login(LoginRequest request);
}
