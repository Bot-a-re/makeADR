package com.adr.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ADR-E의 5W1H 구조화된 설명을 위한 모델
 */
public class DecisionContext {
    
    // Why
    private String problem;
    private String motivation;
    private List<String> goals;
    
    // What
    private String decisionStatement;
    private String scope;
    
    // What-if
    private List<String> tradeoffs;
    private List<String> expectedOutcomes;
    private List<String> risks;
    
    // Who
    private String decisionMaker;
    private List<String> affectedTeams;
    private String owner;
    
    // Where
    private String applicationScope;
    private String boundaries;
    
    // When
    private String validityPeriod;
    private String reviewSchedule;
    private List<String> dependencies;
    
    public DecisionContext() {
        this.goals = new ArrayList<>();
        this.tradeoffs = new ArrayList<>();
        this.expectedOutcomes = new ArrayList<>();
        this.risks = new ArrayList<>();
        this.affectedTeams = new ArrayList<>();
        this.dependencies = new ArrayList<>();
    }
    
    // Getters and Setters
    
    public String getProblem() {
        return problem;
    }
    
    public void setProblem(String problem) {
        this.problem = problem;
    }
    
    public String getMotivation() {
        return motivation;
    }
    
    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }
    
    public List<String> getGoals() {
        return goals;
    }
    
    public void addGoal(String goal) {
        this.goals.add(goal);
    }
    
    public String getDecisionStatement() {
        return decisionStatement;
    }
    
    public void setDecisionStatement(String decisionStatement) {
        this.decisionStatement = decisionStatement;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public List<String> getTradeoffs() {
        return tradeoffs;
    }
    
    public void addTradeoff(String tradeoff) {
        this.tradeoffs.add(tradeoff);
    }
    
    public List<String> getExpectedOutcomes() {
        return expectedOutcomes;
    }
    
    public void addExpectedOutcome(String outcome) {
        this.expectedOutcomes.add(outcome);
    }
    
    public List<String> getRisks() {
        return risks;
    }
    
    public void addRisk(String risk) {
        this.risks.add(risk);
    }
    
    public String getDecisionMaker() {
        return decisionMaker;
    }
    
    public void setDecisionMaker(String decisionMaker) {
        this.decisionMaker = decisionMaker;
    }
    
    public List<String> getAffectedTeams() {
        return affectedTeams;
    }
    
    public void addAffectedTeam(String team) {
        this.affectedTeams.add(team);
    }
    
    public String getOwner() {
        return owner;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public String getApplicationScope() {
        return applicationScope;
    }
    
    public void setApplicationScope(String applicationScope) {
        this.applicationScope = applicationScope;
    }
    
    public String getBoundaries() {
        return boundaries;
    }
    
    public void setBoundaries(String boundaries) {
        this.boundaries = boundaries;
    }
    
    public String getValidityPeriod() {
        return validityPeriod;
    }
    
    public void setValidityPeriod(String validityPeriod) {
        this.validityPeriod = validityPeriod;
    }
    
    public String getReviewSchedule() {
        return reviewSchedule;
    }
    
    public void setReviewSchedule(String reviewSchedule) {
        this.reviewSchedule = reviewSchedule;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public void addDependency(String dependency) {
        this.dependencies.add(dependency);
    }
}
