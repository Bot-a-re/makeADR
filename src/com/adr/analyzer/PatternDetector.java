package com.adr.analyzer;

import com.adr.model.AnalysisResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.Modifier;

/**
 * 디자인 패턴 감지 클래스
 */
public class PatternDetector {

    public void detect(String fileName, String content, AnalysisResult result) {
        String lowerFileName = fileName.toLowerCase();
        String lowerContent = content.toLowerCase();

        // Singleton Pattern
        if (content.contains("private static") &&
                content.contains("getInstance()")) {
            result.addDesignPattern("Singleton", extractClassName(fileName));
        }

        // Factory Pattern
        if (lowerFileName.contains("factory") ||
                content.contains("createInstance") ||
                content.contains("create(")) {
            result.addDesignPattern("Factory", extractClassName(fileName));
        }

        // Builder Pattern
        if (lowerFileName.contains("builder") ||
                content.contains("public Builder") ||
                content.contains(".builder()")) {
            result.addDesignPattern("Builder", extractClassName(fileName));
        }

        // Observer Pattern
        if (lowerFileName.contains("listener") ||
                lowerFileName.contains("observer") ||
                content.contains("addListener") ||
                content.contains("addObserver")) {
            result.addDesignPattern("Observer", extractClassName(fileName));
        }

        // Strategy Pattern
        if (lowerFileName.contains("strategy") ||
                (content.contains("interface") && lowerContent.contains("execute"))) {
            result.addDesignPattern("Strategy", extractClassName(fileName));
        }

        // Adapter Pattern
        if (lowerFileName.contains("adapter")) {
            result.addDesignPattern("Adapter", extractClassName(fileName));
        }

        // Decorator Pattern
        if (lowerFileName.contains("decorator")) {
            result.addDesignPattern("Decorator", extractClassName(fileName));
        }

        // Repository Pattern
        if (lowerFileName.contains("repository") ||
                content.contains("@Repository")) {
            result.addDesignPattern("Repository", extractClassName(fileName));
        }

        // Service Pattern
        if (lowerFileName.contains("service") ||
                content.contains("@Service")) {
            result.addDesignPattern("Service Layer", extractClassName(fileName));
        }

        // DTO Pattern
        if (lowerFileName.contains("dto") ||
                lowerFileName.contains("vo") ||
                (content.contains("class") && isDataClass(content))) {
            result.addDesignPattern("DTO/VO", extractClassName(fileName));
        }
    }

    public void detectJava(String fileName, CompilationUnit cu, AnalysisResult result) {
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cid -> {
            String name = cid.getNameAsString();

            // Singleton
            boolean hasPrivateConstructor = cid.getConstructors().stream()
                    .anyMatch(c -> c.getModifiers().contains(Modifier.privateModifier()));
            boolean hasStaticInstance = cid.getFields().stream()
                    .anyMatch(f -> f.getModifiers().contains(Modifier.staticModifier()) &&
                            f.getCommonType().asString().equals(name));
            if (hasPrivateConstructor && hasStaticInstance) {
                result.addDesignPattern("Singleton", name);
            }

            // Factory
            if (name.toLowerCase().contains("factory") ||
                    cid.getMethods().stream().anyMatch(m -> m.getNameAsString().startsWith("create"))) {
                result.addDesignPattern("Factory", name);
            }

            // Builder
            if (name.toLowerCase().contains("builder") ||
                    cid.getMembers().stream().anyMatch(m -> m instanceof ClassOrInterfaceDeclaration &&
                            ((ClassOrInterfaceDeclaration) m).getNameAsString().equals("Builder"))) {
                result.addDesignPattern("Builder", name);
            }

            // Observer/Listener
            if (name.toLowerCase().contains("listener") || name.toLowerCase().contains("observer") ||
                    cid.getMethods().stream().anyMatch(m -> m.getNameAsString().startsWith("add") &&
                            (m.getNameAsString().contains("Listener") || m.getNameAsString().contains("Observer")))) {
                result.addDesignPattern("Observer", name);
            }

            // Strategy
            if (cid.isInterface() && cid.getMethods().stream().anyMatch(m -> m.getNameAsString().equals("execute"))) {
                result.addDesignPattern("Strategy", name);
            }

            // Repository
            if (cid.getAnnotationByName("Repository").isPresent() || name.endsWith("Repository")) {
                result.addDesignPattern("Repository", name);
            }

            // Service
            if (cid.getAnnotationByName("Service").isPresent() || name.endsWith("Service")) {
                result.addDesignPattern("Service Layer", name);
            }

            // DTO/VO
            if (name.endsWith("DTO") || name.endsWith("VO") || cid.getAnnotationByName("Data").isPresent()) {
                result.addDesignPattern("DTO/VO", name);
            }
        });
    }

    private String extractClassName(String fileName) {
        return fileName.replace(".java", "");
    }

    private boolean isDataClass(String content) {
        // 간단한 휴리스틱: getter/setter가 많고 비즈니스 로직이 적은 경우
        int getterCount = countOccurrences(content, "get");
        int setterCount = countOccurrences(content, "set");

        return (getterCount + setterCount) > 3;
    }

    private int countOccurrences(String content, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}
