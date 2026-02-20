package com.adr.analyzer;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 의존성 분석 클래스
 */
public class DependencyAnalyzer {
    
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+([a-zA-Z0-9_.]+)\\s*;");
    
    public void analyze(String content, AnalysisResult result) {
        String[] lines = content.split("\n");
        String currentPackage = extractPackage(content);
        
        for (String line : lines) {
            Matcher matcher = IMPORT_PATTERN.matcher(line);
            if (matcher.find()) {
                String importedClass = matcher.group(1);
                
                // java.* 패키지는 제외
                if (!importedClass.startsWith("java.") && !importedClass.startsWith("javax.")) {
                    String importedPackage = extractPackageFromClass(importedClass);
                    
                    if (currentPackage != null && !currentPackage.equals(importedPackage)) {
                        DependencyInfo dep = new DependencyInfo(
                            currentPackage, 
                            importedPackage, 
                            "import"
                        );
                        result.addDependency(dep);
                    }
                }
            }
        }
    }
    
    private String extractPackage(String content) {
        Pattern pattern = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String extractPackageFromClass(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        if (lastDot > 0) {
            return fullClassName.substring(0, lastDot);
        }
        return fullClassName;
    }
}
