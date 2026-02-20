package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * C# 언어 분석기
 */
public class CSharpAnalyzer implements LanguageAnalyzer {
    
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^\\s*namespace\\s+([a-zA-Z0-9_.]+)");
    private static final Pattern USING_PATTERN = Pattern.compile("^\\s*using\\s+([a-zA-Z0-9_.]+)\\s*;");
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\b(class|interface|struct|enum|record)\\s+\\w+");
    
    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        // 네임스페이스 분석
        analyzeNamespace(content, result);
        
        // 클래스 카운트
        result.setClassCount(result.getClassCount() + countClasses(content));
        
        // 의존성 분석
        analyzeDependencies(content, result);
        
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
        return new String[]{".cs"};
    }
    
    private void analyzeNamespace(String content, AnalysisResult result) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            Matcher matcher = NAMESPACE_PATTERN.matcher(line);
            if (matcher.find()) {
                result.addPackage(matcher.group(1));
                break;
            }
        }
    }
    
    private int countClasses(String content) {
        Matcher matcher = CLASS_PATTERN.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    
    private void analyzeDependencies(String content, AnalysisResult result) {
        String currentNamespace = extractNamespace(content);
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            Matcher matcher = USING_PATTERN.matcher(line);
            if (matcher.find()) {
                String usedNamespace = matcher.group(1);
                
                // System 네임스페이스는 제외
                if (!usedNamespace.startsWith("System") && currentNamespace != null) {
                    DependencyInfo dep = new DependencyInfo(
                        currentNamespace,
                        usedNamespace,
                        "using"
                    );
                    result.addDependency(dep);
                }
            }
        }
    }
    
    private void detectFrameworks(String content, AnalysisResult result) {
        // ASP.NET Core
        if (content.contains("Microsoft.AspNetCore")) {
            result.addFramework("ASP.NET Core");
        }
        if (content.contains("[ApiController]") || content.contains("[Route")) {
            result.addFramework("ASP.NET Core Web API");
        }
        
        // Entity Framework
        if (content.contains("Microsoft.EntityFrameworkCore") || content.contains("DbContext")) {
            result.addFramework("Entity Framework Core");
        }
        
        // .NET Framework
        if (content.contains("System.Web")) {
            result.addFramework(".NET Framework");
        }
        
        // Dependency Injection
        if (content.contains("IServiceCollection") || content.contains("[Inject]")) {
            result.addFramework("Dependency Injection");
        }
        
        // Testing
        if (content.contains("using Xunit") || content.contains("[Fact]")) {
            result.addFramework("xUnit");
        }
        if (content.contains("using NUnit") || content.contains("[Test]")) {
            result.addFramework("NUnit");
        }
        if (content.contains("using Moq")) {
            result.addFramework("Moq");
        }
        
        // Logging
        if (content.contains("ILogger")) {
            result.addFramework("Microsoft.Extensions.Logging");
        }
        
        // JSON
        if (content.contains("Newtonsoft.Json")) {
            result.addFramework("Json.NET (Newtonsoft)");
        }
        if (content.contains("System.Text.Json")) {
            result.addFramework("System.Text.Json");
        }
    }
    
    private void detectPatterns(String fileName, String content, AnalysisResult result) {
        String lowerFileName = fileName.toLowerCase();
        String lowerContent = content.toLowerCase();
        
        // Singleton
        if (content.contains("private static") && content.matches("(?s).*\\bInstance\\b.*")) {
            result.addDesignPattern("Singleton", extractClassName(fileName));
        }
        
        // Factory
        if (lowerFileName.contains("factory") || content.contains("Create(")) {
            result.addDesignPattern("Factory", extractClassName(fileName));
        }
        
        // Builder
        if (lowerFileName.contains("builder")) {
            result.addDesignPattern("Builder", extractClassName(fileName));
        }
        
        // Repository
        if (lowerFileName.contains("repository") || content.contains("IRepository")) {
            result.addDesignPattern("Repository", extractClassName(fileName));
        }
        
        // Service
        if (lowerFileName.contains("service") || content.contains("IService")) {
            result.addDesignPattern("Service Layer", extractClassName(fileName));
        }
        
        // DTO
        if (lowerFileName.contains("dto") || lowerFileName.contains("model")) {
            result.addDesignPattern("DTO/VO", extractClassName(fileName));
        }
        
        // Controller
        if (lowerFileName.contains("controller") || content.contains("[ApiController]")) {
            result.addDesignPattern("MVC Controller", extractClassName(fileName));
        }
    }
    
    private void analyzeDatabases(String content, AnalysisResult result) {
        // Entity Framework Entity
        if (content.contains("DbSet<") || content.contains("[Table(")) {
            Pattern tablePattern = Pattern.compile("\\[Table\\(\"([^\"]+)\"\\)");
            Matcher matcher = tablePattern.matcher(content);
            if (matcher.find()) {
                result.addDatabaseSchema("Table: " + matcher.group(1) + " (EF Core Entity)");
            } else {
                String className = extractClassNameFromContent(content);
                if (className != null) {
                    result.addDatabaseSchema("Table: " + className + " (EF Core Entity)");
                }
            }
        }
    }
    
    private void analyzeApis(String content, AnalysisResult result) {
        // ASP.NET Core API Controllers
        Pattern routePattern = Pattern.compile("\\[(HttpGet|HttpPost|HttpPut|HttpDelete|HttpPatch)(?:\\(\"([^\"]+)\"\\))?\\]");
        Matcher matcher = routePattern.matcher(content);
        
        String classRoute = extractClassRoute(content);
        
        while (matcher.find()) {
            String method = matcher.group(1).replace("Http", "").toUpperCase();
            String path = matcher.group(2) != null ? matcher.group(2) : "";
            String fullPath = classRoute != null ? classRoute + "/" + path : path;
            
            String methodName = extractMethodNameNear(content, matcher.start());
            result.addApiEndpoint(String.format("%s /%s (%s)", method, fullPath, methodName));
        }
    }
    
    // Helper methods
    
    private String extractNamespace(String content) {
        Matcher matcher = NAMESPACE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String extractClassName(String fileName) {
        return fileName.replace(".cs", "");
    }
    
    private String extractClassNameFromContent(String content) {
        Pattern pattern = Pattern.compile("class\\s+([A-Za-z0-9_]+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String extractClassRoute(String content) {
        Pattern pattern = Pattern.compile("\\[Route\\(\"([^\"]+)\"\\)\\]");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    
    private String extractMethodNameNear(String content, int position) {
        String afterAnnotation = content.substring(position);
        Pattern methodPattern = Pattern.compile("(public|private|protected)?\\s+\\w+\\s+(\\w+)\\s*\\(");
        Matcher matcher = methodPattern.matcher(afterAnnotation);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "unknown";
    }
}
