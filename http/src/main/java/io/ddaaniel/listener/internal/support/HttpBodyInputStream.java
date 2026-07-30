package io.ddaaniel.listener.internal.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import io.ddaaniel.listener.internal.exception.MalformedBodyException;


public class HttpBodyInputStream extends InputStream {
    private final ReadableByteChannel channel;
    private final ByteBuffer buf;
    private final long contentLength;
    private long bytesReadSoFar = 0;

    public HttpBodyInputStream(ReadableByteChannel channel, ByteBuffer buf, long contentLength) {
        this.channel = channel;
        this.buf = buf;
        this.contentLength = contentLength;
    }

    @Override
    public int read() throws IOException {
        if (bytesReadSoFar >= contentLength) {
            return -1;
        }

		if (!buf.hasRemaining()) {
			buf.clear();
			int read = channel.read(buf);
			if (read == -1) {
				if (bytesReadSoFar < contentLength) {
					throw new MalformedBodyException(" -> body shorter than reported content-length ");
				}
				return -1;
			}
			buf.flip();
		}

        int singleByte = buf.get() & 0xFF;
        bytesReadSoFar++;
        return singleByte;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (bytesReadSoFar >= contentLength) {
            return -1;
        }

        long stillMissing = contentLength - bytesReadSoFar;
        int maxToRead = Math.min(len, (int) stillMissing);

		if (!buf.hasRemaining()) {
			buf.clear();
			int read = channel.read(buf);
			if (read == -1) {
				if (bytesReadSoFar < contentLength) {
					throw new MalformedBodyException(" -> body shorter than reported content-length ");
				}
				return -1;
			}
			buf.flip();
		}

        int toCopy = Math.min(maxToRead, buf.remaining());
        buf.get(b, off, toCopy);
        bytesReadSoFar += toCopy;
        return toCopy;
    }
}
