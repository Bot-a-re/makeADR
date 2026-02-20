package com.example.demo;

import com.example.demo.service.UserService;

/**
 * 샘플 애플리케이션 메인 클래스
 */
public class Application {
    
    public static void main(String[] args) {
        System.out.println("Sample Application Started");
        
        UserService userService = UserService.getInstance();
        userService.createUser("John Doe", "john@example.com");
        
        System.out.println("Application Finished");
    }
}
