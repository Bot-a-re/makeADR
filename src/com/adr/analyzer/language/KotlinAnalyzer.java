package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kotlin 언어 분석기
 *
 * 지원 파일: .kt, .kts (Kotlin Script, Gradle KTS)
 *
 * 감지 항목:
 *  - 패키지/클래스/인터페이스/object
 *  - import 의존성
 *  - 프레임워크: Spring Boot, Ktor, Vert.x, Exposed, JPA/Hibernate,
 *    Coroutines, Arrow, Koin, Dagger/Hilt, Room (Android),
 *    Retrofit, OkHttp, Jetpack Compose, Kotest, MockK 등
 *  - 디자인 패턴: Singleton (object), Builder (DSL/apply),
 *    Factory, Sealed Class, Delegate, Coroutine 기반 패턴
 *  - REST API 엔드포인트 (Spring MVC, Ktor routing)
 *  - 데이터베이스 스키마 (JPA @Table, Exposed table)
 */
public class KotlinAnalyzer implements LanguageAnalyzer {

    // package 선언
    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("^package\\s+([\\w.]+)", Pattern.MULTILINE);

    // import 문
    private static final Pattern IMPORT_PATTERN =
            Pattern.compile("^import\\s+([\\w.]+)", Pattern.MULTILINE);

    // class / interface / object / data class / sealed class
    private static final Pattern CLASS_PATTERN =
            Pattern.compile(
                "^\\s*(?:(?:public|private|protected|internal|open|abstract|data|sealed|enum|annotation|inline|value)\\s+)*" +
                "(?:class|interface|object)\\s+(\\w+)",
                Pattern.MULTILINE);

    // fun 정의
    private static final Pattern FUN_PATTERN =
            Pattern.compile("^\\s*(?:(?:public|private|protected|internal|suspend|override|inline)\\s+)*fun\\s+(\\w+)",
                    Pattern.MULTILINE);

    // Spring MVC 매핑 어노테이션
    private static final Pattern SPRING_MAPPING_PATTERN =
            Pattern.compile(
                "@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping|RequestMapping)" +
                "\\(?[^)]*[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);

    // JPA @Table
    private static final Pattern JPA_TABLE_PATTERN =
            Pattern.compile("@Table\\s*\\(\\s*name\\s*=\\s*[\"']([\\w_]+)[\"']",
                    Pattern.CASE_INSENSITIVE);

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".kt", ".kts"};
    }

    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        analyzePackage(fileName, content, result);
        result.setClassCount(result.getClassCount() + countClasses(content));
        analyzeDependencies(fileName, content, result);
        detectFrameworks(fileName, content, result);
        detectPatterns(fileName, content, result);
        analyzeApis(content, result);
        analyzeDatabases(content, result);
    }

    // ── 패키지 ───────────────────────────────────────────────────────────────

    private void analyzePackage(String fileName, String content, AnalysisResult result) {
        Matcher m = PACKAGE_PATTERN.matcher(content);
        if (m.find()) {
            result.addPackage(m.group(1));
        } else {
            result.addPackage(extractDirName(fileName));
        }
    }

    private int countClasses(String content) {
        Matcher m = CLASS_PATTERN.matcher(content);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    // ── 의존성 분석 ───────────────────────────────────────────────────────────

    private void analyzeDependencies(String fileName, String content, AnalysisResult result) {
        String pkg = extractDirName(fileName);
        Matcher imp = IMPORT_PATTERN.matcher(content);
        while (imp.find()) {
            String fullImport = imp.group(1);
            String[] parts = fullImport.split("\\.");
            if (parts.length > 0) {
                String topLevel = parts[0];
                // 내부 패키지가 아닌 것만 외부 의존성으로 등록
                if (!topLevel.equals("java") && !topLevel.equals("javax")
                        && !topLevel.equals("kotlin")) {
                    result.addDependency(new DependencyInfo(pkg, fullImport, "import"));
                }
            }
        }
    }

    // ── 프레임워크/라이브러리 감지 ────────────────────────────────────────────

    private void detectFrameworks(String fileName, String content, AnalysisResult result) {

        // ── Spring / Spring Boot ───────────────────────────────────────────
        if (content.contains("@SpringBootApplication") || content.contains("import org.springframework.boot"))
            result.addFramework("Spring Boot");
        if (content.contains("@RestController") || content.contains("@Controller")
                || content.contains("import org.springframework.web"))
            result.addFramework("Spring MVC");
        if (content.contains("@Service") || content.contains("import org.springframework.stereotype"))
            result.addFramework("Spring Service");
        if (content.contains("@Repository") || content.contains("JpaRepository")
                || content.contains("CrudRepository"))
            result.addFramework("Spring Data");
        if (content.contains("@Entity") || content.contains("@Table")
                || content.contains("import jakarta.persistence") || content.contains("import javax.persistence"))
            result.addFramework("Jakarta Persistence (JPA)");
        if (content.contains("import org.hibernate"))
            result.addFramework("Hibernate");
        if (content.contains("@Transactional"))
            result.addFramework("Spring Transaction");
        if (content.contains("SecurityConfig") || content.contains("import org.springframework.security"))
            result.addFramework("Spring Security");
        if (content.contains("WebFlux") || content.contains("Mono<") || content.contains("Flux<"))
            result.addFramework("Spring WebFlux (Reactive)");

        // ── Ktor ───────────────────────────────────────────────────────────
        if (content.contains("import io.ktor") || content.contains("embeddedServer")
                || content.contains("routing {"))
            result.addFramework("Ktor");

        // ── Vert.x ─────────────────────────────────────────────────────────
        if (content.contains("import io.vertx") || content.contains("Verticle")
                || content.contains("vertx."))
            result.addFramework("Vert.x");

        // ── Exposed (JetBrains ORM) ────────────────────────────────────────
        if (content.contains("import org.jetbrains.exposed") || content.contains("object")
                && content.contains("Table()"))
            result.addFramework("Exposed (Kotlin ORM)");

        // ── 코루틴 ─────────────────────────────────────────────────────────
        if (content.contains("suspend fun") || content.contains("import kotlinx.coroutines")
                || content.contains("launch {") || content.contains("async {")
                || content.contains("runBlocking"))
            result.addFramework("Kotlin Coroutines");

        // ── Arrow (함수형) ──────────────────────────────────────────────────
        if (content.contains("import arrow.") || content.contains("Either<")
                || content.contains("Option<"))
            result.addFramework("Arrow (Functional)");

        // ── DI ─────────────────────────────────────────────────────────────
        if (content.contains("import org.koin") || content.contains("startKoin")
                || content.contains("val module = module {"))
            result.addFramework("Koin (DI)");
        if (content.contains("@Inject") && content.contains("import dagger"))
            result.addFramework("Dagger/Hilt (DI)");

        // ── Android ────────────────────────────────────────────────────────
        if (content.contains("import androidx.") || content.contains("Activity")
                || content.contains("Fragment"))
            result.addFramework("Android Jetpack");
        if (content.contains("@Composable") || content.contains("import androidx.compose"))
            result.addFramework("Jetpack Compose");
        if (content.contains("@Entity") && content.contains("import androidx.room"))
            result.addFramework("Room (Android DB)");
        if (content.contains("ViewModel") || content.contains("LiveData<")
                || content.contains("StateFlow<"))
            result.addFramework("Android Architecture Components");

        // ── 네트워크 ───────────────────────────────────────────────────────
        if (content.contains("import retrofit2") || content.contains("@GET(")
                || content.contains("@POST("))
            result.addFramework("Retrofit");
        if (content.contains("import okhttp3") || content.contains("OkHttpClient"))
            result.addFramework("OkHttp");

        // ── 직렬화 ─────────────────────────────────────────────────────────
        if (content.contains("kotlinx.serialization") || content.contains("@Serializable"))
            result.addFramework("kotlinx.serialization");
        if (content.contains("import com.fasterxml.jackson") || content.contains("ObjectMapper"))
            result.addFramework("Jackson");
        if (content.contains("import com.google.gson") || content.contains("Gson()"))
            result.addFramework("Gson");

        // ── 테스팅 ─────────────────────────────────────────────────────────
        if (content.contains("import io.kotest") || content.contains("FunSpec()")
                || content.contains("DescribeSpec()"))
            result.addFramework("Kotest");
        if (content.contains("import io.mockk") || content.contains("mockk<")
                || content.contains("every {"))
            result.addFramework("MockK");
        if (content.contains("import org.junit") || content.contains("@Test"))
            result.addFramework("JUnit");
        if (content.contains("import org.assertj") || content.contains("assertThat("))
            result.addFramework("AssertJ");

        // ── 로깅 ──────────────────────────────────────────────────────────
        if (content.contains("import org.slf4j") || content.contains("LoggerFactory"))
            result.addFramework("SLF4J");
        if (content.contains("import mu.KLogger") || content.contains("KotlinLogging"))
            result.addFramework("kotlin-logging");

        // ── GraphQL ───────────────────────────────────────────────────────
        if (content.contains("GraphQL") || content.contains("@QueryMapping")) {
            result.addFramework("GraphQL (Spring for GraphQL)");
        }

        // ── Gradle KTS ────────────────────────────────────────────────────
        if (fileName.endsWith(".kts") && content.contains("plugins {"))
            result.addFramework("Gradle Kotlin DSL");
    }

    // ── 디자인 패턴 감지 ─────────────────────────────────────────────────────

    private void detectPatterns(String fileName, String content, AnalysisResult result) {
        String lower    = fileName.toLowerCase();
        String baseName = extractBaseName(fileName);

        // Singleton (Kotlin object)
        if (content.matches("(?s).*^\\s*object\\s+\\w+.*") || lower.contains("singleton"))
            result.addDesignPattern("Singleton (object)", baseName);

        // Builder (apply / also / DSL)
        if (lower.contains("builder") || content.contains(".apply {")
                || content.contains("fn build("))
            result.addDesignPattern("Builder", baseName);

        // Factory / Companion object factory
        if (lower.contains("factory") || content.contains("companion object")
                && content.contains("fun create("))
            result.addDesignPattern("Factory", baseName);

        // Strategy
        if (lower.contains("strategy") || lower.contains("policy"))
            result.addDesignPattern("Strategy", baseName);

        // Observer / Event
        if (lower.contains("event") || lower.contains("listener")
                || content.contains("EventBus") || content.contains("SharedFlow"))
            result.addDesignPattern("Observer / Event", baseName);

        // Repository
        if (lower.contains("repository") || lower.contains("repo"))
            result.addDesignPattern("Repository", baseName);

        // Service Layer
        if (lower.contains("service"))
            result.addDesignPattern("Service Layer", baseName);

        // Controller (MVC)
        if (lower.contains("controller") || content.contains("@RestController")
                || content.contains("@Controller"))
            result.addDesignPattern("MVC Controller", baseName);

        // Sealed Class (Kotlin 특화)
        if (content.contains("sealed class") || content.contains("sealed interface"))
            result.addDesignPattern("Sealed Class (ADT)", baseName);

        // Decorator / Delegate
        if (content.contains("by lazy") || content.contains("by delegate")
                || lower.contains("decorator") || lower.contains("delegate"))
            result.addDesignPattern("Delegate / Decorator", baseName);

        // DTO/Data class
        if (content.contains("data class"))
            result.addDesignPattern("DTO/Data class", baseName);

        // Coroutine-based patterns
        if (content.contains("suspend fun") && lower.contains("service"))
            result.addDesignPattern("Coroutine Service", baseName);

        // Use Case (Clean Architecture)
        if (lower.contains("usecase") || lower.contains("use_case"))
            result.addDesignPattern("Use Case", baseName);

        // ViewModel (Android)
        if (lower.contains("viewmodel") || content.contains("ViewModel()"))
            result.addDesignPattern("ViewModel (MVVM)", baseName);

        // Mapper
        if (lower.contains("mapper"))
            result.addDesignPattern("Mapper", baseName);
    }

    // ── API 분석 ─────────────────────────────────────────────────────────────

    private void analyzeApis(String content, AnalysisResult result) {
        // Spring MVC 매핑 어노테이션
        Matcher sm = SPRING_MAPPING_PATTERN.matcher(content);
        while (sm.find()) {
            String method = sm.group(1)
                    .replace("Mapping", "")
                    .replace("Request", "")
                    .toUpperCase();
            result.addApiEndpoint(method + " " + sm.group(2) + " (Spring MVC)");
        }

        // Ktor routing
        Pattern ktorGet = Pattern.compile(
                "(get|post|put|patch|delete)\\s*\\(\"([^\"]+)\"\\)\\s*\\{", Pattern.CASE_INSENSITIVE);
        Matcher kr = ktorGet.matcher(content);
        while (kr.find()) {
            result.addApiEndpoint(kr.group(1).toUpperCase() + " " + kr.group(2) + " (Ktor)");
        }

        // Retrofit API 인터페이스
        Pattern retrofit = Pattern.compile(
                "@(GET|POST|PUT|PATCH|DELETE)\\(\"([^\"]+)\"\\)", Pattern.CASE_INSENSITIVE);
        Matcher rf = retrofit.matcher(content);
        while (rf.find()) {
            result.addApiEndpoint(rf.group(1).toUpperCase() + " " + rf.group(2) + " (Retrofit)");
        }
    }

    // ── 데이터베이스 분석 ─────────────────────────────────────────────────────

    private void analyzeDatabases(String content, AnalysisResult result) {
        // JPA @Table(name = "...")
        Matcher jpa = JPA_TABLE_PATTERN.matcher(content);
        while (jpa.find()) {
            result.addDatabaseSchema("Table: " + jpa.group(1) + " (JPA Entity)");
        }

        // Exposed table object (object Users : Table())
        Pattern exposed = Pattern.compile(
                "^\\s*object\\s+(\\w+)\\s*:\\s*(?:Int)?Table", Pattern.MULTILINE);
        Matcher exp = exposed.matcher(content);
        while (exp.find()) {
            result.addDatabaseSchema("Table: " + exp.group(1) + " (Exposed)");
        }

        // Room @Entity
        Pattern room = Pattern.compile(
                "@Entity.*?\\bclass\\s+(\\w+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher rm = room.matcher(content);
        while (rm.find()) {
            result.addDatabaseSchema("Table: " + rm.group(1) + " (Room)");
        }
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private String extractDirName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        return parts.length > 1 ? parts[parts.length - 2] : "root";
    }

    private String extractBaseName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        String name = parts[parts.length - 1];
        return name.replaceAll("\\.(kt|kts)$", "");
    }
}
