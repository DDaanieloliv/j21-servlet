package io.ddaaniel.internal.parser.writer;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.util.HexFormat;

import io.ddaaniel.core.httpEntity.ResponseEntity;
import io.ddaaniel.core.httpEntity.httpHeaders.HttpHeaders;

/**
 * ChunkedStreamServletWriter
 */
public class ChunkedStreamServletWriter extends AbstractHttpServletWriter {

	private static final int BUFFER_SIZE = 8192;

    public ChunkedStreamServletWriter(WritableByteChannel channel) {
        super(channel);
    }

    @Override
    public boolean canWrite(ResponseEntity<?> response) {
        HttpHeaders headers = response.getHeaders();
        boolean hasLength = headers != null && headers.get("Content-Length") != null;
        return response.getBody() instanceof InputStream && !hasLength;
    }

    @Override
    public void writeResponse(ResponseEntity<?> response) throws Exception {
        InputStream bodyStream = (InputStream) response.getBody();
        HttpHeaders headers = response.getHeaders() != null ? response.getHeaders() : new HttpHeaders();

        headers.remove("Content-Length");
        headers.set("Transfer-Encoding", "chunked");
        headers.set("Trailer", "X-Content-SHA256, X-Content-Length");
        if (headers.get("Content-Type") == null) {
            headers.set("Content-Type", "application/octet-stream");
        }

        this.writeStatusLine(response.getStatusCode());
        this.writeHeaders(headers);

        try (bodyStream) {
            var digest = MessageDigest.getInstance("SHA-256");
            int n;
            long totalBytes = 0;
            var data = new byte[BUFFER_SIZE];

            while ((n = bodyStream.read(data)) != -1) {
                if (n == 0) continue;

                digest.update(data, 0, n);
                totalBytes += n;

                var hexSize = Integer.toHexString(n) + "\r\n";
                this.writeRawBytes(hexSize.getBytes());

                var wrap = ByteBuffer.wrap(data, 0, n);
                while (wrap.hasRemaining()) {
                    channel.write(wrap);
                }
                this.writeRawBytes("\r\n".getBytes());
            }

            this.writeRawBytes("0\r\n".getBytes()); 

            var sha256Hex = HexFormat.of().formatHex(digest.digest());
            String trailersBlock = "X-Content-SHA256: " + sha256Hex + "\r\n" +
                                   "X-Content-Length: " + totalBytes + "\r\n" +
                                   "\r\n";
            this.writeRawBytes(trailersBlock.getBytes());
        }
    }
}
