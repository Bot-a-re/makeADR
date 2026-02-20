package com.adr.generator;

import com.adr.model.*;

import java.util.*;

/**
 * 프로젝트 분석 결과를 기반으로 아키텍처 대안을 자동 생성하는 클래스
 */
public class AlternativeGenerator {
    
    // ── 상태 상수 ────────────────────────────────────────────────────────────
    private static final String SELECTED   = "SELECTED";
    private static final String CONSIDERED = "CONSIDERED";
    private static final String REJECTED   = "REJECTED";
    private static final String CANDIDATE  = "CANDIDATE";
    
    // ── 주요 프레임워크 상수 ──────────────────────────────────────────────────
    private static final String SPRING_BOOT = "Spring Boot";
    private static final String RICH_ECOSYSTEM   = "성숙한 생태계와 풍부한 라이브러리";
    private static final String LEARNING_CURVE   = "초기 학습 곡선";
    private static final String MICROSERVICES_OP = "마이크로서비스 환경에 적합";
    
    public List<Alternative> generateAlternatives(AnalysisResult result) {
        List<Alternative> alternatives = new ArrayList<>();
        
        Map<Language, Integer> languages = result.getLanguageFileCount();
        Map<String, Integer> frameworks = result.getFrameworkUsage();
        
        // 언어별 프레임워크 대안 생성
        for (Language language : languages.keySet()) {
            alternatives.addAll(generateFrameworkAlternatives(language, frameworks));
        }
        
        // 아키텍처 스타일 대안 생성
        alternatives.addAll(generateArchitectureStyleAlternatives(result));
        
        return alternatives;
    }
    
    private List<Alternative> generateFrameworkAlternatives(Language language, Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();
        
        switch (language) {
            case JAVA:
                alternatives.addAll(generateJavaFrameworkAlternatives(currentFrameworks));
                break;
            case CSHARP:
                alternatives.addAll(generateCSharpFrameworkAlternatives(currentFrameworks));
                break;
            case JAVASCRIPT:
            case TYPESCRIPT:
                alternatives.addAll(generateJavaScriptFrameworkAlternatives(currentFrameworks));
                break;
            case C:
                alternatives.addAll(generateCFrameworkAlternatives(currentFrameworks));
                break;
            case CPP:
                alternatives.addAll(generateCppFrameworkAlternatives(currentFrameworks));
                break;
            case RUBY:
                alternatives.addAll(generateRubyFrameworkAlternatives(currentFrameworks));
                break;
            case RUST:
                alternatives.addAll(generateRustFrameworkAlternatives(currentFrameworks));
                break;
            case KOTLIN:
                alternatives.addAll(generateKotlinFrameworkAlternatives(currentFrameworks));
                break;
            case PYTHON:
                alternatives.addAll(generatePythonFrameworkAlternatives(currentFrameworks));
                break;
            case PHP:
                alternatives.addAll(generatePhpFrameworkAlternatives(currentFrameworks));
                break;
            case JSP:
                alternatives.addAll(generateJspFrameworkAlternatives(currentFrameworks));
                break;
            default:
                break;
        }
        
        return alternatives;
    }
    
    private List<Alternative> generateJavaFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();
        
        boolean hasSpring = currentFrameworks.containsKey("Spring Framework") || 
                           currentFrameworks.containsKey(SPRING_BOOT);
        
        if (hasSpring) {
            // Spring Boot 선택됨
            alternatives.add(new Alternative(
                SPRING_BOOT,
                "엔터프라이즈급 Java 프레임워크",
                Arrays.asList(
                    RICH_ECOSYSTEM,
                    "대규모 커뮤니티 지원",
                    "엔터프라이즈 기능 내장 (보안, 트랜잭션, 캐싱)",
                    "Spring Data, Spring Security 등 통합 용이"
                ),
                Arrays.asList(
                    "상대적으로 무거운 메모리 사용",
                    LEARNING_CURVE,
                    "빌드 시간이 다소 길 수 있음"
                ),
                SELECTED,
                "팀의 기존 경험과 엔터프라이즈 요구사항에 가장 적합. 안정성과 생산성의 균형"
            ));
            
            // Quarkus 거부됨
            alternatives.add(new Alternative(
                "Quarkus",
                "클라우드 네이티브 Java 프레임워크",
                Arrays.asList(
                    "빠른 시작 시간 (밀리초 단위)",
                    "낮은 메모리 사용량",
                    "네이티브 이미지 컴파일 지원",
                    "Kubernetes 최적화"
                ),
                Arrays.asList(
                    "상대적으로 작은 생태계",
                    "팀의 경험 부족",
                    "일부 레거시 라이브러리 호환성 문제"
                ),
                REJECTED,
                "클라우드 네이티브 장점은 있으나, 팀의 Spring 경험과 기존 라이브러리 활용을 우선시"
            ));
            
            // Micronaut 거부됨
            alternatives.add(new Alternative(
                "Micronaut",
                "마이크로서비스 중심 프레임워크",
                Arrays.asList(
                    "컴파일 타임 의존성 주입",
                    "빠른 시작 시간",
                    "낮은 메모리 사용"
                ),
                Arrays.asList(
                    "Spring 대비 작은 생태계",
                    "학습 자료 부족",
                    "팀 경험 부족"
                ),
                REJECTED,
                "성능 이점은 있으나, 현재 프로젝트 규모에서는 Spring의 생산성이 더 중요"
            ));
        } else {
            // 프레임워크 없음 - 옵션 제시
            alternatives.add(new Alternative(
                SPRING_BOOT,
                "엔터프라이즈급 Java 프레임워크",
                Arrays.asList(
                    "검증된 아키텍처 패턴",
                    "풍부한 생태계",
                    "빠른 개발 속도"
                ),
                Arrays.asList(
                    "프레임워크 학습 필요",
                    "의존성 증가"
                ),
                CONSIDERED,
                "엔터프라이즈 애플리케이션에 권장"
            ));
            
            alternatives.add(new Alternative(
                "순수 Java",
                "프레임워크 없이 순수 Java로 개발",
                Arrays.asList(
                    "완전한 제어권",
                    "의존성 최소화",
                    "가벼운 애플리케이션"
                ),
                Arrays.asList(
                    "모든 기능을 직접 구현",
                    "개발 시간 증가",
                    "표준화 부족"
                ),
                CONSIDERED,
                "소규모 프로젝트나 학습 목적에 적합"
            ));
        }
        
        return alternatives;
    }
    
    private List<Alternative> generateCSharpFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();
        
        boolean hasAspNetCore = currentFrameworks.containsKey("ASP.NET Core");
        
        if (hasAspNetCore) {
            alternatives.add(new Alternative(
                "ASP.NET Core",
                "Microsoft의 크로스 플랫폼 웹 프레임워크",
                Arrays.asList(
                    "Microsoft 공식 지원",
                    "뛰어난 성능",
                    "크로스 플랫폼 (Windows, Linux, macOS)",
                    "Entity Framework Core 통합"
                ),
                Arrays.asList(
                    "Microsoft 생태계 의존성",
                    "일부 레거시 .NET Framework 기능 미지원"
                ),
                SELECTED,
                "C# 개발의 표준이며, 성능과 생산성이 검증됨"
            ));
            
            alternatives.add(new Alternative(
                ".NET Framework",
                "레거시 .NET 프레임워크",
                Arrays.asList(
                    "성숙한 생태계",
                    "Windows 통합"
                ),
                Arrays.asList(
                    "Windows 전용",
                    "레거시 기술",
                    "성능 제한"
                ),
                REJECTED,
                "크로스 플랫폼 요구사항과 최신 기능 활용을 위해 ASP.NET Core 선택"
            ));
        }
        
        return alternatives;
    }
    
    private List<Alternative> generateJavaScriptFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();
        
        if (currentFrameworks.containsKey("React")) {
            alternatives.add(new Alternative(
                "React",
                "컴포넌트 기반 UI 라이브러리",
                Arrays.asList(
                    "거대한 생태계",
                    "유연성",
                    "Virtual DOM 성능",
                    "풍부한 서드파티 라이브러리"
                ),
                Arrays.asList(
                    "보일러플레이트 코드",
                    "상태 관리 라이브러리 별도 필요",
                    "빠른 변화"
                ),
                SELECTED,
                "가장 큰 커뮤니티와 생태계, 팀의 기존 경험 활용"
            ));
            
            alternatives.add(new Alternative(
                "Vue.js",
                "프로그레시브 프레임워크",
                Arrays.asList(
                    "쉬운 학습 곡선",
                    "공식 라우터/상태관리",
                    "좋은 문서화"
                ),
                Arrays.asList(
                    "React 대비 작은 생태계",
                    "엔터프라이즈 채택률 낮음"
                ),
                REJECTED,
                "팀의 React 경험과 생태계 크기를 우선시"
            ));
        }
        
        if (currentFrameworks.containsKey("Express.js")) {
            alternatives.add(new Alternative(
                "Express.js",
                "미니멀 Node.js 웹 프레임워크",
                Arrays.asList(
                    "가볍고 유연함",
                    "거대한 미들웨어 생태계",
                    "빠른 개발"
                ),
                Arrays.asList(
                    "구조화 부족",
                    "TypeScript 지원 제한적"
                ),
                SELECTED,
                "유연성과 성능의 균형, 빠른 프로토타이핑에 적합"
            ));
            
            alternatives.add(new Alternative(
                "NestJS",
                "TypeScript 기반 프레임워크",
                Arrays.asList(
                    "TypeScript 네이티브",
                    "Angular 스타일 아키텍처",
                    "내장 기능 풍부"
                ),
                Arrays.asList(
                    "학습 곡선",
                    "Express 대비 무거움"
                ),
                REJECTED,
                "현재 프로젝트 규모에서는 Express의 단순함이 더 적합"
            ));
        }
        
        return alternatives;
    }
    
    private List<Alternative> generateArchitectureStyleAlternatives(AnalysisResult result) {
        List<Alternative> alternatives = new ArrayList<>();
        
        if (!result.getApiEndpoints().isEmpty()) {
            alternatives.add(new Alternative(
                "RESTful API 아키텍처",
                "HTTP 기반 리소스 중심 API",
                Arrays.asList(
                    "표준화된 인터페이스",
                    "캐싱 가능",
                    "클라이언트-서버 분리",
                    "플랫폼 독립적"
                ),
                Arrays.asList(
                    "Over-fetching/Under-fetching",
                    "여러 요청 필요할 수 있음"
                ),
                SELECTED,
                "표준화되고 검증된 아키텍처, 대부분의 클라이언트 지원"
            ));
            
            alternatives.add(new Alternative(
                "GraphQL",
                "쿼리 언어 기반 API",
                Arrays.asList(
                    "정확한 데이터 요청",
                    "단일 엔드포인트",
                    "강력한 타입 시스템"
                ),
                Arrays.asList(
                    "복잡도 증가",
                    "캐싱 어려움",
                    "학습 곡선"
                ),
                REJECTED,
                "현재 요구사항에서는 REST의 단순함이 더 적합"
            ));
        }
        
        return alternatives;
    }
    
    // ── C 언어 대안 ──────────────────────────────────────────────────────────
    
    private List<Alternative> generateCFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();
        
        boolean hasLibcurl = currentFrameworks.containsKey("libcurl");
        boolean hasSqlite  = currentFrameworks.containsKey("SQLite3");
        boolean hasCmake   = currentFrameworks.containsKey("CMake");
        
        // 빌드 시스템
        if (hasCmake) {
            alternatives.add(new Alternative(
                "CMake",
                "크로스 플랫폼 빌드 시스템",
                Arrays.asList(
                    "크로스 플랫폼 지원 (Windows, Linux, macOS)",
                    "IDE 통합 (CLion, VS, VSCode)",
                    "대규모 프로젝트 관리 용이",
                    "광범위한 커뮤니티"
                ),
                Arrays.asList(
                    "복잡한 문법",
                    "학습 곡선 존재"
                ),
                SELECTED,
                "C/C++ 프로젝트의 사실상 표준 빌드 시스템"
            ));
            alternatives.add(new Alternative(
                "Meson",
                "빠르고 현대적인 빌드 시스템",
                Arrays.asList(
                    "빠른 빌드 속도",
                    "간결한 문법",
                    "Ninja 백엔드 기본 사용"
                ),
                Arrays.asList(
                    "CMake 대비 작은 생태계",
                    "레거시 프로젝트 지원 부족"
                ),
                REJECTED,
                "CMake의 광범위한 생태계와 IDE 지원을 우선시"
            ));
        } else {
            alternatives.add(new Alternative(
                "CMake",
                "크로스 플랫폼 빌드 시스템",
                Arrays.asList("크로스 플랫폼", "IDE 통합", "대규모 프로젝트 관리"),
                Arrays.asList("복잡한 문법"),
                CONSIDERED,
                "C 프로젝트 빌드 자동화에 권장"
            ));
        }
        
        // HTTP 클라이언트
        if (hasLibcurl) {
            alternatives.add(new Alternative(
                "libcurl",
                "C 언어 HTTP/FTP 클라이언트 라이브러리",
                Arrays.asList(
                    "광범위한 프로토콜 지원 (HTTP, HTTPS, FTP 등)",
                    "크로스 플랫폼",
                    "성숙한 라이브러리 (20년 이상)",
                    "SSL/TLS 내장"
                ),
                Arrays.asList(
                    "콜백 기반 비동기 API",
                    "C API 특성상 메모리 관리 필요"
                ),
                SELECTED,
                "C에서 HTTP 통신의 표준 라이브러리"
            ));
        }
        
        // 데이터베이스
        if (hasSqlite) {
            alternatives.add(new Alternative(
                "SQLite3",
                "임베디드 관계형 데이터베이스",
                Arrays.asList(
                    "별도 서버 불필요",
                    "단일 파일 데이터베이스",
                    "C 네이티브 API",
                    "경량 및 빠른 성능"
                ),
                Arrays.asList(
                    "동시 쓰기 제한",
                    "대규모 데이터에 부적합"
                ),
                SELECTED,
                "임베디드 및 로컬 애플리케이션에 최적"
            ));
            alternatives.add(new Alternative(
                "PostgreSQL (libpq)",
                "엔터프라이즈 관계형 데이터베이스",
                Arrays.asList(
                    "고성능 및 확장성",
                    "ACID 완전 지원",
                    "풍부한 기능"
                ),
                Arrays.asList(
                    "별도 서버 필요",
                    "설정 복잡도"
                ),
                REJECTED,
                "현재 규모에서는 SQLite의 단순함이 더 적합"
            ));
        }
        
        // 테스팅
        alternatives.add(new Alternative(
            "CUnit / cmocka",
            "C 언어 단위 테스트 프레임워크",
            Arrays.asList(
                "C 네이티브 테스트",
                "Mock 지원 (cmocka)",
                "CI 통합 용이"
            ),
            Arrays.asList(
                "C++ 프레임워크 대비 기능 제한",
                "설정 필요"
            ),
            CONSIDERED,
            "C 프로젝트 품질 보증을 위해 도입 권장"
        ));
        
        return alternatives;
    }
    
    // ── C++ 언어 대안 ─────────────────────────────────────────────────────────
    
    private List<Alternative> generateCppFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();
        
        boolean hasQt      = currentFrameworks.containsKey("Qt Framework");
        boolean hasBoost   = currentFrameworks.containsKey("Boost");
        boolean hasGtest   = currentFrameworks.containsKey("Google Test (gtest)");
        boolean hasCatch2  = currentFrameworks.containsKey("Catch2");
        boolean hasGrpc    = currentFrameworks.containsKey("gRPC");
        boolean hasCuda    = currentFrameworks.containsKey("CUDA");
        boolean hasOpenCV  = currentFrameworks.containsKey("OpenCV");
        
        // GUI 프레임워크
        if (hasQt) {
            alternatives.add(new Alternative(
                "Qt Framework",
                "크로스 플랫폼 C++ GUI/애플리케이션 프레임워크",
                Arrays.asList(
                    "크로스 플랫폼 (Windows, Linux, macOS, 모바일)",
                    "풍부한 위젯 라이브러리",
                    "QML로 현대적 UI 개발",
                    "강력한 시그널-슬롯 메커니즘",
                    "Qt Creator IDE 지원"
                ),
                Arrays.asList(
                    "상업적 사용 시 라이선스 비용",
                    "큰 바이너리 크기",
                    "Qt 특유의 메타 오브젝트 시스템 학습 필요"
                ),
                SELECTED,
                "크로스 플랫폼 GUI 개발의 사실상 표준, 풍부한 생태계"
            ));
            alternatives.add(new Alternative(
                "wxWidgets",
                "오픈소스 크로스 플랫폼 GUI 라이브러리",
                Arrays.asList(
                    "완전 오픈소스 (LGPL)",
                    "네이티브 OS 위젯 사용",
                    "가벼운 런타임"
                ),
                Arrays.asList(
                    "Qt 대비 작은 생태계",
                    "현대적 UI 구현 어려움",
                    "문서화 부족"
                ),
                REJECTED,
                "Qt의 풍부한 생태계와 현대적 UI 지원을 우선시"
            ));
            alternatives.add(new Alternative(
                "GTK+ (gtkmm)",
                "Linux 중심 GUI 툴킷",
                Arrays.asList(
                    "GNOME 생태계 통합",
                    "완전 오픈소스",
                    "C++ 바인딩 (gtkmm)"
                ),
                Arrays.asList(
                    "Windows 지원 제한적",
                    "Qt 대비 작은 커뮤니티"
                ),
                REJECTED,
                "크로스 플랫폼 요구사항으로 Qt 선택"
            ));
        }
        
        // Boost
        if (hasBoost) {
            alternatives.add(new Alternative(
                "Boost",
                "C++ 표준 라이브러리 확장 모음",
                Arrays.asList(
                    "광범위한 라이브러리 (160개 이상)",
                    "C++ 표준 위원회 검토",
                    "고품질 및 검증된 코드",
                    "Boost.Asio로 비동기 네트워킹"
                ),
                Arrays.asList(
                    "큰 의존성 크기",
                    "헤더 전용 라이브러리 컴파일 시간 증가",
                    "일부 라이브러리 복잡한 API"
                ),
                SELECTED,
                "C++ 생태계에서 가장 신뢰받는 라이브러리 모음"
            ));
            alternatives.add(new Alternative(
                "POCO C++ Libraries",
                "네트워크 중심 C++ 프레임워크",
                Arrays.asList(
                    "네트워킹에 특화",
                    "간결한 API",
                    "HTTP 서버/클라이언트 내장"
                ),
                Arrays.asList(
                    "Boost 대비 작은 생태계",
                    "범용성 부족"
                ),
                REJECTED,
                "Boost의 범용성과 표준 근접성을 우선시"
            ));
        }
        
        // 테스팅
        if (hasGtest) {
            alternatives.add(new Alternative(
                "Google Test (gtest)",
                "Google의 C++ 테스트 프레임워크",
                Arrays.asList(
                    "풍부한 assertion 매크로",
                    "Mock 지원 (Google Mock)",
                    "CI/CD 통합 용이",
                    "광범위한 사용"
                ),
                Arrays.asList(
                    "별도 빌드 필요",
                    "Catch2 대비 무거운 설정"
                ),
                SELECTED,
                "C++ 테스팅의 업계 표준"
            ));
            alternatives.add(new Alternative(
                "Catch2",
                "헤더 전용 C++ 테스트 프레임워크",
                Arrays.asList(
                    "헤더 전용 (설정 간단)",
                    "BDD 스타일 지원",
                    "표현력 있는 문법"
                ),
                Arrays.asList(
                    "gtest 대비 작은 생태계",
                    "Mock 기능 별도 라이브러리 필요"
                ),
                REJECTED,
                "gtest의 Mock 통합과 광범위한 사용을 우선시"
            ));
        } else if (hasCatch2) {
            alternatives.add(new Alternative(
                "Catch2",
                "헤더 전용 C++ 테스트 프레임워크",
                Arrays.asList(
                    "헤더 전용 (설정 간단)",
                    "BDD 스타일 지원",
                    "표현력 있는 문법"
                ),
                Arrays.asList(
                    "Mock 기능 별도 라이브러리 필요"
                ),
                SELECTED,
                "간단한 설정과 표현력 있는 테스트 코드"
            ));
        } else {
            alternatives.add(new Alternative(
                "Google Test (gtest)",
                "Google의 C++ 테스트 프레임워크",
                Arrays.asList("풍부한 assertion", "Mock 지원", "CI 통합"),
                Arrays.asList("별도 빌드 필요"),
                CONSIDERED,
                "C++ 프로젝트 품질 보증을 위해 도입 권장"
            ));
        }
        
        // gRPC
        if (hasGrpc) {
            alternatives.add(new Alternative(
                "gRPC",
                "Google의 고성능 RPC 프레임워크",
                Arrays.asList(
                    "Protocol Buffers 기반 효율적 직렬화",
                    "양방향 스트리밍",
                    "다중 언어 지원",
                    "HTTP/2 기반"
                ),
                Arrays.asList(
                    "REST 대비 복잡한 설정",
                    "브라우저 직접 지원 제한",
                    "학습 곡선"
                ),
                SELECTED,
                "마이크로서비스 간 고성능 통신에 최적"
            ));
            alternatives.add(new Alternative(
                "REST (libcurl / Boost.Beast)",
                "HTTP 기반 REST API",
                Arrays.asList(
                    "단순한 구조",
                    "브라우저 직접 지원",
                    "광범위한 도구 지원"
                ),
                Arrays.asList(
                    "gRPC 대비 낮은 성능",
                    "타입 안전성 부족"
                ),
                REJECTED,
                "마이크로서비스 성능 요구사항으로 gRPC 선택"
            ));
        }
        
        // CUDA
        if (hasCuda) {
            alternatives.add(new Alternative(
                "CUDA",
                "NVIDIA GPU 병렬 컴퓨팅 플랫폼",
                Arrays.asList(
                    "NVIDIA GPU 최대 활용",
                    "광범위한 라이브러리 (cuBLAS, cuDNN)",
                    "딥러닝 프레임워크 통합",
                    "성숙한 생태계"
                ),
                Arrays.asList(
                    "NVIDIA GPU 전용",
                    "학습 곡선",
                    "디버깅 어려움"
                ),
                SELECTED,
                "GPU 가속 컴퓨팅의 사실상 표준"
            ));
            alternatives.add(new Alternative(
                "OpenCL",
                "크로스 플랫폼 병렬 컴퓨팅 표준",
                Arrays.asList(
                    "AMD/Intel/NVIDIA GPU 지원",
                    "크로스 플랫폼",
                    "오픈 표준"
                ),
                Arrays.asList(
                    "CUDA 대비 낮은 성능 (NVIDIA에서)",
                    "작은 생태계",
                    "복잡한 API"
                ),
                REJECTED,
                "NVIDIA GPU 환경에서 CUDA의 성능 우위 선택"
            ));
        }
        
        // OpenCV
        if (hasOpenCV) {
            alternatives.add(new Alternative(
                "OpenCV",
                "오픈소스 컴퓨터 비전 라이브러리",
                Arrays.asList(
                    "2500개 이상의 알고리즘",
                    "GPU 가속 지원",
                    "다중 언어 바인딩",
                    "광범위한 커뮤니티"
                ),
                Arrays.asList(
                    "큰 라이브러리 크기",
                    "일부 API 불일관성"
                ),
                SELECTED,
                "컴퓨터 비전의 사실상 표준 라이브러리"
            ));
        }
        
        return alternatives;
    }
    
    // ── Ruby 언어 대안 ────────────────────────────────────────────────────────
    
    private List<Alternative> generateRubyFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();
        
        boolean hasRails    = currentFrameworks.containsKey("Ruby on Rails");
        boolean hasSinatra  = currentFrameworks.containsKey("Sinatra");
        boolean hasGrape    = currentFrameworks.containsKey("Grape (API)");
        boolean hasRSpec    = currentFrameworks.containsKey("RSpec");
        boolean hasSidekiq  = currentFrameworks.containsKey("Sidekiq (Background Jobs)");
        boolean hasDevise   = currentFrameworks.containsKey("Devise (Authentication)");
        boolean hasMongoid  = currentFrameworks.containsKey("Mongoid (MongoDB)");
        
        // 웹 프레임워크
        if (hasRails) {
            alternatives.add(new Alternative(
                "Ruby on Rails",
                "풀스택 Ruby 웹 프레임워크",
                Arrays.asList(
                    "Convention over Configuration으로 빠른 개발",
                    "ActiveRecord ORM 내장",
                    "풍부한 gem 생태계",
                    "MVC 아키텍처 표준화",
                    "Rails 커뮤니티와 방대한 문서"
                ),
                Arrays.asList(
                    "모놀리식 구조로 마이크로서비스 전환 어려움",
                    "성능이 Go/Node.js 대비 낮음",
                    "메모리 사용량 높음",
                    "Ruby 버전 관리 복잡성"
                ),
                SELECTED,
                "빠른 개발 속도와 풍부한 생태계, 스타트업과 중소 규모 프로젝트에 최적"
            ));
            alternatives.add(new Alternative(
                "Sinatra",
                "경량 Ruby 웹 프레임워크",
                Arrays.asList(
                    "매우 가볍고 빠름",
                    "유연한 구조",
                    "마이크로서비스에 적합",
                    "학습 곡선 낮음"
                ),
                Arrays.asList(
                    "Rails 대비 기능 부족",
                    "대규모 프로젝트에 구조화 어려움",
                    "ORM 별도 설정 필요"
                ),
                REJECTED,
                "Rails의 풍부한 기능과 생산성을 우선시"
            ));
            alternatives.add(new Alternative(
                "Hanami",
                "현대적 Ruby 웹 프레임워크",
                Arrays.asList(
                    "클린 아키텍처 지향",
                    "Rails 대비 낮은 메모리 사용",
                    "명시적 설계"
                ),
                Arrays.asList(
                    "작은 커뮤니티",
                    "gem 호환성 제한",
                    "Rails 대비 적은 학습 자료"
                ),
                REJECTED,
                "Rails의 성숙한 생태계와 팀 경험을 우선시"
            ));
        } else if (hasSinatra) {
            alternatives.add(new Alternative(
                "Sinatra",
                "경량 Ruby 웹 프레임워크",
                Arrays.asList(
                    "매우 가볍고 빠름",
                    "유연한 구조",
                    "마이크로서비스에 적합"
                ),
                Arrays.asList(
                    "대규모 프로젝트에 구조화 어려움",
                    "ORM 별도 설정 필요"
                ),
                SELECTED,
                "경량 API 서버 및 마이크로서비스에 최적"
            ));
            alternatives.add(new Alternative(
                "Ruby on Rails",
                "풀스택 Ruby 웹 프레임워크",
                Arrays.asList(
                    "풍부한 기능 내장",
                    "빠른 개발 속도",
                    "ActiveRecord ORM"
                ),
                Arrays.asList(
                    "현재 규모에 과도한 기능",
                    "무거운 의존성"
                ),
                REJECTED,
                "현재 규모에서는 Sinatra의 경량성이 더 적합"
            ));
        } else {
            alternatives.add(new Alternative(
                "Ruby on Rails",
                "풀스택 Ruby 웹 프레임워크",
                Arrays.asList("빠른 개발", "풍부한 생태계", "ActiveRecord ORM"),
                Arrays.asList("모놀리식 구조", "성능 제한"),
                CONSIDERED,
                "웹 애플리케이션 개발에 권장"
            ));
            alternatives.add(new Alternative(
                "Sinatra",
                "경량 Ruby 웹 프레임워크",
                Arrays.asList("경량", "유연성", "마이크로서비스 적합"),
                Arrays.asList("기능 제한"),
                CONSIDERED,
                "API 서버 및 소규모 서비스에 권장"
            ));
        }
        
        // API 프레임워크
        if (hasGrape) {
            alternatives.add(new Alternative(
                "Grape",
                "Ruby REST-like API 프레임워크",
                Arrays.asList(
                    "API 특화 DSL",
                    "자동 파라미터 검증",
                    "Swagger 통합",
                    "버전 관리 내장"
                ),
                Arrays.asList(
                    "Rails 대비 작은 생태계",
                    "풀스택 기능 부족"
                ),
                SELECTED,
                "순수 API 서버 구축에 최적화"
            ));
        }
        
        // 테스팅
        if (hasRSpec) {
            alternatives.add(new Alternative(
                "RSpec",
                "Ruby BDD 테스트 프레임워크",
                Arrays.asList(
                    "표현력 있는 BDD 문법",
                    "풍부한 matcher",
                    "Rails 통합 (rspec-rails)",
                    "광범위한 커뮤니티"
                ),
                Arrays.asList(
                    "Minitest 대비 느린 실행",
                    "복잡한 설정 가능성"
                ),
                SELECTED,
                "Ruby 커뮤니티의 표준 테스트 프레임워크"
            ));
            alternatives.add(new Alternative(
                "Minitest",
                "Ruby 표준 라이브러리 테스트 프레임워크",
                Arrays.asList(
                    "Ruby 표준 라이브러리 포함",
                    "빠른 실행 속도",
                    "단순한 구조"
                ),
                Arrays.asList(
                    "RSpec 대비 표현력 부족",
                    "BDD 스타일 제한적"
                ),
                REJECTED,
                "RSpec의 표현력과 Rails 통합을 우선시"
            ));
        } else {
            alternatives.add(new Alternative(
                "RSpec",
                "Ruby BDD 테스트 프레임워크",
                Arrays.asList("BDD 문법", "풍부한 matcher", "Rails 통합"),
                Arrays.asList("Minitest 대비 느린 실행"),
                CONSIDERED,
                "Ruby 프로젝트 품질 보증을 위해 도입 권장"
            ));
        }
        
        // 백그라운드 잡
        if (hasSidekiq) {
            alternatives.add(new Alternative(
                "Sidekiq",
                "Redis 기반 Ruby 백그라운드 잡 프레임워크",
                Arrays.asList(
                    "고성능 (멀티스레드)",
                    "Redis 기반 안정성",
                    "웹 UI 내장",
                    "Rails 통합 용이"
                ),
                Arrays.asList(
                    "Redis 의존성",
                    "Pro 기능 유료"
                ),
                SELECTED,
                "Ruby 백그라운드 잡의 사실상 표준"
            ));
            alternatives.add(new Alternative(
                "Delayed::Job",
                "데이터베이스 기반 백그라운드 잡",
                Arrays.asList(
                    "추가 인프라 불필요 (DB 사용)",
                    "간단한 설정"
                ),
                Arrays.asList(
                    "Sidekiq 대비 낮은 성능",
                    "DB 부하 증가"
                ),
                REJECTED,
                "Sidekiq의 성능과 안정성을 우선시"
            ));
        }
        
        // 인증
        if (hasDevise) {
            alternatives.add(new Alternative(
                "Devise",
                "Rails 인증 솔루션",
                Arrays.asList(
                    "완전한 인증 기능 내장",
                    "OAuth 통합 (OmniAuth)",
                    "광범위한 커뮤니티",
                    "설정 용이"
                ),
                Arrays.asList(
                    "Rails 전용",
                    "커스터마이징 복잡성",
                    "블랙박스 특성"
                ),
                SELECTED,
                "Rails 인증의 사실상 표준"
            ));
            alternatives.add(new Alternative(
                "Rodauth",
                "Ruby 인증 프레임워크",
                Arrays.asList(
                    "높은 보안성",
                    "Rails/Sinatra 모두 지원",
                    "명시적 설계"
                ),
                Arrays.asList(
                    "Devise 대비 작은 커뮤니티",
                    "학습 자료 부족"
                ),
                REJECTED,
                "Devise의 풍부한 생태계와 팀 경험을 우선시"
            ));
        }
        
        // 데이터베이스 ORM
        if (hasMongoid) {
            alternatives.add(new Alternative(
                "Mongoid",
                "Ruby MongoDB ODM",
                Arrays.asList(
                    "MongoDB 네이티브 통합",
                    "ActiveRecord 유사 API",
                    "유연한 스키마"
                ),
                Arrays.asList(
                    "MongoDB 전용",
                    "관계형 데이터 처리 제한"
                ),
                SELECTED,
                "MongoDB 기반 프로젝트에 최적"
            ));
            alternatives.add(new Alternative(
                "ActiveRecord (PostgreSQL)",
                "Rails 기본 ORM",
                Arrays.asList(
                    "강력한 관계형 데이터 처리",
                    "ACID 트랜잭션",
                    "Rails 완전 통합"
                ),
                Arrays.asList(
                    "스키마 마이그레이션 필요",
                    "NoSQL 유연성 부족"
                ),
                REJECTED,
                "현재 데이터 구조에 MongoDB의 유연성이 더 적합"
            ));
        }
        
        return alternatives;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RUST 프레임워크 대안
    // ─────────────────────────────────────────────────────────────────────────

    private List<Alternative> generateRustFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();

        // ── 웹 프레임워크 ─────────────────────────────────────────────────────
        boolean hasActix  = currentFrameworks.containsKey("Actix-Web");
        boolean hasAxum   = currentFrameworks.containsKey("Axum");
        boolean hasRocket = currentFrameworks.containsKey("Rocket");

        if (hasActix || hasAxum || hasRocket) {
            alternatives.add(new Alternative(
                "Axum",
                "Tokio 기반 경량 웹 프레임워크",
                Arrays.asList(
                    "타입 안전한 라우팅 (TypedPath)",
                    "Tower 미들웨어 생태계 완전 호환",
                    "async/await 네이티브 지원",
                    "낮은 메모리 사용량"
                ),
                Arrays.asList(
                    "Actix에 비해 파일 업로드 등 일부 기능 직접 구현 필요",
                    "상대적으로 적은 예제"
                ),
                hasAxum ? SELECTED : CANDIDATE,
                "안전성과 성능의 균형에서 현재 Rust 웹 개발 표준에 근접"
            ));
            alternatives.add(new Alternative(
                "Actix-Web",
                "고성능 Rust 웹 프레임워크",
                Arrays.asList(
                    "벤치마크 최상위권 성능",
                    "성숙한 생태계",
                    "Actix 액터 모델 통합"
                ),
                Arrays.asList(
                    "Actor 모델 학습 곡선",
                    "보일러플레이트 코드 많음"
                ),
                hasActix ? SELECTED : REJECTED,
                "최고 성능이 필요할 때 선택"
            ));
            alternatives.add(new Alternative(
                "Rocket",
                "개발자 경험 중심 Rust 웹 프레임워크",
                Arrays.asList(
                    "직관적인 라우팅 매크로",
                    "간결한 코드",
                    "Fairing(미들웨어) 시스템"
                ),
                Arrays.asList(
                    "Axum/Actix 대비 약간 낮은 성능",
                    "안정 버전 의존"
                ),
                hasRocket ? SELECTED : REJECTED,
                "빠른 프로토타이핑에 적합"
            ));
        }

        // ── 비동기 런타임 ─────────────────────────────────────────────────────
        boolean hasTokio = currentFrameworks.containsKey("Tokio (Async Runtime)");
        if (hasTokio || hasActix || hasAxum || hasRocket) {
            alternatives.add(new Alternative(
                "Tokio",
                "Rust 표준 비동기 런타임",
                Arrays.asList(
                    "가장 넓은 생태계 (Axum, Tonic 등)",
                    "멀티스레드 work-stealing 스케줄러",
                    "macros 지원 (#[tokio::main])"
                ),
                Arrays.asList(
                    "바이너리 크기 증가",
                    "설정 복잡도"
                ),
                SELECTED,
                "현재 Rust 비동기 개발의 사실상 표준"
            ));
        }

        // ── ORM / DB ──────────────────────────────────────────────────────────
        boolean hasDiesel = currentFrameworks.containsKey("Diesel (ORM)");
        boolean hasSqlx   = currentFrameworks.containsKey("SQLx");
        boolean hasSeaOrm = currentFrameworks.containsKey("SeaORM");

        if (hasDiesel || hasSqlx || hasSeaOrm) {
            alternatives.add(new Alternative(
                "SQLx",
                "비동기 SQL 크레이트 (컴파일 타임 쿼리 검증)",
                Arrays.asList(
                    "컴파일 타임 SQL 문법 검증",
                    "async/await 완전 지원",
                    "다중 DB 드라이버 (PG, MySQL, SQLite)"
                ),
                Arrays.asList(
                    "ORM 추상화 없음 — 직접 SQL 작성",
                    "마이그레이션 도구 별도 필요"
                ),
                hasSqlx ? SELECTED : CANDIDATE,
                "성능과 타입 안전성 모두 필요할 때"
            ));
            alternatives.add(new Alternative(
                "Diesel",
                "컴파일 타임 안전 ORM",
                Arrays.asList(
                    "완전한 ORM 추상화",
                    "내장 마이그레이션 CLI",
                    "성숙한 생태계"
                ),
                Arrays.asList(
                    "async 미지원 (동기 전용)",
                    "높은 학습 곡선"
                ),
                hasDiesel ? SELECTED : REJECTED,
                "동기 코드베이스에 적합"
            ));
        }

        // ── 아키텍처 스타일 ───────────────────────────────────────────────────
        alternatives.add(new Alternative(
            "Layered Architecture (Rust)",
            "계층형 아키텍처 — Handler / Service / Repository",
            Arrays.asList(
                "명확한 책임 분리",
                "테스트 용이 (trait mock)",
                "대부분의 Rust 웹 프로젝트 표준"
            ),
            Arrays.asList(
                "계층 간 데이터 변환 오버헤드"
            ),
            SELECTED,
            "Rust trait 기반 추상화와 잘 맞는 구조"
        ));
        alternatives.add(new Alternative(
            "Hexagonal Architecture (Rust)",
            "포트·어댑터 패턴",
            Arrays.asList(
                "도메인 로직의 완전한 격리",
                "DB·HTTP 교체 용이"
            ),
            Arrays.asList(
                "소규모 프로젝트에 오버엔지니어링",
                "보일러플레이트 증가"
            ),
            REJECTED,
            "대규모 도메인이 없으면 계층형으로 충분"
        ));

        return alternatives;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  KOTLIN 프레임워크 대안
    // ─────────────────────────────────────────────────────────────────────────

    private List<Alternative> generateKotlinFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();

        // ── 웹/서버 프레임워크 ────────────────────────────────────────────────
        boolean hasSpring = currentFrameworks.containsKey(SPRING_BOOT);
        boolean hasKtor   = currentFrameworks.containsKey("Ktor");
        boolean hasVertx  = currentFrameworks.containsKey("Vert.x");

        if (hasSpring || hasKtor || hasVertx) {
            alternatives.add(new Alternative(
                "Spring Boot (Kotlin)",
                "엔터프라이즈급 Kotlin 서버 프레임워크",
                Arrays.asList(
                    "광범위한 Spring 생태계 활용",
                    "Kotlin과 높은 호환성 (data class, coroutine 지원)",
                    "풍부한 레퍼런스와 커뮤니티",
                    "Spring Data, Security 등 즉시 사용 가능"
                ),
                Arrays.asList(
                    "상대적으로 무거운 시작 시간",
                    "람다 / DSL 스타일보다 어노테이션 중심"
                ),
                hasSpring ? SELECTED : CANDIDATE,
                "팀의 Spring 경험 보유 시 최적 선택"
            ));
            alternatives.add(new Alternative(
                "Ktor",
                "JetBrains 공식 Kotlin 비동기 프레임워크",
                Arrays.asList(
                    "Kotlin DSL로 코드 가독성 극대화",
                    "Coroutine 네이티브 지원",
                    "경량 — 필요한 기능만 플러그인으로 추가",
                    "멀티플랫폼(KMP) 클라이언트 공유 가능"
                ),
                Arrays.asList(
                    "Spring 대비 작은 생태계",
                    "엔터프라이즈 기능은 직접 구성 필요"
                ),
                hasKtor ? SELECTED : CANDIDATE,
                "마이크로서비스·경량 API 서버에 적합"
            ));
            alternatives.add(new Alternative(
                "Vert.x (Kotlin)",
                "이벤트 드리븐 비동기 프레임워크",
                Arrays.asList(
                    "높은 처리량",
                    "폴리글랏 지원 (Java, Kotlin, JS 혼합)",
                    "Reactive Extensions 통합"
                ),
                Arrays.asList(
                    "콜백/코루틴 혼재로 학습 곡선 높음",
                    "스프링보다 작은 국내 커뮤니티"
                ),
                hasVertx ? SELECTED : REJECTED,
                "높은 동시 연결 처리가 필요할 때"
            ));
        }

        // ── 코루틴 / 리액티브 ─────────────────────────────────────────────────
        boolean hasCoroutines = currentFrameworks.containsKey("Kotlin Coroutines");
        boolean hasWebFlux    = currentFrameworks.containsKey("Spring WebFlux (Reactive)");

        if (hasCoroutines || hasWebFlux) {
            alternatives.add(new Alternative(
                "Kotlin Coroutines",
                "Kotlin 비동기 처리 표준 방식",
                Arrays.asList(
                    "suspend/async 로 직관적 비동기 코드",
                    "Spring, Ktor 모두 네이티브 지원",
                    "구조적 동시성으로 안전한 취소"
                ),
                Arrays.asList(
                    "RxJava 마이그레이션 시 러닝 커브"
                ),
                SELECTED,
                "Kotlin 프로젝트라면 코루틴이 사실상 표준"
            ));
            alternatives.add(new Alternative(
                "Spring WebFlux / Project Reactor",
                "Spring 리액티브 스택",
                Arrays.asList(
                    "백프레셔 지원",
                    "R2DBC로 완전 논블로킹 DB 처리",
                    "Spring 생태계 그대로 활용"
                ),
                Arrays.asList(
                    "Coroutine보다 복잡한 체인 디버깅",
                    "함수형 스타일 강제"
                ),
                hasWebFlux ? SELECTED : REJECTED,
                "기존 R2DBC 기반 프로젝트에 적합"
            ));
        }

        // ── ORM / DB ──────────────────────────────────────────────────────────
        boolean hasJpa      = currentFrameworks.containsKey("Jakarta Persistence (JPA)") ||
                              currentFrameworks.containsKey("Hibernate");
        boolean hasExposed  = currentFrameworks.containsKey("Exposed (Kotlin ORM)");

        if (hasJpa || hasExposed) {
            alternatives.add(new Alternative(
                "Spring Data JPA (Kotlin)",
                "JPA 기반 레포지토리 패턴",
                Arrays.asList(
                    "data class를 Entity로 사용 간편",
                    "Spring 생태계 완전 통합",
                    "풍부한 쿼리 메서드 지원"
                ),
                Arrays.asList(
                    "지연 로딩 시 data class 주의 필요",
                    "N+1 문제 주의"
                ),
                hasJpa ? SELECTED : CANDIDATE,
                "Spring Boot 프로젝트 표준 선택"
            ));
            alternatives.add(new Alternative(
                "Exposed (JetBrains)",
                "Kotlin DSL 기반 경량 ORM",
                Arrays.asList(
                    "순수 Kotlin 타입 안전 쿼리 빌더",
                    "경량 — Spring 의존성 없음",
                    "Kotlin object로 테이블 정의 (간결)"
                ),
                Arrays.asList(
                    "JPA 대비 기능 제한",
                    "대규모 엔터프라이즈에는 미성숙"
                ),
                hasExposed ? SELECTED : REJECTED,
                "Ktor 등 경량 프레임워크에 잘 어울림"
            ));
        }

        // ── DI ───────────────────────────────────────────────────────────────
        boolean hasKoin  = currentFrameworks.containsKey("Koin (DI)");
        boolean hasDagger = currentFrameworks.containsKey("Dagger/Hilt (DI)");

        if (hasKoin || hasDagger) {
            alternatives.add(new Alternative(
                "Koin",
                "Kotlin 네이티브 경량 DI 프레임워크",
                Arrays.asList(
                    "DSL로 간결한 모듈 정의",
                    "Android / KMP 모두 지원",
                    "리플렉션 없음 — 빠른 시작"
                ),
                Arrays.asList(
                    "런타임 오류 가능성",
                    "대형 프로젝트에서 성능 저하 보고"
                ),
                hasKoin ? SELECTED : CANDIDATE,
                "Android/Ktor 경량 프로젝트 최적"
            ));
            alternatives.add(new Alternative(
                "Dagger / Hilt",
                "컴파일 타임 DI 프레임워크 (Android)",
                Arrays.asList(
                    "컴파일 타임 의존성 검증",
                    "Zero 런타임 오버헤드",
                    "Android 공식 권장"
                ),
                Arrays.asList(
                    "높은 보일러플레이트",
                    "학습 곡선"
                ),
                hasDagger ? SELECTED : REJECTED,
                "Android 대규모 앱에 적합"
            ));
        }

        // ── 아키텍처 스타일 ───────────────────────────────────────────────────
        alternatives.add(new Alternative(
            "Clean Architecture (Kotlin)",
            "도메인 중심 계층 분리 (UseCase / Repository / ViewModel)",
            Arrays.asList(
                "Kotlin data class로 도메인 모델 표현 간결",
                "sealed class로 Result 타입 표현 우수",
                "Android / 서버 모두 적용 가능"
            ),
            Arrays.asList(
                "소규모 프로젝트에 오버엔지니어링 위험",
                "레이어 간 매핑 코드 증가"
            ),
            SELECTED,
            "Kotlin의 sealed class·data class가 DDD와 잘 맞음"
        ));
        alternatives.add(new Alternative(
            "MVVM (Android)",
            "ViewModel + LiveData / StateFlow 패턴",
            Arrays.asList(
                "Jetpack 공식 권장 패턴",
                "UI 상태 관리 용이",
                "테스트 용이성"
            ),
            Arrays.asList(
                "ViewModel 비대화 가능성",
                "이벤트 처리 복잡"
            ),
            CANDIDATE,
            "Android 앱에 표준 아키텍처"
        ));

        return alternatives;
    }

    // ── Python 언어 대안 ────────────────────────────────────────────────────

    private List<Alternative> generatePythonFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();

        boolean hasDjango    = currentFrameworks.containsKey("Django");
        boolean hasFlask     = currentFrameworks.containsKey("Flask");
        boolean hasFastApi   = currentFrameworks.containsKey("FastAPI");
        boolean hasSqlAlch   = currentFrameworks.containsKey("SQLAlchemy");
        boolean hasCelery    = currentFrameworks.containsKey("Celery");
        boolean hasPytorch   = currentFrameworks.containsKey("PyTorch");
        boolean hasTf        = currentFrameworks.containsKey("TensorFlow");
        boolean hasKeras     = currentFrameworks.containsKey("Keras");
        boolean hasPandas    = currentFrameworks.containsKey("Pandas");
        boolean hasPytest    = currentFrameworks.containsKey("pytest");
        boolean hasAsyncio   = currentFrameworks.containsKey("asyncio");
        boolean hasLangchain = currentFrameworks.containsKey("LangChain");

        // ── 웹 프레임워크 ─────────────────────────────────────────────────────

        if (hasDjango) {
            alternatives.add(new Alternative(
                "Django",
                "풀-스택 Python 웹 프레임워크",
                Arrays.asList(
                    "ORM, Admin, Auth, Migration 등 배터리 내장",
                    "DRF를 통한 REST API 단일 확장",
                    "MTV 패턴으로 빠른 CRUD 개발",
                    "성숙한 생태계와 풍부한 플러그인"
                ),
                Arrays.asList(
                    "모놀리식 구조로 마이크로서비스 전환 어려움",
                    "Flask/FastAPI 대비 상대적 중량",
                    "비동기 지원이 제한적"
                ),
                SELECTED,
                "풍부한 내장 기능과 빠른 개발로 대규모 웹 서비스에 적합"
            ));
            alternatives.add(new Alternative(
                "FastAPI",
                "고성능 비동기 Python 웹 프레임워크",
                Arrays.asList(
                    "OpenAPI 자동 문서화",
                    "Pydantic 기반 자동 검증",
                    "Node.js 수준의 비동기 성능",
                    "타입 힌트 100% 지원"
                ),
                Arrays.asList(
                    "Django 대비 자체 Admin/ORM 미제공",
                    "상대적으로 작은 생태계",
                    "비동기 코드 스타일에 익숙해야 함"
                ),
                REJECTED,
                "Django의 풍부한 모놀리식 기능과 생산성을 우선시"
            ));
            alternatives.add(new Alternative(
                "Flask",
                "경량 Python 웹 프레임워크",
                Arrays.asList(
                    "매우 가볍고 유연한 구조",
                    "마이크로서비스에 적합",
                    "신속한 프로토타이핑"
                ),
                Arrays.asList(
                    "ORM/Auth 등 추가 설정 필요",
                    "대규모 프로젝트 구조화 어려움"
                ),
                REJECTED,
                "Django의 완성도와 패턴화를 우선시"
            ));
        } else if (hasFastApi) {
            alternatives.add(new Alternative(
                "FastAPI",
                "고성능 비동기 Python API 프레임워크",
                Arrays.asList(
                    "OpenAPI/Swagger 자동 생성",
                    "Pydantic 기반 데이터 검증",
                    "async/await 네이티브 지원",
                    "높은 성능 (Starlette 기반)"
                ),
                Arrays.asList(
                    "Django Admin/ORM 같은 모노리스 기능 없음",
                    "비동기 코드 스타일 사전 지식 필요"
                ),
                SELECTED,
                "API 전용 서비스, ML 모델 서빙, 마이크로서비스에 최적"
            ));
            alternatives.add(new Alternative(
                "Django + DRF",
                "풀스택 Python 프레임워크 + REST 확장",
                Arrays.asList(
                    "Admin UI 내장",
                    "ORM 및 Migration 내장",
                    "풍부한 생태계"
                ),
                Arrays.asList(
                    "FastAPI 대비 성능 저하",
                    "가볍다고 느릴 수 있음"
                ),
                REJECTED,
                "FastAPI의 성능과 비동기 지원을 우선시"
            ));
            alternatives.add(new Alternative(
                "Flask + SQLAlchemy",
                "경량 웹 프레임워크 + ORM 조합",
                Arrays.asList(
                    "매우 가볍고 유연함",
                    "마이크로서비스에 적합"
                ),
                Arrays.asList(
                    "FastAPI 대비 타입 시스템 없음",
                    "입력 검증 비해 자동화를 지원하지 않음"
                ),
                REJECTED,
                "FastAPI의 자동 문서화와 Pydantic 검증을 우선시"
            ));
        } else if (hasFlask) {
            alternatives.add(new Alternative(
                "Flask",
                "경량 Python 웹 프레임워크",
                Arrays.asList(
                    "매우 가볍고 유연한 구조",
                    "마이크로서비스 및 소규모 API에 적합",
                    "빠른 프로토타이핑",
                    "학습 곡선 낮음"
                ),
                Arrays.asList(
                    "ORM/Auth/Admin 전부 수동 설정",
                    "대규모에서 구조화 어려움",
                    "비동기 지원 미흡"
                ),
                SELECTED,
                "소규모 초기 실험적 프로젝트, 배경 지식 서비스에 적합"
            ));
            alternatives.add(new Alternative(
                "FastAPI",
                "성능 중심 비동기 프레임워크",
                Arrays.asList(
                    "Flask와 유사한 구조로 이전 용이",
                    "Pydantic 타입 검증 자동화",
                    "OpenAPI 문서 자동 생성"
                ),
                Arrays.asList(
                    "팀 스타일 적응 필요",
                    "코루틴 사용 스타일 변경 필요"
                ),
                CONSIDERED,
                "Flask에서 성능과 타입 안전성이 더 중요해질 때 변화 검토"
            ));
        } else {
            alternatives.add(new Alternative(
                "FastAPI",
                "추천 Python 웹 API 프레임워크",
                Arrays.asList(
                    "자동 OpenAPI 문서화",
                    "Pydantic 기반 입력 검증",
                    "async/await 네이티브",
                    "높은 성능"
                ),
                Arrays.asList(
                    "비동기 코드 기초지식 필요"
                ),
                CONSIDERED,
                "Python으로 API 서버 개발 시 첫 번째 선택으로 추천"
            ));
            alternatives.add(new Alternative(
                "Django",
                "풀스택 Python 웹 프레임워크",
                Arrays.asList(
                    "Admin UI 포함",
                    "ORM/Auth/Migration 내장",
                    "빠른 CRUD 개발"
                ),
                Arrays.asList(
                    "충분한 비동기 지원 필요"
                ),
                CONSIDERED,
                "Admin/Auth이 필요한 웹 애플리케이션에 추천"
            ));
        }

        // ── ORM / 데이터베이스 ─────────────────────────────────────────────────

        if (hasSqlAlch) {
            alternatives.add(new Alternative(
                "SQLAlchemy",
                "Python 역사를 가진 ORM / SQL 툴킷",
                Arrays.asList(
                    "Core SQL 및 ORM 두 가지 모드 제공",
                    "디스크립터(declarative)/데이터클래스(dataclass) ORM",
                    "Alembic과 연계한 마이그레이션",
                    "Flask/FastAPI 등 많은 프레임워크와 통합"
                ),
                Arrays.asList(
                    "Django ORM보다 데 커피/복잡한 API",
                    "Async 지원(2.0+)이 상대적으로 최신"
                ),
                SELECTED,
                "Flask/FastAPI 환경에서 가장 조성이 보나 너리 쓰이는 ORM"
            ));
            alternatives.add(new Alternative(
                "Tortoise ORM / Databases",
                "Python async ORM",
                Arrays.asList(
                    "async/await 네이티브",
                    "FastAPI와 자연스러운 통합"
                ),
                Arrays.asList(
                    "SQLAlchemy 대비 작은 생태계",
                    "성숙도 상대적 낮음"
                ),
                REJECTED,
                "SQLAlchemy 2.0의 async 지원으로 충분"
            ));
        }

        // ── 비동기 태스크 큐 ───────────────────────────────────────────────────

        if (hasCelery) {
            alternatives.add(new Alternative(
                "Celery",
                "Python 분산 태스크 큐",
                Arrays.asList(
                    "Redis/RabbitMQ 브로커 지원",
                    "주기적 태스크와 비동기 태스크 모두 지원",
                    "Flask/Django 통합 쉬움",
                    "성숙한 생태계"
                ),
                Arrays.asList(
                    "간단한 작업에 오버코지니어링 가능",
                    "Redis 등 외부 브로커 필요"
                ),
                SELECTED,
                "Python 웹 애플리케이션의 배경 태스크 표준"
            ));
            alternatives.add(new Alternative(
                "RQ (레디스 큐)",
                "단순한 Redis 기반 태스크 큐",
                Arrays.asList(
                    "Celery 대비 간단한 API",
                    "Redis만 있으면 동작"
                ),
                Arrays.asList(
                    "Celery 대비 제한적인 기능",
                    "주기적 태스크 기능 제한"
                ),
                REJECTED,
                "Celery의 풍부한 기능과 성숙도를 우선시"
            ));
        }

        // ── ML / AI 프레임워크 ─────────────────────────────────────────────────

        if (hasPytorch) {
            alternatives.add(new Alternative(
                "PyTorch",
                "동적 계산 그래프 지원 딥러닝 프레임워크",
                Arrays.asList(
                    "연구/실험에 유연한 동적 계산 그래프",
                    "Hugging Face 등 도구와 통합 우수",
                    "Python 코드와 자연스러운 연동",
                    "GPU 가속 지원 (CUDA)"
                ),
                Arrays.asList(
                    "프로덕션 배포 시 TorchServe 등 추가 설정",
                    "TensorFlow 대비 모바일 지원 제한"
                ),
                SELECTED,
                "연구/실험 및 Hugging Face 생태계와의 통합에 아주 보나 적합"
            ));
            alternatives.add(new Alternative(
                "TensorFlow / Keras",
                "프로덕션 중심 딥러닝 프레임워크",
                Arrays.asList(
                    "TFlite로 모바일 배포",
                    "TFX 파이프라인 완성도",
                    "TPU 지원 (Google 클라우드)"
                ),
                Arrays.asList(
                    "PyTorch 대비 덕 연구 코드로서 인기 낮음",
                    "연구 실험에 대한 유연성 저하"
                ),
                REJECTED,
                "PyTorch의 동적 그래프와 연구 탐다운 이점을 우선시"
            ));
        } else if (hasTf || hasKeras) {
            alternatives.add(new Alternative(
                "TensorFlow / Keras",
                "산업군 수준 딥러닝 생태계",
                Arrays.asList(
                    "Keras로 빠른 모델링",
                    "TFX 파이프라인 완성도",
                    "모바일(TFLite)/웹(TFJS) 배포"
                ),
                Arrays.asList(
                    "PyTorch 대비 산동 연구 코드는 덩 덜됨"
                ),
                SELECTED,
                "프로덕션 배포와 다양한 플랫폼에 산업군 수준"
            ));
            alternatives.add(new Alternative(
                "PyTorch + PyTorch Lightning",
                "연구자 중심 딥러닝 프레임워크",
                Arrays.asList(
                    "동적 계산 그래프로 더 유연한 실험",
                    "Hugging Face 생태계 통합",
                    "Lightning으로 풀리코드 감소"
                ),
                Arrays.asList(
                    "TF 대비 모바일/웹 배포 연동 없음"
                ),
                CONSIDERED,
                "ML 연구 완성도가 더 높아진다면 고려"
            ));
        }

        // ── 데이터 처리 ─────────────────────────────────────────────────────────

        if (hasPandas) {
            alternatives.add(new Alternative(
                "Pandas + NumPy",
                "Python 데이터 분석 표준 스택",
                Arrays.asList(
                    "DataFrame 기반 직관적 데이터 조작",
                    "백벡토 수치 연산 최적화",
                    "sklearn / matplotlib 연계 우수"
                ),
                Arrays.asList(
                    "대용량 데이터에서 메모리 한계",
                    "단일 코어 처리 주의 필요"
                ),
                SELECTED,
                "Python 데이터 분석의 사실상 표준"
            ));
            alternatives.add(new Alternative(
                "Polars",
                "몽맥팡 Rust 기반 데이터프레임 라이브러리",
                Arrays.asList(
                    "Pandas 대비 우수한 성능",
                    "병렬처리 내장",
                    "Lazy 평가 API"
                ),
                Arrays.asList(
                    "Pandas 대비 작은 생태계",
                    "대부분의 라이브러리가 Polars API 미지원"
                ),
                REJECTED,
                "Pandas 생태계와의 호환성을 우선시"
            ));
        }

        // ── 테스팅 ──────────────────────────────────────────────────────────────

        if (!hasPytest) {
            alternatives.add(new Alternative(
                "pytest",
                "Python 테스팅 표준 프레임워크",
                Arrays.asList(
                    "간결한 픽처처 기반 테스트",
                    "풍부한 플러그인 생태계",
                    "unittest 코드와 호환"
                ),
                Arrays.asList(
                    "처음 스타일이 다를 수 있음"
                ),
                CONSIDERED,
                "Python 프로젝트 품질 보증을 위해 도입 권장"
            ));
        } else {
            alternatives.add(new Alternative(
                "pytest",
                "Python 테스팅 표준 프레임워크",
                Arrays.asList(
                    "픽처처(Fixture) 기반 테스트",
                    "parametrize로 다양한 입력 검증",
                    "풍부한 플러그인 (pytest-cov, pytest-asyncio 등)"
                ),
                Arrays.asList(
                    "unittest 대비 픽처처 개념 학습 필요"
                ),
                SELECTED,
                "Python 테스팅의 업계 표준"
            ));
        }

        // ── LangChain / AI 응용 ─────────────────────────────────────────────────

        if (hasLangchain) {
            alternatives.add(new Alternative(
                "LangChain",
                "LLM 응용 Python 프레임워크",
                Arrays.asList(
                    "Chain, Agent, Tool 추상화",
                    "Vector DB 통합 (Chroma, FAISS, Pinecone)",
                    "OpenAI/Anthropic/Mistral 등 다양한 LLM 지원"
                ),
                Arrays.asList(
                    "개념 변화 빠른 주의 필요",
                    "포키 API가 자주 리보됨"
                ),
                SELECTED,
                "LLM 기반 AI 앱 개발의 빠른 프로토타이핑에 적합"
            ));
            alternatives.add(new Alternative(
                "LlamaIndex",
                "데이터 연각 중심 RAG 프레임워크",
                Arrays.asList(
                    "RAG(Retrieval Augmented Generation) 보특화",
                    "다양한 데이터 로더(PDF, Notion, DB 등)",
                    "LangChain과 호환 가능"
                ),
                Arrays.asList(
                    "LangChain 대비 일반 Agent 누락되어 있음"
                ),
                REJECTED,
                "LangChain의 Agent/Tool 구조가 현재 요구사항에 더 적합"
            ));
        }

        // ── 아키텍처 스타일 ────────────────────────────────────────────────────

        alternatives.add(new Alternative(
            hasDjango ? "MTV (Model-Template-View)" : "Layered Architecture (Python)",
            hasDjango
                ? "Django MTV 3-레이어 패턴"
                : "Python 계층형 레이어 아키텍처",
            Arrays.asList(
                hasDjango ? "Django 관례에 따른 빠른 개발" : "Router -> Service -> Repository 레이어 분리",
                "Model 연관 로직 명확한 분리",
                "테스트 용이성"
            ),
            Arrays.asList(
                "소규모에서 오버 엔지니어링 가능",
                "레이어 간 DTO 매핑 코드 증가 가능"
            ),
            SELECTED,
            "Python 프로젝트에서 가장 회일 가능성이 내보나는 아키텍처"
        ));

        if (hasAsyncio || hasFastApi) {
            alternatives.add(new Alternative(
                "Async Microservice (Python)",
                "asyncio 기반 비동기 마이크로서비스",
                Arrays.asList(
                    "I/O 바운드 작업에 최적화",
                    "FastAPI + Celery + Redis 조합으로 확장 용이",
                    "마이크로서비스대 독립 배포 가능"
                ),
                Arrays.asList(
                    "스케쥴링 부재로 CPU 바운드 작업에 적합하지 않음",
                    "여러 서비스 관리 복잡도 증가"
                ),
                CONSIDERED,
                "많은 동시 커넥션이 필요한 서비스에서 검토"
            ));
        }

        if (hasPytorch || hasTf || hasKeras) {
            alternatives.add(new Alternative(
                "ML Pipeline Architecture",
                "Python ML 파이프라인 아키텍처",
                Arrays.asList(
                    "데이터 수집 -> 전처리 -> 학습 -> 평가 -> 배포 파이프라인",
                    "MLflow/W&B로 실험 추적",
                    "FastAPI로 ML 모델 서빙",
                    "GitHub Actions + Docker로 CI/CD"
                ),
                Arrays.asList(
                    "인프라 복잡도 높음",
                    "ML 데이터 버지닝 및 실험 관리 필요"
                ),
                SELECTED,
                "ML 프로젝트 재현성과 배포 안정성 확보"
            ));
        }

        return alternatives;
    }

    // ── PHP 언어 대안 ────────────────────────────────────────────────────────

    private List<Alternative> generatePhpFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();

        boolean hasLaravel     = currentFrameworks.containsKey("Laravel");
        boolean hasSymfony     = currentFrameworks.containsKey("Symfony");
        boolean hasWordPress   = currentFrameworks.containsKey("WordPress");
        boolean hasCodeIgniter = currentFrameworks.containsKey("CodeIgniter");
        boolean hasEloquent    = currentFrameworks.containsKey("Eloquent ORM");
        boolean hasDoctrine    = currentFrameworks.containsKey("Doctrine ORM");
        boolean hasTwig        = currentFrameworks.containsKey("Twig");
        boolean hasPhpUnit     = currentFrameworks.containsKey("PHPUnit");
        boolean hasPest        = currentFrameworks.containsKey("Pest (PHP)");
        boolean hasPassport    = currentFrameworks.containsKey("Laravel Passport");
        boolean hasSanctum     = currentFrameworks.containsKey("Laravel Sanctum");
        boolean hasSwoole      = currentFrameworks.containsKey("Swoole");
        boolean hasGuzzle      = currentFrameworks.containsKey("Guzzle HTTP");

        // ── 웹 프레임워크 ─────────────────────────────────────────────────────

        if (hasLaravel) {
            alternatives.add(new Alternative(
                "Laravel",
                "PHP 풀-스택 웹 프레임워크",
                Arrays.asList(
                    "Eloquent ORM, Artisan CLI, Blade 템플릿 내장",
                    "Sanctum/Passport 기반 API 인증 간소화",
                    "Queue, Event, Notification 등 배터리 포함",
                    "대규모 생태계와 활성 커뮤니티"
                ),
                Arrays.asList(
                    "Symfony 대비 엔터프라이즈 DI 컨테이너 추상화 부족",
                    "마법적 파사드로 테스트 어렵게 느껴질 수 있음",
                    "Symfony/Zend 대비 대기업 레거시 결합 어려움"
                ),
                SELECTED,
                "빠른 생산성과 풍부한 생태계로 웹 서비스 개발에 최적"
            ));
            alternatives.add(new Alternative(
                "Symfony",
                "PHP 엔터프라이즈 풀-스택 프레임워크",
                Arrays.asList(
                    "강력한 DI 컨테이너와 컴포넌트 재사용",
                    "Doctrine ORM 기본 통합",
                    "유연한 번들 구조로 마이크로서비스 전환 용이",
                    "대기업 레거시 시스템 통합에 강점"
                ),
                Arrays.asList(
                    "Laravel 대비 러닝 커브 높음",
                    "설정 중심 구조로 초기 개발 속도 낮음",
                    "Laravel 생태계 대비 플러그인 수가 적음"
                ),
                CONSIDERED,
                "엔터프라이즈 규모 확장 또는 Doctrine 기반 도메인 모델링 시 권장"
            ));
            alternatives.add(new Alternative(
                "Slim Framework",
                "PHP 경량 마이크로프레임워크",
                Arrays.asList(
                    "최소한의 의존성으로 REST API 최적",
                    "PSR-7/PSR-15 기반 미들웨어",
                    "Laravel 대비 낮은 메모리 사용량"
                ),
                Arrays.asList(
                    "ORM, 인증 등 추가 패키지 필요",
                    "대규모 모놀리식 앱에 부적합",
                    "생태계가 Laravel/Symfony 대비 제한적"
                ),
                REJECTED,
                "현재 프로젝트 규모에서 Laravel의 배터리-포함 기능이 더 적합"
            ));
        }

        if (hasSymfony && !hasLaravel) {
            alternatives.add(new Alternative(
                "Symfony",
                "PHP 엔터프라이즈 풀-스택 프레임워크",
                Arrays.asList(
                    "강력한 DI 컨테이너와 컴포넌트 재사용",
                    "Doctrine ORM 기본 통합",
                    "API Platform으로 REST/GraphQL API 자동화"
                ),
                Arrays.asList(
                    "초기 설정 복잡도 높음",
                    "Laravel 대비 학습 비용 높음"
                ),
                SELECTED,
                "컴포넌트 기반 아키텍처와 엔터프라이즈 확장성 우선"
            ));
            alternatives.add(new Alternative(
                "Laravel",
                "PHP 풀-스택 웹 프레임워크",
                Arrays.asList(
                    "빠른 생산성, 풍부한 1st-party 패키지",
                    "활성 커뮤니티와 Laracasts 학습 자료"
                ),
                Arrays.asList(
                    "파사드(Facade) 패턴이 DI 원칙에 어긋날 수 있음",
                    "Symfony 대비 번들 재사용성 낮음"
                ),
                CONSIDERED,
                "생산성 우선 또는 소/중규모 프로젝트 전환 시 고려"
            ));
        }

        if (hasWordPress) {
            alternatives.add(new Alternative(
                "WordPress",
                "PHP CMS/블로그 플랫폼",
                Arrays.asList(
                    "방대한 플러그인 생태계 (WooCommerce 포함)",
                    "비개발자도 콘텐츠 관리 가능",
                    "Gutenberg 블록 에디터"
                ),
                Arrays.asList(
                    "레거시 코드 기반으로 구조적 취약점 존재",
                    "플러그인 의존도 높아 유지보수 비용 증가",
                    "REST API 중심 Headless 구성 시 Laravel/Symfony 권장"
                ),
                SELECTED,
                "콘텐츠 중심 사이트와 기존 플러그인 생태계 활용에 적합"
            ));
            alternatives.add(new Alternative(
                "Statamic",
                "Laravel 기반 플랫 파일 CMS",
                Arrays.asList(
                    "데이터베이스 불필요 (플랫 파일)",
                    "Laravel 생태계 활용 가능",
                    "Antlers 템플릿 사용"
                ),
                Arrays.asList(
                    "WordPress 대비 플러그인 수 적음",
                    "기존 WordPress 콘텐츠 마이그레이션 필요"
                ),
                CONSIDERED,
                "콘텐츠 팀이 Laravel 도입을 원하거나 DB 없이 운영하고 싶을 경우 고려"
            ));
        }

        if (hasCodeIgniter) {
            alternatives.add(new Alternative(
                "CodeIgniter 4",
                "경량 PHP 웹 프레임워크",
                Arrays.asList(
                    "낮은 설정으로 빠른 시작 가능",
                    "PSR-7 지원 및 작은 풋프린트",
                    "공유 호스팅 환경과의 호환성"
                ),
                Arrays.asList(
                    "Laravel/Symfony 대비 생태계 제한적",
                    "ORM, DI 등 현대 기능이 상대적으로 부족"
                ),
                SELECTED,
                "공유 호스팅 또는 경량 앱에 적합"
            ));
            alternatives.add(new Alternative(
                "Laravel",
                "PHP 풀-스택 웹 프레임워크",
                Arrays.asList(
                    "현대적 ORM, Queue, Event 등 풍부한 기능",
                    "대규모 커뮤니티와 장기 지원"
                ),
                Arrays.asList(
                    "CodeIgniter 대비 설정 복잡도 높음",
                    "서버 요구사항이 높음"
                ),
                CONSIDERED,
                "확장성이 필요하거나 팀이 성장하는 시점에 마이그레이션 고려"
            ));
        }

        // ── ORM ──────────────────────────────────────────────────────────────

        if (hasEloquent && !hasDoctrine) {
            alternatives.add(new Alternative(
                "Eloquent ORM",
                "Laravel 내장 ActiveRecord ORM",
                Arrays.asList(
                    "직관적인 메서드 체이닝",
                    "관계 정의(hasMany, belongsTo) 간소화",
                    "Migration과 Factory 통합"
                ),
                Arrays.asList(
                    "복잡한 도메인 모델에서 도메인 로직이 모델에 혼재될 수 있음",
                    "Doctrine 대비 DDD(도메인 주도 설계) 적용 어려움"
                ),
                SELECTED,
                "Laravel 프로젝트에서 생산성 높은 ORM"
            ));
            alternatives.add(new Alternative(
                "Doctrine ORM",
                "Data Mapper 패턴 PHP ORM",
                Arrays.asList(
                    "DDD/Clean Architecture 친화적",
                    "엔티티와 DB 스키마 완전 분리",
                    "복잡한 쿼리에 강점"
                ),
                Arrays.asList(
                    "Eloquent 대비 설정과 어노테이션 복잡",
                    "Laravel 기본 스택과 통합 어려움"
                ),
                REJECTED,
                "현재 Laravel ActiveRecord 패턴이 팀 생산성에 더 유리"
            ));
        }

        if (hasDoctrine && !hasEloquent) {
            alternatives.add(new Alternative(
                "Doctrine ORM",
                "Data Mapper 패턴 PHP ORM",
                Arrays.asList(
                    "엔티티와 DB 완전 분리로 DDD 적용 용이",
                    "DBAL로 로우-레벨 쿼리 지원",
                    "Symfony와 완벽 통합"
                ),
                Arrays.asList(
                    "러닝 커브 높음",
                    "Eloquent 대비 설정 부담"
                ),
                SELECTED,
                "Symfony 기반 엔터프라이즈 도메인 모델링에 최적"
            ));
            alternatives.add(new Alternative(
                "Eloquent ORM",
                "Laravel 내장 ActiveRecord ORM",
                Arrays.asList(
                    "직관적 API와 빠른 CRUD 개발",
                    "Migration/Factory 통합"
                ),
                Arrays.asList(
                    "Doctrine 대비 DDD 적용 어려움",
                    "Symfony 기본 스택과 미통합"
                ),
                CONSIDERED,
                "팀이 ActiveRecord 패턴 전환을 원할 경우 고려"
            ));
        }

        // ── 인증 ─────────────────────────────────────────────────────────────

        if (hasSanctum) {
            alternatives.add(new Alternative(
                "Laravel Sanctum",
                "SPA/모바일용 경량 토큰 인증",
                Arrays.asList(
                    "SPA 쿠키 기반 및 토큰 기반 인증 동시 지원",
                    "설정이 간단하고 Passport보다 경량"
                ),
                Arrays.asList(
                    "OAuth2 전체 명세 미지원",
                    "타사 OAuth2 클라이언트 통합 시 제한"
                ),
                SELECTED,
                "SPA와 모바일 API 인증에 실용적인 기본 선택"
            ));
            alternatives.add(new Alternative(
                "Laravel Passport",
                "OAuth2 서버 구현 (Laravel)",
                Arrays.asList(
                    "완전한 OAuth2 명세 지원 (authorization code, PKCE)",
                    "타사 클라이언트 토큰 발급 가능"
                ),
                Arrays.asList(
                    "Sanctum 대비 설정과 운영 복잡도 높음",
                    "내부 SPA 전용 시 오버엔지니어링 가능성"
                ),
                REJECTED,
                "현재 SPA 인증 범위에서 Sanctum으로 충분히 처리 가능"
            ));
        }

        if (hasPassport && !hasSanctum) {
            alternatives.add(new Alternative(
                "Laravel Passport",
                "OAuth2 서버 구현 (Laravel)",
                Arrays.asList(
                    "완전한 OAuth2 명세 지원",
                    "타사 클라이언트 통합 및 토큰 발급"
                ),
                Arrays.asList(
                    "Sanctum 대비 운영 복잡도 높음"
                ),
                SELECTED,
                "OAuth2가 필요한 외부 클라이언트 인증에 적합"
            ));
        }

        // ── 비동기 / 고성능 ──────────────────────────────────────────────────

        if (hasSwoole) {
            alternatives.add(new Alternative(
                "OpenSwoole / Swoole",
                "PHP 비동기 코루틴 서버",
                Arrays.asList(
                    "이벤트 루프 기반 고성능 HTTP 서버",
                    "WebSocket, TCP 서버 내장",
                    "메모리 내 상태로 Nginx 없이 단독 구동"
                ),
                Arrays.asList(
                    "FPM 기반 전통 PHP와 라이프사이클 차이",
                    "공유 호스팅 지원 제한적",
                    "Swoole 지식을 팀 전체가 공유해야 함"
                ),
                SELECTED,
                "고성능 실시간 서비스 또는 WebSocket 서버 요구 시 적합"
            ));
            alternatives.add(new Alternative(
                "ReactPHP",
                "이벤트 기반 PHP 비동기 라이브러리",
                Arrays.asList(
                    "순수 PHP 비동기 I/O, 확장 불필요",
                    "Swoole 대비 도입 진입장벽 낮음",
                    "기존 Composer 패키지와 높은 호환성"
                ),
                Arrays.asList(
                    "Swoole 대비 성능 낮음",
                    "코루틴 미지원"
                ),
                CONSIDERED,
                "Swoole 도입 비용 부담 시 경량 비동기 대안"
            ));
        }

        // ── HTTP 클라이언트 ──────────────────────────────────────────────────

        if (hasGuzzle) {
            alternatives.add(new Alternative(
                "Guzzle HTTP",
                "PHP HTTP 클라이언트 라이브러리",
                Arrays.asList(
                    "동기/비동기 요청 모두 지원",
                    "미들웨어 체인 구성 가능",
                    "Laravel HTTP 파사드의 기반"
                ),
                Arrays.asList(
                    "동기 방식 사용 시 블로킹 발생",
                    "Swoole/ReactPHP 비동기 환경에서 제한적"
                ),
                SELECTED,
                "외부 API 통합을 위한 표준 PHP HTTP 클라이언트"
            ));
            alternatives.add(new Alternative(
                "Symfony HttpClient",
                "Symfony 기본 HTTP 클라이언트",
                Arrays.asList(
                    "Guzzle 없이 PSR-18 기반 경량 클라이언트",
                    "스코프별 클라이언트 설정 지원"
                ),
                Arrays.asList(
                    "Guzzle 대비 생태계/플러그인 수 적음"
                ),
                CONSIDERED,
                "Symfony 프로젝트라면 외부 의존성 제거를 위해 고려"
            ));
        }

        // ── 템플릿 엔진 ──────────────────────────────────────────────────────

        if (hasTwig) {
            alternatives.add(new Alternative(
                "Twig",
                "Symfony 기본 템플릿 엔진",
                Arrays.asList(
                    "안전한 샌드박스 실행 환경",
                    "상속/블록 기반 레이아웃",
                    "Symfony 통합 최적화"
                ),
                Arrays.asList(
                    "Blade(Laravel) 대비 PHP 문법과 차이 존재",
                    "PHP 네이티브 템플릿 대비 컴파일 단계 추가"
                ),
                SELECTED,
                "Symfony 환경에서 구조적이고 안전한 템플릿 렌더링"
            ));
            alternatives.add(new Alternative(
                "Blade (Laravel)",
                "Laravel 내장 템플릿 엔진",
                Arrays.asList(
                    "PHP 문법과 유사하고 학습 비용 낮음",
                    "컴포넌트/슬롯 기반 재사용 용이"
                ),
                Arrays.asList(
                    "Twig 대비 샌드박스 보안 기능 없음",
                    "Laravel에 종속적"
                ),
                REJECTED,
                "현재 Symfony 환경에서 Twig가 더 자연스러운 선택"
            ));
        }

        // ── 테스팅 ───────────────────────────────────────────────────────────

        if (!hasPhpUnit && !hasPest) {
            alternatives.add(new Alternative(
                "PHPUnit",
                "PHP 단위 테스팅 표준 프레임워크",
                Arrays.asList(
                    "PHP 사실상 표준 테스트 프레임워크",
                    "Laravel/Symfony 기본 통합",
                    "강력한 Mock, Assert, DataProvider 지원"
                ),
                Arrays.asList(
                    "Pest 대비 장황한 클래스 기반 문법"
                ),
                CANDIDATE,
                "PHP 단위 테스트 도입 시 가장 먼저 고려할 표준 프레임워크"
            ));
            alternatives.add(new Alternative(
                "Pest",
                "PHPUnit 기반 우아한 PHP 테스팅 DSL",
                Arrays.asList(
                    "함수형 DSL로 간결한 테스트 코드",
                    "TypeCoverage, Arch Testing 플러그인 제공",
                    "PHPUnit 위에서 동작하여 호환성 유지"
                ),
                Arrays.asList(
                    "PHPUnit 대비 역사가 짧음",
                    "팀의 함수형 스타일 학습 필요"
                ),
                CANDIDATE,
                "새 프로젝트라면 Pest로 더 간결한 테스트 코드 작성 가능"
            ));
        }

        if (hasPhpUnit) {
            alternatives.add(new Alternative(
                "PHPUnit",
                "PHP 단위 테스팅 표준 프레임워크",
                Arrays.asList(
                    "PHP 사실상 표준 테스트 프레임워크",
                    "강력한 Mock, Assert, DataProvider 지원"
                ),
                Arrays.asList(
                    "Pest 대비 장황한 클래스 기반 문법"
                ),
                SELECTED,
                "팀이 PHPUnit 방식에 익숙하고 기존 테스트 자산이 풍부함"
            ));
            alternatives.add(new Alternative(
                "Pest",
                "PHPUnit 기반 우아한 PHP 테스팅 DSL",
                Arrays.asList(
                    "함수형 DSL로 간결한 테스트 코드",
                    "PHPUnit 위에서 동작하여 점진적 마이그레이션 가능"
                ),
                Arrays.asList(
                    "기존 PHPUnit 기반 테스트 리팩토링 비용"
                ),
                CONSIDERED,
                "신규 테스트 파일부터 점진적으로 도입 가능"
            ));
        }

        // ── 아키텍처 스타일 ───────────────────────────────────────────────────

        if (hasLaravel || hasSymfony) {
            alternatives.add(new Alternative(
                "MVC Architecture",
                "Model-View-Controller PHP 아키텍처",
                Arrays.asList(
                    "Laravel/Symfony 기본 구조와 일치",
                    "팀 전체가 이미 익숙한 패턴",
                    "빠른 CRUD 개발과 유지보수"
                ),
                Arrays.asList(
                    "도메인 로직이 Controller/Model에 혼재될 수 있음",
                    "복잡한 비즈니스 규모에서 구조 붕괴 위험"
                ),
                SELECTED,
                "현재 규모에서 생산성과 구조적 명확성의 균형점"
            ));
            alternatives.add(new Alternative(
                "Hexagonal Architecture (Ports & Adapters)",
                "도메인 중심 PHP 아키텍처",
                Arrays.asList(
                    "도메인과 인프라 완전 분리",
                    "프레임워크 교체에도 도메인 유지",
                    "테스트 용이성 극대화"
                ),
                Arrays.asList(
                    "초기 구조 설계 비용 높음",
                    "소규모 팀/프로젝트에서 오버엔지니어링 가능"
                ),
                CONSIDERED,
                "팀이 DDD/Clean Architecture로 전환을 계획할 때 고려"
            ));
        }

        if (hasSwoole || currentFrameworks.containsKey("ReactPHP") || currentFrameworks.containsKey("Amp (PHP)")) {
            alternatives.add(new Alternative(
                "Async Microservice Architecture",
                "PHP 비동기 마이크로서비스 아키텍처",
                Arrays.asList(
                    "Swoole/ReactPHP로 이벤트 루프 기반 고성능 처리",
                    "서비스별 독립 배포 및 스케일링",
                    "WebSocket/gRPC 실시간 통신 지원"
                ),
                Arrays.asList(
                    "서비스 오케스트레이션 복잡도 증가",
                    "전통 PHP-FPM 생태계와 운영 방식 차이",
                    "팀 전체의 비동기 PHP 학습 필요"
                ),
                SELECTED,
                "고성능 실시간 처리가 요구되는 마이크로서비스 환경에 적합"
            ));
        }

        return alternatives;
    }

    // ── JSP 언어 대안 ────────────────────────────────────────────────────────

    private List<Alternative> generateJspFrameworkAlternatives(Map<String, Integer> currentFrameworks) {
        List<Alternative> alternatives = new ArrayList<>();

        boolean hasSpringMvc  = currentFrameworks.containsKey("Spring MVC (View)");
        boolean hasStruts     = currentFrameworks.containsKey("Struts (View)") ||
                                currentFrameworks.containsKey("Struts 2 (View)");
        boolean hasJsf        = currentFrameworks.containsKey("JavaServer Faces (JSF)");
        boolean hasJstl       = currentFrameworks.containsKey("JSTL");
        boolean hasJquery     = currentFrameworks.containsKey("jQuery");
        boolean hasThymeleaf  = currentFrameworks.containsKey("Thymeleaf (coexist)");
        boolean hasMyBatis    = currentFrameworks.containsKey("MyBatis");
        boolean hasJstlSql    = currentFrameworks.containsKey("JSTL SQL");
        boolean hasTiles      = currentFrameworks.containsKey("Apache Tiles");
        boolean hasSiteMesh   = currentFrameworks.containsKey("SiteMesh");
        boolean hasBootstrap  = currentFrameworks.containsKey("Bootstrap");
        boolean hasReact      = currentFrameworks.containsKey("React.js");
        boolean hasScriptlet  = currentFrameworks.containsKey("Scriptlet (Anti-pattern)");

        // ── 뷰 레이어 프레임워크 ─────────────────────────────────────────────────────

        if (hasSpringMvc || hasThymeleaf) {
            String status = hasSpringMvc ? SELECTED : CONSIDERED;
            alternatives.add(new Alternative(
                "JSP + Spring MVC",
                "Spring MVC 뷰 레이어 제공",
                Arrays.asList(
                    "Spring 전체 스택과 자연스러운 통합",
                    "비즈니스 로직과 포조 분리",
                    "ModelAndView 기반 컨트롤러 연동"
                ),
                Arrays.asList(
                    "Thymeleaf 대비 자연어(HTML5) 검증 불가",
                    "표현식 언어(EL) 실행시간 오류 감지 불가",
                    "데스크탑 디자이너 프리뷰 렌더링 불가"
                ),
                status,
                hasSpringMvc ? "Spring MVC 프로젝트에서 가장 표준적인 뷰 레이어" : "Spring MVC로 전환 시 권장"
            ));

            String tStatus = hasThymeleaf ? SELECTED : CONSIDERED;
            alternatives.add(new Alternative(
                "Thymeleaf",
                "Spring 공식 템플릿 엔진",
                Arrays.asList(
                    "HTML5 자연어 템플릿 - 브라우저에서 내용 확인 가능",
                    "Spring Security 통합 (sec: 속성)",
                    "Layout Dialect로 레이아웃 연동",
                    "Spring Boot 2.x+ 권장 레거시 템플릿"
                ),
                Arrays.asList(
                    "JSP대비 새로운 문법 학습 필요",
                    "컴파일 타임 템플릿 지원 없음"
                ),
                tStatus,
                hasThymeleaf ? "이미 Thymeleaf를 사용 중입니다." : "Spring Boot 3.x로 마이그레이션 시 Thymeleaf 전환 권장"
            ));
            alternatives.add(new Alternative(
                "REST API + SPA (React/Vue)",
                "Spring Boot REST API + 프론트엔드 분리",
                Arrays.asList(
                    "API 서버와 UI 완전 분리로 독립적 파이프라인",
                    "React/Vue 생태계 활용 가능",
                    "Mobile 앱 용 API 재사용 가능"
                ),
                Arrays.asList(
                    "CORS 설정 및 인증 복잡도 증가",
                    "초기 인프라 설정 투자 필요",
                    "팀에 JavaScript 역량 필요"
                ),
                CONSIDERED,
                "현대적 웹 아키텍처로 전환 시 가능한 대안"
            ));
        }

        if (hasStruts) {
            alternatives.add(new Alternative(
                "Struts 2 (View)",
                "Apache Struts 2 MVC 프레임워크",
                Arrays.asList(
                    "Action 기반 MVC 모델로 구조적 배치",
                    "OGNL 해석 표현력 풍부",
                    "Interceptor 모델로 일관된 지원 및 감시 제어"
                ),
                Arrays.asList(
                    "Struts 1.x 이후 보안 취약점 이력이 있음",
                    "Spring MVC 대비 생태계 위축",
                    "Struts 1.x는 EOL"
                ),
                SELECTED,
                "기존 Struts 기반 시스템 유지보수 시 적용"
            ));
            alternatives.add(new Alternative(
                "Spring MVC",
                "Spring 생태계 주요 MVC 프레임워크",
                Arrays.asList(
                    "Struts2 대비 활성화된 생태계",
                    "Spring Boot와의 연동으로 빠른 설정",
                    "RESTful API 지원 용이"
                ),
                Arrays.asList(
                    "Struts2 코드베이스로부터 마이그레이션 비용"
                ),
                CONSIDERED,
                "신규 기능 개발 또는 팀원 충원 시점에 Spring MVC 전환 검토"
            ));
        }

        if (hasJsf) {
            alternatives.add(new Alternative(
                "JavaServer Faces (JSF)",
                "Jakarta EE 컴포넌트 UI 프레임워크",
                Arrays.asList(
                    "컴포넌트 기반 재사용 (RichFaces, PrimeFaces)",
                    "Managed Bean으로 이벤트 처리",
                    "Jakarta EE 표준 스펙"
                ),
                Arrays.asList(
                    "렌더링 주기가 복잡하여 테스트 어렵고 무거움",
                    "Spring MVC 대비 상대적으로 폐쇄적 프레임워크",
                    "현대 SPA 대비 상태 관리 어려움"
                ),
                SELECTED,
                "Jakarta EE 환경에서 컴포넌트 재사용이 운영 목표일 모든 환경에서"
            ));
            alternatives.add(new Alternative(
                "Spring MVC + Thymeleaf",
                "Spring 기반 템플릿 엔진",
                Arrays.asList(
                    "JSF 대비 단순하고 테스트하기 쉬운 구조",
                    "Spring 여타 컴포넌트와 자연스러운 통합",
                    "HTML5 자연어 템플릿"
                ),
                Arrays.asList(
                    "JSF 컴포넌트 생태계(PrimeFaces 등) 미사용"
                ),
                CONSIDERED,
                "Spring Boot로 전환이 편한 팀이라면 전환 고려"
            ));
        }

        // ── JSTL / 표현식 언어 ────────────────────────────────────────────────

        if (hasJstl && hasScriptlet) {
            alternatives.add(new Alternative(
                "JSTL + EL (Scriptlet 제거)",
                "JSP 표현식 언어 및 표준 태그 라이브러리",
                Arrays.asList(
                    "스크립틀릿 Java 코드 제거로 관심사 분리",
                    "EL 표현식 ${...}으로 데이터 접근",
                    "JSTL <c:forEach>, <c:if> 등 표준화된 로직"
                ),
                Arrays.asList(
                    "스크립틀릿 Java 코드 제거 필요",
                    "현대 템플릿 엔진 대비 기능 제한적"
                ),
                CANDIDATE,
                "기존 JSP 코드에서 스크립틀릿을 제거하는 리팩토링 우선 권장"
            ));
            alternatives.add(new Alternative(
                "Thymeleaf",
                "Spring 생태계 권장 템플릿 엔진",
                Arrays.asList(
                    "HTML5 자연어 템플릿 - 스크립틀릿 불필요",
                    "Spring Boot 3.x 기본 템플릿 엔진",
                    "디자이너도 브라우저로 확인 가능"
                ),
                Arrays.asList(
                    "JSP 와 다른 문법 학습 필요",
                    "JSP보다 제어 표현 불가능"
                ),
                CONSIDERED,
                "포조/View 전도를 Thymeleaf로의 전환 검토"
            ));
        }

        // ── 레이아웃 / 데코레이터 ────────────────────────────────────────────────

        if (hasTiles || hasSiteMesh) {
            alternatives.add(new Alternative(
                "Apache Tiles / SiteMesh",
                "JSP 레이아웃 컴포지트 프레임워크",
                Arrays.asList(
                    "공통 헤더/푸터를 레이아웃으로 분리",
                    "여러 페이지에 걸친 관심사 분리",
                    "Spring MVC와 연동"
                ),
                Arrays.asList(
                    "Thymeleaf Layout Dialect 대비 파일 수 많음",
                    "추가 설정 파일 필요"
                ),
                SELECTED,
                "기존 타일 기반 레이아웃 유지보수 시 적용"
            ));
            alternatives.add(new Alternative(
                "Thymeleaf Layout Dialect",
                "Thymeleaf 레이아웃 확장",
                Arrays.asList(
                    "Tiles/SiteMesh 대비 단순한 계층적 구조",
                    "진정한 HTML5 템플릿으로 레이아웃 지원"
                ),
                Arrays.asList(
                    "Thymeleaf로 전환 시 템플릿 재작성 필요"
                ),
                CONSIDERED,
                "Thymeleaf로 전환 시 적용 가능한 레이아웃 대안"
            ));
        }

        // ── 데이터베이스 접근 ────────────────────────────────────────────────

        if (hasJstlSql || hasMyBatis) {
            alternatives.add(new Alternative(
                "JSTL SQL (Legacy)",
                "JSTL 내에서 JSP에서 SQL 직접 실행",
                Arrays.asList(
                    "빠른 프로토타입에 사용 편리",
                    "별도 DAO 없이 동작 성능"
                ),
                Arrays.asList(
                    "SQL이 뷰에 노출되는 청각적 울림",
                    "보안 취약점 (SQL 인젝션 가능성)",
                    "실제 운영 환경에서 강력히 지양"
                ),
                SELECTED,
                "JSTL SQL은 레거시 코드에서만 유지, 리팩토링 필수"
            ));

            String mStatus = hasMyBatis ? SELECTED : CANDIDATE;
            alternatives.add(new Alternative(
                "MyBatis",
                "SQL 매퍼 기반 ORM",
                Arrays.asList(
                    "SQL을 XML/어노테이션으로 정의, MVC 분리",
                    "동적 SQL 작성 용이",
                    "JSTL SQL 대비 보안 관리 용이"
                ),
                Arrays.asList(
                    "JPA 대비 테이블-컬럼 매핑 업무"
                ),
                mStatus,
                hasMyBatis ? "이미 MyBatis를 사용 중입니다." : "JSTL SQL 제거 후 Service/DAO 레이어 도입 시 권장"
            ));
            alternatives.add(new Alternative(
                "Spring Data JPA",
                "JPA 기반 데이터 접근 레이어",
                Arrays.asList(
                    "엔티티 기반 ORM으로 SQL 작성 감소",
                    "Spring 전체 스택과 중앙식 통합",
                    "자동 쿼리 생성 (findByXxx 메서드)"
                ),
                Arrays.asList(
                    "MyBatis 대비 동적 SQL 작성 어려움"
                ),
                CONSIDERED,
                "Spring Boot 환경에서 데이터 접근 레이어 표준화"
            ));
        }

        // ── 프론트엔드 ──────────────────────────────────────────────────────

        if (hasJquery && !hasReact) {
            alternatives.add(new Alternative(
                "jQuery",
                "JavaScript DOM 조작 라이브러리",
                Arrays.asList(
                    "기존 JSP 프론트엔드와 쉬운 통합",
                    "레거시 브라우저 호환성"
                ),
                Arrays.asList(
                    "단방향 데이터 바인딩 미지원",
                    "컴포넌트 기반 아키텍처 어려움"
                ),
                SELECTED,
                "기존 JSP 프론트엔드 AJAX 연동에 실무적 기본 선택"
            ));
            alternatives.add(new Alternative(
                "Vue.js",
                "초보자 친화적 프론트엔드 프레임워크",
                Arrays.asList(
                    "컴포넌트 기반 UI 반응형 아키텍처",
                    "통합 렌더링(SSR) 가능 (Nuxt.js)",
                    "JSP 레거시와 혁신적 통합 가능 (단순 컴포넌트로)"
                ),
                Arrays.asList(
                    "팀의 새 프론트엔드 프레임워크 학습 필요",
                    "Spring API 분리 구조 전환 필요 가능성"
                ),
                CONSIDERED,
                "jQuery 의존 코드를 점진적으로 Vue 컴포넌트로 전환 가능"
            ));
        }

        if (hasBootstrap) {
            alternatives.add(new Alternative(
                "Bootstrap",
                "CSS 그리드 기반 UI 프레임워크",
                Arrays.asList(
                    "공식 컴포넌트 키트 제공",
                    "반응형 디자인 기본 지원"
                ),
                Arrays.asList(
                    "커스텀 디자인 적용 어려움",
                    "Tailwind CSS 대비 유틸리티 중심 디자인 분리"
                ),
                SELECTED,
                "현재 JSP 화면의 반응형 UI 기반 프레임워크"
            ));
        }

        // ── 아키텍처 ───────────────────────────────────────────────────────

        if (hasSpringMvc || hasStruts || hasJsf) {
            alternatives.add(new Alternative(
                "JSP/Servlet MVC Architecture",
                "Java 웹 애플리케이션 기본 MVC 아키텍처",
                Arrays.asList(
                    "제어 흐름의 명확한 문서화",
                    "세션/요청 모델 기반 상태 관리",
                    "Web.xml 기반 서블릿 및 필터 설정"
                ),
                Arrays.asList(
                    "레거시 XML 설정 부담",
                    "마이크로서비스 전환시 복잡도 증가"
                ),
                SELECTED,
                "현재 Java EE/Spring MVC 환경에서 구조적 적합성 확인"
            ));
            alternatives.add(new Alternative(
                "Spring Boot REST API Architecture",
                "Spring Boot RESTful API 백엔드 + SPA 프론트엔드",
                Arrays.asList(
                    "JSP 의존 제거로 프론트엔드 독립성 확보",
                    "OpenAPI/Swagger 자동 문서화",
                    "모바일 앱 API 재활용 가능"
                ),
                Arrays.asList(
                    "JSP 코드베이스 마이그레이션 비용 있음",
                    "세션 기반 인증에서 JWT 기반으로 인증 변경 필요"
                ),
                CONSIDERED,
                "신규 기능 개발 또는 장기 투자 시점에 전환 검토"
            ));
        }

        return alternatives;
    }
}
