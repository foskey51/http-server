package org.example.modles;

import java.io.*;
import java.util.*;

public class HttpResponse {
    private final int statusCode;
    private final String statusMessage;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpResponse(int statusCode, String statusMessage, 
                       Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers = new HashMap<>(headers);
        this.body = body;
    }

    public void writeTo(OutputStream out) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        
        // Status line
        writer.printf("HTTP/1.1 %d %s\r\n", statusCode, statusMessage);
        
        // Headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            writer.printf("%s: %s\r\n", header.getKey(), header.getValue());
        }
        
        writer.print("\r\n");
        writer.flush();
        
        // Body
        if (body != null && body.length > 0) {
            out.write(body);
        }
        out.flush();
    }
}