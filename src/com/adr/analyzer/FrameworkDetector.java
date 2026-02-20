package com.adr.analyzer;

import com.adr.model.AnalysisResult;
import java.util.HashMap;
import java.util.Map;

/**
 * 프레임워크 및 라이브러리 감지 클래스
 */
public class FrameworkDetector {
    
    private static final Map<String, String> FRAMEWORK_PATTERNS = new HashMap<>();
    
    static {
        // Spring Framework
        FRAMEWORK_PATTERNS.put("org.springframework", "Spring Framework");
        FRAMEWORK_PATTERNS.put("@SpringBootApplication", "Spring Boot");
        FRAMEWORK_PATTERNS.put("@RestController", "Spring MVC");
        FRAMEWORK_PATTERNS.put("@Service", "Spring Service");
        FRAMEWORK_PATTERNS.put("@Repository", "Spring Data");
        
        // Jakarta EE / Java EE
        FRAMEWORK_PATTERNS.put("jakarta.servlet", "Jakarta Servlet");
        FRAMEWORK_PATTERNS.put("jakarta.persistence", "Jakarta Persistence (JPA)");
        FRAMEWORK_PATTERNS.put("javax.servlet", "Java Servlet");
        FRAMEWORK_PATTERNS.put("javax.persistence", "Java Persistence (JPA)");
        
        // Hibernate
        FRAMEWORK_PATTERNS.put("org.hibernate", "Hibernate");
        
        // Logging
        FRAMEWORK_PATTERNS.put("org.slf4j", "SLF4J");
        FRAMEWORK_PATTERNS.put("org.apache.logging.log4j", "Log4j");
        FRAMEWORK_PATTERNS.put("java.util.logging", "Java Util Logging");
        
        // Testing
        FRAMEWORK_PATTERNS.put("org.junit", "JUnit");
        FRAMEWORK_PATTERNS.put("org.testng", "TestNG");
        FRAMEWORK_PATTERNS.put("org.mockito", "Mockito");
        
        // JSON
        FRAMEWORK_PATTERNS.put("com.fasterxml.jackson", "Jackson");
        FRAMEWORK_PATTERNS.put("com.google.gson", "Gson");
        
        // Database
        FRAMEWORK_PATTERNS.put("java.sql", "JDBC");
        
        // Apache Commons
        FRAMEWORK_PATTERNS.put("org.apache.commons", "Apache Commons");
    }
    
    public void detect(String content, AnalysisResult result) {
        for (Map.Entry<String, String> entry : FRAMEWORK_PATTERNS.entrySet()) {
            if (content.contains(entry.getKey())) {
                result.addFramework(entry.getValue());
            }
        }
    }
}
