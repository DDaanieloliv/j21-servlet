package io.ddaaniel.listener;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.ddaaniel.internal.exception.MalformedBodyException;
import io.ddaaniel.internal.exception.URITooLongException;
import io.ddaaniel.internal.httpEntity.entities.ResponseEntity;
import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.request.Request;
import io.ddaaniel.internal.parser.request.state.ParsingState;
import io.ddaaniel.internal.parser.response.Response;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;
import io.ddaaniel.listener.mapper.Server;
import io.ddaaniel.listener.pipe.Handler;
import io.ddaaniel.listener.pipe.routing.RefRouter;


/**
 * Servers
 */
public class Servers {

	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	private ServerSocketChannel listener;
	public Server s = new Server();

	public Servers handleConnection(int port) throws Exception {
		var referenceRouter = new RefRouter();
		var s = new Servers().Serve(port, (req, res) -> {
			try {
				String target = req.uriWrap;
				ResponseEntity<?> response = referenceRouter.dispatch(target);

				if (response != null) {
					String body = response.getBody() != null ? response.getBody().toString() : "";
					var headersMap = res.DefaultHeaders(body.getBytes().length);
					res.WriteStatusLine(response.getStatusCode());
					if (response.getHeaders() != null)  headersMap.putAll(response.getHeaders());
					res.WriteHeaders(headersMap);
					res.WriteBody(body.getBytes());
				} else {
					res.WriteStatusLine(HttpStatus.NOT_FOUND);
					res.WriteHeaders(res.DefaultHeaders(0));
					res.WriteBody(FunHttp.respond404().getBytes());
				}
			} catch (Exception e) {
				System.err.println(" -> Reflection Router Error: " + e.getMessage());
				try {
					res.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
					res.WriteHeaders(res.DefaultHeaders(0));
				} catch (Exception ignored) {}
			}

			return;
		});
		return s;
	}


	public void runConnection(Server server, SocketChannel conn) {
		try (conn) {
			var response = new Response(conn);
			var headers = response.DefaultHeaders(0);
			var r = new Request();
			try {
				r = RequestFromReader(conn);
			} catch (Exception err) { 
				response.WriteStatusLine(HttpStatus.BAD_REQUEST);
				response.WriteHeaders(headers);
				return;
			}
			server.handler.handle(r, response);
		} catch (Exception e) {
			if (!server.closed) { 
				System.err.println(" -> Error in connection: " + e.getMessage()); 
			}
		}
	}

	public void runServer(ServerSocketChannel listener) {
		try {
			while (listener.isOpen() && !s.closed) {
				var socketChannel = listener.accept();
				if (s.closed) {
					if (socketChannel != null) socketChannel.close();
					return;
				}
				executor.submit(() -> { runConnection(s, socketChannel); });
			}
		} catch (Exception e) { if (!s.closed) throw new RuntimeException(e); }
	}


	public Request RequestFromReader(ReadableByteChannel reader) {
		var request = new Request();
		var buf = ByteBuffer.allocate(1024);
		var fliped = false;

		try {
			while (request.State == ParsingState.STATE_DONE || request.State == ParsingState.STATE_ERROR) {
				var read = reader.read(buf);
				if (read == -1) {
					if (request.State != ParsingState.STATE_DONE) {
						request.State = ParsingState.STATE_ERROR; 
						throw new MalformedBodyException(" -> body shorter than reported content-length ");
					}
					break;
				}
				buf.flip();
				fliped = true;
				Parse(buf, request);

				if (buf.remaining() == buf.capacity()) {
					request.State = ParsingState.STATE_ERROR;
					throw new URITooLongException(" -> uri too long, error 414 -- bytes-read: " + read);
				}
				buf.compact();
				fliped = false;
			}
		} catch (Exception  exception) { 
			if (exception instanceof RuntimeException) throw (RuntimeException) exception;
			throw new RuntimeException(" -> Failure to parse the request: ", exception);
		}

		if (!fliped) buf.flip();
		return request;
	}


	private Integer Parse(ByteBuffer buf, Request req) throws Exception {
		var read = 0;
		outer:
		for (;;) {
			switch (req.State) {
				case STATE_DONE: 
					break outer;
				case STATE_ERROR:
					throw new Exception("Somehow its go wrong");
				case STATE_INIT:
					buf.mark();
					var parsed = req.parseRequestLine(buf, req);
					if (parsed == 0) {
						buf.reset();
						break outer;
					}
					req.State = ParsingState.STATE_HEADERS;
					read += parsed;
					break;

				case STATE_HEADERS:
					var parsedHeader = req.Parse(buf);
					var totalReadH = parsedHeader._1;
					var done = parsedHeader._2;
					if (totalReadH == 0) break outer;
					read += totalReadH;
					if (done) req.State = ParsingState.STATE_BODY;
					break;

				case STATE_BODY:
					var length = (req.getContentLength() != -1) ? req.getContentLength() : 0;
					if (length == 0) {
						req.State = ParsingState.STATE_DONE;
						break;
					}
					long alreadyRead = req.readSoFar;
					long stillMissing = length - alreadyRead;
					int available = buf.remaining();
					long remaining = Math.min(stillMissing, available);
					if (remaining > 0) {
						var chunk = new byte[(int) remaining];
						buf.get(chunk);
						req.readSoFar += remaining;
						read += remaining;
					}
					if (req.readSoFar == length) {
						req.State = ParsingState.STATE_DONE;
					}
					else break outer;
					break;
				default: 
					throw new Exception("Somehow its go wrong");
			}
		}
		return read;
	}

	public Servers Serve(int port, Handler handler) throws Exception {
		listener = ServerSocketChannel.open();
		listener.bind(new InetSocketAddress(port));
		s.closed = false;
		s.handler = handler;
		executor.submit(() -> { runServer(listener); } );
		return this;
	}

	public void Close() {
		try {
			s.closed = true;
			if (listener != null && listener.isOpen()) listener.close();
			executor.shutdown();
		} catch (Exception e) { 
			System.err.println(" -> Error when closing server: " + e.getMessage());
		}
	}
}
