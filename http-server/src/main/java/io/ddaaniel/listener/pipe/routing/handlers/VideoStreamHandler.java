package io.ddaaniel.listener.pipe.routing.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import io.ddaaniel.internal.parser.request.header.Headers;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.request.response.status.ResponseStatusCode;

/**
 * VideoStreamHandler
 */
public abstract class VideoStreamHandler {

	public static void handleVideoStreaming(Request req, Headers headers, Response res) throws Exception {
		var videoPath = Path
				.of("/home/daniel/DEV_ENV/personal/dev/httpfromtcp/src/main/java/io/ddaaniel/assets/video.mp4");
		try (FileChannel fileChannel = FileChannel.open(videoPath, StandardOpenOption.READ)) {
			long fileSize = fileChannel.size();
			headers.Replace("content-type", "video/mp4");
			headers.Replace("content-length", String.valueOf(fileSize));
			headers.Delete("transfer-encoding");

			res.WriteStatusLine(ResponseStatusCode.STATUS_OK);
			res.WriteHeaders(headers.h);

			var buffer = ByteBuffer.allocate(8192);
			while (fileChannel.read(buffer) > 0) {
				buffer.flip();
				var rawBytes = new byte[buffer.remaining()];
				buffer.get(rawBytes);
				res.WriteBody(rawBytes);
				buffer.clear();
			}
		}
	}
	
}
