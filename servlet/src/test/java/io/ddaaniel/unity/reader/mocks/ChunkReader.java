package io.ddaaniel.unity.reader.mocks;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;


/**
 * ChunkReader
 */
public class ChunkReader implements ReadableByteChannel {

	private final byte[] data;
	private final int numBytesPerRead;

	private int pos = 0;
	private boolean open = true;
		
	public ChunkReader (byte[] data, int numBytesPerRead) {
		this.data = data;
		this.numBytesPerRead = numBytesPerRead;
	}

	@Override
	public int read(ByteBuffer buf) {
		if (pos >= data.length) {
			return -1;
		}
		var endIndex = Math.min(pos + numBytesPerRead, data.length);
		var view = Arrays.copyOfRange(data, pos, endIndex);
		var n = Math.min(view.length, buf.remaining());

		buf.put(view, 0, n);
		pos += n;
		return n;
	}

	@Override
	public boolean isOpen() { return open; }

	@Override
	public void close() { open = false; } 
}
