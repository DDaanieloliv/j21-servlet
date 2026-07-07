package io.ddaaniel.listener.pipe.oldrouting.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import io.ddaaniel.internal.parser.reader.HttpServletRequest;
import io.ddaaniel.internal.parser.writer.ServletWriter;
import io.ddaaniel.internal.support.httpEntity.httpHeaders.HttpHeaders;
import io.ddaaniel.internal.support.httpStatus.HttpStatus;

/**
 * VideoStreamHandler
 */
public abstract class VideoStreamHandler {

	public static void handleVideoStreaming(HttpServletRequest message, HttpHeaders headers, ServletWriter writer) throws Exception {
		var videoPath = Path
				.of("/home/daniel/DEV_ENV/personal/dev/httpfromtcp/src/main/java/io/ddaaniel/assets/video.mp4");
		try (FileChannel fileChannel = FileChannel.open(videoPath, StandardOpenOption.READ)) {
			long fileSize = fileChannel.size();
			headers.set("content-type", "video/mp4");
			headers.set("content-length", String.valueOf(fileSize));
			headers.set("transfer-encoding", "chunked");

			writer.WriteStatusLine(HttpStatus.OK);
			writer.WriteHeaders(headers);

			var buffer = ByteBuffer.allocate(8192);
			while (fileChannel.read(buffer) > 0) {
				buffer.flip();
				var rawBytes = new byte[buffer.remaining()];
				buffer.get(rawBytes);
				writer.WriteBody(rawBytes);
				buffer.clear();
			}
		}
	}
	
}
