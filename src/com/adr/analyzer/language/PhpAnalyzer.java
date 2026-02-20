package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PHP 언어 분석기
 *
 * 지원 파일: .php, .phtml, .php3, .php4, .php5, .phps, composer.json, composer.lock
 *
 * 감지 항목:
 *  - 네임스페이스/클래스/인터페이스/트레이트
 *  - 의존성 (use 문, composer.json)
 *  - 프레임워크/라이브러리:
 *    웹: Laravel, Symfony, CodeIgniter, WordPress, Yii2, CakePHP, Slim, Lumen, Phalcon, Laminas(Zend)
 *    ORM: Eloquent, Doctrine, Propel, Cycle ORM
 *    템플릿: Blade, Twig, Smarty, Plates
 *    인증: Laravel Sanctum, Passport, JWT-Auth, Tymon, firebase/php-jwt
 *    비동기/Task: Laravel Queue, Horizon, Swoole, ReactPHP, Amp
 *    API: Laravel API Resources, API Platform, Fractal, Dingo API
 *    테스팅: PHPUnit, Pest, Mockery, PHPSpec, Codeception
 *    DB: PDO, MySQLi, Doctrine DBAL, Redis, MongoDB
 *    Composer: 의존성, dev-의존성
 *  - 디자인 패턴: Singleton, Factory, Repository, Service, Observer, Decorator 등
 *  - API 엔드포인트: Laravel/Symfony 라우트
 *  - DB 스키마: Laravel Migration, Doctrine Entity, 원시 CREATE TABLE
 */
public class PhpAnalyzer implements LanguageAnalyzer {

    private static final String DEPENDENCY_TYPE = "dependency";
    private static final String TABLE_PREFIX    = "Table: ";
    private static final String INTERFACE_KW    = "interface ";

    // namespace 선언
    private static final Pattern NAMESPACE_PATTERN =
            Pattern.compile("^namespace\\s+([\\w\\\\]+)\\s*;", Pattern.MULTILINE);

    // use 문 (use Foo\\Bar;  / use Foo\\Bar as Baz;)
    private static final Pattern USE_PATTERN =
            Pattern.compile("^use\\s+([\\w\\\\]+)(?:\\s+as\\s+\\w+)?\\s*;", Pattern.MULTILINE);

    // class / interface / trait / abstract class / enum 정의
    private static final Pattern CLASS_PATTERN =
            Pattern.compile(
                "^(?:abstract\\s+|final\\s+)?(?:class|interface|trait|enum)\\s+(\\w+)",
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    // Laravel Route 정의 (Route::get/post/put/patch/delete/any)
    private static final Pattern LARAVEL_ROUTE_PATTERN =
            Pattern.compile(
                "Route\\s*::\\s*(?:get|post|put|patch|delete|any|match|resource|apiResource)" +
                "\\s*\\(\\s*['\"]([^'\"]+)['\"]",
                Pattern.CASE_INSENSITIVE);

    // Symfony 어노테이션/속성 라우트
    private static final Pattern SYMFONY_ROUTE_PATTERN =
            Pattern.compile(
                "#\\[Route\\(['\"]([^'\"]+)['\"]|@Route\\(['\"]([^'\"]+)['\"]",
                Pattern.CASE_INSENSITIVE);

    // Slim 라우트
    private static final Pattern SLIM_ROUTE_PATTERN =
            Pattern.compile(
                "\\$app\\s*->\\s*(?:get|post|put|patch|delete|any)\\s*\\(\\s*['\"]([^'\"]+)['\"]",
                Pattern.CASE_INSENSITIVE);

    // Eloquent Migration table 생성
    private static final Pattern LARAVEL_SCHEMA_PATTERN =
            Pattern.compile(
                "Schema\\s*::\\s*create\\s*\\(\\s*['\"]([\\w]+)['\"]",
                Pattern.CASE_INSENSITIVE);

    // Doctrine @Table / #[ORM\Table]
    private static final Pattern DOCTRINE_TABLE_PATTERN =
            Pattern.compile(
                "(?:@Table|#\\[ORM\\\\Table)\\s*\\(\\s*name\\s*=\\s*['\"]([\\w]+)['\"]",
                Pattern.CASE_INSENSITIVE);

    // CREATE TABLE SQL
    private static final Pattern SQL_CREATE_TABLE_PATTERN =
            Pattern.compile(
                "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?[`'\"]?([\\w]+)[`'\"]?",
                Pattern.CASE_INSENSITIVE);

    // composer.json require 블록 패키지명
    private static final Pattern COMPOSER_REQUIRE_PATTERN =
            Pattern.compile(
                "\"([a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+)\"\\s*:\\s*\"([^\"]+)\"");

    // PHP 표준 라이브러리 / 내장 함수 네임스페이스 (use 문 중 제외 대상)
    private static final Set<String> PHP_BUILTIN_NAMESPACES = new HashSet<>(Arrays.asList(
        "Exception", "RuntimeException", "InvalidArgumentException", "LogicException",
        "BadMethodCallException", "OutOfRangeException", "OverflowException",
        "UnexpectedValueException", "DomainException", "LengthException",
        "ArrayAccess", "Countable", "Iterator", "IteratorAggregate", "Serializable",
        "Stringable", "Throwable", "Traversable",
        "DateTime", "DateTimeImmutable", "DateTimeInterface", "DateInterval", "DateTimeZone",
        "SplStack", "SplQueue", "SplHeap", "SplMinHeap", "SplMaxHeap",
        "SplFixedArray", "SplDoublyLinkedList",
        "stdClass", "Closure", "Generator", "Fiber",
        "ReflectionClass", "ReflectionMethod", "ReflectionProperty",
        "PDO", "PDOStatement", "PDOException"
    ));

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".php", ".phtml", ".php3", ".php4", ".php5", ".phps"};
    }

    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        String lower = fileName.toLowerCase();

        // composer.json 전용 처리
        if (lower.equals("composer.json")) {
            analyzeComposerJson(content, result);
            return;
        }
        // composer.lock 은 스킵 (실제 잠금 파일이므로 중복 의존성 방지)
        if (lower.equals("composer.lock")) {
            return;
        }

        // 일반 PHP 파일 분석
        analyzeNamespaceAndClasses(content, result);
        analyzeDependencies(fileName, content, result);
        detectFrameworks(content, result);
        detectPatterns(fileName, content, result);
        analyzeApis(content, result);
        analyzeDatabases(content, result);
    }

    // ── 네임스페이스 · 클래스 ─────────────────────────────────────────────────

    private void analyzeNamespaceAndClasses(String content, AnalysisResult result) {
        // 네임스페이스 → 패키지로 등록
        Matcher ns = NAMESPACE_PATTERN.matcher(content);
        while (ns.find()) {
            result.addPackage(ns.group(1).replace("\\", "."));
        }

        // 클래스/인터페이스/트레이트 수 카운트
        Matcher cls = CLASS_PATTERN.matcher(content);
        int count = 0;
        while (cls.find()) {
            count++;
        }
        result.setClassCount(result.getClassCount() + count);
    }

    // ── 의존성 분석 ───────────────────────────────────────────────────────────

    private void analyzeDependencies(String fileName, String content, AnalysisResult result) {
        String module = extractModuleName(fileName);

        Matcher use = USE_PATTERN.matcher(content);
        while (use.find()) {
            String fqn = use.group(1);
            String vendor = extractVendorNamespace(fqn);
            if (!isBuiltinNamespace(vendor)) {
                result.addDependency(new DependencyInfo(module, fqn, "use"));
            }
        }
    }

    private void analyzeComposerJson(String content, AnalysisResult result) {
        boolean inRequire = false;
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.contains("\"require\"") || trimmed.contains("\"require-dev\"")) {
                inRequire = true;
            } else if (inRequire && trimmed.equals("},")) {
                inRequire = false;
            } else if (inRequire) {
                Matcher m = COMPOSER_REQUIRE_PATTERN.matcher(trimmed);
                if (m.find()) {
                    String pkg = m.group(1);
                    if (!pkg.equals("php") && !pkg.startsWith("ext-")) {
                        result.addDependency(
                            new DependencyInfo("composer.json", pkg, DEPENDENCY_TYPE));
                    }
                }
            }
        }
        detectFrameworks(content, result);
    }

    // ── 프레임워크 감지 ───────────────────────────────────────────────────────

    private void detectFrameworks(String content, AnalysisResult result) {

        // ── 웹 프레임워크 ──────────────────────────────────────────────────────
        if (containsAny(content,
                "laravel/framework", "Illuminate\\", "use Illuminate",
                "Laravel\\", "artisan", "Eloquent", "Route::"))
            result.addFramework("Laravel");

        if (containsAny(content,
                "symfony/symfony", "Symfony\\", "use Symfony",
                "Symfony\\Component", "FrameworkBundle", "ContainerInterface"))
            result.addFramework("Symfony");

        if (containsAny(content,
                "wordpress", "wp_enqueue", "add_action", "add_filter",
                "get_posts", "WP_Query", "wp-includes", "wp-content"))
            result.addFramework("WordPress");

        if (containsAny(content,
                "codeigniter", "CodeIgniter\\", "CI_Controller",
                "CI_Model", "$this->load->", "$this->db->"))
            result.addFramework("CodeIgniter");

        if (containsAny(content,
                "yii\\", "use yii\\", "Yii::$app", "yii/yii", "yiisoft/yii2"))
            result.addFramework("Yii2");

        if (containsAny(content,
                "cakephp", "CakePHP", "Cake\\", "use Cake\\", "AppController"))
            result.addFramework("CakePHP");

        if (containsAny(content,
                "slim/slim", "Slim\\", "use Slim\\", "\\Slim\\App",
                "$app->get(", "$app->post("))
            result.addFramework("Slim Framework");

        if (containsAny(content,
                "laravel/lumen", "Laravel\\Lumen", "use Laravel\\Lumen"))
            result.addFramework("Lumen");

        if (containsAny(content,
                "phalcon", "Phalcon\\", "use Phalcon\\"))
            result.addFramework("Phalcon");

        if (containsAny(content,
                "laminas", "Laminas\\", "use Laminas\\",
                "zendframework", "Zend\\", "use Zend\\"))
            result.addFramework("Laminas (Zend)");

        if (containsAny(content,
                "hyperf/hyperf", "Hyperf\\", "use Hyperf\\"))
            result.addFramework("Hyperf");

        if (containsAny(content,
                "spiral/framework", "Spiral\\", "use Spiral\\"))
            result.addFramework("Spiral Framework");

        // ── ORM / 데이터베이스 ──────────────────────────────────────────────────
        if (containsAny(content,
                "Illuminate\\Database", "use Illuminate\\Database",
                "extends Model", "Eloquent", "HasMany", "BelongsTo", "belongsTo"))
            result.addFramework("Eloquent ORM");

        if (containsAny(content,
                "doctrine/orm", "doctrine/dbal", "Doctrine\\ORM",
                "use Doctrine\\", "@Entity", "#[ORM\\Entity",
                "EntityManager", "QueryBuilder"))
            result.addFramework("Doctrine ORM");

        if (containsAny(content,
                "propel/propel", "Propel\\", "PropelQuery", "use Propel\\"))
            result.addFramework("Propel ORM");

        if (containsAny(content,
                "cycle/orm", "Cycle\\ORM", "use Cycle\\ORM"))
            result.addFramework("Cycle ORM");

        if (containsAny(content, "new PDO(", "PDO::FETCH", "PDO::ATTR"))
            result.addFramework("PDO");

        if (containsAny(content, "mysqli_connect", "new mysqli(", "mysqli::"))
            result.addFramework("MySQLi");

        if (containsAny(content,
                "predis/predis", "use Predis\\", "new Predis",
                "phpredis", "\\Redis()", "new Redis()"))
            result.addFramework("Redis (PHP)");

        if (containsAny(content,
                "mongodb/mongodb", "MongoDB\\", "use MongoDB\\",
                "MongoClient", "MongoCollection"))
            result.addFramework("MongoDB (PHP)");

        // ── 템플릿 엔진 ─────────────────────────────────────────────────────────
        if (containsAny(content,
                "twig/twig", "Twig\\", "use Twig\\", "Twig_Environment",
                "TwigFunction", "twig.extension"))
            result.addFramework("Twig");

        if (containsAny(content,
                "smarty/smarty", "new Smarty", "Smarty()", "$smarty->"))
            result.addFramework("Smarty");

        if (containsAny(content,
                "league/plates", "League\\Plates", "use League\\Plates"))
            result.addFramework("Plates");

        // ── 인증 ───────────────────────────────────────────────────────────────
        if (containsAny(content,
                "tymon/jwt-auth", "Tymon\\JWTAuth", "JWTAuth::",
                "firebase/php-jwt", "Firebase\\JWT"))
            result.addFramework("JWT Auth (PHP)");

        if (containsAny(content,
                "laravel/passport", "Passport::", "Laravel\\Passport"))
            result.addFramework("Laravel Passport");

        if (containsAny(content,
                "laravel/sanctum", "Sanctum::", "HasApiTokens"))
            result.addFramework("Laravel Sanctum");

        // ── 비동기 / 서버 ───────────────────────────────────────────────────────
        if (containsAny(content,
                "swoole", "SwooleServer", "\\Swoole\\", "use Swoole\\"))
            result.addFramework("Swoole");

        if (containsAny(content,
                "react/event-loop", "ReactPHP", "React\\EventLoop",
                "LoopInterface", "use React\\"))
            result.addFramework("ReactPHP");

        if (containsAny(content,
                "amphp/amp", "Amp\\", "use Amp\\", "Amp\\Promise"))
            result.addFramework("Amp (PHP)");

        // ── 큐 / 메시지 ─────────────────────────────────────────────────────────
        if (containsAny(content,
                "laravel/horizon", "Horizon::", "Laravel\\Horizon"))
            result.addFramework("Laravel Horizon");

        if (containsAny(content,
                "php-amqplib", "AMQPStreamConnection", "use PhpAmqpLib\\"))
            result.addFramework("RabbitMQ (php-amqplib)");

        if (containsAny(content,
                "rdkafka", "RdKafka\\", "use RdKafka\\",
                "kafka-php", "Kafka\\"))
            result.addFramework("Kafka (PHP)");

        // ── HTTP 클라이언트 ─────────────────────────────────────────────────────
        if (containsAny(content,
                "guzzlehttp/guzzle", "GuzzleHttp\\", "use GuzzleHttp\\",
                "new Client()", "GuzzleHttp\\Client"))
            result.addFramework("Guzzle HTTP");

        if (containsAny(content,
                "symfony/http-client", "Symfony\\Component\\HttpClient",
                "HttpClient::create"))
            result.addFramework("Symfony HttpClient");

        // ── API / 직렬화 ────────────────────────────────────────────────────────
        if (containsAny(content,
                "api-platform/core", "ApiPlatform\\", "use ApiPlatform\\",
                "@ApiResource", "#[ApiResource"))
            result.addFramework("API Platform");

        if (containsAny(content,
                "league/fractal", "League\\Fractal", "use League\\Fractal"))
            result.addFramework("Fractal (Transformer)");

        if (containsAny(content,
                "spatie", "Spatie\\", "use Spatie\\"))
            result.addFramework("Spatie Packages");

        // ── 테스팅 ─────────────────────────────────────────────────────────────
        if (containsAny(content,
                "phpunit/phpunit", "PHPUnit\\", "use PHPUnit\\",
                "TestCase", "extends TestCase", "PHPUnit_Framework"))
            result.addFramework("PHPUnit");

        if (containsAny(content,
                "pestphp/pest", "uses(", "it('", "test('",
                "expect(", "beforeEach("))
            result.addFramework("Pest (PHP)");

        if (containsAny(content,
                "mockery/mockery", "Mockery::", "use Mockery"))
            result.addFramework("Mockery");

        if (containsAny(content,
                "codeception/codeception", "Codeception\\", "use Codeception\\"))
            result.addFramework("Codeception");

        // ── 유틸리티 / 기타 ─────────────────────────────────────────────────────
        if (containsAny(content,
                "vlucas/phpdotenv", "Dotenv\\", "Dotenv::createImmutable",
                "Dotenv::create"))
            result.addFramework("PHP Dotenv");

        if (containsAny(content,
                "monolog/monolog", "Monolog\\", "use Monolog\\",
                "Logger(", "Monolog\\Logger"))
            result.addFramework("Monolog (Logging)");

        if (containsAny(content,
                "phpmailer/phpmailer", "PHPMailer\\", "use PHPMailer\\",
                "new PHPMailer"))
            result.addFramework("PHPMailer");

        if (containsAny(content,
                "intervention/image", "Image::", "use Intervention\\Image"))
            result.addFramework("Intervention Image");

        if (containsAny(content,
                "nesbot/carbon", "Carbon\\", "use Carbon\\", "Carbon::"))
            result.addFramework("Carbon (Date)");

        if (containsAny(content,
                "graphql-php", "GraphQL\\", "use GraphQL\\", "GraphQL::executeQuery"))
            result.addFramework("GraphQL PHP");
    }

    // ── 디자인 패턴 감지 ──────────────────────────────────────────────────────

    private void detectPatterns(String fileName, String content, AnalysisResult result) {
        String lc = content.toLowerCase();
        String module = extractModuleName(fileName);

        if (content.contains("private static $instance") ||
                content.contains("private static $_instance") ||
                content.contains("getInstance()"))
            result.addDesignPattern("Singleton", module);

        if (content.contains("interface Factory") ||
                content.contains("abstract class Factory") ||
                lc.contains("factory") && content.contains("create("))
            result.addDesignPattern("Factory", module);

        if (content.contains("interface Repository") ||
                content.contains("Repository {") ||
                lc.contains("repository"))
            result.addDesignPattern("Repository", module);

        if (content.contains("interface ServiceInterface") ||
                (lc.contains("service") && content.contains(INTERFACE_KW)))
            result.addDesignPattern("Service Layer", module);

        if (content.contains("interface ObserverInterface") ||
                content.contains("attach(") && content.contains("notify(") ||
                content.contains("ShouldBroadcast") || content.contains("dispatchEvent"))
            result.addDesignPattern("Observer", module);

        if (content.contains("abstract class Decorator") ||
                content.contains("extends Decorator") ||
                (lc.contains("decorator") && content.contains(INTERFACE_KW)))
            result.addDesignPattern("Decorator", module);

        if (content.contains("interface Strategy") ||
                (lc.contains("strategy") && content.contains(INTERFACE_KW)))
            result.addDesignPattern("Strategy", module);

        if ((content.contains("interface Command") || lc.contains("command")) &&
                content.contains("execute("))
            result.addDesignPattern("Command", module);

        if (content.contains("interface Builder") ||
                (lc.contains("builder") && content.contains("build(")))
            result.addDesignPattern("Builder", module);

        if (lc.contains("middleware") && content.contains("handle("))
            result.addDesignPattern("Middleware", module);

        if (content.contains("abstract class Controller") ||
                content.contains("extends Controller"))
            result.addDesignPattern("MVC Controller", module);

        if (content.contains("Request $request") ||
                content.contains("FormRequest") ||
                content.contains("extends FormRequest"))
            result.addDesignPattern("DTO/Request", module);

        if (content.contains("@ApiResource") ||
                content.contains("#[ApiResource") ||
                (lc.contains("resource") && content.contains("toArray(")))
            result.addDesignPattern("Resource/Transformer", module);

        if (content.contains("trait "))
            result.addDesignPattern("Trait/Mixin", module);

        if (content.contains("interface EventInterface") ||
                content.contains("extends Event") ||
                content.contains("ShouldQueue"))
            result.addDesignPattern("Event/Listener", module);

        if (content.contains("extends Migration"))
            result.addDesignPattern("Migration", module);
    }

    // ── API 엔드포인트 분석 ───────────────────────────────────────────────────

    private void analyzeApis(String content, AnalysisResult result) {
        // Laravel Route
        Matcher lr = LARAVEL_ROUTE_PATTERN.matcher(content);
        while (lr.find()) {
            result.addApiEndpoint("PHP:" + lr.group(1));
        }
        // Symfony Route
        Matcher sr = SYMFONY_ROUTE_PATTERN.matcher(content);
        while (sr.find()) {
            String path = sr.group(1) != null ? sr.group(1) : sr.group(2);
            if (path != null) result.addApiEndpoint("PHP:" + path);
        }
        // Slim Route
        Matcher slim = SLIM_ROUTE_PATTERN.matcher(content);
        while (slim.find()) {
            result.addApiEndpoint("PHP:" + slim.group(1));
        }
    }

    // ── 데이터베이스 스키마 분석 ──────────────────────────────────────────────

    private void analyzeDatabases(String content, AnalysisResult result) {
        // Laravel Eloquent Migration: Schema::create('table_name', ...)
        Matcher lm = LARAVEL_SCHEMA_PATTERN.matcher(content);
        while (lm.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + lm.group(1) + " (Eloquent Migration)");
        }

        // Doctrine @Table(name="...") / #[ORM\Table(name="...")]
        Matcher dt = DOCTRINE_TABLE_PATTERN.matcher(content);
        while (dt.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + dt.group(1) + " (Doctrine Entity)");
        }

        // Eloquent Model: class Xxx extends Model → implicit table 이름 추출
        Pattern eloquentModel = Pattern.compile(
                "^class\\s+(\\w+)\\s+extends\\s+(?:Model|Eloquent|Authenticatable)",
                Pattern.MULTILINE);
        Matcher em = eloquentModel.matcher(content);
        while (em.find()) {
            // Laravel 규칙: ClassName → snake_case + s (간단 추정)
            String tableName = toSnakeCase(em.group(1));
            result.addDatabaseSchema(TABLE_PREFIX + tableName + " (Eloquent Model)");
        }

        // Doctrine Entity 어노테이션 (class 이름 기반)
        Pattern doctrineEntity = Pattern.compile(
                "(?:@Entity|#\\[ORM\\\\Entity).*?^class\\s+(\\w+)",
                Pattern.DOTALL | Pattern.MULTILINE);
        Matcher de = doctrineEntity.matcher(content);
        while (de.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + de.group(1) + " (Doctrine Entity)");
        }

        // 원시 CREATE TABLE SQL
        Matcher sql = SQL_CREATE_TABLE_PATTERN.matcher(content);
        while (sql.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + sql.group(1) + " (Raw SQL)");
        }
    }

    // ── 헬퍼 메서드 ───────────────────────────────────────────────────────────

    private String extractModuleName(String fileName) {
        int sep = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        String base = sep >= 0 ? fileName.substring(sep + 1) : fileName;
        int dot = base.lastIndexOf('.');
        return dot > 0 ? base.substring(0, dot) : base;
    }

    private String extractVendorNamespace(String fqn) {
        // Illuminate\Database\Eloquent → Illuminate
        String[] parts = fqn.split("\\\\");
        return parts[0];
    }

    private boolean isBuiltinNamespace(String vendor) {
        return PHP_BUILTIN_NAMESPACES.contains(vendor);
    }

    private boolean containsAny(String content, String... keywords) {
        String lower = content.toLowerCase();
        for (String kw : keywords) {
            if (lower.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    /** PascalCase → snake_case 변환 (Eloquent 관례용) */
    private String toSnakeCase(String pascal) {
        StringBuilder sb = new StringBuilder();
        char[] chars = pascal.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isUpperCase(chars[i]) && i > 0) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(chars[i]));
        }
        sb.append('s');
        return sb.toString();
    }
}
