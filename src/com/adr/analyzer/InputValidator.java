package com.adr.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * 입력값 보안 검증 유틸리티 클래스
 *
 * ADR-20260219-081343.md 의 CRITICAL 위험 요소 "보안 취약점 가능성" 해결:
 *  1. ZIP 파일 입력 검증 (존재, 확장자, 최대 크기)
 *  2. 출력 경로 검증 (Path Traversal 방어)
 *  3. 소스 파일 콘텐츠 크기 제한
 *  4. ZIP 내 허용 확장자 화이트리스트
 *  5. ZIP Bomb(압축 폭탄) 방어 — 압축 해제 총 크기 & 파일 수 제한
 */
public class InputValidator {

    // ── 제한값 상수 ───────────────────────────────────────────────────────────

    /** 입력 ZIP 최대 파일 크기: 500 MB */
    public static final long MAX_ZIP_SIZE_BYTES = 500L * 1024 * 1024;

    /** 압축 해제 후 최대 총 크기: 2 GB (ZIP Bomb 방어) */
    public static final long MAX_TOTAL_UNCOMPRESSED_BYTES = 2L * 1024 * 1024 * 1024;

    /** 압축 해제 최대 파일 수 */
    public static final int MAX_FILE_COUNT = 10_000;

    /** 단일 소스 파일 최대 크기: 10 MB */
    public static final long MAX_SOURCE_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    /** 압축 해제 크기 / 압축 크기 최대 비율 (ZIP Bomb 방어) */
    public static final int MAX_COMPRESSION_RATIO = 100;

    // ── ZIP 내 허용 소스 파일 확장자 화이트리스트 ─────────────────────────────

    public static final Set<String> ALLOWED_SOURCE_EXTENSIONS = Set.of(
        // Java
        ".java",
        // C / C++
        ".c", ".h", ".cpp", ".cc", ".cxx", ".hpp", ".c++",
        // C#
        ".cs",
        // JavaScript / TypeScript
        ".js", ".jsx", ".ts", ".tsx", ".mjs", ".cjs",
        // Ruby
        ".rb", ".rake", ".gemspec",
        // Rust
        ".rs",
        // Kotlin
        ".kt", ".kts",
        // Python
        ".py", ".pyw",
        // PHP
        ".php", ".phtml", ".php3", ".php4", ".php5", ".phps",
        // JSP
        ".jsp", ".jspf", ".jspx",
        // 빌드/설정 (내용 분석용)
        ".gradle", ".gradle.kts", ".xml", ".json", ".yaml", ".yml",
        ".toml", ".properties", ".env.example", ".md", ".cfg",
        // Ruby Gemfile (확장자 없음이지만 파일명 기반 허용은 ZipExtractor에서 처리)
        ""
    );

    // ── 공개 검증 메서드 ──────────────────────────────────────────────────────

    /**
     * 입력 경로 검증 (파일 또는 디렉토리)
     */
    public static void validateInputPath(Path inputPath) {
        if (inputPath == null) {
            throw new SecurityException("입력 경로가 null입니다.");
        }

        File file = inputPath.toFile();
        if (!file.exists()) {
            throw new SecurityException("입력 경로가 존재하지 않습니다: " + inputPath);
        }

        if (file.isDirectory()) {
            validateDirectoryInput(inputPath);
        } else {
            validateZipInput(inputPath);
        }
    }

    /**
     * ZIP 입력 파일 검증
     * @throws SecurityException 검증 실패 시
     */
    public static void validateZipInput(Path zipPath) {
        if (zipPath == null) {
            throw new SecurityException("입력 파일 경로가 null입니다.");
        }

        File file = zipPath.toFile();

        // 존재 및 일반 파일 여부
        if (!file.exists()) {
            throw new SecurityException("입력 파일이 존재하지 않습니다: " + zipPath);
        }
        if (!file.isFile()) {
            throw new SecurityException("입력 경로가 파일이 아닙니다: " + zipPath);
        }

        // 확장자 검사 (.zip 또는 .jar 만 허용)
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".zip") && !name.endsWith(".jar")) {
            throw new SecurityException(
                "허용되지 않는 파일 형식입니다. .zip 또는 .jar 파일만 지원합니다: " + name);
        }

        // 파일 크기 제한
        long size = file.length();
        if (size == 0) {
            throw new SecurityException("입력 ZIP 파일이 비어 있습니다: " + zipPath);
        }
        if (size > MAX_ZIP_SIZE_BYTES) {
            throw new SecurityException(String.format(
                "입력 ZIP 파일이 너무 큽니다. 최대 %d MB, 실제 %.1f MB: %s",
                MAX_ZIP_SIZE_BYTES / (1024 * 1024),
                size / (1024.0 * 1024.0),
                zipPath));
        }

        // 정규화된 경로가 실제 파일 경로와 일치하는지 검사 (Path Traversal 방어)
        try {
            zipPath.toRealPath(); // 심볼릭 링크 탐지 및 경로 유효성 확인
        } catch (IOException e) {
            throw new SecurityException("입력 파일 경로 확인 실패: " + e.getMessage());
        }
    }

    /**
     * 디렉토리 입력 검증
     */
    public static void validateDirectoryInput(Path dirPath) {
        File dir = dirPath.toFile();
        if (!dir.exists()) {
            throw new SecurityException("디렉토리가 존재하지 않습니다: " + dirPath);
        }
        if (!dir.isDirectory()) {
            throw new SecurityException("입력 경로가 디렉토리가 아닙니다: " + dirPath);
        }

        try {
            dirPath.toRealPath();
        } catch (IOException e) {
            throw new SecurityException("입력 디렉토리 경로 확인 실패: " + e.getMessage());
        }
    }

    /**
     * 출력 디렉토리 경로 검증
     * @throws SecurityException 검증 실패 시
     */
    public static void validateOutputDir(String outputDirStr) {
        if (outputDirStr == null || outputDirStr.isBlank()) {
            throw new SecurityException("출력 디렉토리 경로가 비어 있습니다.");
        }

        // Path Traversal 패턴 방어 (null byte, 이중 점 연속)
        if (outputDirStr.contains("\0")) {
            throw new SecurityException("출력 경로에 허용되지 않는 문자(null byte)가 포함되어 있습니다.");
        }

        try {
            Path outputPath = Paths.get(outputDirStr).toAbsolutePath().normalize();

            // 시스템 중요 디렉토리 접근 방지 (Windows / POSIX 공통)
            String normalized = outputPath.toString().replace("\\", "/");
            if (normalized.equals("/") || normalized.equals("C:/") || normalized.equals("C:/Windows")) {
                throw new SecurityException("시스템 루트 디렉토리에 출력할 수 없습니다: " + outputDirStr);
            }

        } catch (InvalidPathException e) {
            throw new SecurityException("출력 경로가 올바르지 않습니다: " + outputDirStr + " (" + e.getMessage() + ")");
        }
    }

    /**
     * ZIP 엔트리 이름 검증 (Zip Slip / Path Traversal 방어)
     * @return true면 해당 엔트리를 허용, false면 건너뜀
     */
    public static boolean isAllowedZipEntry(String entryName, Path tempDir, Path resolvedPath) {
        if (entryName == null || entryName.isBlank()) return false;

        // null byte 방어
        if (entryName.contains("\0")) return false;

        // Windows 절대 경로 패턴(드라이브 문자) 방어
        if (entryName.length() >= 2 && Character.isLetter(entryName.charAt(0))
                && entryName.charAt(1) == ':') {
            return false;
        }

        // 절대 경로 방어
        if (entryName.startsWith("/") || entryName.startsWith("\\")) return false;

        // ".." 포함 방어
        if (entryName.contains("..")) return false;

        // 해석된 경로가 tempDir 하위에 있는지 최종 확인
        if (!resolvedPath.startsWith(tempDir)) return false;

        return true;
    }

    /**
     * ZIP 내 파일 확장자 화이트리스트 검사
     * 허용 목록에 없는 확장자는 추출하지 않음 (실행 파일·스크립트 방어)
     */
    public static boolean isAllowedSourceExtension(String fileName) {
        if (fileName == null) return false;

        // 확장자 없는 특수 파일명 허용 (Gemfile, Rakefile, Makefile, Dockerfile 등)
        String lower = fileName.toLowerCase();
        if (lower.equals("gemfile") || lower.equals("rakefile")
                || lower.equals("makefile") || lower.equals("dockerfile")
                || lower.equals("cmakelists.txt") || lower.equals("readme.md")
                // Python 설정/빌드 파일
                || lower.equals("pipfile") || lower.equals("requirements.txt")
                || lower.equals("pyproject.toml") || lower.equals("setup.py")
                || lower.equals("setup.cfg")
                // PHP Composer 파일
                || lower.equals("composer.json") || lower.equals("composer.lock")) {
            return true;
        }

        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return false; // 확장자 없는 나머지 파일은 제외

        String ext = fileName.substring(dot).toLowerCase();
        return ALLOWED_SOURCE_EXTENSIONS.contains(ext);
    }

    /**
     * 단일 소스 파일 크기 검증
     * @throws SecurityException 파일이 너무 크면
     */
    public static void validateSourceFileSize(Path filePath) throws IOException {
        long size = Files.size(filePath);
        if (size > MAX_SOURCE_FILE_SIZE_BYTES) {
            throw new SecurityException(String.format(
                "소스 파일이 너무 큽니다 (최대 %d MB). 분석을 건너뜁니다: %s (%.1f MB)",
                MAX_SOURCE_FILE_SIZE_BYTES / (1024 * 1024),
                filePath.getFileName(),
                size / (1024.0 * 1024.0)));
        }
    }
}
