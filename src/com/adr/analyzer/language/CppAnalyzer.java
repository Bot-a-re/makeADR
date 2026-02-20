package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * C++ 언어 분석기
 * .cpp, .cc, .cxx, .hpp, .h 파일을 분석하여
 * 클래스, 네임스페이스, 라이브러리 의존성, 디자인 패턴 등을 추출
 */
public class CppAnalyzer implements LanguageAnalyzer {

    private static final Pattern INCLUDE_SYSTEM_PATTERN =
            Pattern.compile("^\\s*#include\\s*<([^>]+)>", Pattern.MULTILINE);
    private static final Pattern INCLUDE_LOCAL_PATTERN =
            Pattern.compile("^\\s*#include\\s*\"([^\"]+)\"", Pattern.MULTILINE);
    private static final Pattern NAMESPACE_PATTERN =
            Pattern.compile("\\bnamespace\\s+(\\w+)\\s*\\{");
    private static final Pattern CLASS_PATTERN =
            Pattern.compile("\\b(class|struct|enum\\s+class|enum|union|interface)\\s+(\\w+)");
    private static final Pattern TEMPLATE_PATTERN =
            Pattern.compile("\\btemplate\\s*<");

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".cpp", ".cc", ".cxx", ".hpp", ".c++"};
    }

    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        analyzeNamespace(content, result);
        result.setClassCount(result.getClassCount() + countClasses(content));
        analyzeDependencies(fileName, content, result);
        detectFrameworks(content, result);
        detectPatterns(fileName, content, result);
        analyzeDatabases(content, result);
        analyzeApis(content, result);
    }

    // ── 네임스페이스 ──────────────────────────────────────────────────────────

    private void analyzeNamespace(String content, AnalysisResult result) {
        Matcher m = NAMESPACE_PATTERN.matcher(content);
        while (m.find()) {
            String ns = m.group(1);
            if (!ns.equals("std") && !ns.equals("detail") && !ns.equals("impl")) {
                result.addPackage(ns);
            }
        }
    }

    // ── 클래스 카운트 ─────────────────────────────────────────────────────────

    private int countClasses(String content) {
        Matcher m = CLASS_PATTERN.matcher(content);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    // ── 의존성 (#include) ─────────────────────────────────────────────────────

    private void analyzeDependencies(String fileName, String content, AnalysisResult result) {
        String currentModule = extractModuleName(fileName);

        Matcher loc = INCLUDE_LOCAL_PATTERN.matcher(content);
        while (loc.find()) {
            result.addDependency(new DependencyInfo(currentModule, loc.group(1), "#include"));
        }
    }

    // ── 프레임워크/라이브러리 감지 ────────────────────────────────────────────

    private void detectFrameworks(String content, AnalysisResult result) {
        // STL
        if (content.contains("<vector>") || content.contains("<map>") ||
                content.contains("<unordered_map>") || content.contains("<list>"))
            result.addFramework("C++ STL (Standard Template Library)");
        if (content.contains("<iostream>") || content.contains("<fstream>"))
            result.addFramework("C++ I/O Streams");
        if (content.contains("<thread>") || content.contains("<mutex>") ||
                content.contains("<future>"))
            result.addFramework("C++ Concurrency (std::thread)");
        if (content.contains("<algorithm>"))
            result.addFramework("C++ Algorithms (std::algorithm)");
        if (content.contains("<memory>") && (content.contains("shared_ptr") ||
                content.contains("unique_ptr")))
            result.addFramework("C++ Smart Pointers");

        // Boost
        if (content.contains("boost/") || content.contains("boost::"))
            result.addFramework("Boost");
        if (content.contains("boost::asio"))
            result.addFramework("Boost.Asio (Networking)");
        if (content.contains("boost::filesystem"))
            result.addFramework("Boost.Filesystem");

        // Qt
        if (content.contains("#include <Q") || content.contains("QApplication") ||
                content.contains("QObject") || content.contains("Q_OBJECT"))
            result.addFramework("Qt Framework");
        if (content.contains("QWidget") || content.contains("QMainWindow"))
            result.addFramework("Qt Widgets");
        if (content.contains("QML") || content.contains("QQuickView"))
            result.addFramework("Qt QML");

        // OpenCV
        if (content.contains("<opencv") || content.contains("cv::") ||
                content.contains("cv2::"))
            result.addFramework("OpenCV");

        // Eigen (선형대수)
        if (content.contains("<Eigen/") || content.contains("Eigen::"))
            result.addFramework("Eigen");

        // gRPC / Protobuf
        if (content.contains("<grpc++/") || content.contains("grpc::"))
            result.addFramework("gRPC");
        if (content.contains(".proto") || content.contains("google::protobuf"))
            result.addFramework("Protocol Buffers");

        // 데이터베이스
        if (content.contains("<sqlite3.h>") || content.contains("sqlite3_"))
            result.addFramework("SQLite3");
        if (content.contains("mysqlx::") || content.contains("<mysql_driver.h>"))
            result.addFramework("MySQL Connector/C++");
        if (content.contains("pqxx::") || content.contains("<pqxx/"))
            result.addFramework("libpqxx (PostgreSQL)");

        // 테스팅
        if (content.contains("gtest/gtest.h") || content.contains("TEST(") ||
                content.contains("EXPECT_"))
            result.addFramework("Google Test (gtest)");
        if (content.contains("catch2/") || content.contains("CATCH_CONFIG_MAIN") ||
                content.contains("TEST_CASE"))
            result.addFramework("Catch2");
        if (content.contains("doctest.h") || content.contains("DOCTEST_CONFIG"))
            result.addFramework("doctest");

        // 로깅
        if (content.contains("spdlog") || content.contains("<spdlog/"))
            result.addFramework("spdlog");
        if (content.contains("log4cpp") || content.contains("log4cxx"))
            result.addFramework("log4cpp / log4cxx");

        // 직렬화
        if (content.contains("nlohmann/json") || content.contains("nlohmann::json"))
            result.addFramework("nlohmann/json");
        if (content.contains("rapidjson") || content.contains("rapidjson::"))
            result.addFramework("RapidJSON");

        // 네트워킹
        if (content.contains("Poco::") || content.contains("<Poco/"))
            result.addFramework("POCO C++ Libraries");
        if (content.contains("cpr::") || content.contains("<cpr/"))
            result.addFramework("CPR (C++ Requests)");

        // 게임 엔진
        if (content.contains("SFML") || content.contains("<SFML/"))
            result.addFramework("SFML");
        if (content.contains("SDL_") || content.contains("<SDL2/"))
            result.addFramework("SDL2");
        if (content.contains("Ogre::") || content.contains("<Ogre/"))
            result.addFramework("OGRE 3D");

        // 빌드
        if (content.contains("cmake_minimum_required"))
            result.addFramework("CMake");

        // OpenMP / MPI
        if (content.contains("<omp.h>") || content.contains("#pragma omp"))
            result.addFramework("OpenMP");
        if (content.contains("<mpi.h>") || content.contains("MPI_Init"))
            result.addFramework("MPI");

        // OpenGL / Vulkan
        if (content.contains("<GL/gl.h>") || content.contains("glBegin"))
            result.addFramework("OpenGL");
        if (content.contains("<vulkan/vulkan.h>") || content.contains("vkCreateInstance"))
            result.addFramework("Vulkan");

        // CUDA
        if (content.contains("<cuda_runtime.h>") || content.contains("__global__"))
            result.addFramework("CUDA");

        // Templates (메타프로그래밍)
        if (TEMPLATE_PATTERN.matcher(content).find())
            result.addFramework("C++ Templates / Metaprogramming");
    }

    // ── 디자인 패턴 감지 ─────────────────────────────────────────────────────

    private void detectPatterns(String fileName, String content, AnalysisResult result) {
        String lower = fileName.toLowerCase();
        String baseName = extractBaseName(fileName);

        // Singleton
        if (content.contains("static") && content.contains("getInstance") &&
                content.contains("private:"))
            result.addDesignPattern("Singleton", baseName);

        // Factory / Abstract Factory
        if (lower.contains("factory") || content.matches("(?s).*\\bCreate\\w+\\s*\\(.*"))
            result.addDesignPattern("Factory", baseName);

        // Builder
        if (lower.contains("builder") || content.matches("(?s).*\\.build\\(\\).*"))
            result.addDesignPattern("Builder", baseName);

        // Observer
        if (lower.contains("observer") || lower.contains("listener") ||
                content.contains("notify(") || content.contains("subscribe("))
            result.addDesignPattern("Observer", baseName);

        // Strategy
        if (lower.contains("strategy") || lower.contains("policy"))
            result.addDesignPattern("Strategy", baseName);

        // Decorator
        if (lower.contains("decorator") || lower.contains("wrapper"))
            result.addDesignPattern("Decorator", baseName);

        // Command
        if (lower.contains("command") && content.contains("execute("))
            result.addDesignPattern("Command", baseName);

        // PIMPL (Pointer to Implementation)
        if (content.contains("Impl") && content.contains("unique_ptr") &&
                content.contains("private:"))
            result.addDesignPattern("PIMPL (Pointer to Implementation)", baseName);

        // CRTP (Curiously Recurring Template Pattern)
        if (content.matches("(?s).*class\\s+\\w+\\s*:\\s*public\\s+\\w+<\\w+>.*"))
            result.addDesignPattern("CRTP (Template Pattern)", baseName);

        // Repository / Service
        if (lower.contains("repository") || lower.contains("repo"))
            result.addDesignPattern("Repository", baseName);
        if (lower.contains("service"))
            result.addDesignPattern("Service Layer", baseName);

        // Controller
        if (lower.contains("controller"))
            result.addDesignPattern("MVC Controller", baseName);

        // Header Guard
        if (content.contains("#ifndef") && content.contains("#define") && lower.endsWith(".hpp"))
            result.addDesignPattern("Header Guard / Module", baseName);
    }

    // ── 데이터베이스 분석 ─────────────────────────────────────────────────────

    private void analyzeDatabases(String content, AnalysisResult result) {
        // SQLite
        if (content.contains("sqlite3_exec") || content.contains("sqlite3_prepare")) {
            Pattern tbl = Pattern.compile("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)",
                    Pattern.CASE_INSENSITIVE);
            Matcher m = tbl.matcher(content);
            while (m.find()) result.addDatabaseSchema("Table: " + m.group(1) + " (SQLite3)");
        }
        // libpqxx
        if (content.contains("pqxx::")) {
            Pattern tbl = Pattern.compile("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)",
                    Pattern.CASE_INSENSITIVE);
            Matcher m = tbl.matcher(content);
            while (m.find()) result.addDatabaseSchema("Table: " + m.group(1) + " (PostgreSQL)");
        }
    }

    // ── API 분석 ─────────────────────────────────────────────────────────────

    private void analyzeApis(String content, AnalysisResult result) {
        // Boost.Beast HTTP 서버
        if (content.contains("boost::beast::http") || content.contains("beast::http::"))
            result.addApiEndpoint("HTTP Server (Boost.Beast)");

        // POCO HTTP 서버
        if (content.contains("Poco::Net::HTTPServer") || content.contains("Poco::Net::ServerSocket"))
            result.addApiEndpoint("HTTP Server (POCO)");

        // gRPC 서비스
        if (content.contains("grpc::Server") || content.contains("ServerBuilder"))
            result.addApiEndpoint("gRPC Server");

        // 소켓 서버
        if (content.contains("bind(") && content.contains("listen("))
            result.addApiEndpoint("TCP Socket Server");

        // REST 라우팅 (Crow, Pistache 등)
        Pattern crowRoute = Pattern.compile("CROW_ROUTE\\s*\\(\\s*app,\\s*\"([^\"]+)\"\\)");
        Matcher crow = crowRoute.matcher(content);
        while (crow.find()) result.addApiEndpoint("GET/POST " + crow.group(1) + " (Crow)");

        Pattern pistache = Pattern.compile("Routes::(Get|Post|Put|Delete)\\s*\\(\\s*router,\\s*\"([^\"]+)\"");
        Matcher pist = pistache.matcher(content);
        while (pist.find())
            result.addApiEndpoint(pist.group(1).toUpperCase() + " " + pist.group(2) + " (Pistache)");
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private String extractModuleName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        return parts.length > 1 ? parts[parts.length - 2] : "root";
    }

    private String extractBaseName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        String name = parts[parts.length - 1];
        return name.replaceAll("\\.(cpp|cc|cxx|hpp|h|c\\+\\+)$", "");
    }
}
