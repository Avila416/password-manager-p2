package com.passwordmanager.service;

import com.passwordmanager.dto.AuthResponseDTO;
import com.passwordmanager.dto.ChangePasswordDTO;
import com.passwordmanager.dto.ForgotMasterPasswordRequestDTO;
import com.passwordmanager.dto.ForgotPasswordRequestDTO;
import com.passwordmanager.dto.LoginRequestDTO;
import com.passwordmanager.dto.RegisterRequestDTO;
import com.passwordmanager.dto.TwoFactorStatusDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
    void requestForgotPasswordCode(String email);
    void resetForgotPassword(ForgotPasswordRequestDTO request);
    void requestForgotMasterPasswordCode(String email);
    void resetForgotMasterPassword(ForgotMasterPasswordRequestDTO request);
    void setupMasterPassword(String email, String masterPassword, String confirmMasterPassword);
    void changeMasterPassword(String email, ChangePasswordDTO request);
    TwoFactorStatusDTO updateTwoFactorStatus(String email, TwoFactorStatusDTO request);
}
