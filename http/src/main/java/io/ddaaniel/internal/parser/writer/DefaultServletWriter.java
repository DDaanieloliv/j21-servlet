package io.ddaaniel.internal.parser.writer;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatus;
import io.ddaaniel.core.httpStatus.HttpStatusCode;
import io.ddaaniel.internal.support.HttpFun.FunHttp;



public class DefaultServletWriter {

    private final WritableByteChannel writer;


    public DefaultServletWriter(WritableByteChannel writer) {
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

	public void writeRegularStream(InputStream bodyStream, HttpHeaders headers) {
		try (bodyStream) {
			this.WriteStatusLine(HttpStatus.OK);

			headers.remove("Transfer-Encoding");
			headers.remove("Trailer");
			this.WriteHeaders(headers);

			int n;
			var buffer = new byte[1024];
			while ((n = bodyStream.read(buffer)) != -1) {
				if (n == 0) continue;

				byte[] rawData = new byte[n];
				System.arraycopy(buffer, 0, rawData, 0, n);
				this.WriteBody(rawData);
			}
		} catch (IOException e) {
			System.err.println(" -> Error writing regular stream: " + e.getMessage());
			WriteErrorResponse();
		}
	}

	public void writeChunkedStream(InputStream bodyStream, HttpHeaders headers) {
		try (bodyStream) {

			this.WriteStatusLine(HttpStatus.OK);
			headers.remove("Content-Length");
			headers.set("Transfer-Encoding", "chunked");
			if (headers.get("Content-Type") == null) {
				headers.set("Content-Type", "text/plain");
			}
			headers.set("Trailer", "X-Content-SHA256, X-Content-Length");
			this.WriteHeaders(headers);

			var fullBody = new ByteArrayOutputStream();
			int n;
			var data = new byte[32];
			while ((n = bodyStream.read(data)) != -1) {
				if (n == 0) continue;
				fullBody.write(data, 0, n);

				var hexSize = Integer.toHexString(n) + "\r\n";
				this.WriteBody(hexSize.getBytes());

				var chunkData = new byte[n];
				System.arraycopy(data, 0, chunkData, 0, n);
				this.WriteBody(chunkData);
				this.WriteBody("\r\n".getBytes());
			}

			this.WriteBody("0\r\n".getBytes()); 

			var finalPayload = fullBody.toByteArray();
			var digest = MessageDigest.getInstance("SHA-256");
			var sha256Hex = HexFormat.of().formatHex(digest.digest(finalPayload));

			String trailersBlock = "X-Content-SHA256: " + sha256Hex + "\r\n" +
				"X-Content-Length: " + finalPayload.length + "\r\n" +
				"\r\n";
			this.WriteBody(trailersBlock.getBytes());

		} catch (IOException | NoSuchAlgorithmException e) {
			System.err.println(" -> Error writing response: " + e.getMessage());
			WriteErrorResponse();
		}
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
