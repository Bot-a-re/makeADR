package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JavaScript/TypeScript 언어 분석기
 */
public class JavaScriptAnalyzer implements LanguageAnalyzer {
    
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+.*?from\\s+['\"]([^'\"]+)['\"]");
    private static final Pattern REQUIRE_PATTERN = Pattern.compile("require\\(['\"]([^'\"]+)['\"]\\)");
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\b(class|interface|type|enum)\\s+\\w+");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\b(function|const|let|var)\\s+\\w+\\s*=\\s*(async\\s+)?\\(");
    
    private final boolean isTypeScript;
    
    public JavaScriptAnalyzer(boolean isTypeScript) {
        this.isTypeScript = isTypeScript;
    }
    
    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        // 모듈 분석 (파일 경로 기반)
        analyzeModule(fileName, result);
        
        // 클래스/타입 카운트
        result.setClassCount(result.getClassCount() + countClassesAndTypes(content));
        
        // 의존성 분석
        analyzeDependencies(fileName, content, result);
        
        // 프레임워크 감지
        detectFrameworks(content, result);
        
        // 디자인 패턴 감지
        detectPatterns(fileName, content, result);
        
        // 데이터베이스 분석
        analyzeDatabases(content, result);
        
        // API 분석
        analyzeApis(content, result);
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return isTypeScript ? new String[]{".ts", ".tsx"} : new String[]{".js", ".jsx"};
    }
    
    private void analyzeModule(String fileName, AnalysisResult result) {
        // 파일 경로를 모듈로 간주
        String moduleName = extractModuleName(fileName);
        if (moduleName != null) {
            result.addPackage(moduleName);
        }
    }
    
    private int countClassesAndTypes(String content) {
        Matcher matcher = CLASS_PATTERN.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        
        // 함수형 컴포넌트도 카운트 (React 등)
        Matcher funcMatcher = FUNCTION_PATTERN.matcher(content);
        while (funcMatcher.find()) {
            count++;
        }
        
        return count;
    }
    
    private void analyzeDependencies(String fileName, String content, AnalysisResult result) {
        String currentModule = extractModuleName(fileName);
        
        // ES6 import 분석
        Matcher importMatcher = IMPORT_PATTERN.matcher(content);
        while (importMatcher.find()) {
            String importedModule = importMatcher.group(1);
            
            // 상대 경로나 node_modules가 아닌 경우만
            if (!importedModule.startsWith(".") && !importedModule.startsWith("@") && currentModule != null) {
                DependencyInfo dep = new DependencyInfo(
                    currentModule,
                    importedModule,
                    "import"
                );
                result.addDependency(dep);
            }
        }
        
        // CommonJS require 분석
        Matcher requireMatcher = REQUIRE_PATTERN.matcher(content);
        while (requireMatcher.find()) {
            String requiredModule = requireMatcher.group(1);
            
            if (!requiredModule.startsWith(".") && !requiredModule.startsWith("@") && currentModule != null) {
                DependencyInfo dep = new DependencyInfo(
                    currentModule,
                    requiredModule,
                    "require"
                );
                result.addDependency(dep);
            }
        }
    }
    
    private void detectFrameworks(String content, AnalysisResult result) {
        // React
        if (content.contains("from 'react'") || content.contains("from \"react\"")) {
            result.addFramework("React");
        }
        if (content.contains("useState") || content.contains("useEffect")) {
            result.addFramework("React Hooks");
        }
        
        // Vue
        if (content.contains("from 'vue'") || content.contains("from \"vue\"")) {
            result.addFramework("Vue.js");
        }
        
        // Angular
        if (content.contains("@angular/core") || content.contains("@Component")) {
            result.addFramework("Angular");
        }
        
        // Express
        if (content.contains("from 'express'") || content.contains("require('express')")) {
            result.addFramework("Express.js");
        }
        
        // Next.js
        if (content.contains("from 'next") || content.contains("next/")) {
            result.addFramework("Next.js");
        }
        
        // NestJS
        if (content.contains("@nestjs/")) {
            result.addFramework("NestJS");
        }
        
        // TypeScript specific
        if (isTypeScript) {
            result.addFramework("TypeScript");
        }
        
        // Testing
        if (content.contains("from 'jest'") || content.contains("describe(") || content.contains("test(")) {
            result.addFramework("Jest");
        }
        if (content.contains("from 'mocha'")) {
            result.addFramework("Mocha");
        }
        
        // State Management
        if (content.contains("from 'redux'") || content.contains("useDispatch")) {
            result.addFramework("Redux");
        }
        if (content.contains("from 'zustand'")) {
            result.addFramework("Zustand");
        }
        
        // Database/ORM
        if (content.contains("from 'mongoose'")) {
            result.addFramework("Mongoose");
        }
        if (content.contains("from 'typeorm'")) {
            result.addFramework("TypeORM");
        }
        if (content.contains("from 'prisma'")) {
            result.addFramework("Prisma");
        }
    }
    
    private void detectPatterns(String fileName, String content, AnalysisResult result) {
        String lowerFileName = fileName.toLowerCase();
        
        // Singleton
        if (content.matches("(?s).*getInstance.*") && content.contains("static")) {
            result.addDesignPattern("Singleton", extractFileName(fileName));
        }
        
        // Factory
        if (lowerFileName.contains("factory") || content.contains("create(")) {
            result.addDesignPattern("Factory", extractFileName(fileName));
        }
        
        // Repository
        if (lowerFileName.contains("repository") || lowerFileName.contains("repo")) {
            result.addDesignPattern("Repository", extractFileName(fileName));
        }
        
        // Service
        if (lowerFileName.contains("service")) {
            result.addDesignPattern("Service Layer", extractFileName(fileName));
        }
        
        // Controller
        if (lowerFileName.contains("controller")) {
            result.addDesignPattern("MVC Controller", extractFileName(fileName));
        }
        
        // Component (React/Vue/Angular)
        if (lowerFileName.contains("component") || content.contains("@Component") || 
            content.matches("(?s).*export\\s+(default\\s+)?function\\s+\\w+.*return.*<.*>.*")) {
            result.addDesignPattern("Component", extractFileName(fileName));
        }
        
        // Hook (React)
        if (lowerFileName.startsWith("use") && (content.contains("useState") || content.contains("useEffect"))) {
            result.addDesignPattern("Custom Hook", extractFileName(fileName));
        }
        
        // Middleware
        if (lowerFileName.contains("middleware") || content.matches("(?s).*\\(req,\\s*res,\\s*next\\).*")) {
            result.addDesignPattern("Middleware", extractFileName(fileName));
        }
    }
    
    private void analyzeDatabases(String content, AnalysisResult result) {
        // Mongoose Schema
        if (content.contains("new Schema(") || content.contains("mongoose.Schema")) {
            Pattern schemaPattern = Pattern.compile("const\\s+(\\w+)Schema\\s*=");
            Matcher matcher = schemaPattern.matcher(content);
            if (matcher.find()) {
                result.addDatabaseSchema("Collection: " + matcher.group(1) + " (Mongoose)");
            }
        }
        
        // TypeORM Entity
        if (content.contains("@Entity(")) {
            Pattern entityPattern = Pattern.compile("@Entity\\(['\"]([^'\"]+)['\"]\\)");
            Matcher matcher = entityPattern.matcher(content);
            if (matcher.find()) {
                result.addDatabaseSchema("Table: " + matcher.group(1) + " (TypeORM)");
            }
        }
        
        // Prisma Model (TypeScript)
        if (content.contains("PrismaClient")) {
            result.addDatabaseSchema("Database: Prisma Client");
        }
    }
    
    private void analyzeApis(String content, AnalysisResult result) {
        // Express.js routes
        Pattern expressPattern = Pattern.compile("(app|router)\\.(get|post|put|delete|patch)\\(['\"]([^'\"]+)['\"]");
        Matcher matcher = expressPattern.matcher(content);
        
        while (matcher.find()) {
            String method = matcher.group(2).toUpperCase();
            String path = matcher.group(3);
            result.addApiEndpoint(String.format("%s %s (Express)", method, path));
        }
        
        // NestJS decorators
        Pattern nestPattern = Pattern.compile("@(Get|Post|Put|Delete|Patch)\\(['\"]([^'\"]+)['\"]\\)");
        Matcher nestMatcher = nestPattern.matcher(content);
        
        while (nestMatcher.find()) {
            String method = nestMatcher.group(1).toUpperCase();
            String path = nestMatcher.group(2);
            result.addApiEndpoint(String.format("%s %s (NestJS)", method, path));
        }
        
        // Next.js API routes (파일 기반)
        if (content.contains("export default") && content.contains("req") && content.contains("res")) {
            result.addApiEndpoint("API Route (Next.js)");
        }
    }
    
    // Helper methods
    
    private String extractModuleName(String fileName) {
        // 파일 경로에서 모듈명 추출
        String[] parts = fileName.replace("\\", "/").split("/");
        if (parts.length > 1) {
            return parts[parts.length - 2]; // 상위 디렉토리명을 모듈로 간주
        }
        return "root";
    }
    
    private String extractFileName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        String name = parts[parts.length - 1];
        return name.replaceAll("\\.(js|jsx|ts|tsx)$", "");
    }
}
