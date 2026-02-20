package com.adr.analyzer;

import com.adr.analyzer.language.*;
import com.adr.model.AnalysisResult;
import com.adr.model.Language;
import com.adr.model.ModuleInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * 다중 언어 지원 소스코드 분석 총괄 클래스
 */
public class SourceAnalyzer {
    
    private final Map<String, LanguageAnalyzer> analyzers;
    
    public SourceAnalyzer() {
        this.analyzers = new HashMap<>();
        
        // 언어별 분석기 등록
        JavaAnalyzer javaAnalyzer = new JavaAnalyzer();
        for (String ext : javaAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, javaAnalyzer);
        }
        
        CSharpAnalyzer csharpAnalyzer = new CSharpAnalyzer();
        for (String ext : csharpAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, csharpAnalyzer);
        }
        
        JavaScriptAnalyzer jsAnalyzer = new JavaScriptAnalyzer(false);
        for (String ext : jsAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, jsAnalyzer);
        }
        
        JavaScriptAnalyzer tsAnalyzer = new JavaScriptAnalyzer(true);
        for (String ext : tsAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, tsAnalyzer);
        }
        
        // C 분석기 등록 (.c, .h)
        CAnalyzer cAnalyzer = new CAnalyzer();
        for (String ext : cAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, cAnalyzer);
        }
        
        // C++ 분석기 등록 (.cpp, .cc, .cxx, .hpp, .c++)
        CppAnalyzer cppAnalyzer = new CppAnalyzer();
        for (String ext : cppAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, cppAnalyzer);
        }
        
        // Ruby 분석기 등록 (.rb, .rake, .gemspec)
        RubyAnalyzer rubyAnalyzer = new RubyAnalyzer();
        for (String ext : rubyAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, rubyAnalyzer);
        }

        // Rust 분석기 등록 (.rs)
        RustAnalyzer rustAnalyzer = new RustAnalyzer();
        for (String ext : rustAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, rustAnalyzer);
        }

        // Kotlin 분석기 등록 (.kt, .kts)
        KotlinAnalyzer kotlinAnalyzer = new KotlinAnalyzer();
        for (String ext : kotlinAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, kotlinAnalyzer);
        }

        // Python 분석기 등록 (.py, .pyw)
        PythonAnalyzer pythonAnalyzer = new PythonAnalyzer();
        for (String ext : pythonAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, pythonAnalyzer);
        }

        // PHP 분석기 등록 (.php, .phtml, .php3, .php4, .php5, .phps)
        PhpAnalyzer phpAnalyzer = new PhpAnalyzer();
        for (String ext : phpAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, phpAnalyzer);
        }

        // JSP 분석기 등록 (.jsp, .jspf, .jspx)
        JspAnalyzer jspAnalyzer = new JspAnalyzer();
        for (String ext : jspAnalyzer.getSupportedExtensions()) {
            analyzers.put(ext, jspAnalyzer);
        }
    }
    
    public AnalysisResult analyze(Path projectPath) throws IOException {
        AnalysisResult result = new AnalysisResult();
        
        // 프로젝트 이름 설정
        result.setProjectName(projectPath.getFileName().toString());
        
        // 모든 지원 언어의 소스 파일 수집
        Map<Language, List<Path>> filesByLanguage = collectSourceFiles(projectPath);
        
        // 언어별 통계
        int totalFiles = 0;
        for (Map.Entry<Language, List<Path>> entry : filesByLanguage.entrySet()) {
            int count = entry.getValue().size();
            totalFiles += count;
            result.addLanguageFile(entry.getKey());
            for (int i = 1; i < count; i++) {
                result.addLanguageFile(entry.getKey());
            }
            System.out.println("   - " + entry.getKey().getDisplayName() + " 파일: " + count + "개");
        }
        
        // (총 파일 수는 languageFileCount 기반 getTotalFileCount()로 계산)

        
        // 각 파일 분석
        for (Map.Entry<Language, List<Path>> entry : filesByLanguage.entrySet()) {
            Language language = entry.getKey();
            List<Path> files = entry.getValue();
            
            String extension = language.getExtension();
            LanguageAnalyzer analyzer = analyzers.get(extension);
            
            if (analyzer != null) {
                for (Path file : files) {
                    try {
                        // 보안: 단일 파일 크기 제한 검사 (OOM 방어)
                        InputValidator.validateSourceFileSize(file);
                        String content = Files.readString(file);
                        analyzer.analyzeFile(file.getFileName().toString(), content, result);
                    } catch (SecurityException se) {
                        System.err.println("⚠️  파일 크기 초과 (건너뜀): " + file.getFileName());
                    } catch (IOException e) {
                        System.err.println("⚠️  파일 읽기 실패: " + file + " - " + e.getMessage());
                    }
                }
            }
        }
        
        // 모듈 정보 생성
        generateModuleInfo(result);
        
        return result;
    }
    
    private Map<Language, List<Path>> collectSourceFiles(Path root) throws IOException {
        Map<Language, List<Path>> filesByLanguage = new HashMap<>();
        
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                 .forEach(path -> {
                     Language lang = Language.fromFileName(path.getFileName().toString());
                     if (lang != Language.UNKNOWN) {
                         filesByLanguage.computeIfAbsent(lang, k -> new ArrayList<>()).add(path);
                     }
                 });
        }
        
        return filesByLanguage;
    }
    
    private void generateModuleInfo(AnalysisResult result) {
        Map<String, ModuleInfo> moduleMap = new HashMap<>();
        
        for (String packageName : result.getPackages()) {
            String[] parts = packageName.split("\\.");
            if (parts.length > 0) {
                String moduleName = parts[parts.length - 1];
                ModuleInfo module = moduleMap.computeIfAbsent(
                    moduleName, 
                    k -> new ModuleInfo(moduleName, packageName)
                );
                module.incrementClassCount();
            }
        }
        
        moduleMap.values().forEach(result::addModule);
    }
}

