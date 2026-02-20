package com.example.demo.repository;

import com.example.demo.model.User;
import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 데이터 접근 계층
 * Repository 패턴 적용
 */
public class UserRepository {
    
    private Map<String, User> database;
    
    public UserRepository() {
        this.database = new HashMap<>();
    }
    
    public void save(User user) {
        database.put(user.getEmail(), user);
    }
    
    public User findByEmail(String email) {
        return database.get(email);
    }
    
    public void delete(String email) {
        database.remove(email);
    }
}
