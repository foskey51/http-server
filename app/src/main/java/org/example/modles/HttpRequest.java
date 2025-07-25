package org.example.modles;

import java.util.*;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(String method, String path, String version,
            Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = new HashMap<>(headers);
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    public boolean acceptsGzip() {
        String acceptEncoding = getHeader("accept-encoding");
        return acceptEncoding != null && acceptEncoding.contains("gzip");
    }
}