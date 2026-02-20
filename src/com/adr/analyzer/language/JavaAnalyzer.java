package com.adr.analyzer.language;

import com.adr.analyzer.*;
import com.adr.model.AnalysisResult;

/**
 * Java 언어 분석기
 */
public class JavaAnalyzer implements LanguageAnalyzer {
    
    private final PackageAnalyzer packageAnalyzer;
    private final DependencyAnalyzer dependencyAnalyzer;
    private final FrameworkDetector frameworkDetector;
    private final PatternDetector patternDetector;
    private final DatabaseAnalyzer databaseAnalyzer;
    private final ApiAnalyzer apiAnalyzer;
    
    public JavaAnalyzer() {
        this.packageAnalyzer = new PackageAnalyzer();
        this.dependencyAnalyzer = new DependencyAnalyzer();
        this.frameworkDetector = new FrameworkDetector();
        this.patternDetector = new PatternDetector();
        this.databaseAnalyzer = new DatabaseAnalyzer();
        this.apiAnalyzer = new ApiAnalyzer();
    }
    
    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        // 패키지 분석
        packageAnalyzer.analyze(content, result);
        
        // 클래스 카운트
        int classCount = countClasses(content);
        result.setClassCount(result.getClassCount() + classCount);
        
        // 의존성 분석
        dependencyAnalyzer.analyze(content, result);
        
        // 프레임워크 감지
        frameworkDetector.detect(content, result);
        
        // 디자인 패턴 감지
        patternDetector.detect(fileName, content, result);
        
        // 데이터베이스 스키마 분석
        databaseAnalyzer.analyze(content, result);
        
        // API 엔드포인트 분석
        apiAnalyzer.analyze(content, result);
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".java"};
    }
    
    private int countClasses(String content) {
        int count = 0;
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches(".*\\b(class|interface|enum|record)\\s+\\w+.*")) {
                count++;
            }
        }
        
        return count;
    }
}
