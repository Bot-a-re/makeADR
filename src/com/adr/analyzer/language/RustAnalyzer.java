package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rust 언어 분석기
 *
 * 지원 파일: .rs, Cargo.toml, build.rs
 *
 * 감지 항목:
 *  - 모듈/크레이트 구조 (mod, crate)
 *  - 구조체/트레이트/enum (struct, trait, impl, enum)
 *  - 의존성 (use, extern crate, Cargo.toml [dependencies])
 *  - 프레임워크/라이브러리: Tokio, Actix-Web, Axum, Rocket, Hyper,
 *    Serde, Diesel, SQLx, SeaORM, Clap, Rayon, Warp, Tonic (gRPC),
 *    Reqwest, Sqlx, Bevy 등
 *  - 디자인 패턴: Builder, Strategy, Command, State, Observer, Singleton 등
 *  - API 엔드포인트 (Actix-Web, Axum, Rocket 라우팅)
 *  - 데이터베이스 스키마 (Diesel migration, SQLx)
 */
public class RustAnalyzer implements LanguageAnalyzer {

    // use 문
    private static final Pattern USE_PATTERN =
            Pattern.compile("^\\s*use\\s+([\\w:]+)", Pattern.MULTILINE);

    // extern crate
    private static final Pattern EXTERN_CRATE_PATTERN =
            Pattern.compile("^\\s*extern\\s+crate\\s+(\\w+)", Pattern.MULTILINE);

    // mod 선언
    private static final Pattern MOD_PATTERN =
            Pattern.compile("^\\s*(?:pub\\s+)?mod\\s+(\\w+)", Pattern.MULTILINE);

    // struct / enum / trait 정의
    private static final Pattern TYPE_PATTERN =
            Pattern.compile("^\\s*(?:pub\\s+)?(?:struct|enum|trait)\\s+(\\w+)", Pattern.MULTILINE);

    // fn 정의
    private static final Pattern FN_PATTERN =
            Pattern.compile("^\\s*(?:pub\\s+)?(?:async\\s+)?fn\\s+(\\w+)", Pattern.MULTILINE);

    // Cargo.toml [dependencies] 섹션
    private static final Pattern CARGO_DEP_PATTERN =
            Pattern.compile("^(\\w[\\w-]*)\\s*=", Pattern.MULTILINE);

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".rs"};
    }

    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        analyzeCrate(fileName, content, result);
        result.setClassCount(result.getClassCount() + countTypes(content));
        analyzeDependencies(fileName, content, result);
        detectFrameworks(fileName, content, result);
        detectPatterns(fileName, content, result);
        analyzeApis(content, result);
        analyzeDatabases(content, result);
    }

    // ── 크레이트/모듈 ─────────────────────────────────────────────────────────

    private void analyzeCrate(String fileName, String content, AnalysisResult result) {
        // 최상위 mod를 패키지로 등록
        Matcher m = MOD_PATTERN.matcher(content);
        if (m.find()) {
            result.addPackage(m.group(1));
        } else {
            result.addPackage(extractCrateName(fileName));
        }
    }

    private int countTypes(String content) {
        Matcher m = TYPE_PATTERN.matcher(content);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    // ── 의존성 분석 ───────────────────────────────────────────────────────────

    private void analyzeDependencies(String fileName, String content, AnalysisResult result) {
        String crate = extractCrateName(fileName);

        // use 문
        Matcher use = USE_PATTERN.matcher(content);
        while (use.find()) {
            String lib = use.group(1).split("::")[0];
            if (!isStdLib(lib)) {
                result.addDependency(new DependencyInfo(crate, lib, "use"));
            }
        }

        // extern crate
        Matcher ext = EXTERN_CRATE_PATTERN.matcher(content);
        while (ext.find()) {
            result.addDependency(new DependencyInfo(crate, ext.group(1), "extern_crate"));
        }

        // Cargo.toml
        if (fileName.toLowerCase().equals("cargo.toml")) {
            boolean inDeps = false;
            for (String line : content.split("\n")) {
                String trimmed = line.trim();
                if (trimmed.equals("[dependencies]") || trimmed.equals("[dev-dependencies]")
                        || trimmed.equals("[build-dependencies]")) {
                    inDeps = true;
                    continue;
                }
                if (trimmed.startsWith("[") && inDeps) {
                    inDeps = false;
                }
                if (inDeps) {
                    Matcher dep = CARGO_DEP_PATTERN.matcher(trimmed);
                    if (dep.find()) {
                        result.addDependency(new DependencyInfo("Cargo.toml", dep.group(1), "dependency"));
                    }
                }
            }
        }
    }

    private boolean isStdLib(String lib) {
        return lib.equals("std") || lib.equals("core") || lib.equals("alloc")
                || lib.equals("self") || lib.equals("super") || lib.equals("crate");
    }

    // ── 프레임워크/라이브러리 감지 ────────────────────────────────────────────

    private void detectFrameworks(String fileName, String content, AnalysisResult result) {

        // ── 비동기 런타임 ──────────────────────────────────────────────────
        if (content.contains("tokio") || content.contains("#[tokio::main]"))
            result.addFramework("Tokio (Async Runtime)");
        if (content.contains("async_std") || content.contains("async-std"))
            result.addFramework("async-std");
        if (content.contains("smol"))
            result.addFramework("Smol (Async Runtime)");

        // ── 웹 프레임워크 ──────────────────────────────────────────────────
        if (content.contains("actix_web") || content.contains("actix-web")
                || content.contains("use actix"))
            result.addFramework("Actix-Web");
        if (content.contains("axum") || content.contains("use axum"))
            result.addFramework("Axum");
        if (content.contains("rocket") || content.contains("#[rocket::main]")
                || content.contains("#[get(") || content.contains("#[post("))
            result.addFramework("Rocket");
        if (content.contains("warp") || content.contains("warp::"))
            result.addFramework("Warp");
        if (content.contains("hyper") || content.contains("hyper::"))
            result.addFramework("Hyper (HTTP)");
        if (content.contains("tide") || content.contains("tide::"))
            result.addFramework("Tide");

        // ── 직렬화 ─────────────────────────────────────────────────────────
        if (content.contains("serde") || content.contains("#[derive(Serialize")
                || content.contains("#[derive(Deserialize"))
            result.addFramework("Serde (Serialization)");
        if (content.contains("serde_json") || content.contains("serde_json::"))
            result.addFramework("serde_json");

        // ── DB / ORM ───────────────────────────────────────────────────────
        if (content.contains("diesel") || content.contains("use diesel"))
            result.addFramework("Diesel (ORM)");
        if (content.contains("sqlx") || content.contains("sqlx::"))
            result.addFramework("SQLx");
        if (content.contains("sea_orm") || content.contains("sea-orm")
                || content.contains("SeaOrm"))
            result.addFramework("SeaORM");
        if (content.contains("rusqlite") || content.contains("rusqlite::"))
            result.addFramework("Rusqlite (SQLite)");
        if (content.contains("mongodb") || content.contains("mongodb::"))
            result.addFramework("MongoDB (Rust)");
        if (content.contains("redis") && content.contains("redis::"))
            result.addFramework("Redis (Rust)");

        // ── HTTP 클라이언트 ────────────────────────────────────────────────
        if (content.contains("reqwest") || content.contains("reqwest::"))
            result.addFramework("Reqwest (HTTP Client)");
        if (content.contains("ureq") || content.contains("ureq::"))
            result.addFramework("Ureq (HTTP Client)");

        // ── CLI ────────────────────────────────────────────────────────────
        if (content.contains("clap") || content.contains("#[derive(Parser")
                || content.contains("clap::"))
            result.addFramework("Clap (CLI)");
        if (content.contains("structopt") || content.contains("StructOpt"))
            result.addFramework("StructOpt (CLI)");

        // ── 병렬/동시성 ────────────────────────────────────────────────────
        if (content.contains("rayon") || content.contains("rayon::"))
            result.addFramework("Rayon (Parallelism)");
        if (content.contains("crossbeam") || content.contains("crossbeam::"))
            result.addFramework("Crossbeam (Concurrency)");

        // ── gRPC / 네트워크 ────────────────────────────────────────────────
        if (content.contains("tonic") || content.contains("tonic::"))
            result.addFramework("Tonic (gRPC)");
        if (content.contains("prost") || content.contains("prost::"))
            result.addFramework("Prost (Protocol Buffers)");

        // ── 테스팅 ─────────────────────────────────────────────────────────
        if (content.contains("#[test]") || content.contains("#[cfg(test)]"))
            result.addFramework("Rust Built-in Test");
        if (content.contains("mockall") || content.contains("mockall::"))
            result.addFramework("Mockall (Mocking)");
        if (content.contains("proptest") || content.contains("proptest::"))
            result.addFramework("Proptest (Property Testing)");

        // ── 로깅 ───────────────────────────────────────────────────────────
        if (content.contains("log::") || content.contains("use log;"))
            result.addFramework("log (Logging Facade)");
        if (content.contains("tracing") || content.contains("tracing::"))
            result.addFramework("Tracing (Observability)");
        if (content.contains("env_logger") || content.contains("env_logger::"))
            result.addFramework("env_logger");

        // ── 게임 엔진 ──────────────────────────────────────────────────────
        if (content.contains("bevy") || content.contains("bevy::"))
            result.addFramework("Bevy (Game Engine)");

        // ── 임베디드 ───────────────────────────────────────────────────────
        if (content.contains("#![no_std]"))
            result.addFramework("no_std (Embedded/Bare Metal)");
        if (content.contains("embedded_hal") || content.contains("embedded-hal"))
            result.addFramework("embedded-hal");

        // ── WebAssembly ────────────────────────────────────────────────────
        if (content.contains("wasm_bindgen") || content.contains("wasm-bindgen"))
            result.addFramework("wasm-bindgen (WebAssembly)");
        if (content.contains("web_sys") || content.contains("web-sys"))
            result.addFramework("web-sys");
    }

    // ── 디자인 패턴 감지 ─────────────────────────────────────────────────────

    private void detectPatterns(String fileName, String content, AnalysisResult result) {
        String lower    = fileName.toLowerCase();
        String baseName = extractBaseName(fileName);

        // Builder 패턴 (Rust 관례 — .builder() 메서드 또는 Builder struct)
        if (lower.contains("builder") || content.contains("fn builder(")
                || content.contains("fn build("))
            result.addDesignPattern("Builder", baseName);

        // Strategy / Trait Object (dyn Trait)
        if (content.contains("dyn ") || lower.contains("strategy"))
            result.addDesignPattern("Strategy (Trait Object)", baseName);

        // Command
        if (lower.contains("command") || lower.contains("cmd"))
            result.addDesignPattern("Command", baseName);

        // State Machine (Rust typestate 패턴)
        if (lower.contains("state") || content.matches("(?s).*enum\\s+\\w+State.*"))
            result.addDesignPattern("State / Typestate", baseName);

        // Observer / Event
        if (lower.contains("event") || lower.contains("listener")
                || content.contains("EventEmitter"))
            result.addDesignPattern("Observer / Event", baseName);

        // Singleton (once_cell / lazy_static)
        if (content.contains("once_cell") || content.contains("lazy_static")
                || content.contains("OnceCell") || content.contains("OnceLock"))
            result.addDesignPattern("Singleton (OnceLock/once_cell)", baseName);

        // Factory
        if (lower.contains("factory") || content.contains("fn new(")
                && content.contains("-> Self"))
            result.addDesignPattern("Factory", baseName);

        // Repository
        if (lower.contains("repository") || lower.contains("repo"))
            result.addDesignPattern("Repository", baseName);

        // Service Layer
        if (lower.contains("service"))
            result.addDesignPattern("Service Layer", baseName);

        // Middleware (Actix / Axum)
        if (lower.contains("middleware") || content.contains("from_fn")
                || content.contains("Transform"))
            result.addDesignPattern("Middleware", baseName);

        // Iterator (Rust 네이티브 패턴)
        if (content.contains("impl Iterator") || content.contains("fn next("))
            result.addDesignPattern("Iterator", baseName);

        // Error 처리 (thiserror / anyhow)
        if (content.contains("thiserror") || content.contains("anyhow")
                || content.contains("impl Error"))
            result.addDesignPattern("Error Handling (thiserror/anyhow)", baseName);
    }

    // ── API 분석 ─────────────────────────────────────────────────────────────

    private void analyzeApis(String content, AnalysisResult result) {
        // Actix-Web 라우팅 매크로 (#[get("/path")])
        Pattern actixRoute = Pattern.compile(
                "#\\[(get|post|put|patch|delete)\\(\"([^\"]+)\"\\)\\]", Pattern.CASE_INSENSITIVE);
        Matcher ar = actixRoute.matcher(content);
        while (ar.find()) {
            result.addApiEndpoint(ar.group(1).toUpperCase() + " " + ar.group(2) + " (Actix-Web)");
        }

        // Axum route
        Pattern axumRoute = Pattern.compile(
                "\\.route\\(\"([^\"]+)\",\\s*(?:get|post|put|patch|delete)\\(", Pattern.CASE_INSENSITIVE);
        Matcher ax = axumRoute.matcher(content);
        while (ax.find()) {
            result.addApiEndpoint("ROUTE " + ax.group(1) + " (Axum)");
        }

        // Rocket 라우팅 매크로
        Pattern rocketRoute = Pattern.compile(
                "#\\[(get|post|put|patch|delete)\\(\"([^\"]+)\"\\)\\]");
        Matcher rr = rocketRoute.matcher(content);
        while (rr.find()) {
            result.addApiEndpoint(rr.group(1).toUpperCase() + " " + rr.group(2) + " (Rocket)");
        }

        // Warp 필터
        Pattern warpFilter = Pattern.compile(
                "warp::path\\(\"([^\"]+)\"\\)");
        Matcher wf = warpFilter.matcher(content);
        while (wf.find()) {
            result.addApiEndpoint("PATH /" + wf.group(1) + " (Warp)");
        }
    }

    // ── 데이터베이스 분석 ─────────────────────────────────────────────────────

    private void analyzeDatabases(String content, AnalysisResult result) {
        // Diesel migration - create_table
        Pattern dieselTable = Pattern.compile(
                "create_table\\s*!?\\s*\\(?\\s*[\"']?(\\w+)[\"']?", Pattern.CASE_INSENSITIVE);
        Matcher dt = dieselTable.matcher(content);
        while (dt.find()) {
            result.addDatabaseSchema("Table: " + dt.group(1) + " (Diesel Migration)");
        }

        // SQLx query! 매크로 (테이블 이름 추출)
        Pattern sqlxQuery = Pattern.compile(
                "(?:FROM|INTO|UPDATE)\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher sq = sqlxQuery.matcher(content);
        while (sq.find()) {
            String table = sq.group(1).toLowerCase();
            if (!table.equals("select") && !table.equals("where") && !table.equals("set")) {
                result.addDatabaseSchema("Table: " + sq.group(1) + " (SQLx)");
            }
        }
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private String extractCrateName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        return parts.length > 1 ? parts[parts.length - 2] : "root";
    }

    private String extractBaseName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        String name = parts[parts.length - 1];
        return name.replaceAll("\\.rs$", "");
    }
}
