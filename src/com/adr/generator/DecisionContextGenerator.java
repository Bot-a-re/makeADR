package com.adr.generator;

import com.adr.model.*;

import java.util.List;
import java.util.Map;

/**
 * ADR-E의 DecisionContext를 자동으로 생성하는 클래스
 */
public class DecisionContextGenerator {
    
    public DecisionContext generate(AnalysisResult result) {
        DecisionContext context = new DecisionContext();
        
        // Why - 문제와 동기
        generateWhy(context, result);
        
        // What - 결정 내용
        generateWhat(context, result);
        
        // What-if - 트레이드오프와 결과
        generateWhatIf(context, result);
        
        // Who - 이해관계자
        generateWho(context, result);
        
        // Where - 적용 범위
        generateWhere(context, result);
        
        // When - 시기와 의존성
        generateWhen(context, result);
        
        return context;
    }
    
    private void generateWhy(DecisionContext context, AnalysisResult result) {
        // 문제 식별
        StringBuilder problem = new StringBuilder();
        
        if (result.getApiEndpoints().isEmpty() && !result.getFrameworkUsage().isEmpty()) {
            problem.append("API 엔드포인트가 명시적으로 정의되지 않음. ");
        }
        
        if (result.getDesignPatterns().isEmpty() && result.getClassCount() > 10) {
            problem.append("명확한 디자인 패턴 없이 클래스 수가 증가. ");
        }
        
        if (result.getDatabaseSchemas().isEmpty() && result.getClassCount() > 5) {
            problem.append("데이터 영속성 전략이 불명확. ");
        }
        
        if (problem.length() == 0) {
            problem.append("프로젝트 아키텍처 구조화 및 문서화 필요");
        }
        
        context.setProblem(problem.toString().trim());
        
        // 동기
        context.setMotivation("장기적인 유지보수성, 확장성, 팀 협업을 위한 명확한 아키텍처 정의 필요");
        
        // 목표
        context.addGoal("명확하고 추적 가능한 아키텍처 구조 확립");
        context.addGoal("팀 간 일관된 이해와 커뮤니케이션 촉진");
        context.addGoal("기술 부채 최소화 및 품질 속성 보장");
    }
    
    private void generateWhat(DecisionContext context, AnalysisResult result) {
        // 결정 문장 생성
        StringBuilder decision = new StringBuilder();
        
        Map<Language, Integer> languages = result.getLanguageFileCount();
        if (languages.size() > 1) {
            decision.append("다중 언어 기반 ");
        }
        
        if (!result.getApiEndpoints().isEmpty()) {
            decision.append("RESTful API 중심 ");
        }
        
        if (!result.getFrameworkUsage().isEmpty()) {
            String mainFramework = result.getFrameworkUsage().keySet().iterator().next();
            decision.append(mainFramework).append(" 기반 ");
        }
        
        decision.append("아키텍처 채택");
        
        context.setDecisionStatement(decision.toString().trim());
        
        // 범위
        context.setScope(String.format(
            "전체 프로젝트 (%d개 파일, %d개 클래스)",
            result.getTotalFileCount(),
            result.getClassCount()
        ));
    }
    
    private void generateWhatIf(DecisionContext context, AnalysisResult result) {
        // 트레이드오프
        if (!result.getFrameworkUsage().isEmpty()) {
            context.addTradeoff("프레임워크 의존성 증가 vs 개발 생산성 향상");
            context.addTradeoff("학습 곡선 vs 표준화된 패턴");
        }
        
        if (result.getLanguageFileCount().size() > 1) {
            context.addTradeoff("다중 언어 복잡도 vs 각 언어의 강점 활용");
        }
        
        // 예상 결과
        context.addExpectedOutcome("개발 생산성 향상");
        context.addExpectedOutcome("코드 품질 및 일관성 개선");
        context.addExpectedOutcome("유지보수 비용 감소");
        
        // 위험
        if (result.getRisks().size() > 0) {
            for (RiskInfo risk : result.getRisks()) {
                if (risk.getSeverity() == RiskInfo.Severity.HIGH || 
                    risk.getSeverity() == RiskInfo.Severity.CRITICAL) {
                    context.addRisk(risk.getTitle());
                }
            }
        }
    }
    
    private void generateWho(DecisionContext context, AnalysisResult result) {
        context.setDecisionMaker("아키텍처 팀 / 기술 리더");
        
        // 영향받는 팀
        context.addAffectedTeam("개발팀 - 구현 및 코드 작성");
        
        if (!result.getApiEndpoints().isEmpty()) {
            context.addAffectedTeam("프론트엔드팀 - API 통합");
        }
        
        if (!result.getDatabaseSchemas().isEmpty()) {
            context.addAffectedTeam("데이터팀 - 데이터베이스 관리");
        }
        
        context.addAffectedTeam("운영팀 - 배포 및 모니터링");
        
        context.setOwner("아키텍처 팀");
    }
    
    private void generateWhere(DecisionContext context, AnalysisResult result) {
        StringBuilder scope = new StringBuilder();
        
        if (!result.getModules().isEmpty()) {
            scope.append("모듈: ");
            result.getModules().stream()
                .limit(3)
                .forEach(m -> scope.append(m.getName()).append(", "));
            if (result.getModules().size() > 3) {
                scope.append("외 ").append(result.getModules().size() - 3).append("개");
            }
        } else {
            scope.append("전체 프로젝트");
        }
        
        context.setApplicationScope(scope.toString());
        context.setBoundaries("레거시 시스템과의 통합 인터페이스는 별도 검토 필요");
    }
    
    private void generateWhen(DecisionContext context, AnalysisResult result) {
        context.setValidityPeriod("프로젝트 전체 생명주기 (단, 주요 기술 스택 변경 시 재검토)");
        context.setReviewSchedule("분기별 아키텍처 리뷰 또는 주요 요구사항 변경 시");
        
        if (!result.getFrameworkUsage().isEmpty()) {
            context.addDependency("선택된 프레임워크 버전 호환성");
        }
        
        if (result.getLanguageFileCount().size() > 1) {
            context.addDependency("다중 언어 빌드 환경 구성");
        }
    }
}
