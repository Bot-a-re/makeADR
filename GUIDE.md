# ADR Generator 사용 가이드

## 개요

ADR Generator는 ZIP 파일로 압축된 Java 소스코드를 분석하여 Architecture Decision Record (ADR) 문서를 자동으로 생성하는 CLI 도구입니다.

## 주요 기능

### 1. 소스코드 분석
- **패키지 구조 분석**: Java 패키지 구조 및 모듈 구성 파악
- **의존성 분석**: import 문을 통한 모듈 간 의존 관계 추적
- **프레임워크 감지**: Spring, JPA, Hibernate 등 사용된 프레임워크 자동 감지
- **디자인 패턴 감지**: Singleton, Factory, Repository 등 적용된 디자인 패턴 식별
- **데이터베이스 스키마 분석**: JPA Entity 및 SQL DDL 분석
- **API 엔드포인트 분석**: Spring MVC 어노테이션 기반 REST API 추출

### 2. ADR 문서 생성
자동 생성되는 ADR 문서에는 다음 내용이 포함됩니다:

- 📋 **프로젝트 개요 및 통계**
- 🏗️ **아키텍처 결정사항** (아키텍처 스타일, 계층 구조)
- 📦 **모듈 구조** (패키지별 클래스 수)
- 🛠️ **기술 스택** (사용된 프레임워크 및 라이브러리)
- 🎨 **디자인 패턴** (적용된 패턴 및 해당 클래스)
- 🗄️ **데이터베이스 설계** (테이블 목록)
- 🌐 **API 설계** (엔드포인트 목록)
- 📊 **아키텍처 다이어그램** (Mermaid 차트)
  - 데이터 흐름도
  - 모듈 구성도
  - 클래스 다이어그램
- ⚠️ **아키텍처 위험 요소 및 권장사항**
- 📝 **결론 및 다음 단계**

### 3. 위험 요소 분석
다음과 같은 아키텍처 위험 요소를 자동으로 식별합니다:

- 높은 의존성 복잡도
- 과도한 프레임워크 사용
- 디자인 패턴 부재
- 데이터 접근 계층 미분리
- 복잡한 데이터베이스 스키마
- 과도한 API 엔드포인트
- 낮은 모듈화 수준
- 대규모 모놀리식 구조

각 위험 요소에 대해 심각도(LOW, MEDIUM, HIGH, CRITICAL)와 권장사항을 제공합니다.

## 설치 및 실행

### 요구사항
- JDK 25 (c:\jdk-25.0.2)

### 빌드
```bash
compile.bat
```

### 실행
```bash
run.bat <zip-file> [output-directory]
```

### 예제
```bash
# 현재 디렉토리의 output 폴더에 생성
run.bat project-source.zip

# 지정된 디렉토리에 생성
run.bat project-source.zip ./my-output
```

## ZIP 파일 준비

### 방법 1: PowerShell 사용
```powershell
Compress-Archive -Path "프로젝트폴더\*" -DestinationPath "project-source.zip"
```

### 방법 2: 탐색기 사용
1. 프로젝트 폴더를 마우스 우클릭
2. "압축" → "ZIP 파일로 압축" 선택

## 출력 결과

실행이 완료되면 다음과 같은 파일이 생성됩니다:

```
output/
└── ADR-YYYYMMDD-HHMMSS.md
```

생성된 Markdown 파일은 다음 도구로 열람할 수 있습니다:
- Visual Studio Code (Markdown Preview Enhanced 확장 추천)
- Typora
- GitHub/GitLab (Mermaid 차트 자동 렌더링)
- 기타 Markdown 뷰어

## Mermaid 차트 렌더링

생성된 ADR 문서에는 Mermaid 형식의 다이어그램이 포함되어 있습니다.

### GitHub/GitLab
- 자동으로 렌더링됩니다.

### VS Code
1. "Markdown Preview Enhanced" 확장 설치
2. Ctrl+Shift+V로 미리보기 열기

### 온라인 뷰어
- https://mermaid.live/ 에서 코드 복사/붙여넣기

## 분석 범위

ADR Generator는 다음 항목을 분석합니다:

| 분석 항목 | 설명 |
|----------|------|
| 패키지 구조 | package 선언 분석 |
| 모듈 의존성 | import 문 분석 |
| 프레임워크 | Spring, JPA, Hibernate, JUnit 등 |
| 디자인 패턴 | Singleton, Factory, Builder, Observer, Strategy, Repository, Service, DTO 등 |
| 데이터베이스 | @Entity, @Table, CREATE TABLE 등 |
| API 엔드포인트 | @GetMapping, @PostMapping 등 Spring MVC 어노테이션 |

## 제한사항

- Java 소스코드만 분석 가능
- 컴파일된 .class 파일은 분석하지 않음
- 외부 라이브러리 의존성은 import 문을 통해서만 파악
- 런타임 동작은 분석하지 않음

## 문제 해결

### "ZIP 파일을 찾을 수 없습니다" 오류
- ZIP 파일 경로가 올바른지 확인
- 절대 경로 사용 권장: `run.bat d:\path\to\project.zip`

### "Java 파일이 발견되지 않음"
- ZIP 파일 내부에 .java 파일이 있는지 확인
- ZIP 파일 구조가 올바른지 확인

### 한글 깨짐
- UTF-8 인코딩을 지원하는 에디터 사용
- VS Code, Notepad++ 등 권장

## 라이선스

MIT License

## 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.
