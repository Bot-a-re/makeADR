# ADR Generator 사용 가이드

## 개요

ADR Generator는 ZIP 파일로 압축된 Java 소스코드를 분석하여 Architecture Decision Record (ADR) 문서를 자동으로 생성하는 CLI 도구입니다.

## 주요 기능

### 1. 소스코드 분석
- **다중 언어 및 고성능 파싱**: Java, C#, JS/TS, C/C++, Ruust, Kotlin 등 지원. 특히 Java는 **JavaParser**를 통합하여 AST 기반의 정밀 분석 수행
- **패키지 구조 분석**: 패키지 구조 및 모듈 구성 파악
- **의존성 분석**: import 문 및 의존성 정의 파일을 통한 관계 추적
- **프레임워크 감지**: 주요 언어별 프레임워크 자동 감지
- **디자인 패턴 감지**: AST 분석(Java) 및 패턴 매칭을 통한 디자인 패턴 식별
- **데이터베이스/API 분석**: 스키마 및 엔드포인트 자동 추출

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
run.bat <input-path> [output-directory] [--serve] [--debug]
```

- `<input-path>`: 프로젝트 ZIP 파일 또는 프로젝트 루트 디렉토리 경로
- `--serve`: 생성 후 로컬 미리보기 웹 서버 시작 (http://localhost:8080)

### 예제
```bash
# 현재 디렉토리의 output 폴더에 생성
run.bat project-source.zip

# 지정된 디렉토리에 생성
run.bat project-source.zip ./my-output
```

## 분석 대상 준비

### 방법 1: 디렉토리 직접 분석 (권장)
별도의 압축 과정 없이 프로젝트 디렉토리를 바로 분석할 수 있습니다.
```bash
run.bat C:\Projects\MyProject
```

### 방법 2: ZIP 파일 사용
프로젝트 폴더를 ZIP으로 압축하여 입력으로 전달할 수 있습니다.
```powershell
Compress-Archive -Path "MyProject\*" -DestinationPath "project-source.zip"
```

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

### 로컬 미리보기 서버 (`--serve`)
`--serve` 옵션을 사용하여 실행하면 로컬 웹 서버가 시작됩니다.
1. `run.bat my-project --serve` 실행
2. 브라우저에서 `http://localhost:8080` 접속
3. 생성된 ADR 목록 확인 및 실시간 Mermaid 렌더링 확인

### GitHub 호스팅
- GitHub 리포지토리에 업로드하면 자동으로 렌더링됩니다.

## 분석 범위

ADR Generator는 다음 항목을 분석합니다:

| 분석 항목 | 설명 |
|----------|------|
| 패키지 구조 | package / namespace 선언 분석 |
| 모듈 의존성 | import / using / require 분석 |
| 프레임워크 | Spring, Express, ASP.NET, Rails 등 |
| 디자인 패턴 | JavaParser 기반 AST 분석 및 다국어 패턴 매칭 |
| 데이터베이스 | ORM Entity 및 SQL 문 분석 |
| API 엔드포인트 | Web 프레임워크 어노테이션 및 라우팅 분석 |

## CI/CD 통합

`.github/workflows/adr-generation.yml` 템플릿을 참고하여 GitHub Actions에 통합할 수 있습니다. PR이 생성될 때마다 아키텍처 변경 사항을 자동으로 문서화할 수 있습니다.

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
