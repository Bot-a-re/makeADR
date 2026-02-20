package com.adr.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 아키텍처 결정의 대안을 나타내는 모델
 */
public class Alternative {
    
    private String name;
    private String description;
    private List<String> pros;
    private List<String> cons;
    private String status;  // SELECTED, REJECTED, CONSIDERED
    private String rationale;
    
    public Alternative(String name, String description) {
        this.name = name;
        this.description = description;
        this.pros = new ArrayList<>();
        this.cons = new ArrayList<>();
        this.status = "CONSIDERED";
    }
    
    public Alternative(String name, String description, List<String> pros, List<String> cons, 
                      String status, String rationale) {
        this.name = name;
        this.description = description;
        this.pros = pros != null ? new ArrayList<>(pros) : new ArrayList<>();
        this.cons = cons != null ? new ArrayList<>(cons) : new ArrayList<>();
        this.status = status;
        this.rationale = rationale;
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getPros() {
        return pros;
    }
    
    public void addPro(String pro) {
        this.pros.add(pro);
    }
    
    public List<String> getCons() {
        return cons;
    }
    
    public void addCon(String con) {
        this.cons.add(con);
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getRationale() {
        return rationale;
    }
    
    public void setRationale(String rationale) {
        this.rationale = rationale;
    }
    
    public boolean isSelected() {
        return "SELECTED".equals(status);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(status);
    }
}
