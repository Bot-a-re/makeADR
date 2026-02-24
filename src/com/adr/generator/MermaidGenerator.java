package com.adr.generator;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;
import com.adr.model.ModuleInfo;

import java.util.*;

/**
 * Mermaid 차트 생성 클래스
 */
public class MermaidGenerator {

    /**
     * 모듈 구성도 생성 (Graph)
     */
    public String generateModuleDiagram(AnalysisResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("```mermaid\n");
        sb.append("graph TB\n");
        sb.append("    %% Styling\n");
        sb.append("    classDef core fill:#e1f5fe,stroke:#01579b,stroke-width:2px;\n");
        sb.append("    classDef module fill:#ffffff,stroke:#333,stroke-width:1px;\n");

        List<ModuleInfo> modules = result.getModules();

        if (modules.isEmpty()) {
            sb.append("    A[No Modules Detected]\n");
        } else {
            // 모듈 노드 생성
            for (int i = 0; i < modules.size(); i++) {
                ModuleInfo module = modules.get(i);
                String nodeId = "M" + i;
                String className = module.getClassCount() > 10 ? "core" : "module";
                sb.append(String.format("    %s[\"%s<br/>(%d classes)\"]::: %s\n",
                        nodeId, module.getName(), module.getClassCount(), className));
            }
        }

        // 의존성 관계 추가
        Map<String, String> packageToNodeId = new HashMap<>();
        for (int i = 0; i < modules.size(); i++) {
            packageToNodeId.put(modules.get(i).getPackageName(), "M" + i);
        }

        Set<String> addedEdges = new HashSet<>();
        for (DependencyInfo dep : result.getDependencies()) {
            String fromId = packageToNodeId.get(dep.getFrom());
            String toId = packageToNodeId.get(dep.getTo());

            if (fromId != null && toId != null && !fromId.equals(toId)) {
                String edge = fromId + "-->" + toId;
                if (!addedEdges.contains(edge)) {
                    sb.append(String.format("    %s --> %s\n", fromId, toId));
                    addedEdges.add(edge);
                }
            }
        }
        sb.append("```\n");
        return sb.toString();
    }

    /**
     * 데이터 흐름도 생성 (Flowchart)
     */
    public String generateDataFlowDiagram(AnalysisResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("```mermaid\n");
        sb.append("flowchart LR\n");

        boolean hasApi = !result.getApiEndpoints().isEmpty();
        boolean hasService = result.getDesignPatterns().containsKey("Service Layer");
        boolean hasRepository = result.getDesignPatterns().containsKey("Repository");
        boolean hasDatabase = !result.getDatabaseSchemas().isEmpty();

        if (!hasApi && !hasService && !hasRepository) {
            sb.append("    A[Application Entry Point]\n");
            sb.append("    B[Business Logic]\n");
            sb.append("    A --> B\n");
        } else {
            // 계층형 아키텍처 표현
            if (hasApi) {
                sb.append("    Client([Client/User])\n");
                sb.append("    API[API Layer<br/>REST Controllers]\n");
                sb.append("    Client --> API\n");
            }

            if (hasService) {
                if (!hasApi) {
                    sb.append("    Entry[Application Entry]\n");
                    sb.append("    Service[Service Layer<br/>Business Logic]\n");
                    sb.append("    Entry --> Service\n");
                } else {
                    sb.append("    Service[Service Layer<br/>Business Logic]\n");
                    sb.append("    API --> Service\n");
                }
            }

            if (hasRepository) {
                if (!hasService && hasApi) {
                    sb.append("    Repository[Repository Layer<br/>Data Access]\n");
                    sb.append("    API --> Repository\n");
                } else if (hasService) {
                    sb.append("    Repository[Repository Layer<br/>Data Access]\n");
                    sb.append("    Service --> Repository\n");
                } else {
                    sb.append("    App[Application]\n");
                    sb.append("    Repository[Repository Layer<br/>Data Access]\n");
                    sb.append("    App --> Repository\n");
                }
            }

            if (hasDatabase) {
                sb.append("    DB[(Database)]\n");
                if (hasRepository) {
                    sb.append("    Repository --> DB\n");
                } else if (hasService) {
                    sb.append("    Service --> DB\n");
                } else if (hasApi) {
                    sb.append("    API --> DB\n");
                } else {
                    sb.append("    App[Application]\n");
                    sb.append("    App --> DB\n");
                }
            }

            // 스타일 적용
            sb.append("\n");
            sb.append("    style Client fill:#e1f5ff\n");
            if (hasApi)
                sb.append("    style API fill:#fff4e6\n");
            if (hasService)
                sb.append("    style Service fill:#f3e5f5\n");
            if (hasRepository)
                sb.append("    style Repository fill:#e8f5e9\n");
            if (hasDatabase)
                sb.append("    style DB fill:#fce4ec\n");
        }

        sb.append("```\n");
        return sb.toString();
    }

    /**
     * 클래스 다이어그램 생성 (주요 패턴만)
     */
    public String generateClassDiagram(AnalysisResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("```mermaid\n");
        sb.append("classDiagram\n");

        Map<String, List<String>> patterns = result.getDesignPatterns();

        if (patterns.isEmpty()) {
            sb.append("    class Application {\n");
            sb.append("        +main()\n");
            sb.append("    }\n");
        } else {
            // 주요 패턴별로 대표 클래스 표시
            for (Map.Entry<String, List<String>> entry : patterns.entrySet()) {
                String pattern = entry.getKey();
                List<String> classes = entry.getValue();

                // 최대 3개까지만 표시
                for (int i = 0; i < Math.min(3, classes.size()); i++) {
                    String className = classes.get(i);
                    sb.append(String.format("    class %s {\n", sanitizeClassName(className)));
                    sb.append(String.format("        <<<%s>>>\n", pattern));
                    sb.append("    }\n");
                }
            }
        }

        sb.append("```\n");
        return sb.toString();
    }

    /**
     * 레이어 간 상속/참조 시퀀스 다이어그램 생성
     */
    public String generateArchitectureSequenceDiagram(AnalysisResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("```mermaid\n");
        sb.append("sequenceDiagram\n");
        sb.append("    autonumber\n");

        boolean hasApi = !result.getApiEndpoints().isEmpty();
        boolean hasService = result.getDesignPatterns().containsKey("Service Layer");
        boolean hasRepository = result.getDesignPatterns().containsKey("Repository");

        if (hasApi)
            sb.append("    participant C as Client\n");
        if (hasApi)
            sb.append("    participant A as API Controller\n");
        if (hasService)
            sb.append("    participant S as Service Layer\n");
        if (hasRepository)
            sb.append("    participant R as Repository\n");
        if (!result.getDatabaseSchemas().isEmpty())
            sb.append("    participant D as Database\n");

        if (hasApi) {
            sb.append("    C->>A: Request Data\n");
            if (hasService) {
                sb.append("    A->>S: Process Business Logic\n");
                if (hasRepository) {
                    sb.append("    S->>R: Fetch Data\n");
                    if (!result.getDatabaseSchemas().isEmpty()) {
                        sb.append("    R->>D: SQL Query\n");
                        sb.append("    D-->>R: ResultSet\n");
                    }
                    sb.append("    R-->>S: Record Object\n");
                }
                sb.append("    S-->>A: DTO/Model\n");
            } else if (hasRepository) {
                sb.append("    A->>R: Fetch Data\n");
                sb.append("    R-->>A: Record Object\n");
            }
            sb.append("    A-->>C: JSON/HTML Response\n");
        }

        sb.append("```\n");
        return sb.toString();
    }

    private String sanitizeClassName(String className) {
        // Mermaid에서 사용할 수 없는 문자 제거
        return className.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
