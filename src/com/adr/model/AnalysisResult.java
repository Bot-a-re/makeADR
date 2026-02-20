package com.adr.model;

import java.util.*;

/**
 * 소스코드 분석 결과를 담는 모델 클래스
 */
public class AnalysisResult {
    
    private String projectName;
    private int javaFileCount;
    private int classCount;
    private Set<String> packages;
    private List<ModuleInfo> modules;
    private List<DependencyInfo> dependencies;
    private Map<String, Integer> frameworkUsage;
    private Map<String, List<String>> designPatterns;
    private List<String> databaseSchemas;
    private List<String> apiEndpoints;
    private List<RiskInfo> risks;
    private Map<Language, Integer> languageFileCount;  // 언어별 파일 수
    
    // ADR-E 관련 필드
    private DecisionContext decisionContext;
    private List<Alternative> alternatives;
    
    public AnalysisResult() {
        this.packages = new HashSet<>();
        this.modules = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.frameworkUsage = new HashMap<>();
        this.designPatterns = new HashMap<>();
        this.databaseSchemas = new ArrayList<>();
        this.apiEndpoints = new ArrayList<>();
        this.risks = new ArrayList<>();
        this.languageFileCount = new HashMap<>();
        this.alternatives = new ArrayList<>();
    }
    
    // Getters and Setters
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public int getJavaFileCount() {
        return javaFileCount;
    }
    
    public void setJavaFileCount(int javaFileCount) {
        this.javaFileCount = javaFileCount;
    }
    
    public int getClassCount() {
        return classCount;
    }
    
    public void setClassCount(int classCount) {
        this.classCount = classCount;
    }
    
    public Set<String> getPackages() {
        return packages;
    }
    
    public int getPackageCount() {
        return packages.size();
    }
    
    public void addPackage(String packageName) {
        this.packages.add(packageName);
    }
    
    public List<ModuleInfo> getModules() {
        return modules;
    }
    
    public void addModule(ModuleInfo module) {
        this.modules.add(module);
    }
    
    public List<DependencyInfo> getDependencies() {
        return dependencies;
    }
    
    public void addDependency(DependencyInfo dependency) {
        this.dependencies.add(dependency);
    }
    
    public Map<String, Integer> getFrameworkUsage() {
        return frameworkUsage;
    }
    
    public void addFramework(String framework) {
        frameworkUsage.put(framework, frameworkUsage.getOrDefault(framework, 0) + 1);
    }
    
    public Map<String, List<String>> getDesignPatterns() {
        return designPatterns;
    }
    
    public void addDesignPattern(String pattern, String className) {
        designPatterns.computeIfAbsent(pattern, k -> new ArrayList<>()).add(className);
    }
    
    public List<String> getDatabaseSchemas() {
        return databaseSchemas;
    }
    
    public void addDatabaseSchema(String schema) {
        this.databaseSchemas.add(schema);
    }
    
    public List<String> getApiEndpoints() {
        return apiEndpoints;
    }
    
    public void addApiEndpoint(String endpoint) {
        this.apiEndpoints.add(endpoint);
    }
    
    public List<RiskInfo> getRisks() {
        return risks;
    }
    
    public void addRisk(RiskInfo risk) {
        this.risks.add(risk);
    }
    
    public Map<Language, Integer> getLanguageFileCount() {
        return languageFileCount;
    }
    
    public void addLanguageFile(Language language) {
        languageFileCount.put(language, languageFileCount.getOrDefault(language, 0) + 1);
    }
    
    public int getTotalFileCount() {
        return languageFileCount.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    // ADR-E 관련 메서드
    
    public DecisionContext getDecisionContext() {
        return decisionContext;
    }
    
    public void setDecisionContext(DecisionContext decisionContext) {
        this.decisionContext = decisionContext;
    }
    
    public List<Alternative> getAlternatives() {
        return alternatives;
    }
    
    public void addAlternative(Alternative alternative) {
        this.alternatives.add(alternative);
    }
    
    public Alternative getSelectedAlternative() {
        return alternatives.stream()
            .filter(Alternative::isSelected)
            .findFirst()
            .orElse(null);
    }
    
    public List<Alternative> getRejectedAlternatives() {
        return alternatives.stream()
            .filter(Alternative::isRejected)
            .toList();
    }
}

