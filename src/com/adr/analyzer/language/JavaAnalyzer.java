package com.adr.analyzer.language;

import com.adr.analyzer.*;
import com.adr.model.AnalysisResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import java.util.Optional;

/**
 * Java 언어 분석기
 */
public class JavaAnalyzer implements LanguageAnalyzer {

    private final PackageAnalyzer packageAnalyzer;
    private final DependencyAnalyzer dependencyAnalyzer;
    private final FrameworkDetector frameworkDetector;
    private final PatternDetector patternDetector;
    private final DatabaseAnalyzer databaseAnalyzer;
    private final ApiAnalyzer apiAnalyzer;

    public JavaAnalyzer() {
        this.packageAnalyzer = new PackageAnalyzer();
        this.dependencyAnalyzer = new DependencyAnalyzer();
        this.frameworkDetector = new FrameworkDetector();
        this.patternDetector = new PatternDetector();
        this.databaseAnalyzer = new DatabaseAnalyzer();
        this.apiAnalyzer = new ApiAnalyzer();

        // JavaParser 설정: 최신 자바 기능 지원
        StaticJavaParser.getParserConfiguration()
                .setLanguageLevel(com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_21);
    }

    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        // JavaParser를 이용한 AST 분석 시도
        Optional<CompilationUnit> cuOpt = Optional.empty();
        try {
            cuOpt = Optional.of(StaticJavaParser.parse(content));
        } catch (Exception e) {
            // 파싱 실패 시 기존의 정규식 기반 방식으로 폴백하거나 로그 출력
            System.err.println("⚠️  JavaParser 파싱 실패: " + fileName + " - " + e.getMessage());
        }

        // 패키지 분석
        packageAnalyzer.analyze(content, result);

        // 클래스 카운트 (AST가 있으면 AST 기반, 없으면 기존 정규식 기반)
        if (cuOpt.isPresent()) {
            int classCount = cuOpt.get().findAll(TypeDeclaration.class).size();
            result.setClassCount(result.getClassCount() + classCount);
        } else {
            int classCount = countClasses(content);
            result.setClassCount(result.getClassCount() + classCount);
        }

        // 의존성 분석
        dependencyAnalyzer.analyze(content, result);

        // 프레임워크 감지
        frameworkDetector.detect(content, result);

        // 디자인 패턴 감지
        if (cuOpt.isPresent()) {
            patternDetector.detectJava(fileName, cuOpt.get(), result);
        } else {
            patternDetector.detect(fileName, content, result);
        }

        // 데이터베이스 스키마 분석
        databaseAnalyzer.analyze(content, result);

        // API 엔드포인트 분석
        apiAnalyzer.analyze(content, result);
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] { ".java" };
    }

    private int countClasses(String content) {
        int count = 0;
        String[] lines = content.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches(".*\\b(class|interface|enum|record)\\s+\\w+.*")) {
                count++;
            }
        }

        return count;
    }
}
