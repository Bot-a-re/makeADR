package com.adr.model;

/**
 * 모듈 정보를 담는 클래스
 */
public class ModuleInfo {
    
    private String name;
    private String packageName;
    private int classCount;
    private String description;
    
    public ModuleInfo(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
        this.classCount = 0;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public int getClassCount() {
        return classCount;
    }
    
    public void setClassCount(int classCount) {
        this.classCount = classCount;
    }
    
    public void incrementClassCount() {
        this.classCount++;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
