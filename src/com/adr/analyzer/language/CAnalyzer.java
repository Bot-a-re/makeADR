package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * C 언어 분석기
 * .c, .h 파일을 분석하여 구조체, 함수, 라이브러리 의존성 등을 추출
 */
public class CAnalyzer implements LanguageAnalyzer {

    // #include 분석
    private static final Pattern INCLUDE_SYSTEM_PATTERN =
            Pattern.compile("^\\s*#include\\s*<([^>]+)>");
    private static final Pattern INCLUDE_LOCAL_PATTERN =
            Pattern.compile("^\\s*#include\\s*\"([^\"]+)\"");

    // 구조체/열거형/공용체
    private static final Pattern STRUCT_PATTERN =
            Pattern.compile("\\b(struct|enum|union)\\s+(\\w+)\\s*\\{");

    // 함수 정의 (반환형 함수명(파라미터) { )
    private static final Pattern FUNCTION_DEF_PATTERN =
            Pattern.compile("^[a-zA-Z_][\\w\\s\\*]+\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{",
                    Pattern.MULTILINE);

    // typedef
    private static final Pattern TYPEDEF_PATTERN =
            Pattern.compile("\\btypedef\\b");

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".c", ".h"};
    }

    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        // 모듈(디렉토리) 추출
        analyzeModule(fileName, result);

        // 구조체/열거형을 클래스에 준하여 카운트
        result.setClassCount(result.getClassCount() + countStructsAndEnums(content));

        // 의존성 분석 (#include)
        analyzeDependencies(fileName, content, result);

        // 라이브러리/프레임워크 감지
        detectFrameworks(content, result);

        // 디자인 패턴 감지
        detectPatterns(fileName, content, result);

        // 데이터베이스 분석
        analyzeDatabases(content, result);

        // API 분석 (소켓/HTTP 라이브러리 기반)
        analyzeApis(content, result);
    }

    // ── 모듈 ──────────────────────────────────────────────────────────────────

    private void analyzeModule(String fileName, AnalysisResult result) {
        String module = extractModuleName(fileName);
        result.addPackage(module);
    }

    // ── 구조체/열거형 카운트 ──────────────────────────────────────────────────

    private int countStructsAndEnums(String content) {
        Matcher m = STRUCT_PATTERN.matcher(content);
        int count = 0;
        while (m.find()) count++;

        // typedef 도 타입 정의로 간주
        Matcher td = TYPEDEF_PATTERN.matcher(content);
        while (td.find()) count++;

        return count;
    }

    // ── 의존성 (#include) ─────────────────────────────────────────────────────

    private void analyzeDependencies(String fileName, String content, AnalysisResult result) {
        String currentModule = extractModuleName(fileName);
        String[] lines = content.split("\n");

        for (String line : lines) {
            // 시스템 헤더
            Matcher sys = INCLUDE_SYSTEM_PATTERN.matcher(line);
            if (sys.find()) {
                String header = sys.group(1);
                // 표준 라이브러리는 의존성으로 기록하지 않고 프레임워크 감지에서 처리
                continue;
            }
            // 로컬 헤더
            Matcher loc = INCLUDE_LOCAL_PATTERN.matcher(line);
            if (loc.find()) {
                String header = loc.group(1);
                result.addDependency(new DependencyInfo(currentModule, header, "#include"));
            }
        }
    }

    // ── 프레임워크/라이브러리 감지 ────────────────────────────────────────────

    private void detectFrameworks(String content, AnalysisResult result) {
        // 표준 라이브러리
        if (content.contains("<stdio.h>"))   result.addFramework("C Standard I/O (stdio.h)");
        if (content.contains("<stdlib.h>"))  result.addFramework("C Standard Library (stdlib.h)");
        if (content.contains("<string.h>"))  result.addFramework("C String Library (string.h)");
        if (content.contains("<math.h>"))    result.addFramework("C Math Library (math.h)");
        if (content.contains("<pthread.h>")) result.addFramework("POSIX Threads (pthread)");
        if (content.contains("<unistd.h>"))  result.addFramework("POSIX API (unistd.h)");
        if (content.contains("<sys/socket.h>") || content.contains("<winsock2.h>"))
            result.addFramework("Socket API");

        // 네트워킹/HTTP
        if (content.contains("libcurl") || content.contains("<curl/curl.h>"))
            result.addFramework("libcurl");
        if (content.contains("libuv") || content.contains("<uv.h>"))
            result.addFramework("libuv");

        // 데이터베이스
        if (content.contains("<sqlite3.h>"))   result.addFramework("SQLite3");
        if (content.contains("<mysql.h>") || content.contains("mysql_"))
            result.addFramework("MySQL C API");
        if (content.contains("<libpq-fe.h>"))  result.addFramework("PostgreSQL (libpq)");

        // 테스팅
        if (content.contains("CUnit") || content.contains("<CUnit/"))
            result.addFramework("CUnit");
        if (content.contains("cmocka") || content.contains("<cmocka.h>"))
            result.addFramework("cmocka");
        if (content.contains("check.h") || content.contains("START_TEST"))
            result.addFramework("Check");

        // 빌드 시스템 힌트
        if (content.contains("cmake_minimum_required") || content.contains("CMakeLists"))
            result.addFramework("CMake");
        if (content.contains("AUTOMAKE") || content.contains("AC_INIT"))
            result.addFramework("Autotools");

        // OpenMP / MPI
        if (content.contains("<omp.h>") || content.contains("#pragma omp"))
            result.addFramework("OpenMP");
        if (content.contains("<mpi.h>") || content.contains("MPI_Init"))
            result.addFramework("MPI");

        // OpenGL / Vulkan
        if (content.contains("<GL/gl.h>") || content.contains("<OpenGL/gl.h>"))
            result.addFramework("OpenGL");
        if (content.contains("<vulkan/vulkan.h>"))
            result.addFramework("Vulkan");
    }

    // ── 디자인 패턴 감지 ─────────────────────────────────────────────────────

    private void detectPatterns(String fileName, String content, AnalysisResult result) {
        String lower = fileName.toLowerCase();
        String baseName = extractBaseName(fileName);

        // Singleton (전역 static 포인터 + 초기화 함수)
        if (content.contains("static ") && content.contains("getInstance") ||
                content.matches("(?s).*static\\s+\\w+\\s*\\*\\s*instance\\s*=.*")) {
            result.addDesignPattern("Singleton", baseName);
        }

        // Factory (create_ 접두사 함수)
        if (content.matches("(?s).*\\w+\\s*\\*\\s*create_\\w+\\s*\\(.*")) {
            result.addDesignPattern("Factory", baseName);
        }

        // Observer (callback 함수 포인터)
        if (content.contains("(*callback)") || content.contains("callback_fn") ||
                content.matches("(?s).*void\\s*\\(\\*\\w+\\)\\s*\\(.*")) {
            result.addDesignPattern("Observer/Callback", baseName);
        }

        // State Machine
        if (content.contains("switch") && content.contains("state") &&
                content.matches("(?s).*case\\s+\\w+_STATE.*")) {
            result.addDesignPattern("State Machine", baseName);
        }

        // Module pattern (헤더 파일 기반 캡슐화)
        if (lower.endsWith(".h") && content.contains("#ifndef") && content.contains("#define")) {
            result.addDesignPattern("Header Guard / Module", baseName);
        }

        // 파일명 기반 패턴
        if (lower.contains("util") || lower.contains("helper"))
            result.addDesignPattern("Utility", baseName);
        if (lower.contains("test") || lower.contains("spec"))
            result.addDesignPattern("Test", baseName);
    }

    // ── 데이터베이스 분석 ─────────────────────────────────────────────────────

    private void analyzeDatabases(String content, AnalysisResult result) {
        if (content.contains("sqlite3_exec") || content.contains("sqlite3_prepare")) {
            Pattern tbl = Pattern.compile("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)",
                    Pattern.CASE_INSENSITIVE);
            Matcher m = tbl.matcher(content);
            while (m.find()) {
                result.addDatabaseSchema("Table: " + m.group(1) + " (SQLite3)");
            }
        }
        if (content.contains("mysql_query") || content.contains("PQexec")) {
            Pattern tbl = Pattern.compile("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)",
                    Pattern.CASE_INSENSITIVE);
            Matcher m = tbl.matcher(content);
            while (m.find()) {
                result.addDatabaseSchema("Table: " + m.group(1) + " (C DB API)");
            }
        }
    }

    // ── API 분석 ─────────────────────────────────────────────────────────────

    private void analyzeApis(String content, AnalysisResult result) {
        // libcurl HTTP 요청
        if (content.contains("curl_easy_setopt") && content.contains("CURLOPT_URL")) {
            Pattern urlPat = Pattern.compile("CURLOPT_URL,\\s*\"([^\"]+)\"");
            Matcher m = urlPat.matcher(content);
            while (m.find()) {
                result.addApiEndpoint("HTTP (libcurl): " + m.group(1));
            }
        }

        // 소켓 서버 (bind + listen)
        if (content.contains("bind(") && content.contains("listen(")) {
            result.addApiEndpoint("TCP Socket Server (C Socket API)");
        }

        // HTTP 서버 (mongoose.h 등)
        if (content.contains("mg_http_listen") || content.contains("mg_listen")) {
            result.addApiEndpoint("HTTP Server (Mongoose C)");
        }
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private String extractModuleName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        return parts.length > 1 ? parts[parts.length - 2] : "root";
    }

    private String extractBaseName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        String name = parts[parts.length - 1];
        return name.replaceAll("\\.(c|h)$", "");
    }
}
