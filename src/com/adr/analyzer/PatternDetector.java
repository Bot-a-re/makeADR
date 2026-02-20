package com.adr.analyzer;

import com.adr.model.AnalysisResult;

/**
 * 디자인 패턴 감지 클래스
 */
public class PatternDetector {
    
    public void detect(String fileName, String content, AnalysisResult result) {
        String lowerFileName = fileName.toLowerCase();
        String lowerContent = content.toLowerCase();
        
        // Singleton Pattern
        if (content.contains("private static") && 
            content.contains("getInstance()")) {
            result.addDesignPattern("Singleton", extractClassName(fileName));
        }
        
        // Factory Pattern
        if (lowerFileName.contains("factory") || 
            content.contains("createInstance") ||
            content.contains("create(")) {
            result.addDesignPattern("Factory", extractClassName(fileName));
        }
        
        // Builder Pattern
        if (lowerFileName.contains("builder") ||
            content.contains("public Builder") ||
            content.contains(".builder()")) {
            result.addDesignPattern("Builder", extractClassName(fileName));
        }
        
        // Observer Pattern
        if (lowerFileName.contains("listener") ||
            lowerFileName.contains("observer") ||
            content.contains("addListener") ||
            content.contains("addObserver")) {
            result.addDesignPattern("Observer", extractClassName(fileName));
        }
        
        // Strategy Pattern
        if (lowerFileName.contains("strategy") ||
            (content.contains("interface") && lowerContent.contains("execute"))) {
            result.addDesignPattern("Strategy", extractClassName(fileName));
        }
        
        // Adapter Pattern
        if (lowerFileName.contains("adapter")) {
            result.addDesignPattern("Adapter", extractClassName(fileName));
        }
        
        // Decorator Pattern
        if (lowerFileName.contains("decorator")) {
            result.addDesignPattern("Decorator", extractClassName(fileName));
        }
        
        // Repository Pattern
        if (lowerFileName.contains("repository") ||
            content.contains("@Repository")) {
            result.addDesignPattern("Repository", extractClassName(fileName));
        }
        
        // Service Pattern
        if (lowerFileName.contains("service") ||
            content.contains("@Service")) {
            result.addDesignPattern("Service Layer", extractClassName(fileName));
        }
        
        // DTO Pattern
        if (lowerFileName.contains("dto") ||
            lowerFileName.contains("vo") ||
            (content.contains("class") && isDataClass(content))) {
            result.addDesignPattern("DTO/VO", extractClassName(fileName));
        }
    }
    
    private String extractClassName(String fileName) {
        return fileName.replace(".java", "");
    }
    
    private boolean isDataClass(String content) {
        // 간단한 휴리스틱: getter/setter가 많고 비즈니스 로직이 적은 경우
        int getterCount = countOccurrences(content, "get");
        int setterCount = countOccurrences(content, "set");
        
        return (getterCount + setterCount) > 3;
    }
    
    private int countOccurrences(String content, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}
