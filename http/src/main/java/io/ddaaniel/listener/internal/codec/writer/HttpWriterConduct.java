package io.ddaaniel.listener.internal.codec.writer;

import java.nio.channels.WritableByteChannel;


/**
 * HttpWriterConduct
 */
public interface HttpWriterConduct {

	boolean matches(DefaultHttpServletResponse response);

	void write(WritableByteChannel channel, DefaultHttpServletResponse response) throws Exception;
}
