package org.example.serialization;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import org.example.generation.*;
import org.example.modles.*;

public class ResponseGenerator {
    private final Path distFolder;
    private final MimeTypeResolver mimeResolver;

    public ResponseGenerator(Path distFolder) {
        this.distFolder = distFolder;
        this.mimeResolver = new MimeTypeResolver();
    }

    public HttpResponse generateResponse(HttpRequest request) {
        try {
            if (!"GET".equals(request.getMethod())) {
                return createErrorResponse(405, "Method Not Allowed");
            }

            String requestPath = request.getPath();
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            // Security check - prevent directory traversal
            if (requestPath.contains("..")) {
                return createErrorResponse(400, "Bad Request");
            }

            Path filePath = distFolder.resolve(requestPath.substring(1));
            
            // Check if file exists and is readable
            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                return createErrorResponse(404, "Not Found");
            }

            // Check if it's a directory
            if (Files.isDirectory(filePath)) {
                Path indexFile = filePath.resolve("index.html");
                if (Files.exists(indexFile) && Files.isReadable(indexFile)) {
                    filePath = indexFile;
                } else {
                    return createDirectoryListing(filePath, requestPath);
                }
            }

            return createFileResponse(filePath, request.acceptsGzip());

        } catch (Exception e) {
            System.err.println("Error generating response: " + e.getMessage());
            return createErrorResponse(500, "Internal Server Error");
        }
    }

    private HttpResponse createFileResponse(Path filePath, boolean acceptsGzip) throws IOException {
        byte[] content = Files.readAllBytes(filePath);
        String mimeType = mimeResolver.getMimeType(filePath);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", mimeType);
        headers.put("Cache-Control", "public, max-age=3600");
        
        // Apply gzip compression for text-based files
        if (acceptsGzip && shouldCompress(mimeType)) {
            content = gzipCompress(content);
            headers.put("Content-Encoding", "gzip");
        }
        
        headers.put("Content-Length", String.valueOf(content.length));
        
        return new HttpResponse(200, "OK", headers, content);
    }

    private boolean shouldCompress(String mimeType) {
        return mimeType.startsWith("text/") || 
               mimeType.equals("application/javascript") ||
               mimeType.equals("application/json") ||
               mimeType.equals("application/xml") ||
               mimeType.contains("css");
    }

    private byte[] gzipCompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
        }
        return baos.toByteArray();
    }

    private HttpResponse createDirectoryListing(Path dirPath, String requestPath) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><title>Directory Listing</title></head><body>");
        html.append("<h1>Directory Listing for ").append(requestPath).append("</h1>");
        html.append("<ul>");
        
        if (!requestPath.equals("/")) {
            html.append("<li><a href=\"../\">../</a></li>");
        }
        
        try (var stream = Files.list(dirPath)) {
            stream.sorted().forEach(path -> {
                String name = path.getFileName().toString();
                String link = requestPath.endsWith("/") ? requestPath + name : requestPath + "/" + name;
                if (Files.isDirectory(path)) {
                    name += "/";
                    link += "/";
                }
                html.append("<li><a href=\"").append(link).append("\">").append(name).append("</a></li>");
            });
        }
        
        html.append("</ul></body></html>");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/html; charset=utf-8");
        headers.put("Content-Length", String.valueOf(html.length()));
        
        return new HttpResponse(200, "OK", headers, html.toString().getBytes("UTF-8"));
    }

    private HttpResponse createErrorResponse(int statusCode, String statusMessage) {
        String html = String.format(
            "<!DOCTYPE html><html><head><title>%d %s</title></head>" +
            "<body><h1>%d %s</h1></body></html>",
            statusCode, statusMessage, statusCode, statusMessage
        );
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/html; charset=utf-8");
        headers.put("Content-Length", String.valueOf(html.length()));
        
        return new HttpResponse(statusCode, statusMessage, headers, html.getBytes());
    }
}
