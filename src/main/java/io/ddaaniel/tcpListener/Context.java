package io.ddaaniel.tcpListener;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.BlockingQueue;

/**
 * Context
 */
public class Context {

	private final ByteBuffer buf = ByteBuffer.allocate(1024);
	private final ByteBuffer part = ByteBuffer.allocate(8);
	private final BlockingQueue<String> channel;

	public ReadableByteChannel selectorKey;

	public Context(BlockingQueue<String> chann)
	{
		this.channel = chann;
	}

	public BlockingQueue<String> getChannel() {
		return this.channel;
	} 
	
	public ReadableByteChannel getConnection() {
		return this.selectorKey;
	} 

	public ByteBuffer getBuff() {
		return this.buf;
	} 

	public ByteBuffer getPart() {
		return this.part;
	} 

	public void attachKeyContext(ReadableByteChannel keyContext) {
		this.selectorKey = keyContext;
	}
}
