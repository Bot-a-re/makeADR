package com.adr.model;

/**
 * 의존성 정보를 담는 클래스
 */
public class DependencyInfo {
    
    private String from;
    private String to;
    private String type;
    
    public DependencyInfo(String from, String to, String type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }
    
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
