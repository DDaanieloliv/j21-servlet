package io.ddaaniel.listener.internal.codec.writer;

import java.nio.channels.WritableByteChannel;



/**
 * Writer
 */
public interface Writer {

	boolean canWrite();
	
	WritableByteChannel channel();

	void writeToConnection(HttpServletResponse response) throws Exception;
}
