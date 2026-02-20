package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSP (JavaServer Pages) 언어 분석기
 *
 * 지원 파일: .jsp, .jspf (JSP fragment), .jspx (JSP XML)
 *
 * 감지 항목:
 *  - JSP 페이지 디렉티브 (page, include, taglib)
 *  - JSTL (Core, Fmt, Fn, SQL, XML 태그 라이브러리)
 *  - EL (Expression Language: ${...}, #{...})
 *  - Java 코드 스크립틀릿 (<%...%>, <%=...%>, <%!...%>)
 *  - Spring MVC 연동 (spring:url, spring:message, spring:form, spring:bind)
 *  - Struts 태그 라이브러리
 *  - 커스텀 태그 라이브러리 (taglib directive)
 *  - 임포트 감지 (page import)
 *  - HTML 폼 분석 (form action → API 엔드포인트)
 *  - 디자인 패턴: MVC View, Template Method, Front Controller, Decorator(Filter)
 *  - include/forward 통한 컴포넌트 참조
 */
public class JspAnalyzer implements LanguageAnalyzer {

    private static final String TABLE_PREFIX    = "Table: ";
    private static final String DEPENDENCY_TYPE = "dependency";

    // ── JSP 디렉티브 ─────────────────────────────────────────────────────────

    // <%@ page ... %> 디렉티브
    private static final Pattern PAGE_DIRECTIVE_PATTERN =
            Pattern.compile(
                "<%@\\s*page\\s+([^%]+)%>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // page import="..."
    private static final Pattern PAGE_IMPORT_PATTERN =
            Pattern.compile(
                "import\\s*=\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE);

    // <%@ taglib uri="..." prefix="..." %>
    private static final Pattern TAGLIB_PATTERN =
            Pattern.compile(
                "<%@\\s*taglib\\s+[^%]*uri\\s*=\\s*\"([^\"]+)\"[^%]*prefix\\s*=\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE);

    // <%@ include file="..." %>
    private static final Pattern INCLUDE_DIRECTIVE_PATTERN =
            Pattern.compile(
                "<%@\\s*include\\s+file\\s*=\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE);

    // <jsp:include page="..." /> 또는 <jsp:forward page="..." />
    private static final Pattern JSP_ACTION_PATTERN =
            Pattern.compile(
                "<jsp:(include|forward|useBean|setProperty|getProperty)\\s+[^>]*(?:page|name)\\s*=\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE);

    // HTML form action 추출
    private static final Pattern FORM_ACTION_PATTERN =
            Pattern.compile(
                "<form\\s+[^>]*action\\s*=\\s*[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);

    // EL 표현식 ${...}
    private static final Pattern EL_PATTERN =
            Pattern.compile("\\$\\{[^}]+}");

    // Java scriptlet <%...%>
    private static final Pattern SCRIPTLET_PATTERN =
            Pattern.compile("<%[^@=!][^%]*%>", Pattern.DOTALL);

    // SQL 직접 사용 (JSTL sql 또는 scriptlet 내 SQL)
    private static final Pattern SQL_QUERY_PATTERN =
            Pattern.compile(
                "<sql:query[^>]*>([^<]+)</sql:query>|" +
                "(?:Statement|PreparedStatement|ResultSet)\\s+\\w+",
                Pattern.CASE_INSENSITIVE);

    // CREATE TABLE
    private static final Pattern SQL_CREATE_TABLE_PATTERN =
            Pattern.compile(
                "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?[`'\"]?([\\w]+)[`'\"]?",
                Pattern.CASE_INSENSITIVE);

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".jsp", ".jspf", ".jspx"};
    }

    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        String module = extractModuleName(fileName);

        analyzeDirectives(fileName, content, result);
        analyzeTaglibs(content, result);
        detectFrameworks(content, result);
        detectPatterns(fileName, content, result);
        analyzeApis(content, result);
        analyzeDatabases(content, result);
        countScriptlets(module, content, result);
    }

    // ── 디렉티브 분석 ─────────────────────────────────────────────────────────

    private void analyzeDirectives(String fileName, String content, AnalysisResult result) {
        String module = extractModuleName(fileName);

        // page 디렉티브 → import 파싱
        Matcher page = PAGE_DIRECTIVE_PATTERN.matcher(content);
        while (page.find()) {
            String attrs = page.group(1);
            Matcher imp = PAGE_IMPORT_PATTERN.matcher(attrs);
            while (imp.find()) {
                // import="com.example.Foo,com.example.Bar" 형식 분리
                for (String cls : imp.group(1).split(",")) {
                    String trimmed = cls.trim();
                    if (!trimmed.isEmpty() && !isJavaBuiltin(trimmed)) {
                        result.addDependency(new DependencyInfo(module, trimmed, "jsp-import"));
                    }
                }
            }
        }

        // include directive → 패키지로 등록
        Matcher inc = INCLUDE_DIRECTIVE_PATTERN.matcher(content);
        while (inc.find()) {
            result.addPackage("include:" + inc.group(1));
        }

        // jsp:include / jsp:forward → 패키지
        Matcher action = JSP_ACTION_PATTERN.matcher(content);
        while (action.find()) {
            String actionType = action.group(1).toLowerCase();
            String target = action.group(2);
            if ("include".equals(actionType) || "forward".equals(actionType)) {
                result.addPackage(actionType + ":" + target);
            }
        }
    }

    // ── 태그 라이브러리 분석 ────────────────────────────────────────────────────

    private void analyzeTaglibs(String content, AnalysisResult result) {
        Matcher tl = TAGLIB_PATTERN.matcher(content);
        while (tl.find()) {
            String uri    = tl.group(1).toLowerCase();
            String prefix = tl.group(2);
            result.addDependency(
                new DependencyInfo("taglib", uri + " (prefix:" + prefix + ")", DEPENDENCY_TYPE));
        }
    }

    // ── 프레임워크 탐지 ───────────────────────────────────────────────────────

    private void detectFrameworks(String content, AnalysisResult result) {
        String lc = content.toLowerCase();

        // ── JSTL ──────────────────────────────────────────────────────────────
        if (lc.contains("http://java.sun.com/jsp/jstl") ||
                lc.contains("http://java.sun.com/jstl") ||
                lc.contains("<c:") || lc.contains("<fmt:") || lc.contains("<fn:"))
            result.addFramework("JSTL");

        if (lc.contains("<sql:") || lc.contains("jakarta.servlet.jsp.jstl.sql"))
            result.addFramework("JSTL SQL");

        if (lc.contains("<x:") || lc.contains("jakarta.servlet.jsp.jstl.xml"))
            result.addFramework("JSTL XML");

        // ── Spring MVC ─────────────────────────────────────────────────────────
        if (lc.contains("<spring:") ||
                lc.contains("spring:url") || lc.contains("spring:message") ||
                lc.contains("spring:form") || lc.contains("spring:bind") ||
                lc.contains("http://www.springframework.org/tags"))
            result.addFramework("Spring MVC (View)");

        if (lc.contains("<form:") ||
                lc.contains("http://www.springframework.org/tags/form"))
            result.addFramework("Spring MVC Form Tags");

        if (lc.contains("spring security") || lc.contains("<sec:") ||
                lc.contains("http://www.springframework.org/security/tags"))
            result.addFramework("Spring Security (JSP)");

        // ── Struts ─────────────────────────────────────────────────────────────
        if (lc.contains("<s:") && lc.contains("struts") ||
                lc.contains("/struts-tags") || lc.contains("org.apache.struts"))
            result.addFramework("Struts (View)");

        if (lc.contains("struts2") || lc.contains("struts 2") ||
                lc.contains("org.apache.struts2"))
            result.addFramework("Struts 2 (View)");

        // ── Apache Tiles / SiteMesh / Freemarker sidecar ──────────────────────
        if (lc.contains("tiles:") || lc.contains("apache tiles") ||
                lc.contains("org.apache.tiles"))
            result.addFramework("Apache Tiles");

        if (lc.contains("sitemesh") || lc.contains("decorator:") ||
                lc.contains("com.opensymphony.sitemesh"))
            result.addFramework("SiteMesh");

        // ── Jakarta EE / Servlet ───────────────────────────────────────────────
        if (lc.contains("javax.servlet") || lc.contains("jakarta.servlet"))
            result.addFramework("Jakarta EE Servlet");

        if (lc.contains("javax.faces") || lc.contains("jakarta.faces") ||
                lc.contains("<h:") || lc.contains("<f:") || lc.contains("<p:"))
            result.addFramework("JavaServer Faces (JSF)");

        // ── Ajax / JavaScript 라이브러리 ───────────────────────────────────────
        if (lc.contains("jquery"))
            result.addFramework("jQuery");

        if (lc.contains("bootstrap"))
            result.addFramework("Bootstrap");

        if (lc.contains("react") && lc.contains("reactdom"))
            result.addFramework("React.js");

        // ── 보안 ──────────────────────────────────────────────────────────────
        if (lc.contains("shiro") || lc.contains("<shiro:") ||
                lc.contains("org.apache.shiro"))
            result.addFramework("Apache Shiro (JSP)");

        // ── ORM/DB 힌트 ───────────────────────────────────────────────────────
        if (lc.contains("mybatis") || lc.contains("sqlmap") ||
                lc.contains("org.mybatis"))
            result.addFramework("MyBatis");

        // ── 템플릿 관련 ───────────────────────────────────────────────────────
        if (lc.contains("freemarker") || lc.contains("ftl"))
            result.addFramework("FreeMarker (coexist)");

        if (lc.contains("thymeleaf") || lc.contains("th:"))
            result.addFramework("Thymeleaf (coexist)");
    }

    // ── 디자인 패턴 탐지 ──────────────────────────────────────────────────────

    private void detectPatterns(String fileName, String content, AnalysisResult result) {
        String lc = content.toLowerCase();
        String module = extractModuleName(fileName);

        // MVC View 패턴
        if (content.contains("<%@ page") || content.contains("<c:") || content.contains("${"))
            result.addDesignPattern("MVC View", module);

        // Front Controller 패턴 힌트 (forward to dispatcher)
        if (lc.contains("dispatcher") || lc.contains("requestdispatcher") ||
                content.contains("jsp:forward"))
            result.addDesignPattern("Front Controller", module);

        // Template Method 패턴 힌트 (공통 헤더/푸터 include)
        if (content.contains("<%@ include") || content.contains("jsp:include"))
            result.addDesignPattern("Template Method (JSP include)", module);

        // Filter/Decorator 힌트
        if (lc.contains("filter") || lc.contains("requestdispatcher.forward"))
            result.addDesignPattern("Filter/Decorator", module);

        // Scriptlet 사용 = 안티패턴 감지
        if (SCRIPTLET_PATTERN.matcher(content).find())
            result.addDesignPattern("Scriptlet (Anti-pattern)", module);

        // Model 객체 접근
        if (content.contains("${") && (lc.contains("model.") || lc.contains("requestscope.")))
            result.addDesignPattern("Model Binding", module);

        // Spring Security 인증/인가
        if (content.contains("<sec:") || lc.contains("authentication"))
            result.addDesignPattern("Security (Authentication)", module);

        // AJAX 패턴
        if (lc.contains("xmlhttprequest") || lc.contains("$.ajax") ||
                lc.contains("fetch("))
            result.addDesignPattern("AJAX", module);
    }

    // ── API 엔드포인트 분석 ───────────────────────────────────────────────────

    private void analyzeApis(String content, AnalysisResult result) {
        // HTML form action → API 엔드포인트
        Matcher form = FORM_ACTION_PATTERN.matcher(content);
        while (form.find()) {
            String action = form.group(1).trim();
            if (!action.isEmpty() && !action.startsWith("#")) {
                result.addApiEndpoint("JSP:" + action);
            }
        }
    }

    // ── 데이터베이스 스키마 분석 ──────────────────────────────────────────────

    private void analyzeDatabases(String content, AnalysisResult result) {
        // JSTL sql:query 또는 scriptlet 내 SQL
        Matcher sql = SQL_QUERY_PATTERN.matcher(content);
        while (sql.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + "SQL Usage (JSP)");
            break; // 중복 방지
        }

        // CREATE TABLE SQL
        Matcher ct = SQL_CREATE_TABLE_PATTERN.matcher(content);
        while (ct.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + ct.group(1) + " (Raw SQL in JSP)");
        }
    }

    // ── 스크립틀릿 카운트 ──────────────────────────────────────────────────────

    private void countScriptlets(String module, String content, AnalysisResult result) {
        // 스크립틀릿이 있으면 클래스 카운트에 1 추가 (서블릿 범주 안에 포함)
        if (SCRIPTLET_PATTERN.matcher(content).find()) {
            result.setClassCount(result.getClassCount() + 1);
        }
        // EL 표현식 존재 시 패키지로 표시
        if (EL_PATTERN.matcher(content).find()) {
            result.addPackage("el_usage:" + module);
        }
    }

    // ── 헬퍼 메서드 ───────────────────────────────────────────────────────────

    private String extractModuleName(String fileName) {
        int sep = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        String base = sep >= 0 ? fileName.substring(sep + 1) : fileName;
        int dot = base.lastIndexOf('.');
        return dot > 0 ? base.substring(0, dot) : base;
    }

    /**
     * Java 내장 패키지 및 공통 stdlib 여부 판단.
     * JSP import에서 java.*, javax.*, jakarta.* 등은 별도 의존성으로 수집하지 않음.
     */
    private boolean isJavaBuiltin(String fqn) {
        return fqn.startsWith("java.") ||
               fqn.startsWith("javax.") ||
               fqn.startsWith("jakarta.") ||
               fqn.startsWith("sun.") ||
               fqn.startsWith("com.sun.");
    }
}
