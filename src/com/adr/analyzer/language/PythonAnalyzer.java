package com.adr.analyzer.language;

import com.adr.model.AnalysisResult;
import com.adr.model.DependencyInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Python 언어 분석기
 *
 * 지원 파일: .py, .pyw, requirements.txt, Pipfile, pyproject.toml, setup.py, setup.cfg
 *
 * 감지 항목:
 *  - 모듈/패키지 구조 (import, from ... import)
 *  - 클래스/함수 정의 (class, def)
 *  - 의존성 (import, requirements.txt, pyproject.toml, Pipfile)
 *  - 프레임워크/라이브러리:
 *    웹: Django, Flask, FastAPI, Tornado, Sanic, Starlette, aiohttp, Falcon, Bottle, CherryPy
 *    ORM/DB: SQLAlchemy, Django ORM, Peewee, Tortoise ORM, databases
 *    비동기: asyncio, Celery, Dramatiq, RQ, APScheduler
 *    API: DRF, Graphene, Strawberry, Pydantic
 *    ML/AI: TensorFlow, PyTorch, scikit-learn, Keras, Hugging Face, LangChain, NumPy, Pandas
 *    테스팅: pytest, unittest, hypothesis
 *    메시지: Kafka-python, Pika (RabbitMQ), Redis-py
 *    CLI: Click, Typer, argparse, Fire
 *    기타: Scrapy, Celery, Alembic, pydantic, attrs, aiofiles
 *  - 디자인 패턴: Singleton, Factory, Strategy, Observer, Decorator, Repository, Command 등
 *  - API 엔드포인트 (Django URL, Flask/FastAPI route)
 *  - 데이터베이스 스키마 (Django Model, SQLAlchemy Table, Alembic migration)
 */
public class PythonAnalyzer implements LanguageAnalyzer {

    private static final String DEPENDENCY_TYPE = "dependency";
    private static final String TABLE_PREFIX    = "Table: ";

    // import 문 (import X, import X as Y)
    private static final Pattern IMPORT_PATTERN =
            Pattern.compile("^import\\s+([\\w.]+)", Pattern.MULTILINE);

    // from … import 문
    private static final Pattern FROM_IMPORT_PATTERN =
            Pattern.compile("^from\\s+([\\w.]+)\\s+import", Pattern.MULTILINE);

    // class 정의
    private static final Pattern CLASS_PATTERN =
            Pattern.compile("^class\\s+(\\w+)", Pattern.MULTILINE);

    // Flask route 데코레이터
    private static final Pattern FLASK_ROUTE_PATTERN =
            Pattern.compile(
                "@(?:app|router|blueprint|bp)\\.(?:route|get|post|put|patch|delete)\\(\\s*[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);

    // Django URL patterns
    private static final Pattern DJANGO_URL_PATTERN =
            Pattern.compile(
                "(?:path|re_path|url)\\s*\\(\\s*[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);

    // Django Model 클래스
    private static final Pattern DJANGO_MODEL_PATTERN =
            Pattern.compile(
                "^class\\s+(\\w+)\\s*\\([^)]*(?:models\\.Model|Model)\\s*\\)",
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    // requirements.txt 줄
    private static final Pattern REQUIREMENTS_PATTERN =
            Pattern.compile("^([A-Za-z][\\w.-]+)(?:[>=<!].+)?$", Pattern.MULTILINE);

    // pyproject.toml 의존성
    private static final Pattern PYPROJECT_DEP_PATTERN =
            Pattern.compile("^([A-Za-z][\\w.-]+)\\s*[>=<!]", Pattern.MULTILINE);

    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".py", ".pyw"};
    }

    @Override
    public void analyzeFile(String fileName, String content, AnalysisResult result) {
        String lower = fileName.toLowerCase();

        // requirements.txt / Pipfile / pyproject.toml / setup.cfg 분리 처리
        if (lower.equals("requirements.txt") || lower.startsWith("requirements") && lower.endsWith(".txt")) {
            analyzeRequirements(content, result);
            return;
        }
        if (lower.equals("pipfile")) {
            analyzePipfile(content, result);
            return;
        }
        if (lower.equals("pyproject.toml")) {
            analyzePyprojectToml(content, result);
            return;
        }
        if (lower.equals("setup.py") || lower.equals("setup.cfg")) {
            analyzeSetupFile(content, result);
            return;
        }

        // 일반 .py / .pyw 파일
        analyzeModule(fileName, result);
        result.setClassCount(result.getClassCount() + countClasses(content));
        analyzeDependencies(fileName, content, result);
        detectFrameworks(content, result);
        detectPatterns(fileName, content, result);
        analyzeApis(content, result);
        analyzeDatabases(content, result);
    }

    // ── 모듈/패키지 ──────────────────────────────────────────────────────────

    private void analyzeModule(String fileName, AnalysisResult result) {
        // __init__.py 는 패키지 디렉토리를 의미함
        if (fileName.endsWith("__init__.py")) {
            result.addPackage(extractDirName(fileName));
            return;
        }
        // 모듈명을 패키지로 등록
        result.addPackage(extractModuleName(fileName));
    }

    private int countClasses(String content) {
        Matcher m = CLASS_PATTERN.matcher(content);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    // ── 의존성 분석 ───────────────────────────────────────────────────────────

    private void analyzeDependencies(String fileName, String content, AnalysisResult result) {
        String module = extractModuleName(fileName);

        // import X
        Matcher imp = IMPORT_PATTERN.matcher(content);
        while (imp.find()) {
            String lib = imp.group(1).split("\\.")[0];
            if (!isStdLib(lib)) {
                result.addDependency(new DependencyInfo(module, lib, "import"));
            }
        }

        // from X import Y
        Matcher frm = FROM_IMPORT_PATTERN.matcher(content);
        while (frm.find()) {
            String lib = frm.group(1).split("\\.")[0];
            if (!isStdLib(lib) && !lib.startsWith(".")) {
                result.addDependency(new DependencyInfo(module, lib, "from-import"));
            }
        }
    }

    private void analyzeRequirements(String content, AnalysisResult result) {
        Matcher m = REQUIREMENTS_PATTERN.matcher(content);
        while (m.find()) {
            String pkg = m.group(1).trim();
            if (!pkg.startsWith("#") && !pkg.isEmpty()) {
                result.addDependency(new DependencyInfo("requirements.txt", pkg, DEPENDENCY_TYPE));
            }
        }
        // 프레임워크도 감지
        detectFrameworks(content, result);
    }

    private void analyzePipfile(String content, AnalysisResult result) {
        boolean inPackages = false;
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.equals("[packages]") || trimmed.equals("[dev-packages]")) {
                inPackages = true;
                continue;
            }
            if (trimmed.startsWith("[") && inPackages) {
                inPackages = false;
            }
            if (inPackages && trimmed.contains("=")) {
                String pkg = trimmed.split("=")[0].trim();
                if (!pkg.isEmpty() && !pkg.startsWith("#")) {
                    result.addDependency(new DependencyInfo("Pipfile", pkg, DEPENDENCY_TYPE));
                }
            }
        }
        detectFrameworks(content, result);
    }

    private void analyzePyprojectToml(String content, AnalysisResult result) {
        boolean inDeps = false;
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.equals("[tool.poetry.dependencies]")
                    || trimmed.equals("[project]")
                    || trimmed.equals("dependencies =")) {
                inDeps = true;
                continue;
            }
            if (trimmed.startsWith("[") && !trimmed.contains("dependencies") && inDeps) {
                inDeps = false;
            }
            if (inDeps) {
                Matcher dep = PYPROJECT_DEP_PATTERN.matcher(trimmed);
                if (dep.find()) {
                    result.addDependency(new DependencyInfo("pyproject.toml", dep.group(1), DEPENDENCY_TYPE));
                }
            }
        }
        detectFrameworks(content, result);
    }

    private void analyzeSetupFile(String content, AnalysisResult result) {
        // install_requires = [...] 안 패키지
        Pattern installReq = Pattern.compile(
                "install_requires\\s*=\\s*\\[([^]]+)]", Pattern.DOTALL);
        Matcher m = installReq.matcher(content);
        if (m.find()) {
            String block = m.group(1);
            Pattern pkgPat = Pattern.compile("[\"']([A-Za-z][\\w.-]+)[\"']");
            Matcher pm = pkgPat.matcher(block);
            while (pm.find()) {
                result.addDependency(new DependencyInfo("setup.py", pm.group(1), DEPENDENCY_TYPE));
            }
        }
        detectFrameworks(content, result);
    }

    // ── 프레임워크/라이브러리 감지 ────────────────────────────────────────────

    private void detectFrameworks(String content, AnalysisResult result) {

        // ── 웹 프레임워크 ──────────────────────────────────────────────────
        if (containsAny(content, "django", "from django", "import django"))
            result.addFramework("Django");
        if (containsAny(content, "from flask", "import flask", "Flask(__name__)", "Flask("))
            result.addFramework("Flask");
        if (containsAny(content, "from fastapi", "import fastapi", "FastAPI()")
                || content.contains("fastapi"))
            result.addFramework("FastAPI");
        if (containsAny(content, "import tornado", "tornado.web", "tornado.ioloop"))
            result.addFramework("Tornado");
        if (containsAny(content, "from sanic", "import sanic", "Sanic("))
            result.addFramework("Sanic");
        if (containsAny(content, "from starlette", "import starlette"))
            result.addFramework("Starlette");
        if (containsAny(content, "import aiohttp", "from aiohttp", "aiohttp.web"))
            result.addFramework("aiohttp");
        if (containsAny(content, "import falcon", "from falcon"))
            result.addFramework("Falcon");
        if (containsAny(content, "from bottle", "import bottle", "Bottle()"))
            result.addFramework("Bottle");
        if (containsAny(content, "import cherrypy", "from cherrypy"))
            result.addFramework("CherryPy");

        // ── REST API ──────────────────────────────────────────────────────
        if (containsAny(content, "rest_framework", "serializers.ModelSerializer",
                "APIView", "viewsets.ModelViewSet"))
            result.addFramework("Django REST Framework (DRF)");

        // ── 데이터 검증 ────────────────────────────────────────────────────
        if (containsAny(content, "from pydantic", "import pydantic", "BaseModel"))
            result.addFramework("Pydantic");

        // ── ORM / 데이터베이스 ──────────────────────────────────────────────
        if (containsAny(content, "from sqlalchemy", "import sqlalchemy", "create_engine",
                "declarative_base", "sessionmaker", "Column", "Integer\", \"String"))
            result.addFramework("SQLAlchemy");
        if (containsAny(content, "from peewee", "import peewee", "peewee.Model"))
            result.addFramework("Peewee (ORM)");
        if (containsAny(content, "from tortoise", "import tortoise", "Tortoise.init"))
            result.addFramework("Tortoise ORM");
        if (containsAny(content, "import alembic", "from alembic", "alembic.op"))
            result.addFramework("Alembic (DB Migration)");
        if (containsAny(content, "import pymongo", "from pymongo", "MongoClient"))
            result.addFramework("PyMongo (MongoDB)");
        if (containsAny(content, "import motor", "from motor"))
            result.addFramework("Motor (Async MongoDB)");
        if (containsAny(content, "import redis", "from redis", "Redis("))
            result.addFramework("Redis-py");
        if (containsAny(content, "import psycopg2", "import psycopg", "from psycopg"))
            result.addFramework("psycopg (PostgreSQL)");
        if (containsAny(content, "import aiomysql", "import mysql.connector", "import MySQLdb"))
            result.addFramework("MySQL Connector");
        if (containsAny(content, "import elasticsearch", "from elasticsearch"))
            result.addFramework("Elasticsearch (Python)");

        // ── 비동기/태스크 큐 ──────────────────────────────────────────────
        if (containsAny(content, "import asyncio", "from asyncio", "async def", "await "))
            result.addFramework("asyncio");
        if (containsAny(content, "from celery", "import celery", "Celery(", "@celery.task", "@app.task"))
            result.addFramework("Celery");
        if (containsAny(content, "import dramatiq", "from dramatiq"))
            result.addFramework("Dramatiq");
        if (containsAny(content, "import rq", "from rq", "Queue("))
            result.addFramework("RQ (Redis Queue)");

        // ── 메시지 브로커 ─────────────────────────────────────────────────
        if (containsAny(content, "import kafka", "from kafka", "KafkaProducer", "KafkaConsumer"))
            result.addFramework("Kafka-python");
        if (containsAny(content, "import pika", "from pika", "pika.BlockingConnection"))
            result.addFramework("Pika (RabbitMQ)");

        // ── ML / AI ───────────────────────────────────────────────────────
        if (containsAny(content, "import tensorflow", "from tensorflow", "import tf", "tf.keras"))
            result.addFramework("TensorFlow");
        if (containsAny(content, "import torch", "from torch", "torch.nn", "torch.optim"))
            result.addFramework("PyTorch");
        if (containsAny(content, "from sklearn", "import sklearn", "from scikit_learn"))
            result.addFramework("scikit-learn");
        if (containsAny(content, "import keras", "from keras"))
            result.addFramework("Keras");
        if (containsAny(content, "from transformers", "import transformers",
                "AutoModel", "AutoTokenizer", "pipeline("))
            result.addFramework("Hugging Face Transformers");
        if (containsAny(content, "from langchain", "import langchain", "LLMChain", "ChatOpenAI"))
            result.addFramework("LangChain");
        if (containsAny(content, "import openai", "from openai", "openai.ChatCompletion"))
            result.addFramework("OpenAI SDK");
        if (containsAny(content, "import numpy", "from numpy", "import np"))
            result.addFramework("NumPy");
        if (containsAny(content, "import pandas", "from pandas", "import pd", "DataFrame("))
            result.addFramework("Pandas");
        if (containsAny(content, "import matplotlib", "from matplotlib"))
            result.addFramework("Matplotlib");
        if (containsAny(content, "import scipy", "from scipy"))
            result.addFramework("SciPy");
        if (containsAny(content, "import xgboost", "import lightgbm", "import catboost"))
            result.addFramework("Gradient Boosting (XGBoost/LightGBM/CatBoost)");

        // ── GraphQL ───────────────────────────────────────────────────────
        if (containsAny(content, "import graphene", "from graphene",
                "graphene.ObjectType", "graphene.Schema"))
            result.addFramework("Graphene (GraphQL)");
        if (containsAny(content, "import strawberry", "from strawberry",
                "@strawberry.type", "@strawberry.mutation"))
            result.addFramework("Strawberry (GraphQL)");

        // ── gRPC ──────────────────────────────────────────────────────────
        if (containsAny(content, "import grpc", "from grpc", "grpc.server("))
            result.addFramework("gRPC (Python)");

        // ── 웹 스크래핑 ───────────────────────────────────────────────────
        if (containsAny(content, "import scrapy", "from scrapy", "scrapy.Spider"))
            result.addFramework("Scrapy");
        if (containsAny(content, "from bs4", "import BeautifulSoup", "BeautifulSoup("))
            result.addFramework("BeautifulSoup");
        if (containsAny(content, "import requests", "from requests"))
            result.addFramework("Requests (HTTP)");
        if (containsAny(content, "import httpx", "from httpx"))
            result.addFramework("HTTPX (Async HTTP)");

        // ── CLI ────────────────────────────────────────────────────────────
        if (containsAny(content, "import click", "from click", "@click.command"))
            result.addFramework("Click (CLI)");
        if (containsAny(content, "import typer", "from typer", "typer.Typer()"))
            result.addFramework("Typer (CLI)");
        if (containsAny(content, "import argparse", "ArgumentParser"))
            result.addFramework("argparse");
        if (containsAny(content, "import fire", "from fire", "fire.Fire("))
            result.addFramework("Fire (CLI)");

        // ── 테스팅 ─────────────────────────────────────────────────────────
        if (containsAny(content, "import pytest", "from pytest", "@pytest.mark",
                "@pytest.fixture", "def test_"))
            result.addFramework("pytest");
        if (containsAny(content, "import unittest", "from unittest", "TestCase",
                "self.assert"))
            result.addFramework("unittest");
        if (containsAny(content, "from hypothesis", "import hypothesis", "@given("))
            result.addFramework("Hypothesis (Property Testing)");
        if (containsAny(content, "from unittest.mock", "from mock", "MagicMock", "patch("))
            result.addFramework("unittest.mock");

        // ── 로깅 ───────────────────────────────────────────────────────────
        if (containsAny(content, "import logging", "from logging", "logging.getLogger"))
            result.addFramework("logging (stdlib)");
        if (containsAny(content, "import structlog", "from structlog"))
            result.addFramework("structlog");
        if (containsAny(content, "from loguru", "import loguru"))
            result.addFramework("Loguru");

        // ── 설정/환경 ─────────────────────────────────────────────────────
        if (containsAny(content, "from dotenv", "load_dotenv", "import dotenv"))
            result.addFramework("python-dotenv");
        if (containsAny(content, "from decouple", "import decouple"))
            result.addFramework("python-decouple");
        if (containsAny(content, "import dynaconf", "from dynaconf"))
            result.addFramework("Dynaconf");

        // ── 스케줄링 ──────────────────────────────────────────────────────
        if (containsAny(content, "import apscheduler", "from apscheduler"))
            result.addFramework("APScheduler");

        // ── 데이터 직렬화 ─────────────────────────────────────────────────
        if (containsAny(content, "import marshmallow", "from marshmallow",
                "Schema", "fields."))
            result.addFramework("Marshmallow");
        if (containsAny(content, "import attrs", "from attrs", "@attr.s", "@attrs"))
            result.addFramework("attrs");
        if (containsAny(content, "from dataclasses", "import dataclass", "@dataclass"))
            result.addFramework("dataclasses (stdlib)");

        // ── 캐싱 ──────────────────────────────────────────────────────────
        if (containsAny(content, "import cachetools", "from cachetools",
                "from functools import lru_cache"))
            result.addFramework("Caching (cachetools/lru_cache)");
    }

    // ── 디자인 패턴 감지 ─────────────────────────────────────────────────────

    private void detectPatterns(String fileName, String content, AnalysisResult result) {
        String lower    = fileName.toLowerCase();
        String baseName = extractBaseName(fileName);

        // Singleton
        if (lower.contains("singleton") || content.contains("_instance = None")
                || (content.contains("__new__") && content.contains("cls._instance")))
            result.addDesignPattern("Singleton", baseName);

        // Factory / Factory Method
        if (lower.contains("factory") || content.contains("def create(")
                || content.contains("def make("))
            result.addDesignPattern("Factory", baseName);

        // Abstract Factory
        if (lower.contains("abstract_factory") || lower.contains("abstractfactory"))
            result.addDesignPattern("Abstract Factory", baseName);

        // Builder
        if (lower.contains("builder") || content.contains("def build(")
                || content.contains("def with_"))
            result.addDesignPattern("Builder", baseName);

        // Strategy
        if (lower.contains("strategy") || lower.contains("policy"))
            result.addDesignPattern("Strategy", baseName);

        // Observer / Event
        if (lower.contains("observer") || lower.contains("listener")
                || lower.contains("event") || content.contains("def notify(")
                || content.contains("def subscribe(") || content.contains("def publish("))
            result.addDesignPattern("Observer / Event", baseName);

        // Command
        if (lower.contains("command") || lower.contains("cmd")
                || content.contains("def execute("))
            result.addDesignPattern("Command", baseName);

        // Decorator (Python 네이티브 데코레이터 패턴)
        if (content.contains("@") && (content.contains("functools.wraps")
                || lower.contains("decorator") || lower.contains("middleware")))
            result.addDesignPattern("Decorator", baseName);

        // Repository
        if (lower.contains("repository") || lower.contains("repo"))
            result.addDesignPattern("Repository", baseName);

        // Service Layer
        if (lower.contains("service"))
            result.addDesignPattern("Service Layer", baseName);

        // Controller (MVC)
        if (lower.contains("controller") || lower.contains("view")
                || content.contains("@app.route") || content.contains("APIView"))
            result.addDesignPattern("MVC Controller / View", baseName);

        // Adapter
        if (lower.contains("adapter"))
            result.addDesignPattern("Adapter", baseName);

        // Facade
        if (lower.contains("facade"))
            result.addDesignPattern("Facade", baseName);

        // Proxy
        if (lower.contains("proxy"))
            result.addDesignPattern("Proxy", baseName);

        // Template Method
        if (lower.contains("template") && content.contains("def "))
            result.addDesignPattern("Template Method", baseName);

        // Iterator (Python 네이티브)
        if (content.contains("def __iter__(") || content.contains("def __next__("))
            result.addDesignPattern("Iterator", baseName);

        // Context Manager
        if (content.contains("def __enter__(") || content.contains("def __exit__("))
            result.addDesignPattern("Context Manager", baseName);

        // Mixin
        if (lower.contains("mixin"))
            result.addDesignPattern("Mixin", baseName);

        // Use Case (Clean Architecture)
        if (lower.contains("usecase") || lower.contains("use_case"))
            result.addDesignPattern("Use Case", baseName);

        // DTO / Data Transfer Object
        if (lower.contains("dto") || lower.contains("schema")
                || content.contains("@dataclass"))
            result.addDesignPattern("DTO / Schema", baseName);

        // Mapper
        if (lower.contains("mapper"))
            result.addDesignPattern("Mapper", baseName);
    }

    // ── API 분석 ─────────────────────────────────────────────────────────────

    private void analyzeApis(String content, AnalysisResult result) {
        // Flask / FastAPI 라우트 데코레이터
        Matcher fr = FLASK_ROUTE_PATTERN.matcher(content);
        while (fr.find()) {
            result.addApiEndpoint("ROUTE " + fr.group(1) + " (Flask/FastAPI)");
        }

        // FastAPI 전용 (method 분리)
        Pattern fastapiMethod = Pattern.compile(
                "@(?:\\w+\\.)?(?:router\\.)?(get|post|put|patch|delete)\\(\\s*[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);
        Matcher fm = fastapiMethod.matcher(content);
        while (fm.find()) {
            result.addApiEndpoint(fm.group(1).toUpperCase() + " " + fm.group(2) + " (FastAPI)");
        }

        // Django URL
        Matcher du = DJANGO_URL_PATTERN.matcher(content);
        while (du.find()) {
            result.addApiEndpoint("URL " + du.group(1) + " (Django)");
        }

        // DRF @action
        Pattern drfAction = Pattern.compile(
                "@action\\(.*?methods=\\[([^]]+)].*?url_path=[\"']([^\"']+)[\"']",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher da = drfAction.matcher(content);
        while (da.find()) {
            result.addApiEndpoint(da.group(1).replaceAll("[\"'\\[\\]]", "").trim()
                    + " /" + da.group(2) + " (DRF @action)");
        }

        // aiohttp
        Pattern aio = Pattern.compile(
                "router\\.add_(?:get|post|put|patch|delete)\\(\\s*[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);
        Matcher aioMatcher = aio.matcher(content);
        while (aioMatcher.find()) {
            result.addApiEndpoint("ROUTE " + aioMatcher.group(1) + " (aiohttp)");
        }
    }

    // ── 데이터베이스 분석 ─────────────────────────────────────────────────────

    private void analyzeDatabases(String content, AnalysisResult result) {
        // Django Model 클래스 → 테이블 이름
        Matcher dm = DJANGO_MODEL_PATTERN.matcher(content);
        while (dm.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + dm.group(1) + " (Django Model)");
        }

        // SQLAlchemy __tablename__
        Pattern sa = Pattern.compile(
                "__tablename__\\s*=\\s*[\"']([\\w]+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher sam = sa.matcher(content);
        while (sam.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + sam.group(1) + " (SQLAlchemy)");
        }

        // Alembic op.create_table
        Pattern alembic = Pattern.compile(
                "op\\.create_table\\s*\\(\\s*[\"']([\\w]+)[\"']",
                Pattern.CASE_INSENSITIVE);
        Matcher alm = alembic.matcher(content);
        while (alm.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + alm.group(1) + " (Alembic Migration)");
        }

        // Tortoise ORM Model
        Pattern tortoise = Pattern.compile(
                "^class\\s+(\\w+)\\s*\\([^)]*Model[^)]*\\).*?class Meta.*?table\\s*=\\s*[\"']([\\w]+)[\"']",
                Pattern.DOTALL | Pattern.MULTILINE);
        Matcher tort = tortoise.matcher(content);
        while (tort.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + tort.group(2) + " (Tortoise ORM)");
        }

        // Peewee Model
        Pattern peewee = Pattern.compile(
                "^class\\s+(\\w+)\\s*\\([^)]*(?:Model|peewee\\.Model)[^)]*\\)",
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher pm = peewee.matcher(content);
        while (pm.find()) {
            result.addDatabaseSchema(TABLE_PREFIX + pm.group(1) + " (Peewee Model)");
        }

        // FROM / INTO / UPDATE SQL 구문에서 테이블명 추출
        Pattern sqlTable = Pattern.compile(
                "(?:FROM|INTO|UPDATE)\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher sq = sqlTable.matcher(content);
        while (sq.find()) {
            String table = sq.group(1).toLowerCase();
            if (!isCommonSqlKeyword(table)) {
                result.addDatabaseSchema(TABLE_PREFIX + sq.group(1) + " (Raw SQL)");
            }
        }
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private boolean isStdLib(String libName) {
        return PYTHON_STD_LIBS.contains(libName.toLowerCase());
    }

    private boolean isCommonSqlKeyword(String w) {
        return w.equals("select") || w.equals("where") || w.equals("join")
                || w.equals("set") || w.equals("values") || w.equals("table");
    }

    private boolean containsAny(String content, String... tokens) {
        String lower = content.toLowerCase();
        for (String t : tokens) {
            if (lower.contains(t.toLowerCase())) return true;
        }
        return false;
    }

    /** 파일명에서 모듈명(확장자 제거)을 반환. extractBaseName 과 동일 역할 */
    private String extractModuleName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        String name = parts[parts.length - 1];
        return name.replaceAll("\\.pyw?$", "");
    }

    private String extractDirName(String fileName) {
        String[] parts = fileName.replace("\\", "/").split("/");
        return parts.length > 1 ? parts[parts.length - 2] : "root";
    }

    /** extractModuleName 과 동일 — 패턴 감지 메서드에서 가독성을 위해 사용 */
    private String extractBaseName(String fileName) {
        return extractModuleName(fileName);
    }

    // Python 3 표준 라이브러리 (주요 모듈 목록)
    private static final java.util.Set<String> PYTHON_STD_LIBS = java.util.Set.of(
        "abc", "aifc", "argparse", "array", "ast", "asynchat", "asyncio", "asyncore",
        "atexit", "audioop", "base64", "bdb", "binascii", "binhex", "bisect", "builtins",
        "bz2", "calendar", "cgi", "cgitb", "chunk", "cmath", "cmd", "code", "codecs",
        "codeop", "colorsys", "compileall", "concurrent", "configparser", "contextlib",
        "contextvars", "copy", "copyreg", "cProfile", "csv", "ctypes", "curses", "dataclasses",
        "datetime", "dbm", "decimal", "difflib", "dis", "distutils", "doctest", "email",
        "encodings", "enum", "errno", "faulthandler", "fcntl", "filecmp", "fileinput",
        "fnmatch", "fractions", "ftplib", "functools", "gc", "getopt", "getpass", "gettext",
        "glob", "grp", "gzip", "hashlib", "heapq", "hmac", "html", "http", "idlelib",
        "imaplib", "importlib", "inspect", "io", "ipaddress", "itertools", "json",
        "keyword", "lib2to3", "linecache", "locale", "logging", "lzma", "mailbox",
        "marshal", "math", "mimetypes", "mmap", "modulefinder", "multiprocessing",
        "netrc", "nis", "nntplib", "numbers", "operator", "optparse", "os", "ossaudiodev",
        "pathlib", "pdb", "pickle", "pickletools", "pipes", "pkgutil", "platform",
        "plistlib", "poplib", "posix", "posixpath", "pprint", "profile", "pstats",
        "pty", "pwd", "py_compile", "pyclbr", "pydoc", "queue", "quopri", "random",
        "re", "readline", "reprlib", "resource", "rlcompleter", "runpy", "sched",
        "secrets", "select", "selectors", "shelve", "shlex", "shutil", "signal",
        "site", "smtpd", "smtplib", "sndhdr", "socket", "socketserver", "spwd",
        "sqlite3", "sre_compile", "sre_constants", "sre_parse", "ssl", "stat",
        "statistics", "string", "stringprep", "struct", "subprocess", "sunau",
        "symtable", "sys", "sysconfig", "syslog", "tabnanny", "tarfile", "telnetlib",
        "tempfile", "termios", "test", "textwrap", "threading", "time", "timeit",
        "tkinter", "token", "tokenize", "tomllib", "trace", "traceback", "tracemalloc",
        "tty", "turtle", "turtledemo", "types", "typing", "unicodedata", "unittest",
        "urllib", "uu", "uuid", "venv", "warnings", "wave", "weakref", "webbrowser",
        "winreg", "winsound", "wsgiref", "xdrlib", "xml", "xmlrpc", "zipapp",
        "zipfile", "zipimport", "zlib", "zoneinfo",
        // 자주 쓰는 별칭
        "np", "pd", "tf", "plt"
    );
}
