package org.example.generation;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MimeTypeResolver {
    private final Map<String, String> mimeTypes;

    public MimeTypeResolver() {
        mimeTypes = new HashMap<>();
        initializeMimeTypes();
    }

    private void initializeMimeTypes() {
        // Text files
        mimeTypes.put("html", "text/html");
        mimeTypes.put("htm", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "application/javascript");
        mimeTypes.put("json", "application/json");
        mimeTypes.put("xml", "application/xml");
        mimeTypes.put("txt", "text/plain");
        
        // Images
        mimeTypes.put("png", "image/png");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("svg", "image/svg+xml");
        mimeTypes.put("ico", "image/x-icon");
        
        // Other common types
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("zip", "application/zip");
        mimeTypes.put("gz", "application/gzip");
        mimeTypes.put("tar", "application/x-tar");
    }

    public String getMimeType(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            String extension = fileName.substring(lastDot + 1).toLowerCase();
            return mimeTypes.getOrDefault(extension, "application/octet-stream");
        }
        
        return "application/octet-stream";
    }
}

