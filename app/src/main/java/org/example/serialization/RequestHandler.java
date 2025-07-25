package org.example.serialization;

import java.io.*;
import org.example.modles.*;
import java.util.*;

public class RequestHandler {
    
    public HttpRequest parseRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.trim().isEmpty()) {
            return null;
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Invalid HTTP request line: " + requestLine);
        }

        String method = parts[0];
        String path = parts[1];
        String version = parts[2];

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String name = line.substring(0, colonIndex).trim().toLowerCase();
                String value = line.substring(colonIndex + 1).trim();
                headers.put(name, value);
            }
        }

        // Read body if present
        StringBuilder body = new StringBuilder();
        String contentLength = headers.get("content-length");
        if (contentLength != null) {
            int length = Integer.parseInt(contentLength);
            char[] buffer = new char[length];
            int bytesRead = reader.read(buffer, 0, length);
            if (bytesRead > 0) {
                body.append(buffer, 0, bytesRead);
            }
        }

        return new HttpRequest(method, path, version, headers, body.toString());
    }
}