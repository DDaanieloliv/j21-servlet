package io.ddaaniel.internal.parser.request.response;


import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import io.ddaaniel.internal.httpEntity.entities.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.httpStatus.HttpStatusCode;
import io.ddaaniel.internal.parser.request.header.HeaderHandler;

public class Response {
    private final WritableByteChannel writer;

    public Response(WritableByteChannel writer) {
        this.writer = writer;
    }

    public HeaderHandler DefaultHeaders(int contentLen) {
        var h = new HeaderHandler();
        
        h.Replace("Content-Length", String.valueOf(contentLen));
        h.Replace("Connection", "close");
        h.Replace("Content-Type", "text/plain");

        return h;
    }

    public int WriteStatusLine(HttpStatusCode statuscode) {
		var status = HttpStatus.valueOf(statuscode.value());
        try {
            String statusStr;
            switch (status) {
                case OK: statusStr = "HTTP/1.1 200 OK\r\n"; break;
                case BAD_REQUEST: statusStr = "HTTP/1.1 400 Bad Request\r\n"; break;
				case NOT_FOUND: statusStr = "HTTP/1.1 404 Not Found\r\n"; break;
                case INTERNAL_SERVER_ERROR: statusStr = "HTTP/1.1 500 Internal Server Error\r\n"; break;
                default: throw new IllegalArgumentException("Unrecognized code: " + statuscode.value());
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

    public int WriteHeaders(HttpHeaders h) {
        try {
            var string = new StringBuilder();
            h.forEach((key, value) -> { 
                string.append(String.format("%s:", key)); 
				for (String str : value) {
					string.append(String.format(" %s", str)); 
				}
                string.append("%s \r\n"); 
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
