# ADR Generator

ZIP 파일로부터 소스코드를 분석하여 Architecture Decision Record (ADR) 문서를 자동 생성하는 Java CLI 도구입니다.

## 기능

- **다중 언어 지원**: Java, C#, JavaScript, TypeScript, C, C++, Ruby, **Rust, Kotlin** 프로젝트 분석
- **Explainabile ADR 프레임워크**: Explainable Architectural Decision Record 지원
  - 🎯 명시적 의사결정 문장
  - 🔄 자동 대안 분석 (선택/거부 이유)
  - 📝 5W1H 구조화된 설명
  - 👥 이해관계자 중심 문서화
- ZIP 파일 내 소스코드 자동 분석
- ADR 문서 자동 생성 (Markdown 형식)
- 아키텍처 위험 요소 분석 및 설명
- Mermaid 차트를 통한 데이터 흐름도 생성
- 모듈 구성도 시각화

## 지원 언어

| 언어 | 확장자 | 지원 프레임워크/라이브러리 |
|------|--------|---------------------------|
| **Java** | `.java` | Spring, JPA, Hibernate, JUnit |
| **C#** | `.cs` | ASP.NET Core, Entity Framework, xUnit, NUnit |
| **JavaScript** | `.js`, `.jsx` | React, Vue, Express, Next.js |
| **TypeScript** | `.ts`, `.tsx` | Angular, NestJS, TypeORM, Prisma |
| **C** | `.c`, `.h` | libcurl, SQLite3, pthread, OpenMP, MPI, OpenGL |
| **C++** | `.cpp`, `.cc`, `.cxx`, `.hpp` | Boost, Qt, OpenCV, gRPC, gtest, Catch2, CUDA, SFML |
| **Ruby** | `.rb`, `.rake`, `Gemfile` | Rails, Sinatra, Grape, RSpec, Sidekiq, Devise, Mongoid |
| **Rust** | `.rs`, `Cargo.toml` | Tokio, Axum, Actix-Web, Rocket, Serde, Diesel, SQLx, Tonic (gRPC), Clap, Rayon |
| **Kotlin** | `.kt`, `.kts`, `build.gradle.kts` | Spring Boot, Ktor, Vert.x, Coroutines, Exposed, Koin, Dagger/Hilt, Jetpack Compose, Kotest, MockK |


## 분석 항목

1. **패키지 구조 및 모듈 의존성**
2. **사용된 프레임워크/라이브러리**
3. **디자인 패턴**
4. **데이터베이스 스키마**
5. **API 엔드포인트**
6. **아키텍처 위험 요소**

## 요구사항
- Windows 10/11
- JDK 25 (c:\jdk-25.0.2)

## 사용법

```bash
java -cp bin com.adr.Main <input-zip-file> [output-directory]
```

### 예제

```bash
java -cp bin com.adr.Main project-source.zip ./output
```

## 출력

- `ADR-<timestamp>.md`: 생성된 ADR 문서
- 문서 내 Mermaid 차트 포함

## 프로젝트 구조

```
makeADR/
├── src/
│   └── com/
│       └── adr/
│           ├── Main.java                    # 메인 진입점
│           ├── analyzer/
│           │   ├── ZipExtractor.java        # ZIP 파일 추출
│           │   ├── SourceAnalyzer.java      # 소스코드 분석 총괄
│           │   ├── PackageAnalyzer.java     # 패키지 구조 분석
│           │   ├── DependencyAnalyzer.java  # 의존성 분석
│           │   ├── FrameworkDetector.java   # 프레임워크 감지
│           │   ├── PatternDetector.java     # 디자인 패턴 감지
│           │   ├── DatabaseAnalyzer.java    # DB 스키마 분석
│           │   └── ApiAnalyzer.java         # API 엔드포인트 분석
│           ├── model/
│           │   ├── AnalysisResult.java      # 분석 결과 모델
│           │   ├── ModuleInfo.java          # 모듈 정보
│           │   ├── DependencyInfo.java      # 의존성 정보
│           │   └── RiskInfo.java            # 위험 요소 정보
│           ├── generator/
│           │   ├── AdrGenerator.java        # ADR 문서 생성
│           │   ├── MermaidGenerator.java    # Mermaid 차트 생성
│           │   └── RiskAnalyzer.java        # 위험 요소 분석
│           └── util/
│               ├── FileUtil.java            # 파일 유틸리티
│               └── StringUtil.java          # 문자열 유틸리티
├── bin/                                      # 컴파일된 클래스 파일
├── output/                                   # 생성된 ADR 문서
├── compile.bat                               # Windows 컴파일 스크립트
├── run.bat                                   # Windows 실행 스크립트
└── README.md
```

## 빌드

```bash
compile.bat
```

## 라이선스

MIT License


