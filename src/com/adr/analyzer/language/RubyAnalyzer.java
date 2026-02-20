package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ruby 언어 분석기
 * .rb, Gemfile, Rakefile 등을 분석하여
 * 클래스, 모듈, gem 의존성, 프레임워크, 디자인 패턴 등을 추출
 */
public class RubyAnalyzer implements LanguageAnalyzer {

    // require / require_relative
    private static final Pattern REQUIRE_PATTERN =
            Pattern.compile("^\\s*require\\s+['\"]([^'\"]+)['\"]", Pattern.MULTILINE);
    private static final Pattern REQUIRE_REL_PATTERN =
            Pattern.compile("^\\s*require_relative\\s+['\"]([^'\"]+)['\"]", Pattern.MULTILINE);

    // class / module 정의
    private static final Pattern CLASS_PATTERN =
            Pattern.compile("^\\s*(class|module)\\s+([A-Z][\\w:]*)", Pattern.MULTILINE);

    // Gemfile gem 선언
    private static final Pattern GEM_PATTERN =
            Pattern.compile("^\\s*gem\\s+['\"]([^'\"]+)['\"]", Pattern.MULTILINE);

    // 메서드 정의
    private static final Pattern METHOD_PATTERN =
            Pattern.compile("^\\s*def\\s+(\\w+)", Pattern.MULTILINE);

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".rb", ".rake", ".gemspec"};
    }

    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        analyzeModule(fileName, content, result);
        result.setClassCount(result.getClassCount() + countClassesAndModules(content));
        analyzeDependencies(fileName, content, result);
        detectFrameworks(fileName, content, result);
        detectPatterns(fileName, content, result);
        analyzeDatabases(content, result);
        analyzeApis(content, result);
    }

    // ── 모듈/네임스페이스 ─────────────────────────────────────────────────────

    private void analyzeModule(String fileName, String content, AnalysisResult result) {
        // 최상위 module 이름을 패키지로 등록
        Matcher m = CLASS_PATTERN.matcher(content);
        while (m.find()) {
            if (m.group(1).equals("module")) {
                result.addPackage(m.group(2));
                return; // 첫 번째 모듈만
            }
        }
        // 모듈이 없으면 디렉토리명
        result.addPackage(extractModuleName(fileName));
    }

    // ── 클래스/모듈 카운트 ────────────────────────────────────────────────────

    private int countClassesAndModules(String content) {
        Matcher m = CLASS_PATTERN.matcher(content);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    // ── 의존성 (require) ──────────────────────────────────────────────────────

    private void analyzeDependencies(String fileName, String content, AnalysisResult result) {
        String currentModule = extractModuleName(fileName);

        // require (외부 gem)
        Matcher req = REQUIRE_PATTERN.matcher(content);
        while (req.find()) {
            String lib = req.group(1);
            if (!lib.startsWith(".")) {
                result.addDependency(new DependencyInfo(currentModule, lib, "require"));
            }
        }

        // require_relative (내부 파일)
        Matcher rel = REQUIRE_REL_PATTERN.matcher(content);
        while (rel.find()) {
            result.addDependency(new DependencyInfo(currentModule, rel.group(1), "require_relative"));
        }

        // Gemfile gem 선언
        if (fileName.toLowerCase().contains("gemfile")) {
            Matcher gem = GEM_PATTERN.matcher(content);
            while (gem.find()) {
                result.addDependency(new DependencyInfo("Gemfile", gem.group(1), "gem"));
            }
        }
    }

    // ── 프레임워크/라이브러리 감지 ────────────────────────────────────────────

    private void detectFrameworks(String fileName, String content, AnalysisResult result) {
        // Rails
        if (content.contains("Rails") || content.contains("ActionController") ||
                content.contains("ActiveRecord") || content.contains("ApplicationController"))
            result.addFramework("Ruby on Rails");
        if (content.contains("ActiveRecord::Base") || content.contains("ApplicationRecord"))
            result.addFramework("ActiveRecord (ORM)");
        if (content.contains("ActionMailer") || content.contains("ApplicationMailer"))
            result.addFramework("ActionMailer");
        if (content.contains("ActionCable"))
            result.addFramework("ActionCable (WebSocket)");
        if (content.contains("ActiveJob") || content.contains("ApplicationJob"))
            result.addFramework("ActiveJob (Background Jobs)");

        // Sinatra
        if (content.contains("require 'sinatra'") || content.contains("require \"sinatra\"") ||
                content.contains("Sinatra::Base") || content.contains("Sinatra::Application"))
            result.addFramework("Sinatra");

        // Hanami
        if (content.contains("Hanami::") || content.contains("require 'hanami'"))
            result.addFramework("Hanami");

        // Grape (API DSL)
        if (content.contains("Grape::API") || content.contains("require 'grape'"))
            result.addFramework("Grape (API)");

        // Rack
        if (content.contains("Rack::") || content.contains("require 'rack'"))
            result.addFramework("Rack");

        // 데이터베이스
        if (content.contains("require 'sequel'") || content.contains("Sequel::"))
            result.addFramework("Sequel (ORM)");
        if (content.contains("require 'mongoid'") || content.contains("Mongoid::"))
            result.addFramework("Mongoid (MongoDB)");
        if (content.contains("require 'redis'") || content.contains("Redis.new"))
            result.addFramework("Redis");
        if (content.contains("require 'pg'") || content.contains("PG.connect"))
            result.addFramework("PostgreSQL (pg gem)");
        if (content.contains("require 'mysql2'"))
            result.addFramework("MySQL (mysql2 gem)");
        if (content.contains("require 'sqlite3'"))
            result.addFramework("SQLite3");

        // 테스팅
        if (content.contains("require 'rspec'") || content.contains("RSpec.describe") ||
                content.contains("describe ") && content.contains("it "))
            result.addFramework("RSpec");
        if (content.contains("require 'minitest'") || content.contains("Minitest::Test") ||
                content.contains("def test_"))
            result.addFramework("Minitest");
        if (content.contains("require 'test/unit'") || content.contains("Test::Unit"))
            result.addFramework("Test::Unit");
        if (content.contains("require 'capybara'") || content.contains("Capybara"))
            result.addFramework("Capybara (Integration Testing)");
        if (content.contains("require 'factory_bot'") || content.contains("FactoryBot"))
            result.addFramework("FactoryBot");
        if (content.contains("require 'faker'"))
            result.addFramework("Faker");

        // HTTP 클라이언트
        if (content.contains("require 'faraday'") || content.contains("Faraday.new"))
            result.addFramework("Faraday (HTTP Client)");
        if (content.contains("require 'httparty'") || content.contains("HTTParty"))
            result.addFramework("HTTParty");
        if (content.contains("require 'rest-client'") || content.contains("RestClient"))
            result.addFramework("RestClient");

        // 인증
        if (content.contains("Devise") || content.contains("require 'devise'"))
            result.addFramework("Devise (Authentication)");
        if (content.contains("Warden") || content.contains("require 'warden'"))
            result.addFramework("Warden");
        if (content.contains("JWT") || content.contains("require 'jwt'"))
            result.addFramework("JWT");
        if (content.contains("OmniAuth") || content.contains("require 'omniauth'"))
            result.addFramework("OmniAuth");

        // 백그라운드 잡
        if (content.contains("Sidekiq") || content.contains("require 'sidekiq'"))
            result.addFramework("Sidekiq (Background Jobs)");
        if (content.contains("Resque") || content.contains("require 'resque'"))
            result.addFramework("Resque");
        if (content.contains("DelayedJob") || content.contains("delay."))
            result.addFramework("Delayed::Job");

        // 직렬화
        if (content.contains("ActiveModelSerializers") || content.contains("ActiveModel::Serializer"))
            result.addFramework("ActiveModel Serializers");
        if (content.contains("require 'json'") || content.contains("JSON.parse"))
            result.addFramework("JSON (stdlib)");

        // 로깅
        if (content.contains("require 'logger'") || content.contains("Logger.new"))
            result.addFramework("Ruby Logger");

        // 빌드/태스크
        if (fileName.toLowerCase().contains("rakefile") || content.contains("Rake::Task") ||
                content.contains("namespace :") && content.contains("task :"))
            result.addFramework("Rake (Build Tool)");

        // Bundler
        if (fileName.toLowerCase().contains("gemfile"))
            result.addFramework("Bundler (Dependency Management)");

        // GraphQL
        if (content.contains("GraphQL::Schema") || content.contains("require 'graphql'"))
            result.addFramework("GraphQL Ruby");

        // Elasticsearch
        if (content.contains("Elasticsearch::") || content.contains("require 'elasticsearch'"))
            result.addFramework("Elasticsearch");

        // 캐싱
        if (content.contains("Rails.cache") || content.contains("Dalli::"))
            result.addFramework("Memcached / Dalli");
    }

    // ── 디자인 패턴 감지 ─────────────────────────────────────────────────────

    private void detectPatterns(String fileName, String content, AnalysisResult result) {
        String lower = fileName.toLowerCase();
        String baseName = extractBaseName(fileName);

        // Singleton (Ruby module + instance 패턴)
        if (content.contains("include Singleton") || content.contains("require 'singleton'"))
            result.addDesignPattern("Singleton", baseName);

        // Observer (Ruby Observable)
        if (content.contains("include Observable") || content.contains("require 'observer'"))
            result.addDesignPattern("Observer", baseName);

        // Decorator (SimpleDelegator)
        if (content.contains("SimpleDelegator") || content.contains("Forwardable") ||
                lower.contains("decorator"))
            result.addDesignPattern("Decorator", baseName);

        // Factory
        if (lower.contains("factory") || content.matches("(?s).*def\\s+self\\.create.*"))
            result.addDesignPattern("Factory", baseName);

        // Builder
        if (lower.contains("builder") || content.matches("(?s).*def\\s+build\\b.*"))
            result.addDesignPattern("Builder", baseName);

        // Strategy
        if (lower.contains("strategy") || lower.contains("policy"))
            result.addDesignPattern("Strategy", baseName);

        // Command
        if (lower.contains("command") && content.contains("def call"))
            result.addDesignPattern("Command", baseName);

        // Service Object (Rails 관례)
        if (lower.contains("service") || (content.contains("def call") && !lower.contains("controller")))
            result.addDesignPattern("Service Object", baseName);

        // Repository
        if (lower.contains("repository") || lower.contains("repo"))
            result.addDesignPattern("Repository", baseName);

        // Presenter / Decorator (Rails)
        if (lower.contains("presenter") || lower.contains("view_model"))
            result.addDesignPattern("Presenter / View Model", baseName);

        // Concern (Rails Mixin)
        if (content.contains("extend ActiveSupport::Concern") || lower.contains("concern"))
            result.addDesignPattern("Concern (Mixin)", baseName);

        // Controller (Rails / Sinatra)
        if (lower.contains("controller") || content.contains("ApplicationController") ||
                content.contains("< ActionController"))
            result.addDesignPattern("MVC Controller", baseName);

        // Model (ActiveRecord)
        if (content.contains("< ApplicationRecord") || content.contains("< ActiveRecord::Base"))
            result.addDesignPattern("ActiveRecord Model", baseName);

        // Interactor / Use Case
        if (lower.contains("interactor") || lower.contains("use_case"))
            result.addDesignPattern("Interactor / Use Case", baseName);

        // Serializer
        if (lower.contains("serializer"))
            result.addDesignPattern("Serializer", baseName);

        // Worker (Sidekiq / Resque)
        if (lower.contains("worker") || lower.contains("job"))
            result.addDesignPattern("Background Worker", baseName);
    }

    // ── 데이터베이스 분석 ─────────────────────────────────────────────────────

    private void analyzeDatabases(String content, AnalysisResult result) {
        // Rails migration에서 테이블명 추출
        Pattern createTable = Pattern.compile(
                "create_table\\s+[:\"]([\\w]+)[\":]?", Pattern.CASE_INSENSITIVE);
        Matcher m = createTable.matcher(content);
        while (m.find()) {
            result.addDatabaseSchema("Table: " + m.group(1) + " (ActiveRecord Migration)");
        }

        // Sequel 테이블
        Pattern sequelTable = Pattern.compile(
                "DB\\.create_table\\s+[:\"]([\\w]+)[\":]?", Pattern.CASE_INSENSITIVE);
        Matcher sq = sequelTable.matcher(content);
        while (sq.find()) {
            result.addDatabaseSchema("Table: " + sq.group(1) + " (Sequel)");
        }

        // Mongoid 컬렉션
        if (content.contains("include Mongoid::Document")) {
            Pattern coll = Pattern.compile("class\\s+(\\w+).*include Mongoid::Document",
                    Pattern.DOTALL);
            Matcher mc = coll.matcher(content);
            if (mc.find()) {
                result.addDatabaseSchema("Collection: " + mc.group(1) + " (Mongoid)");
            }
        }
    }

    // ── API 분석 ─────────────────────────────────────────────────────────────

    private void analyzeApis(String content, AnalysisResult result) {
        // Rails routes (routes.rb)
        Pattern railsRoute = Pattern.compile(
                "(get|post|put|patch|delete)\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
        Matcher rr = railsRoute.matcher(content);
        while (rr.find()) {
            result.addApiEndpoint(rr.group(1).toUpperCase() + " " + rr.group(2) + " (Rails Route)");
        }

        // Sinatra routes
        Pattern sinatraRoute = Pattern.compile(
                "^\\s*(get|post|put|patch|delete)\\s+['\"]([^'\"]+)['\"]", Pattern.MULTILINE);
        Matcher sr = sinatraRoute.matcher(content);
        while (sr.find()) {
            result.addApiEndpoint(sr.group(1).toUpperCase() + " " + sr.group(2) + " (Sinatra)");
        }

        // Grape API endpoints
        Pattern grapeRoute = Pattern.compile(
                "(get|post|put|patch|delete)\\s+['\"]?([\\w/]+)['\"]?\\s+do");
        Matcher gr = grapeRoute.matcher(content);
        while (gr.find()) {
            result.addApiEndpoint(gr.group(1).toUpperCase() + " " + gr.group(2) + " (Grape)");
        }

        // resources (Rails)
        Pattern resources = Pattern.compile("resources\\s+[:\"]([\\w]+)[\":]?");
        Matcher res = resources.matcher(content);
        while (res.find()) {
            result.addApiEndpoint("CRUD /api/" + res.group(1) + " (Rails resources)");
        }
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private String extractModuleName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        return parts.length > 1 ? parts[parts.length - 2] : "root";
    }

    private String extractBaseName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        String name = parts[parts.length - 1];
        return name.replaceAll("\\.(rb|rake|gemspec)$", "");
    }
}
