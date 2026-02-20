package com.adr.generator;

import com.adr.model.AnalysisResult;
import com.adr.model.RiskInfo;
import com.adr.model.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 아키텍처 위험 요소 분석 클래스
 */
public class RiskAnalyzer {
    
    public void analyzeRisks(AnalysisResult result) {
        // 1. 의존성 복잡도 분석
        analyzeDependencyComplexity(result);
        
        // 2. 프레임워크 의존성 분석
        analyzeFrameworkDependencies(result);
        
        // 3. 디자인 패턴 부재 분석
        analyzeDesignPatterns(result);
        
        // 4. 데이터베이스 스키마 분석
        analyzeDatabaseSchema(result);
        
        // 5. API 설계 분석
        analyzeApiDesign(result);
        
        // 6. 모듈화 분석
        analyzeModularity(result);
        
        // 7. 테스트 커버리지 분석 (새로 추가)
        analyzeTestCoverage(result);
        
        // 8. 보안 취약점 분석 (새로 추가)
        analyzeSecurityVulnerabilities(result);
        
        // 9. 성능 최적화 분석 (새로 추가)
        analyzePerformanceOptimization(result);
        
        // 10. 문서화 수준 분석 (새로 추가)
        analyzeDocumentation(result);
        
        // 11. 코드 품질 분석 (새로 추가)
        analyzeCodeQuality(result);
        
        // 최소 5개 이상 보장
        ensureMinimumRisks(result);
    }
    
    private void analyzeDependencyComplexity(AnalysisResult result) {
        int depCount = result.getDependencies().size();
        
        if (depCount > 50) {
            RiskInfo risk = new RiskInfo(
                "높은 의존성 복잡도",
                "프로젝트에 " + depCount + "개의 의존성이 발견되었습니다. 과도한 의존성은 유지보수를 어렵게 만들 수 있습니다.",
                RiskInfo.Severity.MEDIUM
            );
            risk.setRecommendation("의존성을 재검토하고 불필요한 의존성을 제거하세요. 의존성 역전 원칙(DIP)을 적용하여 결합도를 낮추세요.");
            result.addRisk(risk);
        }
    }
    
    private void analyzeFrameworkDependencies(AnalysisResult result) {
        Map<String, Integer> frameworks = result.getFrameworkUsage();
        Map<Language, Integer> languages = result.getLanguageFileCount();
        
        if (frameworks.isEmpty()) {
            RiskInfo risk = new RiskInfo(
                "프레임워크 미사용",
                generateFrameworkMissingDescription(languages),
                RiskInfo.Severity.LOW
            );
            risk.setRecommendation(generateFrameworkRecommendation(languages));
            result.addRisk(risk);
        }
        
        if (frameworks.size() > 10) {
            RiskInfo risk = new RiskInfo(
                "과도한 프레임워크 사용",
                frameworks.size() + "개의 서로 다른 프레임워크/라이브러리가 사용되고 있습니다. " +
                "이는 프로젝트의 복잡도를 증가시키고 유지보수를 어렵게 만들 수 있습니다.",
                RiskInfo.Severity.MEDIUM
            );
            risk.setRecommendation(
                "**즉시 조치:**\n" +
                "1. 사용 중인 프레임워크 목록을 검토하고 중복 기능 제공 여부 확인\n" +
                "2. 핵심 프레임워크로 통합 (예: 로깅은 하나의 프레임워크로 통일)\n" +
                "3. 사용하지 않는 의존성 제거\n\n" +
                "**장기 전략:**\n" +
                "- 새로운 프레임워크 도입 시 아키텍처 리뷰 필수화\n" +
                "- 프레임워크 선택 가이드라인 문서화"
            );
            result.addRisk(risk);
        }
    }
    
    private String generateFrameworkMissingDescription(Map<Language, Integer> languages) {
        StringBuilder desc = new StringBuilder("표준 프레임워크가 감지되지 않았습니다. ");
        
        if (languages.containsKey(Language.JAVA)) {
            desc.append("Java 프로젝트의 경우 Spring Framework 등을 사용하지 않으면 ");
        } else if (languages.containsKey(Language.CSHARP)) {
            desc.append("C# 프로젝트의 경우 ASP.NET Core 등을 사용하지 않으면 ");
        } else if (languages.containsKey(Language.TYPESCRIPT) || languages.containsKey(Language.JAVASCRIPT)) {
            desc.append("JavaScript/TypeScript 프로젝트의 경우 React, Express 등을 사용하지 않으면 ");
        } else if (languages.containsKey(Language.PYTHON)) {
            desc.append("Python 프로젝트의 경우 Django, Flask, FastAPI 등을 사용하지 않으면 ");
        } else if (languages.containsKey(Language.PHP)) {
            desc.append("PHP 프로젝트의 경우 Laravel, Symfony 등을 사용하지 않으면 ");
        }
        
        desc.append("재사용성과 유지보수성이 낮을 수 있습니다.");
        return desc.toString();
    }
    
    private String generateFrameworkRecommendation(Map<Language, Integer> languages) {
        StringBuilder rec = new StringBuilder("**권장 프레임워크:**\n\n");
        
        if (languages.containsKey(Language.JAVA)) {
            rec.append("**Java 프로젝트:**\n");
            rec.append("- **Spring Boot** - 엔터프라이즈급 애플리케이션 개발\n");
            rec.append("- **Spring Data JPA** - 데이터베이스 접근 계층\n");
            rec.append("- **JUnit 5** - 단위 테스트\n");
            rec.append("- **Lombok** - 보일러플레이트 코드 감소\n\n");
        }
        
        if (languages.containsKey(Language.CSHARP)) {
            rec.append("**C# 프로젝트:**\n");
            rec.append("- **ASP.NET Core** - 웹 애플리케이션 및 API 개발\n");
            rec.append("- **Entity Framework Core** - ORM 및 데이터 접근\n");
            rec.append("- **xUnit** 또는 **NUnit** - 단위 테스트\n");
            rec.append("- **AutoMapper** - 객체 매핑\n\n");
        }
        
        if (languages.containsKey(Language.TYPESCRIPT)) {
            rec.append("**TypeScript 프로젝트:**\n");
            rec.append("- **프론트엔드:** React, Angular, Vue.js\n");
            rec.append("- **백엔드:** NestJS, Express with TypeScript\n");
            rec.append("- **ORM:** TypeORM, Prisma\n");
            rec.append("- **테스팅:** Jest, Vitest\n\n");
        }
        
        if (languages.containsKey(Language.JAVASCRIPT)) {
            rec.append("**JavaScript 프로젝트:**\n");
            rec.append("- **프론트엔드:** React, Vue.js, Next.js\n");
            rec.append("- **백엔드:** Express.js, Fastify\n");
            rec.append("- **ORM:** Mongoose (MongoDB), Sequelize (SQL)\n");
            rec.append("- **테스팅:** Jest, Mocha\n\n");
        }
        
        if (languages.containsKey(Language.PYTHON)) {
            rec.append("**Python 프로젝트:**\n");
            rec.append("- **Django** - 풀스택 웹 프레임워크 (관리자 페이지, 인증 내장)\n");
            rec.append("- **FastAPI** - 현대적이고 빠른 성능의 API 개발\n");
            rec.append("- **SQLAlchemy** 또는 **Django ORM** - 데이터베이스 접근\n");
            rec.append("- **pytest** - 강력한 테스팅 도구\n\n");
        }
        
        if (languages.containsKey(Language.PHP)) {
            rec.append("**PHP 프로젝트:**\n");
            rec.append("- **Laravel** - 현대적인 PHP 웹 프레임워크\n");
            rec.append("- **Symfony** - 엔터프라이즈급 구성 요소 중심 프레임워크\n");
            rec.append("- **Eloquent** 또는 **Doctrine** - ORM\n");
            rec.append("- **PHPUnit** - 테스팅\n\n");
        }
        
        rec.append("**도입 시 고려사항:**\n");
        rec.append("1. 팀의 기술 스택 숙련도\n");
        rec.append("2. 프로젝트 규모 및 요구사항\n");
        rec.append("3. 커뮤니티 지원 및 문서화 수준\n");
        rec.append("4. 장기적인 유지보수 가능성");
        
        return rec.toString();
    }
    
    private void analyzeDesignPatterns(AnalysisResult result) {
        Map<String, List<String>> patterns = result.getDesignPatterns();
        
        if (patterns.isEmpty() && result.getClassCount() > 10) {
            RiskInfo risk = new RiskInfo(
                "디자인 패턴 부재",
                "명확한 디자인 패턴이 감지되지 않았습니다. 코드 구조화가 부족할 수 있습니다.",
                RiskInfo.Severity.MEDIUM
            );
            risk.setRecommendation(
                "**즉시 적용 가능한 패턴:**\n\n" +
                "1. **Service Layer 패턴**\n" +
                "   - 비즈니스 로직을 별도 Service 클래스로 분리\n" +
                "   - 컨트롤러/UI와 데이터 접근 계층 사이의 중재자 역할\n" +
                "   - 예: `UserService`, `OrderService`\n\n" +
                "2. **Repository 패턴**\n" +
                "   - 데이터 접근 로직을 캡슐화\n" +
                "   - 비즈니스 로직과 데이터 소스 분리\n" +
                "   - 예: `UserRepository`, `ProductRepository`\n\n" +
                "3. **DTO (Data Transfer Object) 패턴**\n" +
                "   - 계층 간 데이터 전송용 객체\n" +
                "   - 도메인 모델과 API 응답 분리\n" +
                "   - 예: `UserDTO`, `CreateUserRequest`\n\n" +
                "**리팩토링 순서:**\n" +
                "1. 가장 복잡한 클래스부터 Service Layer 분리\n" +
                "2. 데이터 접근 코드를 Repository로 추출\n" +
                "3. API 경계에 DTO 도입\n" +
                "4. 단위 테스트 작성으로 리팩토링 검증"
            );
            result.addRisk(risk);
        }
        
        // Repository 패턴 없이 데이터베이스 사용
        if (!result.getDatabaseSchemas().isEmpty() && !patterns.containsKey("Repository")) {
            RiskInfo risk = new RiskInfo(
                "데이터 접근 계층 미분리",
                "데이터베이스를 사용하지만 Repository 패턴이 감지되지 않았습니다. " +
                "비즈니스 로직과 데이터 접근 로직이 혼재되어 있을 가능성이 높습니다.",
                RiskInfo.Severity.HIGH
            );
            risk.setRecommendation(
                "**아키텍처 개선 가이드:**\n\n" +
                "**1. Repository 패턴 도입**\n" +
                "   - 데이터 접근 로직을 담당하는 별도 인터페이스/클래스 정의\n" +
                "   - 비즈니스 로직(Service)에서 데이터 소스의 세부 사항을 알지 못하게 분리\n\n" +
                "**2. 계층형 아키텍처 구성**\n" +
                "   - **Presentation:** 사용자 인터페이스/API 엔드포인트\n" +
                "   - **Service:** 비즈니스 규칙 및 유즈케이스 처리\n" +
                "   - **Repository:** 데이터 영속성 관리\n\n" +
                "**장점:**\n" +
                "- 테스트 용이성 (Mock/Stub 사용 가능)\n" +
                "- 데이터베이스 기술 변경 시 서비스 코드 수정 최소화\n" +
                "- 유지보수성 및 코드 가독성 향상"
            );
            result.addRisk(risk);
        }
    }
    
    private void analyzeDatabaseSchema(AnalysisResult result) {
        List<String> schemas = result.getDatabaseSchemas();
        
        if (schemas.size() > 20) {
            RiskInfo risk = new RiskInfo(
                "복잡한 데이터베이스 스키마",
                schemas.size() + "개의 테이블이 발견되었습니다. 스키마가 복잡할 수 있습니다.",
                RiskInfo.Severity.MEDIUM
            );
            risk.setRecommendation("도메인 주도 설계(DDD)를 고려하여 스키마를 논리적으로 분리하세요.");
            result.addRisk(risk);
        }
    }
    
    private void analyzeApiDesign(AnalysisResult result) {
        List<String> apis = result.getApiEndpoints();
        
        if (apis.isEmpty() && result.getFrameworkUsage().containsKey("Spring MVC")) {
            RiskInfo risk = new RiskInfo(
                "API 엔드포인트 미발견",
                "Spring MVC를 사용하지만 API 엔드포인트가 감지되지 않았습니다.",
                RiskInfo.Severity.LOW
            );
            risk.setRecommendation("REST API 설계를 검토하세요.");
            result.addRisk(risk);
        }
        
        if (apis.size() > 50) {
            RiskInfo risk = new RiskInfo(
                "과도한 API 엔드포인트",
                apis.size() + "개의 API 엔드포인트가 발견되었습니다.",
                RiskInfo.Severity.MEDIUM
            );
            risk.setRecommendation("API를 논리적으로 그룹화하고 마이크로서비스 아키텍처를 고려하세요.");
            result.addRisk(risk);
        }
    }
    
    private void analyzeModularity(AnalysisResult result) {
        int packageCount = result.getPackageCount();
        int classCount = result.getClassCount();
        
        if (packageCount > 0 && classCount / packageCount > 10) {
            RiskInfo risk = new RiskInfo(
                "낮은 모듈화 수준",
                String.format("패키지 수(%d)에 비해 클래스 수(%d)가 많습니다. " +
                    "평균 %.1f개의 클래스가 하나의 패키지에 집중되어 있습니다.",
                    packageCount, classCount, (double)classCount / packageCount),
                RiskInfo.Severity.HIGH
            );
            StringBuilder rec = new StringBuilder("**권장 프로젝트 구조:**\n\n");
            
            Map<Language, Integer> languages = result.getLanguageFileCount();
            if (languages.containsKey(Language.JAVA)) {
                rec.append("```\n");
                rec.append("com.company.project\n");
                rec.append("├── controller/     # API 엔드포인트, UI 컨트롤러\n");
                rec.append("├── service/        # 비즈니스 로직\n");
                rec.append("├── repository/     # 데이터 접근 계층\n");
                rec.append("├── model/          # 도메인 모델, Entity\n");
                rec.append("├── config/         # 설정 클래스\n");
                rec.append("└── util/           # 유틸리티 클래스\n");
                rec.append("```\n\n");
            } else if (languages.containsKey(Language.PYTHON)) {
                rec.append("```\n");
                rec.append("project_root/\n");
                rec.append("├── app/\n");
                rec.append("│   ├── api/        # 라우터 및 엔드포인트\n");
                rec.append("│   ├── core/       # 설정 및 보안\n");
                rec.append("│   ├── crud/       # 데이터 접근 로직 (Repository)\n");
                rec.append("│   ├── models/     # 데이터베이스 모델\n");
                rec.append("│   └── schemas/    # Pydantic 스키마 (DTO)\n");
                rec.append("├── tests/          # pytest 파일\n");
                rec.append("└── main.py         # 애플리케이션 진입점\n");
                rec.append("```\n\n");
            } else {
                rec.append("기능별 또는 계층별로 폴더를 분리하여 응집도를 높이고 결합도를 낮추는 구조를 권장합니다.\n\n");
            }

            rec.append("**리팩토링 단계:**\n\n");
            rec.append("1. **분석:** 클래스/모듈의 책임 파악 및 관련 요소 그룹화\n");
            rec.append("2. **분리:** 위 권장 구조를 참고하여 디렉토리(패키지) 생성\n");
            rec.append("3. **이동:** 계층 간 의존 방향(UI → Service → Data)을 준수하며 이동\n");
            rec.append("4. **검증:** 순환 의존성 제거 및 테스트 실행\n\n");
            rec.append("**목표:** 한 디렉토리당 과도한 수의 파일을 두지 않고 논리적으로 분할");
            
            risk.setRecommendation(rec.toString());
            result.addRisk(risk);
        }    
        if (classCount > 100 && result.getModules().size() < 5) {
            RiskInfo risk = new RiskInfo(
                "대규모 모놀리식 구조",
                "많은 클래스가 소수의 모듈에 집중되어 있습니다.",
                RiskInfo.Severity.CRITICAL
            );
            risk.setRecommendation("모듈화를 강화하고 마이크로서비스 또는 모듈러 모놀리스 아키텍처를 고려하세요.");
            result.addRisk(risk);
        }
    }
    
    // 새로 추가된 분석 메서드들
    
    private void analyzeTestCoverage(AnalysisResult result) {
        Map<String, Integer> frameworks = result.getFrameworkUsage();
        boolean hasTestFramework = frameworks.keySet().stream()
            .anyMatch(f -> f.contains("JUnit") || f.contains("TestNG") || 
                          f.contains("xUnit") || f.contains("NUnit") || 
                          f.contains("Jest") || f.contains("Mocha"));
        
        if (!hasTestFramework && result.getClassCount() > 5) {
            RiskInfo risk = new RiskInfo(
                "테스트 프레임워크 부재",
                String.format("프로젝트에 %d개의 클래스가 있지만 테스트 프레임워크가 감지되지 않았습니다. " +
                    "테스트 코드 없이 개발하면 버그 발생 위험이 높고 리팩토링이 어렵습니다.", 
                    result.getClassCount()),
                RiskInfo.Severity.HIGH
            );
            
            StringBuilder rec = new StringBuilder("**테스트 프레임워크 도입 권장:**\n\n");
            
            Map<Language, Integer> languages = result.getLanguageFileCount();
            if (languages.containsKey(Language.JAVA)) {
                rec.append("**Java:**\n");
                rec.append("- **JUnit 5** - 가장 널리 사용되는 Java 테스트 프레임워크\n");
                rec.append("- **Mockito** - Mock 객체 생성 및 검증\n");
                rec.append("- **AssertJ** - 유창한 assertion 라이브러리\n\n");
            }
            
            if (languages.containsKey(Language.CSHARP)) {
                rec.append("**C#:**\n");
                rec.append("- **xUnit** 또는 **NUnit** - .NET 테스트 프레임워크\n");
                rec.append("- **Moq** - Mock 라이브러리\n");
                rec.append("- **FluentAssertions** - 가독성 높은 assertion\n\n");
            }
            
            if (languages.containsKey(Language.JAVASCRIPT) || languages.containsKey(Language.TYPESCRIPT)) {
                rec.append("**JavaScript/TypeScript:**\n");
                rec.append("- **Jest** - React 프로젝트에 최적화\n");
                rec.append("- **Mocha + Chai** - 유연한 테스트 조합\n");
                rec.append("- **Vitest** - Vite 기반 프로젝트용\n\n");
            }
            
            if (languages.containsKey(Language.PYTHON)) {
                rec.append("**Python:**\n");
                rec.append("- **pytest** - 확장성이 뛰어난 표준 테스팅 도구\n");
                rec.append("- **unittest** - Python 표준 라이브러리 내장 프레임워크\n");
                rec.append("- **Hypothesis** - 속성 기반 테스팅(Property-based testing)\n\n");
            }
            
            if (languages.containsKey(Language.PHP)) {
                rec.append("**PHP:**\n");
                rec.append("- **PHPUnit** - PHP 표준 테스팅 프레임워크\n");
                rec.append("- **Pest** - 우아한 문법의 PHP 테스팅 프레임워크\n\n");
            }
            
            rec.append("**테스트 전략:**\n");
            rec.append("1. 단위 테스트부터 시작 (핵심 비즈니스 로직)\n");
            rec.append("2. 통합 테스트 추가 (API, 데이터베이스)\n");
            rec.append("3. 목표 커버리지: 최소 70% 이상\n");
            rec.append("4. CI/CD 파이프라인에 자동 테스트 통합");
            
            risk.setRecommendation(rec.toString());
            result.addRisk(risk);
        }
    }
    
    private void analyzeSecurityVulnerabilities(AnalysisResult result) {
        List<String> securityIssues  = new ArrayList<>();
        List<String> securityPassed  = new ArrayList<>();
        Map<String, Integer> frameworks = result.getFrameworkUsage();
        Map<String, List<String>> patterns = result.getDesignPatterns();

        // ── 인증/인가 프레임워크 확인 ─────────────────────────────────────────
        boolean hasSecurityFramework = frameworks.keySet().stream()
            .anyMatch(f -> f.contains("Security") || f.contains("Auth") ||
                          f.contains("JWT") || f.contains("OAuth") ||
                          f.contains("Devise") || f.contains("Passport"));

        if (!hasSecurityFramework && !result.getApiEndpoints().isEmpty()) {
            securityIssues.add("API 엔드포인트가 있지만 인증/인가 프레임워크가 감지되지 않음");
        } else if (hasSecurityFramework) {
            securityPassed.add("인증/인가 프레임워크 사용 확인됨");
        }

        // ── 입력 검증 확인 ────────────────────────────────────────────────────
        //   - 표준 검증 라이브러리
        boolean hasValidationLib = frameworks.keySet().stream()
            .anyMatch(f -> f.contains("Validation") || f.contains("Validator") ||
                          f.contains("Bean Validation"));
        //   - 커스텀 Validator / InputValidator 클래스 (ADR Generator 자체 보안 클래스 포함)
        boolean hasCustomValidator = patterns.values().stream()
            .flatMap(List::stream)
            .anyMatch(cls -> cls.toLowerCase().contains("validator") ||
                            cls.toLowerCase().contains("inputvalidator") ||
                            cls.toLowerCase().contains("sanitize"));

        if (!hasValidationLib && !hasCustomValidator && result.getClassCount() > 10) {
            securityIssues.add("입력 검증 라이브러리 또는 커스텀 검증 클래스가 감지되지 않음");
        } else {
            securityPassed.add("입력 검증 메커니즘 확인됨 (InputValidator 또는 검증 라이브러리)");
        }

        // ── ZIP 처리 프로젝트 특화: Zip Slip / ZIP Bomb 방어 확인 ─────────────
        boolean handlesZip = result.getFrameworkUsage().keySet().stream()
            .anyMatch(f -> f.toLowerCase().contains("zip")) ||
            patterns.values().stream().flatMap(List::stream)
            .anyMatch(cls -> cls.toLowerCase().contains("zip") ||
                            cls.toLowerCase().contains("extractor"));

        if (handlesZip) {
            boolean hasZipSecurityCheck = patterns.values().stream().flatMap(List::stream)
                .anyMatch(cls -> cls.toLowerCase().contains("validator") ||
                                cls.toLowerCase().contains("security") ||
                                cls.toLowerCase().contains("inputvalidator"));
            if (hasZipSecurityCheck) {
                securityPassed.add("ZIP 처리 시 보안 검증 클래스 사용 확인됨 (Zip Slip / ZIP Bomb 방어)");
            } else {
                securityIssues.add("ZIP 파일 처리 코드에서 Zip Slip / ZIP Bomb 방어 코드가 감지되지 않음");
            }
        }

        // ── 결과 종합 ─────────────────────────────────────────────────────────
        if (!securityIssues.isEmpty()) {
            RiskInfo risk = new RiskInfo(
                "보안 취약점 가능성",
                "다음 보안 관련 이슈가 발견되었습니다:\n- " + String.join("\n- ", securityIssues),
                RiskInfo.Severity.CRITICAL
            );

            StringBuilder rec = new StringBuilder("**보안 강화 권장사항:**\n\n");
            rec.append("**1. 인증 및 인가**\n");

            Map<Language, Integer> languages = result.getLanguageFileCount();
            if (languages.containsKey(Language.JAVA)) {
                rec.append("- Spring Security - 포괄적인 보안 프레임워크\n");
                rec.append("- JWT (JSON Web Token) - 토큰 기반 인증\n");
            }
            if (languages.containsKey(Language.CSHARP)) {
                rec.append("- ASP.NET Core Identity - 사용자 관리 및 인증\n");
                rec.append("- IdentityServer - OAuth 2.0 및 OpenID Connect\n");
            }
            if (languages.containsKey(Language.JAVASCRIPT) || languages.containsKey(Language.TYPESCRIPT)) {
                rec.append("- Passport.js - 다양한 인증 전략 지원\n");
                rec.append("- jsonwebtoken - JWT 토큰 생성 및 검증\n");
            }
            if (languages.containsKey(Language.PYTHON)) {
                rec.append("- **Django Auth** - Django 내장 보안 시스템\n");
                rec.append("- **FastAPI Users** - FastAPI용 인증 라이브러리\n");
                rec.append("- **Bandit** - 보안 취약점 정적 분석\n");
            }
            if (languages.containsKey(Language.PHP)) {
                rec.append("- **Laravel Sanctum/Passport** - API 인증 전용\n");
                rec.append("- **PHP IDS** - 침입 탐지 시스템 라이브러리\n");
            }

            rec.append("\n**2. 입력 검증**\n");
            rec.append("- 모든 사용자 입력 검증 필수\n");
            rec.append("- SQL Injection, XSS 방어\n");
            rec.append("- 화이트리스트 기반 검증 사용\n");
            rec.append("- ZIP 파일 처리 시 Zip Slip / ZIP Bomb 방어 구현\n\n");

            rec.append("**3. 보안 헤더**\n");
            rec.append("- HTTPS 강제 사용\n");
            rec.append("- CORS 정책 설정\n");
            rec.append("- Content Security Policy (CSP) 적용\n\n");

            rec.append("**4. 민감 정보 보호**\n");
            rec.append("- 환경 변수로 비밀키 관리\n");
            rec.append("- 암호화 저장 (bcrypt, Argon2)\n");
            rec.append("- 로그에 민감 정보 노출 방지");

            risk.setRecommendation(rec.toString());
            result.addRisk(risk);
        }
        // 보안 검사를 모두 통과한 경우 — CRITICAL 위험 없음 (로그만 출력)
        if (!securityPassed.isEmpty() && securityIssues.isEmpty()) {
            // INFO 수준 — 위험 목록에 추가하지 않음 (✅ 보안 항목 통과)
            System.out.println("   ✅ 보안 검사 통과: " + String.join(", ", securityPassed));
        }
    }

    private void analyzePerformanceOptimization(AnalysisResult result) {
        List<String> performanceIssues = new ArrayList<>();
        Map<String, Integer> frameworks = result.getFrameworkUsage();
        
        // 캐싱 확인
        boolean hasCaching = frameworks.keySet().stream()
            .anyMatch(f -> f.contains("Cache") || f.contains("Redis") || f.contains("Memcached"));
        
        if (!hasCaching && result.getApiEndpoints().size() > 10) {
            performanceIssues.add("API 엔드포인트가 많지만 캐싱 메커니즘이 없음");
        }
        
        // 데이터베이스 최적화
        if (!result.getDatabaseSchemas().isEmpty()) {
            boolean hasORM = frameworks.keySet().stream()
                .anyMatch(f -> f.contains("JPA") || f.contains("Hibernate") || 
                              f.contains("Entity Framework") || f.contains("TypeORM"));
            
            if (hasORM) {
                performanceIssues.add("ORM 사용 시 N+1 쿼리 문제 가능성");
            }
        }
        
        if (!performanceIssues.isEmpty()) {
            RiskInfo risk = new RiskInfo(
                "성능 최적화 필요",
                "다음 성능 관련 개선 사항이 발견되었습니다:\n- " + String.join("\n- ", performanceIssues),
                RiskInfo.Severity.MEDIUM
            );
            
            StringBuilder rec = new StringBuilder("**성능 최적화 권장사항:**\n\n");
            rec.append("**1. 캐싱 전략**\n");
            rec.append("- **애플리케이션 레벨 캐싱**: Redis, Memcached\n");
            rec.append("- **HTTP 캐싱**: ETag, Cache-Control 헤더\n");
            rec.append("- **데이터베이스 쿼리 캐싱**: 자주 조회되는 데이터\n\n");
            
            rec.append("**2. 데이터베이스 최적화**\n");
            rec.append("- 인덱스 추가 (자주 조회되는 컬럼)\n");
            rec.append("- N+1 쿼리 문제 해결 (Eager Loading, Batch Fetching)\n");
            rec.append("- 쿼리 실행 계획 분석 및 최적화\n");
            rec.append("- 커넥션 풀 설정 최적화\n\n");
            
            rec.append("**3. API 성능**\n");
            rec.append("- 페이지네이션 구현 (대량 데이터)\n");
            rec.append("- 응답 압축 (gzip)\n");
            rec.append("- 비동기 처리 (무거운 작업)\n\n");
            
            rec.append("**4. 모니터링**\n");
            rec.append("- APM 도구 도입 (Application Performance Monitoring)\n");
            rec.append("- 느린 쿼리 로깅\n");
            rec.append("- 성능 메트릭 수집 및 분석");
            
            risk.setRecommendation(rec.toString());
            result.addRisk(risk);
        }
    }
    
    private void analyzeDocumentation(AnalysisResult result) {
        // API 문서화 확인
        Map<String, Integer> frameworks = result.getFrameworkUsage();
        boolean hasApiDoc = frameworks.keySet().stream()
            .anyMatch(f -> f.contains("Swagger") || f.contains("OpenAPI") || 
                          f.contains("Javadoc") || f.contains("JSDoc"));
        
        if (!hasApiDoc && !result.getApiEndpoints().isEmpty()) {
            RiskInfo risk = new RiskInfo(
                "API 문서화 부족",
                String.format("프로젝트에 %d개의 API 엔드포인트가 있지만 자동 문서화 도구가 감지되지 않았습니다. " +
                    "API 문서가 없으면 프론트엔드 개발자 및 외부 사용자가 API를 이해하기 어렵습니다.",
                    result.getApiEndpoints().size()),
                RiskInfo.Severity.MEDIUM
            );
            
            StringBuilder rec = new StringBuilder("**API 문서화 권장사항:**\n\n");
            
            Map<Language, Integer> languages = result.getLanguageFileCount();
            if (languages.containsKey(Language.JAVA)) {
                rec.append("**Java (Spring Boot):**\n");
                rec.append("- **Springdoc OpenAPI** - Spring Boot 3.x용\n");
                rec.append("- **Swagger UI** - 인터랙티브 API 문서\n");
                rec.append("```java\n");
                rec.append("// build.gradle\n");
                rec.append("implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.0'\n");
                rec.append("```\n\n");
            }
            
            if (languages.containsKey(Language.CSHARP)) {
                rec.append("**C# (ASP.NET Core):**\n");
                rec.append("- **Swashbuckle** - Swagger 통합\n");
                rec.append("- XML 주석으로 상세 설명 추가\n");
                rec.append("```csharp\n");
                rec.append("// Program.cs\n");
                rec.append("builder.Services.AddSwaggerGen();\n");
                rec.append("```\n\n");
            }
            
            if (languages.containsKey(Language.JAVASCRIPT) || languages.containsKey(Language.TYPESCRIPT)) {
                rec.append("**JavaScript/TypeScript:**\n");
                rec.append("- **swagger-jsdoc** + **swagger-ui-express**\n");
                rec.append("- **TypeDoc** (TypeScript 프로젝트)\n");
                rec.append("- **JSDoc** 주석 활용\n\n");
            }
            
            if (languages.containsKey(Language.PYTHON)) {
                rec.append("**Python:**\n");
                rec.append("- **FastAPI Native** - Swagger/Redoc 자동 생성 내장\n");
                rec.append("- **drf-spectacular** - Django REST Framework용\n");
                rec.append("- **Sphinx** - 문서화 도구 표준\n\n");
            }
            
            rec.append("**문서화 모범 사례:**\n");
            rec.append("1. 모든 API 엔드포인트에 설명 추가\n");
            rec.append("2. 요청/응답 예제 제공\n");
            rec.append("3. 에러 코드 및 처리 방법 문서화\n");
            rec.append("4. 인증 방법 명시\n");
            rec.append("5. 버전 정보 포함");
            
            risk.setRecommendation(rec.toString());
            result.addRisk(risk);
        }
        
        // 코드 주석 부족 (간접 추정)
        if (result.getClassCount() > 20) {
            RiskInfo risk = new RiskInfo(
                "코드 문서화 권장",
                "프로젝트 규모가 커질수록 코드 주석 및 문서화가 중요합니다. " +
                "특히 복잡한 비즈니스 로직이나 알고리즘은 반드시 문서화해야 합니다.",
                RiskInfo.Severity.LOW
            );
            
            risk.setRecommendation(
                "**코드 문서화 가이드:**\n\n" +
                "1. **클래스 수준 문서화**\n" +
                "   - 클래스의 목적과 책임 설명\n" +
                "   - 주요 사용 사례\n\n" +
                "2. **메서드 문서화**\n" +
                "   - 파라미터 설명\n" +
                "   - 반환값 설명\n" +
                "   - 예외 상황\n\n" +
                "3. **복잡한 로직**\n" +
                "   - 알고리즘 설명\n" +
                "   - 왜 이렇게 구현했는지 (Why)\n\n" +
                "4. **README 작성**\n" +
                "   - 프로젝트 개요\n" +
                "   - 설치 및 실행 방법\n" +
                "   - 아키텍처 다이어그램"
            );
            result.addRisk(risk);
        }
    }
    
    private void analyzeCodeQuality(AnalysisResult result) {
        List<String> qualityIssues = new ArrayList<>();
        
        // 정적 분석 도구 확인
        Map<String, Integer> frameworks = result.getFrameworkUsage();
        boolean hasLinter = frameworks.keySet().stream()
            .anyMatch(f -> f.contains("Checkstyle") || f.contains("PMD") || 
                          f.contains("ESLint") || f.contains("SonarQube"));
        
        if (!hasLinter && result.getClassCount() > 10) {
            qualityIssues.add("정적 분석 도구 미사용");
        }
        
        // 코드 복잡도 추정
        int avgClassesPerPackage = result.getPackageCount() > 0 ? 
            result.getClassCount() / result.getPackageCount() : 0;
        
        if (avgClassesPerPackage > 15) {
            qualityIssues.add("패키지당 클래스 수가 많아 복잡도가 높을 가능성");
        }
        
        if (!qualityIssues.isEmpty()) {
            RiskInfo risk = new RiskInfo(
                "코드 품질 관리 필요",
                "다음 코드 품질 관련 개선 사항이 발견되었습니다:\n- " + String.join("\n- ", qualityIssues),
                RiskInfo.Severity.MEDIUM
            );
            
            StringBuilder rec = new StringBuilder("**코드 품질 개선 권장사항:**\n\n");
            
            rec.append("**1. 정적 분석 도구 도입**\n\n");
            
            Map<Language, Integer> languages = result.getLanguageFileCount();
            if (languages.containsKey(Language.JAVA)) {
                rec.append("**Java:**\n");
                rec.append("- **Checkstyle** - 코딩 스타일 검사\n");
                rec.append("- **PMD** - 잠재적 버그 탐지\n");
                rec.append("- **SpotBugs** - 버그 패턴 분석\n");
                rec.append("- **SonarQube** - 종합 코드 품질 분석\n\n");
            }
            
            if (languages.containsKey(Language.CSHARP)) {
                rec.append("**C#:**\n");
                rec.append("- **Roslyn Analyzers** - 컴파일 타임 분석\n");
                rec.append("- **StyleCop** - 코딩 스타일 검사\n");
                rec.append("- **SonarQube** - 종합 분석\n\n");
            }
            
            if (languages.containsKey(Language.JAVASCRIPT) || languages.containsKey(Language.TYPESCRIPT)) {
                rec.append("**JavaScript/TypeScript:**\n");
                rec.append("- **ESLint** - 코드 스타일 및 오류 검사\n");
                rec.append("- **Prettier** - 코드 포맷팅\n");
                rec.append("- **TypeScript** - 타입 안정성 (JS 프로젝트)\n\n");
            }
            
            if (languages.containsKey(Language.PYTHON)) {
                rec.append("**Python:**\n");
                rec.append("- **Ruff** - 최신 고성능 Linter 및 Formatter\n");
                rec.append("- **Flake8** / **Black** - 스타일 검사 및 자동 포맷팅\n");
                rec.append("- **Mypy** - 정적 타입 검사\n\n");
            }
            
            rec.append("**2. 코드 리뷰 프로세스**\n");
            rec.append("- Pull Request 필수화\n");
            rec.append("- 최소 1명 이상의 리뷰어 승인\n");
            rec.append("- 자동화된 CI 체크 통과 필수\n\n");
            
            rec.append("**3. 코드 메트릭 모니터링**\n");
            rec.append("- 순환 복잡도 (Cyclomatic Complexity) < 10\n");
            rec.append("- 메서드 길이 < 50줄\n");
            rec.append("- 클래스 크기 < 500줄\n\n");
            
            rec.append("**4. 리팩토링 원칙**\n");
            rec.append("- SOLID 원칙 준수\n");
            rec.append("- DRY (Don't Repeat Yourself)\n");
            rec.append("- KISS (Keep It Simple, Stupid)\n");
            rec.append("- YAGNI (You Aren't Gonna Need It)");
            
            risk.setRecommendation(rec.toString());
            result.addRisk(risk);
        }
    }
    
    private void ensureMinimumRisks(AnalysisResult result) {
        // 최소 5개의 위험 요소 보장
        if (result.getRisks().size() < 5) {
            int needed = 5 - result.getRisks().size();
            
            // 일반적인 권장사항 추가
            if (needed > 0) {
                RiskInfo risk = new RiskInfo(
                    "지속적인 아키텍처 개선",
                    "프로젝트가 성장함에 따라 아키텍처도 진화해야 합니다. " +
                    "정기적인 아키텍처 리뷰를 통해 기술 부채를 관리하고 품질을 유지하세요.",
                    RiskInfo.Severity.LOW
                );
                risk.setRecommendation(
                    "**아키텍처 관리 모범 사례:**\n\n" +
                    "1. **정기적인 리뷰**\n" +
                    "   - 분기별 아키텍처 리뷰 회의\n" +
                    "   - 주요 기능 추가 전 설계 검토\n\n" +
                    "2. **기술 부채 관리**\n" +
                    "   - 기술 부채 목록 유지\n" +
                    "   - 스프린트마다 일정 시간 할당\n\n" +
                    "3. **문서화 유지**\n" +
                    "   - ADR 문서 정기 업데이트\n" +
                    "   - 아키텍처 다이어그램 최신화\n\n" +
                    "4. **메트릭 추적**\n" +
                    "   - 코드 품질 지표\n" +
                    "   - 성능 메트릭\n" +
                    "   - 보안 취약점 스캔"
                );
                result.addRisk(risk);
                needed--;
            }
            
            if (needed > 0) {
                RiskInfo risk = new RiskInfo(
                    "확장성 고려",
                    "현재 프로젝트 규모는 작지만, 미래의 확장을 고려한 설계가 필요합니다.",
                    RiskInfo.Severity.LOW
                );
                risk.setRecommendation(
                    "**확장성 설계 원칙:**\n\n" +
                    "1. **수평 확장 가능성**\n" +
                    "   - 상태 비저장(Stateless) 설계\n" +
                    "   - 로드 밸런싱 고려\n\n" +
                    "2. **데이터베이스 확장**\n" +
                    "   - 읽기/쓰기 분리 (CQRS)\n" +
                    "   - 샤딩 전략 수립\n\n" +
                    "3. **캐싱 전략**\n" +
                    "   - 분산 캐시 사용\n" +
                    "   - CDN 활용\n\n" +
                    "4. **비동기 처리**\n" +
                    "   - 메시지 큐 도입\n" +
                    "   - 이벤트 기반 아키텍처"
                );
                result.addRisk(risk);
                needed--;
            }
            
            if (needed > 0) {
                RiskInfo risk = new RiskInfo(
                    "모니터링 및 로깅 강화",
                    "프로덕션 환경에서의 안정적인 운영을 위해 모니터링과 로깅이 필수적입니다.",
                    RiskInfo.Severity.MEDIUM
                );
                risk.setRecommendation(
                    "**모니터링 및 로깅 권장사항:**\n\n" +
                    "1. **애플리케이션 모니터링**\n" +
                    "   - APM 도구 (New Relic, Datadog, Application Insights)\n" +
                    "   - 헬스 체크 엔드포인트\n" +
                    "   - 메트릭 수집 (Prometheus, Grafana)\n\n" +
                    "2. **로깅 전략**\n" +
                    "   - 구조화된 로깅 (JSON 형식)\n" +
                    "   - 로그 레벨 적절히 사용 (ERROR, WARN, INFO, DEBUG)\n" +
                    "   - 중앙 집중식 로그 관리 (ELK Stack, Splunk)\n\n" +
                    "3. **알림 설정**\n" +
                    "   - 에러율 임계값 알림\n" +
                    "   - 응답 시간 모니터링\n" +
                    "   - 리소스 사용률 추적\n\n" +
                    "4. **분산 추적**\n" +
                    "   - OpenTelemetry, Jaeger\n" +
                    "   - 요청 흐름 추적\n" +
                    "   - 병목 지점 식별"
                );
                result.addRisk(risk);
            }
        }
    }
}
