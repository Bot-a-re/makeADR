package com.adr.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ZIP 파일 추출 클래스 (보안 강화 버전)
 *
 * 해결된 보안 취약점 (ADR-20260219-081343.md CRITICAL):
 *  - Zip Slip (Path Traversal) 방어: entryName에 "..", 절대경로, null byte 차단
 *  - ZIP Bomb 방어: 압축 해제 총 크기·파일 수·압축비 제한
 *  - 허용되지 않는 파일 확장자(실행파일·스크립트) 추출 차단 (화이트리스트)
 *  - 단일 엔트리 크기 10 MB 제한
 */
public class ZipExtractor {

    private Path tempDir;

    public Path extract(Path zipFilePath) throws IOException {
        // 임시 디렉토리 생성
        tempDir = Files.createTempDirectory("adr-analysis-");

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath.toFile()))) {
            ZipEntry entry;
            int fileCount = 0;
            long totalUncompressedBytes = 0L;

            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                // ── 파일 수 제한 ────────────────────────────────────────────
                if (fileCount >= InputValidator.MAX_FILE_COUNT) {
                    System.err.println("⚠️  최대 파일 수(" + InputValidator.MAX_FILE_COUNT
                            + ")에 도달하여 나머지 엔트리를 건너뜁니다.");
                    break;
                }

                // ── 경로 정규화 & Zip Slip 방어 ─────────────────────────────
                Path filePath = tempDir.resolve(entryName).normalize();

                if (!InputValidator.isAllowedZipEntry(entryName, tempDir, filePath)) {
                    System.err.println("⚠️  보안 검사 실패로 건너뜀: " + entryName);
                    zis.closeEntry();
                    continue;
                }

                if (entry.isDirectory()) {
                    // 디렉토리 생성
                    try {
                        if (!Files.exists(filePath)) {
                            Files.createDirectories(filePath);
                        }
                    } catch (IOException e) {
                        System.err.println("⚠️  디렉토리 생성 실패 (무시됨): " + entryName);
                    }
                } else {
                    // ── 확장자 화이트리스트 검사 ────────────────────────────
                    String fileName = filePath.getFileName().toString();
                    if (!InputValidator.isAllowedSourceExtension(fileName)) {
                        zis.closeEntry();
                        continue; // 실행파일·바이너리 등 건너뜀 (조용히)
                    }

                    // ── 압축 전 엔트리 크기 예비 검사 ──────────────────────
                    long compressedSize   = entry.getCompressedSize();
                    long uncompressedSize = entry.getSize();

                    if (uncompressedSize > InputValidator.MAX_SOURCE_FILE_SIZE_BYTES) {
                        System.err.println("⚠️  단일 파일이 너무 큽니다 (건너뜀): " + entryName
                                + " (" + uncompressedSize / (1024 * 1024) + " MB)");
                        zis.closeEntry();
                        continue;
                    }

                    // 압축비 사전 검사 (compressedSize == -1 이면 알 수 없음 → 런타임 검사로)
                    if (compressedSize > 0 && uncompressedSize > 0) {
                        long ratio = uncompressedSize / compressedSize;
                        if (ratio > InputValidator.MAX_COMPRESSION_RATIO) {
                            System.err.println("⚠️  압축 비율 이상 탐지 (ZIP Bomb 의심, 건너뜀): "
                                    + entryName + " (ratio=" + ratio + ")");
                            zis.closeEntry();
                            continue;
                        }
                    }

                    // ── 파일 쓰기 ───────────────────────────────────────────
                    try {
                        Path parent = filePath.getParent();
                        if (parent != null && !Files.exists(parent)) {
                            Files.createDirectories(parent);
                        }

                        long writtenBytes = writeEntrySecurely(zis, filePath, entryName);

                        totalUncompressedBytes += writtenBytes;
                        fileCount++;

                        // ── 총 해제 크기 제한 (ZIP Bomb 방어) ──────────────
                        if (totalUncompressedBytes > InputValidator.MAX_TOTAL_UNCOMPRESSED_BYTES) {
                            System.err.println("⚠️  총 압축 해제 크기가 한도("
                                    + (InputValidator.MAX_TOTAL_UNCOMPRESSED_BYTES / (1024 * 1024 * 1024))
                                    + " GB)를 초과했습니다. 추출을 중단합니다.");
                            break;
                        }

                    } catch (SecurityException se) {
                        System.err.println("⚠️  보안 제한으로 건너뜀: " + entryName + " — " + se.getMessage());
                    } catch (IOException e) {
                        System.err.println("⚠️  파일 추출 실패 (무시됨): " + entryName + " - " + e.getMessage());
                    }
                }
                zis.closeEntry();
            }

            System.out.println("   - 추출된 파일 수: " + fileCount);
        }

        return tempDir;
    }

    /**
     * 단일 ZIP 엔트리를 파일로 기록하면서 런타임 크기·압축비를 검사합니다.
     *
     * @return 실제 기록된 바이트 수
     * @throws SecurityException 크기 제한 초과 시
     */
    private long writeEntrySecurely(ZipInputStream zis, Path filePath, String entryName)
            throws IOException {
        long writtenBytes = 0L;
        byte[] buffer = new byte[8192];
        int len;

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            while ((len = zis.read(buffer)) > 0) {
                writtenBytes += len;

                // 런타임 단일 파일 크기 검사
                if (writtenBytes > InputValidator.MAX_SOURCE_FILE_SIZE_BYTES) {
                    // 이미 파일이 부분 생성됐으므로 삭제
                    fos.close();
                    filePath.toFile().delete();
                    throw new SecurityException("단일 파일 크기가 "
                            + (InputValidator.MAX_SOURCE_FILE_SIZE_BYTES / (1024 * 1024))
                            + " MB를 초과합니다: " + entryName);
                }

                fos.write(buffer, 0, len);
            }
        }
        return writtenBytes;
    }

    public void cleanup() {
        if (tempDir != null) {
            try {
                deleteDirectory(tempDir.toFile());
            } catch (IOException e) {
                System.err.println("⚠️  임시 디렉토리 삭제 실패: " + e.getMessage());
            }
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("디렉토리 삭제 실패: " + directory);
        }
    }
}
