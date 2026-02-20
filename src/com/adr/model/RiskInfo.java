package com.adr.model;

/**
 * 아키텍처 위험 요소 정보를 담는 클래스
 */
public class RiskInfo {
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    private String title;
    private String description;
    private Severity severity;
    private String recommendation;
    private String location;
    
    public RiskInfo(String title, String description, Severity severity) {
        this.title = title;
        this.description = description;
        this.severity = severity;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getSeverityIcon() {
        return switch (severity) {
            case CRITICAL -> "🔴";
            case HIGH -> "🟠";
            case MEDIUM -> "🟡";
            case LOW -> "🟢";
        };
    }
}
