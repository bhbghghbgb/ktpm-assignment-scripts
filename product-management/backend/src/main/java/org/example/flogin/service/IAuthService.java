package org.example.flogin.service;

import org.example.flogin.dto.LoginDTO;

public interface IAuthService {
    String login(LoginDTO loginDTO);

    // Placeholder cho phương thức validation
    boolean validateUser(String username, String password);
}