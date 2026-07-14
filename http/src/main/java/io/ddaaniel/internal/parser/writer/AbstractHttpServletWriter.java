package io.ddaaniel.internal.parser.writer;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.core.httpStatus.HttpStatusCode;

/**
 * AbstractHttpServletWriter
 */
public abstract class AbstractHttpServletWriter implements HttpServletWriter {

	protected final WritableByteChannel channel;

	protected AbstractHttpServletWriter(WritableByteChannel channel) {
		this.channel = channel;
	}

	protected void writeStatusLine(HttpStatusCode statusCode) {
        try {
			String statusStr = String.format("HTTP/1.1 s% s%", statusCode.value(), statusCode.toString());

            var statusline = ByteBuffer.wrap(statusStr.getBytes());
            while (statusline.hasRemaining()) {
               channel.write(statusline);
            }
        } catch (Exception e) { 
            throw new RuntimeException(" -> Failed to write status line ", e); 
        }
	}

	protected void writeHeaders(HttpHeaders headers) {
        try {
            var string = new StringBuilder();
            headers.forEach((key, value) -> { 
                string.append(String.format("%s:", key)); 
				for (String str : value) {
					string.append(String.format(" %s", str)); 
				}
                string.append("\r\n"); 
            });
            string.append("\r\n");
            
            var buf = ByteBuffer.wrap(string.toString().getBytes());
            while (buf.hasRemaining()) {
                channel.write(buf);
            }
        } catch (Exception e) { 
            throw new RuntimeException(" -> Failure to write headers ", e); 
        }
	}

	protected void writeRawBytes(byte[] bytes) {
		try {
            var buf = ByteBuffer.wrap(bytes);
            while (buf.hasRemaining()) {
                channel.write(buf);
            }
        } catch (Exception e) {
            throw new RuntimeException(" -> Failure to write raw bytes", e);
        }
	}
}
