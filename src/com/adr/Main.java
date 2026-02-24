package com.adr;

import com.adr.analyzer.InputValidator;
import com.adr.analyzer.SourceAnalyzer;
import com.adr.analyzer.ZipExtractor;
import com.adr.generator.AdrGenerator;
import com.adr.model.AnalysisResult;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import com.adr.model.Language;

/**
 * ADR Generator - ZIP 파일로부터 소스코드를 분석하여 ADR 문서를 생성하는 메인 클래스
 *
 * 보안 강화 (ADR-20260219-081343.md CRITICAL 해결):
 * - 입력 ZIP 경로 검증 (크기, 확장자, 경로 검증)
 * - 출력 경로 sanitize (Path Traversal 방어)
 * - 스택 트레이스 비노출 (프로덕션 모드)
 */
public class Main {

    /** --debug 플래그로 활성화 시 전체 스택 트레이스 출력 */
    private static boolean debugMode = false;
    /** --serve 플래그로 활성화 시 로컬 웹 서버 시작 */
    private static boolean serveMode = false;

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("ADR Generator - Architecture Decision Record Generator");
        System.out.println("=".repeat(60));

        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        // --debug 플래그 파싱
        String inputPathStr = args[0];
        String outputDir = args.length > 1 ? args[1] : "./output";
        for (String arg : args) {
            if ("--debug".equalsIgnoreCase(arg)) {
                debugMode = true;
            }
            if ("--serve".equalsIgnoreCase(arg)) {
                serveMode = true;
            }
        }

        try {
            Path inputPath = Paths.get(inputPathStr).toAbsolutePath().normalize();

            try {
                InputValidator.validateInputPath(inputPath);
            } catch (SecurityException se) {
                System.err.println("❌ 입력 검증 오류: " + se.getMessage());
                System.exit(1);
            }

            // ── 2. 출력 경로 보안 검증 ────────────────────────────────────────
            try {
                InputValidator.validateOutputDir(outputDir);
            } catch (SecurityException se) {
                System.err.println("❌ 출력 경로 오류: " + se.getMessage());
                System.exit(1);
            }

            File inputFile = inputPath.toFile();
            boolean isZip = !inputFile.isDirectory();

            if (isZip) {
                System.out.println("\n📦 입력 파일: " + inputFile.getAbsolutePath()
                        + String.format(" (%.1f MB)", inputFile.length() / (1024.0 * 1024.0)));
            } else {
                System.out.println("\n📁 입력 디렉토리: " + inputFile.getAbsolutePath());
            }
            System.out.println("📁 출력 디렉토리: "
                    + Paths.get(outputDir).toAbsolutePath().normalize());

            // ── 3. 소스코드 분석 준비 ──────────────────────────────────────────
            Path analysisPath;
            ZipExtractor extractor = null;

            if (isZip) {
                System.out.println("\n[1/3] ZIP 파일 추출 중...");
                extractor = new ZipExtractor();
                try {
                    analysisPath = extractor.extract(inputPath);
                } catch (SecurityException se) {
                    System.err.println("❌ ZIP 추출 보안 오류: " + se.getMessage());
                    System.exit(1);
                    return;
                }
                System.out.println("✅ 추출 완료: " + analysisPath);
            } else {
                System.out.println("\n[1/3] 디렉토리 분석 준비 완료");
                analysisPath = inputPath;
            }

            // ── 4. 소스코드 분석 ──────────────────────────────────────────────
            System.out.println("\n[2/3] 소스코드 분석 중...");
            SourceAnalyzer analyzer = new SourceAnalyzer();
            AnalysisResult result = analyzer.analyze(analysisPath);
            System.out.println("✅ 분석 완료");
            // 언어별 파일 수 출력
            Map<Language, Integer> langCounts = result.getLanguageFileCount();
            if (langCounts.isEmpty()) {
                System.out.println("   - 발견된 소스 파일: 0");
            } else {
                langCounts.entrySet().stream()
                        .filter(e -> e.getKey() != Language.UNKNOWN)
                        .sorted(Map.Entry.<Language, Integer>comparingByValue().reversed())
                        .forEach(e -> System.out.printf("   - %s 파일: %d개%n",
                                e.getKey().getDisplayName(), e.getValue()));
                System.out.printf("   - 총 소스 파일: %d개%n", result.getTotalFileCount());
            }
            System.out.println("   - 발견된 패키지: " + result.getPackageCount());
            System.out.println("   - 발견된 클래스: " + result.getClassCount());

            // ── 5. ADR 문서 생성 ──────────────────────────────────────────────
            System.out.println("\n[3/3] ADR 문서 생성 중...");
            AdrGenerator generator = new AdrGenerator();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String adrFileName = "ADR-" + timestamp + ".md";
            Path adrPath = Paths.get(outputDir, adrFileName);

            generator.generate(result, adrPath);
            System.out.println("✅ ADR 문서 생성 완료: " + adrPath.toAbsolutePath());

            // ── 6. 임시 파일 정리 ─────────────────────────────────────────────
            if (extractor != null) {
                extractor.cleanup();
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("✨ 모든 작업이 완료되었습니다!");
            System.out.println("=".repeat(60));

            if (serveMode) {
                PreviewServer server = new PreviewServer(8080, Paths.get(outputDir));
                server.start();
            }

        } catch (Exception e) {
            System.err.println("\n❌ 오류 발생: " + e.getMessage());
            if (debugMode) {
                // --debug 모드에서만 스택 트레이스 출력 (내부 구조 노출 최소화)
                e.printStackTrace();
            } else {
                System.err.println("   (상세 정보는 --debug 플래그를 사용하세요)");
            }
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("\n사용법:");
        System.out.println("  java -cp bin com.adr.Main <input-path> [output-directory] [--debug] [--serve]");
        System.out.println("  (input-path는 .zip 파일 또는 프로젝트 디렉토리일 수 있습니다)");
        System.out.println("\n예제:");
        System.out.println("  java -cp bin com.adr.Main project-source.zip");
        System.out.println("  java -cp bin com.adr.Main ./my-project ./output --serve");
        System.out.println("  java -cp bin com.adr.Main project-source.zip ./output --debug");
        System.out.println("\n보안 제한:");
        System.out.printf("  최대 ZIP 크기    : %d MB%n",
                InputValidator.MAX_ZIP_SIZE_BYTES / (1024 * 1024));
        System.out.printf("  최대 해제 크기   : %d GB%n",
                InputValidator.MAX_TOTAL_UNCOMPRESSED_BYTES / (1024 * 1024 * 1024));
        System.out.printf("  최대 파일 수     : %,d%n",
                InputValidator.MAX_FILE_COUNT);
        System.out.printf("  단일 파일 최대   : %d MB%n",
                InputValidator.MAX_SOURCE_FILE_SIZE_BYTES / (1024 * 1024));
        System.out.println();
    }
}
