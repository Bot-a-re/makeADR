package com.adr.model;

/**
 * 지원하는 프로그래밍 언어 열거형
 */
public enum Language {
    JAVA(".java", "Java"),
    CSHARP(".cs", "C#"),
    JAVASCRIPT(".js", "JavaScript"),
    TYPESCRIPT(".ts", "TypeScript"),
    C(".c", "C"),
    CPP(".cpp", "C++"),
    RUBY(".rb", "Ruby"),
    RUST(".rs", "Rust"),
    KOTLIN(".kt", "Kotlin"),
    PYTHON(".py", "Python"),
    PHP(".php", "PHP"),
    JSP(".jsp", "JSP"),
    UNKNOWN("", "Unknown");
    
    private final String extension;
    private final String displayName;
    
    Language(String extension, String displayName) {
        this.extension = extension;
        this.displayName = displayName;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static Language fromExtension(String extension) {
        String lower = extension.toLowerCase();

        // C++ 다중 확장자 처리
        if (lower.equals(".cpp") || lower.equals(".cc") || lower.equals(".cxx") || lower.equals(".c++")) {
            return CPP;
        }
        // C++ 헤더
        if (lower.equals(".hpp")) {
            return CPP;
        }
        // C 헤더 파일
        if (lower.equals(".h") || lower.equals(".c")) {
            return C;
        }
        // Ruby 확장자
        if (lower.equals(".rb") || lower.equals(".rake") || lower.equals(".gemspec")) {
            return RUBY;
        }
        // Kotlin 다중 확장자 처리 (.kt, .kts)
        if (lower.equals(".kt") || lower.equals(".kts")) {
            return KOTLIN;
        }
        // Python 확장자
        if (lower.equals(".py") || lower.equals(".pyw")) {
            return PYTHON;
        }
        // PHP 확장자
        if (lower.equals(".php") || lower.equals(".phtml")
                || lower.equals(".php3") || lower.equals(".php4")
                || lower.equals(".php5") || lower.equals(".phps")) {
            return PHP;
        }
        // JSP 확장자
        if (lower.equals(".jsp") || lower.equals(".jspf") || lower.equals(".jspx")) {
            return JSP;
        }
        for (Language lang : values()) {
            if (lang.extension.equalsIgnoreCase(extension)) {
                return lang;
            }
        }
        return UNKNOWN;
    }
    
    public static Language fromFileName(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            String ext = fileName.substring(lastDot);
            return fromExtension(ext);
        }
        // 확장자 없는 Ruby 파일 (Rakefile, Gemfile)
        String baseName = fileName.toLowerCase();
        if (baseName.equals("rakefile") || baseName.equals("gemfile")) {
            return RUBY;
        }
        // Rust 빌드 파일
        if (baseName.equals("cargo.toml") || baseName.equals("build.rs")) {
            return RUST;
        }
        // Python 설정/빌드 파일
        if (baseName.equals("requirements.txt") || baseName.equals("pipfile")
                || baseName.equals("pyproject.toml") || baseName.equals("setup.py")
                || baseName.equals("setup.cfg")) {
            return PYTHON;
        }
        // PHP Composer 파일
        if (baseName.equals("composer.json") || baseName.equals("composer.lock")) {
            return PHP;
        }
        return UNKNOWN;
    }
}
