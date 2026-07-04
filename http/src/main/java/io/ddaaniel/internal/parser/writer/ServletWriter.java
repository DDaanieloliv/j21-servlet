package io.ddaaniel.internal.parser.writer;


import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

import io.ddaaniel.internal.support.HttpFun.FunHttp;
import io.ddaaniel.internal.support.httpEntity.ResponseEntity;
import io.ddaaniel.internal.support.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.support.httpStatus.HttpStatus;
import io.ddaaniel.internal.support.httpStatus.HttpStatusCode;



public class ServletWriter {

    private final WritableByteChannel writer;


    public ServletWriter(WritableByteChannel writer) {
        this.writer = writer;
    }

	public void WriteResponse(ResponseEntity<?> response) {
		String body = response.getBody() != null ? response.getBody().toString() : "";
		var headersMap = this.DefaultHeaders(body.getBytes().length);
		this.WriteStatusLine(response.getStatusCode());
		if (response.getHeaders() != null) headersMap.putAll(response.getHeaders());
		this.WriteHeaders(headersMap);
		this.WriteBody(body.getBytes());
	}

	public void WriteErrorResponse() {
		byte[] errorBody = FunHttp.respond404().getBytes();
		this.WriteStatusLine(HttpStatus.NOT_FOUND);
		this.WriteHeaders(this.DefaultHeaders(errorBody.length));
		this.WriteBody(FunHttp.respond404().getBytes());
	}

    public HttpHeaders DefaultHeaders(int contentLen) {
		var header = new HttpHeaders();
		header.set("Content-Length", String.valueOf(contentLen));
		header.set("Connection", "close");
		header.set("Content-Type", "text/plain");
        return header;
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
                string.append("\r\n"); 
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
