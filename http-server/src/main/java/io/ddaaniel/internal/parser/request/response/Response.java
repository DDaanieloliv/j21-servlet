package io.ddaaniel.internal.parser.request.response;


import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import io.ddaaniel.internal.parser.request.header.Headers;
import io.ddaaniel.internal.parser.request.header.fieldline.Header;
import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;

public class Response {
    private final WritableByteChannel writer;

    public Response(WritableByteChannel writer) {
        this.writer = writer;
    }

    public Headers DefaultHeaders(int contentLen) {
        var h = new Headers();
        
        h.Replace("Content-Length", String.valueOf(contentLen));
        h.Replace("Connection", "close");
        h.Replace("Content-Type", "text/plain");

        return h;
    }

    public int WriteStatusLine(ResponseStatusCode statuscode) {
        try {
            String statusStr;
            switch (statuscode) {
                case STATUS_OK: statusStr = "HTTP/1.1 200 OK\r\n"; break;
                case STATUS_BAD_REQUEST: statusStr = "HTTP/1.1 400 Bad Request\r\n"; break;
				case STATUS_NOT_FOUND: statusStr = "HTTP/1.1 404 Not Found\r\n"; break;
                case STATUS_INTERNAL_SERVER_ERROR: statusStr = "HTTP/1.1 500 Internal Server Error\r\n"; break;
                default: throw new IllegalArgumentException("Unrecognized code: " + statuscode);
            }

            var statusline = ByteBuffer.wrap(statusStr.getBytes());
            var written = 0; 
            while (statusline.hasRemaining()) {
                written += writer.write(statusline);
            }
            return written;
        } catch (Exception e) { 
            throw new RuntimeException(" -> Failed to write status line ", e); 
        }
    }

    public int WriteHeaders(Header h) {
        try {
            var string = new StringBuilder();
            h.map().forEach((key, value) -> { 
                string.append(String.format("%s: %s\r\n", key, value)); 
            });
            string.append("\r\n");
            
            var buf = ByteBuffer.wrap(string.toString().getBytes());
            var written = 0;
            while (buf.hasRemaining()) {
                written += writer.write(buf);
            }
            return written;
        } catch (Exception e) { 
            throw new RuntimeException(" -> Failure to write headers ", e); 
        }
    }

    public int WriteBody(byte[] p) {
        try {
            var bodyBuffer = ByteBuffer.wrap(p);
            var written = 0;
            while (bodyBuffer.hasRemaining()) {
                written += writer.write(bodyBuffer);
            }
            return written;
        } catch (Exception e) {
            throw new RuntimeException(" -> Failure to write body ", e);
        }
    }
}
