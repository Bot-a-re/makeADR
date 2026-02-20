package com.example.demo.service;

import com.example.demo.repository.UserRepository;
import com.example.demo.model.User;

/**
 * 사용자 비즈니스 로직을 처리하는 서비스 클래스
 * Singleton 패턴 적용
 */
public class UserService {
    
    private static UserService instance;
    private UserRepository userRepository;
    
    private UserService() {
        this.userRepository = new UserRepository();
    }
    
    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    public User createUser(String name, String email) {
        User user = new User(name, email);
        userRepository.save(user);
        System.out.println("User created: " + name);
        return user;
    }
    
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
