package com.adr;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 생성된 ADR 문서를 브라우저에서 확인할 수 있는 경량 웹 서버
 */
public class PreviewServer {

    private final int port;
    private final Path outputDir;

    public PreviewServer(int port, Path outputDir) {
        this.port = port;
        this.outputDir = outputDir;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new PreviewHandler());
        server.setExecutor(null);
        System.out.println("🚀 Preview Server started at http://localhost:" + port);
        System.out.println("   (Press Ctrl+C to stop)");
        server.start();
    }

    private class PreviewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                serveIndex(exchange);
            } else {
                serveFile(exchange, path.substring(1));
            }
        }

        private void serveIndex(HttpExchange exchange) throws IOException {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head>");
            html.append("<meta charset='UTF-8'>");
            html.append("<title>ADR Generator Preview</title>");
            html.append("<style>");
            html.append(
                    "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; max-width: 800px; margin: 40px auto; padding: 0 20px; color: #333; }");
            html.append("h1 { border-bottom: 2px solid #eee; padding-bottom: 10px; }");
            html.append("ul { list-style: none; padding: 0; }");
            html.append(
                    "li { margin: 10px 0; padding: 10px; background: #f9f9f9; border-radius: 5px; transition: background 0.2s; }");
            html.append("li:hover { background: #f0f0f0; }");
            html.append("a { text-decoration: none; color: #0366d6; font-weight: bold; }");
            html.append(
                    "footer { margin-top: 40px; font-size: 0.8em; color: #888; border-top: 1px solid #eee; padding-top: 10px; }");
            html.append("</style></head><body>");
            html.append("<h1>✨ Generated ADR Documents</h1>");
            html.append("<ul>");

            if (Files.exists(outputDir)) {
                Files.list(outputDir)
                        .filter(p -> p.toString().endsWith(".md"))
                        .sorted((p1, p2) -> p2.getFileName().toString().compareTo(p1.getFileName().toString()))
                        .forEach(p -> {
                            String name = p.getFileName().toString();
                            html.append("<li><a href='/").append(name).append("'>").append(name).append("</a></li>");
                        });
            } else {
                html.append("<li>No documents found in ").append(outputDir).append("</li>");
            }

            html.append("</ul>");
            html.append("<footer>Built with ADR Generator</footer>");
            html.append("</body></html>");

            byte[] response = html.toString().getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }

        private void serveFile(HttpExchange exchange, String fileName) throws IOException {
            Path filePath = outputDir.resolve(fileName).normalize();

            // 보안: outputDir 외부 파일 접근 방지
            if (!filePath.startsWith(outputDir)) {
                sendError(exchange, 403, "Forbidden");
                return;
            }

            if (!Files.exists(filePath)) {
                sendError(exchange, 404, "Not Found");
                return;
            }

            byte[] content = Files.readAllBytes(filePath);

            if (fileName.endsWith(".md")) {
                // Markdown을 간단히 HTML로 감싸서 보기 편하게 함 (실제 렌더링은 클라이언트사이드 라이브러리 없이 단순 텍스트로)
                String body = new String(content, "UTF-8");
                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html><html><head>");
                html.append("<meta charset='UTF-8'>");
                html.append("<title>").append(fileName).append("</title>");
                html.append("<script src='https://cdn.jsdelivr.net/npm/marked/marked.min.js'></script>");
                html.append("<script src='https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js'></script>");
                html.append("<style>");
                html.append(
                        "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; max-width: 900px; margin: 40px auto; padding: 0 40px; color: #24292e; background: #fff; }");
                html.append("pre { background: #f6f8fa; padding: 16px; border-radius: 6px; overflow: auto; }");
                html.append(
                        "code { font-family: SFMono-Regular, Consolas, 'Liberation Mono', Menlo, monospace; font-size: 85%; }");
                html.append(".mermaid { background: white; border: 1px solid #eee; padding: 10px; margin: 20px 0; }");
                html.append("</style></head><body>");
                html.append("<div id='content'></div>");
                html.append("<script>");
                html.append("document.getElementById('content').innerHTML = marked.parse(`")
                        .append(body.replace("`", "\\`").replace("${", "\\${")).append("`);");
                html.append("mermaid.initialize({ startOnLoad: true });");
                html.append("</script>");
                html.append("</body></html>");

                content = html.toString().getBytes("UTF-8");
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            } else {
                exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            }

            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }

        private void sendError(HttpExchange exchange, int code, String message) throws IOException {
            byte[] response = message.getBytes();
            exchange.sendResponseHeaders(code, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }
}
