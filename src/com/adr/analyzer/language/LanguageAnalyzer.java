package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;

/**
 * 언어별 분석기 인터페이스
 */
public interface LanguageAnalyzer {
    
    /**
     * 소스코드 파일을 분석하여 결과에 추가
     */
    void analyzeFile(String fileName, String content, AnalysisResult result);
    
    /**
     * 이 분석기가 지원하는 파일 확장자
     */
    String[] getSupportedExtensions();
}
