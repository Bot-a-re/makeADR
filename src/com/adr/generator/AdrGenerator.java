package com.adr.generator;

import com.adr.model.AnalysisResult;
import com.adr.model.ModuleInfo;
import com.adr.model.DependencyInfo;
import com.adr.model.RiskInfo;
import com.adr.model.Language;
import com.adr.model.DecisionContext;
import com.adr.model.Alternative;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * ADR 문서 생성 클래스
 */
public class AdrGenerator {
    
    private final MermaidGenerator mermaidGenerator;
    private final RiskAnalyzer riskAnalyzer;
    private final DecisionContextGenerator contextGenerator;
    private final AlternativeGenerator alternativeGenerator;
    
    public AdrGenerator() {
        this.mermaidGenerator = new MermaidGenerator();
        this.riskAnalyzer = new RiskAnalyzer();
        this.contextGenerator = new DecisionContextGenerator();
        this.alternativeGenerator = new AlternativeGenerator();
    }
    
    public void generate(AnalysisResult result, Path outputPath) throws IOException {
        // ADR-E: DecisionContext 생성
        DecisionContext context = contextGenerator.generate(result);
        result.setDecisionContext(context);
        
        // ADR-E: 대안 생성
        List<Alternative> alternatives = alternativeGenerator.generateAlternatives(result);
        for (Alternative alt : alternatives) {
            result.addAlternative(alt);
        }
        
        // 위험 요소 분석
        riskAnalyzer.analyzeRisks(result);
        
        // ADR 문서 생성
        StringBuilder adr = new StringBuilder();
        
        // 헤더
        appendHeader(adr, result);
        
        // 1. 개요
        appendOverview(adr, result);
        
        // 2. ADR-E: 핵심 의사결정 (새로 추가)
        appendDecisionStatement(adr, result);
        
        // 3. ADR-E: 고려된 대안 (새로 추가)
        appendAlternatives(adr, result);
        
        // 4. ADR-E: 5W1H 구조화된 설명 (새로 추가)
        appendFiveWOneH(adr, result);
        
        // 5. 아키텍처 결정사항
        appendArchitectureDecisions(adr, result);
        
        // 6. 모듈 구조
        appendModuleStructure(adr, result);
        
        // 7. 기술 스택
        appendTechnologyStack(adr, result);
        
        // 8. 디자인 패턴
        appendDesignPatterns(adr, result);
        
        // 9. 데이터베이스 설계
        appendDatabaseDesign(adr, result);
        
        // 10. API 설계
        appendApiDesign(adr, result);
        
        // 11. 아키텍처 다이어그램
        appendArchitectureDiagrams(adr, result);
        
        // 12. 위험 요소 및 권장사항
        appendRisksAndRecommendations(adr, result);
        
        // 13. 결론
        appendConclusion(adr, result);
        
        // 파일 저장
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, adr.toString());
    }
    
    private void appendHeader(StringBuilder adr, AnalysisResult result) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        adr.append("# Architecture Decision Record (ADR)\n\n");
        adr.append("## 프로젝트: ").append(result.getProjectName()).append("\n\n");
        adr.append("**생성일시:** ").append(timestamp).append("\n\n");
        adr.append("**분석 도구:** ADR Generator v1.0\n\n");
        adr.append("---\n\n");
    }
    
    private void appendOverview(StringBuilder adr, AnalysisResult result) {
        adr.append("## 1. 📋 개요\n\n");
        adr.append("본 문서는 소스코드 분석을 통해 자동으로 생성된 Architecture Decision Record입니다.\n\n");
        
        // 언어별 파일 통계
        Map<Language, Integer> langCount = result.getLanguageFileCount();
        if (!langCount.isEmpty()) {
            adr.append("### 프로젝트 언어 구성\n\n");
            adr.append("| 언어 | 파일 수 | 비율 |\n");
            adr.append("|------|---------|------|\n");
            
            int total = result.getTotalFileCount();
            for (Map.Entry<Language, Integer> entry : langCount.entrySet()) {
                int count = entry.getValue();
                double percentage = (count * 100.0) / total;
                adr.append(String.format("| %s | %d | %.1f%% |\n", 
                    entry.getKey().getDisplayName(), count, percentage));
            }
            adr.append("\n");
        }
        
        adr.append("### 프로젝트 통계\n\n");
        adr.append("| 항목 | 수량 |\n");
        adr.append("|------|------|\n");
        adr.append("| 총 소스 파일 | ").append(result.getTotalFileCount()).append(" |\n");
        adr.append("| 클래스/인터페이스/타입 | ").append(result.getClassCount()).append(" |\n");
        adr.append("| 패키지/네임스페이스/모듈 | ").append(result.getPackageCount()).append(" |\n");
        adr.append("| 논리적 모듈 | ").append(result.getModules().size()).append(" |\n");
        adr.append("| API 엔드포인트 | ").append(result.getApiEndpoints().size()).append(" |\n");
        adr.append("| 데이터베이스 테이블 | ").append(result.getDatabaseSchemas().size()).append(" |\n");
        adr.append("\n");
    }
    
    // ADR-E 섹션들
    
    private void appendDecisionStatement(StringBuilder adr, AnalysisResult result) {
        adr.append("## 2. 🎯 핵심 의사결정 (ADR-E)\n\n");
        
        DecisionContext context = result.getDecisionContext();
        if (context != null && context.getDecisionStatement() != null) {
            adr.append("### 의사결정\n\n");
            adr.append("**결정:** ").append(context.getDecisionStatement()).append("\n\n");
            
            if (context.getMotivation() != null) {
                adr.append("**근거:** ").append(context.getMotivation()).append("\n\n");
            }
            
            adr.append("**영향받는 이해관계자:**\n");
            for (String team : context.getAffectedTeams()) {
                adr.append("- ").append(team).append("\n");
            }
            adr.append("\n");
        } else {
            adr.append("핵심 의사결정이 자동으로 식별되지 않았습니다.\n\n");
        }
    }
    
    private void appendAlternatives(StringBuilder adr, AnalysisResult result) {
        adr.append("## 3. 🔄 고려된 대안 (ADR-E)\n\n");
        
        List<Alternative> alternatives = result.getAlternatives();
        if (alternatives.isEmpty()) {
            adr.append("대안 분석이 생성되지 않았습니다.\n\n");
            return;
        }
        
        // 선택된 대안
        Alternative selected = result.getSelectedAlternative();
        if (selected != null) {
            adr.append("### ✅ 선택된 옵션: ").append(selected.getName()).append("\n\n");
            adr.append("**설명:** ").append(selected.getDescription()).append("\n\n");
            
            if (!selected.getPros().isEmpty()) {
                adr.append("**장점:**\n");
                for (String pro : selected.getPros()) {
                    adr.append("- ").append(pro).append("\n");
                }
                adr.append("\n");
            }
            
            if (!selected.getCons().isEmpty()) {
                adr.append("**단점:**\n");
                for (String con : selected.getCons()) {
                    adr.append("- ").append(con).append("\n");
                }
                adr.append("\n");
            }
            
            if (selected.getRationale() != null) {
                adr.append("**선택 이유:** ").append(selected.getRationale()).append("\n\n");
            }
        }
        
        // 거부된 대안들
        List<Alternative> rejected = result.getRejectedAlternatives();
        if (!rejected.isEmpty()) {
            adr.append("### ❌ 거부된 대안들\n\n");
            
            for (Alternative alt : rejected) {
                adr.append("#### ").append(alt.getName()).append("\n\n");
                adr.append("**설명:** ").append(alt.getDescription()).append("\n\n");
                
                if (!alt.getPros().isEmpty()) {
                    adr.append("**장점:**\n");
                    for (String pro : alt.getPros()) {
                        adr.append("- ").append(pro).append("\n");
                    }
                    adr.append("\n");
                }
                
                if (!alt.getCons().isEmpty()) {
                    adr.append("**단점:**\n");
                    for (String con : alt.getCons()) {
                        adr.append("- ").append(con).append("\n");
                    }
                    adr.append("\n");
                }
                
                if (alt.getRationale() != null) {
                    adr.append("**거부 이유:** ").append(alt.getRationale()).append("\n\n");
                }
            }
        }
        
        // 고려 중인 대안들
        List<Alternative> considered = alternatives.stream()
            .filter(a -> "CONSIDERED".equals(a.getStatus()))
            .toList();
        
        if (!considered.isEmpty()) {
            adr.append("### 🤔 고려 중인 대안들\n\n");
            
            for (Alternative alt : considered) {
                adr.append("#### ").append(alt.getName()).append("\n\n");
                adr.append("**설명:** ").append(alt.getDescription()).append("\n\n");
                
                if (!alt.getPros().isEmpty()) {
                    adr.append("**장점:**\n");
                    for (String pro : alt.getPros()) {
                        adr.append("- ").append(pro).append("\n");
                    }
                    adr.append("\n");
                }
                
                if (!alt.getCons().isEmpty()) {
                    adr.append("**단점:**\n");
                    for (String con : alt.getCons()) {
                        adr.append("- ").append(con).append("\n");
                    }
                    adr.append("\n");
                }
            }
        }
    }
    
    private void appendFiveWOneH(StringBuilder adr, AnalysisResult result) {
        adr.append("## 4. 📝 구조화된 설명 - 5W1H (ADR-E)\n\n");
        
        DecisionContext context = result.getDecisionContext();
        if (context == null) {
            adr.append("구조화된 설명이 생성되지 않았습니다.\n\n");
            return;
        }
        
        // Why
        adr.append("### 🤔 Why (왜)\n\n");
        if (context.getProblem() != null) {
            adr.append("**문제:** ").append(context.getProblem()).append("\n\n");
        }
        if (context.getMotivation() != null) {
            adr.append("**동기:** ").append(context.getMotivation()).append("\n\n");
        }
        if (!context.getGoals().isEmpty()) {
            adr.append("**목표:**\n");
            for (String goal : context.getGoals()) {
                adr.append("- ").append(goal).append("\n");
            }
            adr.append("\n");
        }
        
        // What
        adr.append("### 📋 What (무엇을)\n\n");
        if (context.getDecisionStatement() != null) {
            adr.append("**결정 내용:** ").append(context.getDecisionStatement()).append("\n\n");
        }
        if (context.getScope() != null) {
            adr.append("**범위:** ").append(context.getScope()).append("\n\n");
        }
        
        // What-if
        adr.append("### ⚖️ What-if (만약)\n\n");
        if (!context.getTradeoffs().isEmpty()) {
            adr.append("**트레이드오프:**\n");
            for (String tradeoff : context.getTradeoffs()) {
                adr.append("- ").append(tradeoff).append("\n");
            }
            adr.append("\n");
        }
        if (!context.getExpectedOutcomes().isEmpty()) {
            adr.append("**예상 결과:**\n");
            for (String outcome : context.getExpectedOutcomes()) {
                adr.append("- ").append(outcome).append("\n");
            }
            adr.append("\n");
        }
        if (!context.getRisks().isEmpty()) {
            adr.append("**주요 위험:**\n");
            for (String risk : context.getRisks()) {
                adr.append("- ").append(risk).append("\n");
            }
            adr.append("\n");
        }
        
        // Who
        adr.append("### 👥 Who (누가)\n\n");
        if (context.getDecisionMaker() != null) {
            adr.append("**의사결정자:** ").append(context.getDecisionMaker()).append("\n\n");
        }
        if (!context.getAffectedTeams().isEmpty()) {
            adr.append("**영향받는 팀:**\n");
            for (String team : context.getAffectedTeams()) {
                adr.append("- ").append(team).append("\n");
            }
            adr.append("\n");
        }
        if (context.getOwner() != null) {
            adr.append("**책임자:** ").append(context.getOwner()).append("\n\n");
        }
        
        // Where
        adr.append("### 📍 Where (어디서)\n\n");
        if (context.getApplicationScope() != null) {
            adr.append("**적용 범위:** ").append(context.getApplicationScope()).append("\n\n");
        }
        if (context.getBoundaries() != null) {
            adr.append("**경계:** ").append(context.getBoundaries()).append("\n\n");
        }
        
        // When
        adr.append("### ⏰ When (언제)\n\n");
        if (context.getValidityPeriod() != null) {
            adr.append("**유효 기간:** ").append(context.getValidityPeriod()).append("\n\n");
        }
        if (context.getReviewSchedule() != null) {
            adr.append("**재검토 일정:** ").append(context.getReviewSchedule()).append("\n\n");
        }
        if (!context.getDependencies().isEmpty()) {
            adr.append("**의존성:**\n");
            for (String dep : context.getDependencies()) {
                adr.append("- ").append(dep).append("\n");
            }
            adr.append("\n");
        }
    }
    
    private void appendArchitectureDecisions(StringBuilder adr, AnalysisResult result) {
        adr.append("## 5. 🏗️ 아키텍처 결정사항\n\n");

        
        // 아키텍처 스타일 추론
        String architectureStyle = inferArchitectureStyle(result);
        adr.append("### 2.1 아키텍처 스타일\n\n");
        adr.append("**결정:** ").append(architectureStyle).append("\n\n");
        adr.append(getArchitectureStyleDescription(architectureStyle)).append("\n\n");
        
        // 계층 구조
        adr.append("### 2.2 계층 구조\n\n");
        if (result.getDesignPatterns().containsKey("Service Layer") && 
            result.getDesignPatterns().containsKey("Repository")) {
            adr.append("**결정:** 3-Tier 계층형 아키텍처\n\n");
            adr.append("- **Presentation Layer:** API Controllers\n");
            adr.append("- **Business Logic Layer:** Service Layer\n");
            adr.append("- **Data Access Layer:** Repository Layer\n\n");
            adr.append("**근거:** 명확한 관심사의 분리와 유지보수성 향상\n\n");
        } else {
            adr.append("**결정:** 단순 계층 구조 또는 미정의\n\n");
            adr.append("**근거:** 소규모 프로젝트 또는 프로토타입 단계로 추정\n\n");
        }
    }
    
    private void appendModuleStructure(StringBuilder adr, AnalysisResult result) {
        adr.append("## 6. 📦 모듈 구조\n\n");
        
        List<ModuleInfo> modules = result.getModules();
        if (modules.isEmpty()) {
            adr.append("명확한 모듈 구조가 감지되지 않았습니다.\n\n");
        } else {
            adr.append("| 모듈명 | 패키지 | 클래스 수 |\n");
            adr.append("|--------|---------|----------|\n");
            for (ModuleInfo module : modules) {
                adr.append("| ").append(module.getName()).append(" | ");
                adr.append(module.getPackageName()).append(" | ");
                adr.append(module.getClassCount()).append(" |\n");
            }
            adr.append("\n");
        }
    }
    
    private void appendTechnologyStack(StringBuilder adr, AnalysisResult result) {
        adr.append("## 7. 🛠️ 기술 스택\n\n");
        
        Map<String, Integer> frameworks = result.getFrameworkUsage();
        if (frameworks.isEmpty()) {
            String langName = result.getLanguageFileCount().keySet().stream()
                .findFirst().map(Language::getDisplayName).orElse("언어");
            adr.append("표준 프레임워크가 감지되지 않았습니다. 순수 ").append(langName).append("(으)로 작성된 것으로 보입니다.\n\n");
        } else {
            adr.append("### 사용된 프레임워크 및 라이브러리\n\n");
            
            // 카테고리별로 분류
            Map<String, List<String>> categorized = categorizeFrameworks(frameworks);
            
            for (Map.Entry<String, List<String>> entry : categorized.entrySet()) {
                adr.append("**").append(entry.getKey()).append("**\n");
                for (String framework : entry.getValue()) {
                    adr.append("- ").append(framework).append("\n");
                }
                adr.append("\n");
            }
        }
    }
    
    private void appendDesignPatterns(StringBuilder adr, AnalysisResult result) {
        adr.append("## 8. 🎨 디자인 패턴\n\n");
        
        Map<String, List<String>> patterns = result.getDesignPatterns();
        if (patterns.isEmpty()) {
            adr.append("명확한 디자인 패턴이 감지되지 않았습니다.\n\n");
        } else {
            for (Map.Entry<String, List<String>> entry : patterns.entrySet()) {
                adr.append("### ").append(entry.getKey()).append("\n\n");
                adr.append("**적용된 클래스:**\n");
                for (String className : entry.getValue()) {
                    adr.append("- `").append(className).append("`\n");
                }
                adr.append("\n");
                adr.append(getPatternDescription(entry.getKey())).append("\n\n");
            }
        }
    }
    
    private void appendDatabaseDesign(StringBuilder adr, AnalysisResult result) {
        adr.append("## 9. 🗄️ 데이터베이스 설계\n\n");
        
        List<String> schemas = result.getDatabaseSchemas();
        if (schemas.isEmpty()) {
            adr.append("데이터베이스 스키마가 감지되지 않았습니다.\n\n");
        } else {
            adr.append("### 감지된 테이블\n\n");
            for (String schema : schemas) {
                adr.append("- ").append(schema).append("\n");
            }
            adr.append("\n");
        }
    }
    
    private void appendApiDesign(StringBuilder adr, AnalysisResult result) {
        adr.append("## 10. 🌐 API 설계\n\n");
        
        List<String> apis = result.getApiEndpoints();
        if (apis.isEmpty()) {
            adr.append("REST API 엔드포인트가 감지되지 않았습니다.\n\n");
        } else {
            adr.append("### API 엔드포인트\n\n");
            adr.append("| HTTP Method | Endpoint | Handler |\n");
            adr.append("|-------------|----------|----------|\n");
            for (String api : apis) {
                String[] parts = api.split(" ", 3);
                if (parts.length == 3) {
                    adr.append("| ").append(parts[0]).append(" | ");
                    adr.append(parts[1]).append(" | ");
                    adr.append(parts[2]).append(" |\n");
                }
            }
            adr.append("\n");
        }
    }
    
    private void appendArchitectureDiagrams(StringBuilder adr, AnalysisResult result) {
        adr.append("## 11. 📊 아키텍처 다이어그램\n\n");
        
        adr.append("### 8.1 데이터 흐름도\n\n");
        adr.append(mermaidGenerator.generateDataFlowDiagram(result));
        adr.append("\n");
        
        adr.append("### 8.2 모듈 구성도\n\n");
        adr.append(mermaidGenerator.generateModuleDiagram(result));
        adr.append("\n");
        
        adr.append("### 8.3 주요 클래스 다이어그램\n\n");
        adr.append(mermaidGenerator.generateClassDiagram(result));
        adr.append("\n");
    }
    
    private void appendRisksAndRecommendations(StringBuilder adr, AnalysisResult result) {
        adr.append("## 12. ⚠️ 아키텍처 위험 요소 및 권장사항\n\n");
        
        List<RiskInfo> risks = result.getRisks();
        if (risks.isEmpty()) {
            adr.append("✅ 주요 아키텍처 위험 요소가 감지되지 않았습니다.\n\n");
        } else {
            // 심각도별로 정렬
            risks.sort((r1, r2) -> r2.getSeverity().compareTo(r1.getSeverity()));
            
            for (RiskInfo risk : risks) {
                adr.append("### ").append(risk.getSeverityIcon()).append(" ");
                adr.append(risk.getTitle()).append(" [").append(risk.getSeverity()).append("]\n\n");
                adr.append("**설명:** ").append(risk.getDescription()).append("\n\n");
                if (risk.getRecommendation() != null) {
                    adr.append("**권장사항:** ").append(risk.getRecommendation()).append("\n\n");
                }
            }
        }
    }
    
    private void appendConclusion(StringBuilder adr, AnalysisResult result) {
        adr.append("## 13. 📝 결론\n\n");
        
        int riskCount = result.getRisks().size();
        long criticalRisks = result.getRisks().stream()
            .filter(r -> r.getSeverity() == RiskInfo.Severity.CRITICAL)
            .count();
        
        if (criticalRisks > 0) {
            adr.append("본 프로젝트는 **").append(criticalRisks).append("개의 심각한 아키텍처 위험 요소**를 포함하고 있습니다. ");
            adr.append("즉각적인 개선이 필요합니다.\n\n");
        } else if (riskCount > 5) {
            adr.append("본 프로젝트는 여러 개선 가능한 영역이 있습니다. ");
            adr.append("위에서 제시한 권장사항을 검토하여 아키텍처를 개선하시기 바랍니다.\n\n");
        } else {
            adr.append("본 프로젝트는 전반적으로 양호한 아키텍처 구조를 가지고 있습니다. ");
            adr.append("지속적인 코드 리뷰와 리팩토링을 통해 품질을 유지하시기 바랍니다.\n\n");
        }
        
        adr.append("### 다음 단계\n\n");
        adr.append("1. 위험 요소 검토 및 우선순위 결정\n");
        adr.append("2. 개선 계획 수립\n");
        adr.append("3. 점진적 리팩토링 실행\n");
        adr.append("4. 정기적인 아키텍처 리뷰\n\n");
        
        adr.append("---\n\n");
        adr.append("*본 문서는 ADR Generator에 의해 자동 생성되었습니다.*\n");
    }
    
    // Helper methods
    
    private String inferArchitectureStyle(AnalysisResult result) {
        if (!result.getApiEndpoints().isEmpty()) {
            return "RESTful API 기반 아키텍처";
        } else if (result.getDesignPatterns().containsKey("Service Layer")) {
            return "계층형 아키텍처 (Layered Architecture)";
        } else {
            return "단순 구조 (Simple Structure)";
        }
    }
    
    private String getArchitectureStyleDescription(String style) {
        return switch (style) {
            case "RESTful API 기반 아키텍처" -> 
                "**설명:** HTTP 프로토콜을 통한 RESTful API를 제공하는 아키텍처입니다.\n\n" +
                "**장점:** 클라이언트-서버 분리, 확장성, 플랫폼 독립성\n\n" +
                "**고려사항:** API 버저닝, 인증/인가, 에러 핸들링";
            case "계층형 아키텍처 (Layered Architecture)" ->
                "**설명:** 관심사의 분리를 통해 각 계층이 명확한 책임을 가지는 아키텍처입니다.\n\n" +
                "**장점:** 유지보수성, 테스트 용이성, 명확한 책임 분리\n\n" +
                "**고려사항:** 계층 간 의존성 관리, 성능 오버헤드";
            default ->
                "**설명:** 명확한 아키텍처 패턴이 적용되지 않은 단순 구조입니다.\n\n" +
                "**고려사항:** 프로젝트 규모가 커질 경우 아키텍처 재설계 필요";
        };
    }
    
    private Map<String, List<String>> categorizeFrameworks(Map<String, Integer> frameworks) {
        Map<String, List<String>> categorized = new java.util.LinkedHashMap<>();
        
        for (String framework : frameworks.keySet()) {
            String category = categorizeFramework(framework);
            categorized.computeIfAbsent(category, k -> new java.util.ArrayList<>()).add(framework);
        }
        
        return categorized;
    }
    
    private String categorizeFramework(String framework) {
        if (framework.contains("Spring") || framework.contains("Django") || framework.contains("Flask") || framework.contains("Laravel")) return "프레임워크";
        if (framework.contains("JPA") || framework.contains("Hibernate") || framework.contains("JDBC") || framework.contains("SQL") || framework.contains("ORM")) return "데이터 접근";
        if (framework.contains("JUnit") || framework.contains("Test") || framework.contains("pytest") || framework.contains("Mocha")) return "테스팅";
        if (framework.contains("Log") || framework.contains("Logger")) return "로깅";
        if (framework.contains("Jackson") || framework.contains("Gson") || framework.contains("JSON") || framework.contains("Serde")) return "직렬화/JSON 처리";
        return "기타";
    }
    
    private String getPatternDescription(String pattern) {
        return switch (pattern) {
            case "Singleton" -> "**목적:** 클래스의 인스턴스가 하나만 존재하도록 보장";
            case "Factory" -> "**목적:** 객체 생성 로직을 캡슐화하여 유연성 향상";
            case "Builder" -> "**목적:** 복잡한 객체의 생성 과정을 단계별로 구성";
            case "Observer" -> "**목적:** 객체 간의 일대다 의존 관계를 정의하여 이벤트 처리";
            case "Strategy" -> "**목적:** 알고리즘을 캡슐화하여 런타임에 선택 가능하도록 함";
            case "Repository" -> "**목적:** 데이터 접근 로직을 캡슐화하여 비즈니스 로직과 분리";
            case "Service Layer" -> "**목적:** 비즈니스 로직을 캡슐화하여 재사용성 향상";
            case "DTO/VO" -> "**목적:** 계층 간 데이터 전송을 위한 객체";
            default -> "**목적:** " + pattern + " 패턴 적용";
        };
    }
}
