package com.adr.analyzer;

import com.adr.model.AnalysisResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API 엔드포인트 분석 클래스
 */
public class ApiAnalyzer {
    
    private static final Pattern REQUEST_MAPPING = Pattern.compile("@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\\s*\\(\\s*[\"']([^\"']+)[\"']");
    private static final Pattern PATH_VARIABLE = Pattern.compile("@PathVariable");
    
    public void analyze(String content, AnalysisResult result) {
        String[] lines = content.split("\n");
        String classLevelPath = extractClassLevelPath(content);
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            Matcher matcher = REQUEST_MAPPING.matcher(line);
            if (matcher.find()) {
                String method = matcher.group(1).replace("Mapping", "").toUpperCase();
                if (method.equals("REQUEST")) {
                    method = extractHttpMethod(lines, i);
                }
                String path = matcher.group(2);
                
                // 클래스 레벨 경로와 결합
                String fullPath = classLevelPath != null ? classLevelPath + path : path;
                
                // 메서드명 추출
                String methodName = extractMethodName(lines, i);
                
                result.addApiEndpoint(String.format("%s %s (%s)", method, fullPath, methodName));
            }
        }
    }
    
    private String extractClassLevelPath(String content) {
        Pattern classMapping = Pattern.compile("@RequestMapping\\s*\\(\\s*[\"']([^\"']+)[\"']");
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            if (line.contains("class")) {
                break;
            }
            Matcher matcher = classMapping.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "";
    }
    
    private String extractHttpMethod(String[] lines, int currentLine) {
        // @RequestMapping의 method 속성 찾기
        for (int i = currentLine; i < Math.min(currentLine + 3, lines.length); i++) {
            if (lines[i].contains("RequestMethod.GET")) return "GET";
            if (lines[i].contains("RequestMethod.POST")) return "POST";
            if (lines[i].contains("RequestMethod.PUT")) return "PUT";
            if (lines[i].contains("RequestMethod.DELETE")) return "DELETE";
            if (lines[i].contains("RequestMethod.PATCH")) return "PATCH";
        }
        return "GET"; // 기본값
    }
    
    private String extractMethodName(String[] lines, int currentLine) {
        // 어노테이션 다음 줄에서 메서드명 찾기
        for (int i = currentLine + 1; i < Math.min(currentLine + 5, lines.length); i++) {
            String line = lines[i].trim();
            Pattern methodPattern = Pattern.compile("(public|private|protected)?\\s*\\w+\\s+(\\w+)\\s*\\(");
            Matcher matcher = methodPattern.matcher(line);
            if (matcher.find()) {
                return matcher.group(2);
            }
        }
        return "unknown";
    }
}
