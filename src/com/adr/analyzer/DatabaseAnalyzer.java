package com.adr.analyzer;

import com.adr.model.AnalysisResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 데이터베이스 스키마 분석 클래스
 */
public class DatabaseAnalyzer {
    
    private static final Pattern TABLE_ANNOTATION = Pattern.compile("@Table\\s*\\(\\s*name\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern ENTITY_ANNOTATION = Pattern.compile("@Entity");
    private static final Pattern CREATE_TABLE = Pattern.compile("CREATE\\s+TABLE\\s+([a-zA-Z0-9_]+)", Pattern.CASE_INSENSITIVE);
    
    public void analyze(String content, AnalysisResult result) {
        // JPA Entity 감지
        if (content.contains("@Entity")) {
            Matcher tableMatcher = TABLE_ANNOTATION.matcher(content);
            if (tableMatcher.find()) {
                String tableName = tableMatcher.group(1);
                result.addDatabaseSchema("Table: " + tableName + " (JPA Entity)");
            } else {
                // @Table 어노테이션이 없으면 클래스명에서 추출
                String className = extractClassName(content);
                if (className != null) {
                    result.addDatabaseSchema("Table: " + toSnakeCase(className) + " (JPA Entity)");
                }
            }
        }
        
        // SQL CREATE TABLE 문 감지
        Matcher createTableMatcher = CREATE_TABLE.matcher(content);
        while (createTableMatcher.find()) {
            String tableName = createTableMatcher.group(1);
            result.addDatabaseSchema("Table: " + tableName + " (SQL DDL)");
        }
    }
    
    private String extractClassName(String content) {
        Pattern classPattern = Pattern.compile("class\\s+([A-Za-z0-9_]+)");
        Matcher matcher = classPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
