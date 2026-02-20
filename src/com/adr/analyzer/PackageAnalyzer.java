package com.adr.analyzer;

import com.adr.model.AnalysisResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 패키지 구조 분석 클래스
 */
public class PackageAnalyzer {
    
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;");
    
    public void analyze(String content, AnalysisResult result) {
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            Matcher matcher = PACKAGE_PATTERN.matcher(line);
            if (matcher.find()) {
                String packageName = matcher.group(1);
                result.addPackage(packageName);
                break;
            }
        }
    }
}
