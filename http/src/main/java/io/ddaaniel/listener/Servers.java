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
import io.ddaaniel.internal.httpEntity.entities.RequestEntity;
import io.ddaaniel.internal.httpEntity.entities.ResponseEntity;
import io.ddaaniel.internal.httpStatus.HttpStatus;
import io.ddaaniel.internal.parser.request.RequestHandler;
import io.ddaaniel.internal.parser.request.header.HeaderHandler;
import io.ddaaniel.internal.parser.request.mapper.Request;
import io.ddaaniel.internal.parser.request.mapper.state.ParsingState;
import io.ddaaniel.internal.parser.request.response.Response;
import io.ddaaniel.internal.parser.util.HttpFun.FunHttp;
import io.ddaaniel.internal.parser.util.serializer.SerializationManager;
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
				String target = req.r.getUrl().toString();
				ResponseEntity<?> response = referenceRouter.dispatch(target);

				if (response != null) {
					String body = response.getBody() != null ? response.getBody().toString() : "";
					var headersMap = res.DefaultHeaders(body.length()).h;
					res.WriteStatusLine(response.getStatusCode());
					if (response.getHeaders() != null)  headersMap.putAll(response.getHeaders());
					res.WriteHeaders(headersMap);
					res.WriteBody(body.getBytes());
				} else {
					res.WriteStatusLine(HttpStatus.NOT_FOUND);
					res.WriteHeaders(res.DefaultHeaders(0).h);
					res.WriteBody(FunHttp.respond404().getBytes());
				}
			} catch (Exception e) {
				System.err.println(" -> Reflection Router Error: " + e.getMessage());
				try {
					res.WriteStatusLine(HttpStatus.INTERNAL_SERVER_ERROR);
					res.WriteHeaders(res.DefaultHeaders(0).h);
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
			var r = new RequestHandler();
			try {
				r = RequestFromReader(conn);
			} catch (Exception err) { 
				response.WriteStatusLine(HttpStatus.BAD_REQUEST);
				response.WriteHeaders(headers.h);
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


	public RequestHandler RequestFromReader(ReadableByteChannel reader) {
		var request = new RequestHandler();
		var header = new HeaderHandler();
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
				Parse(buf, request, header);

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


	private Integer Parse(ByteBuffer buf, RequestHandler requestHandler, HeaderHandler headerHandler) throws Exception {
		var read = 0;
		outer:
		for (;;) {
			switch (requestHandler.State) {
				case STATE_DONE: 
					break outer;
				case STATE_ERROR:
					throw new Exception("Somehow its go wrong");
				case STATE_INIT:
					buf.mark();
					var parsedRequestLine = RequestHandler.parseRequestLine(buf);
					var requestLine = parsedRequestLine._1;
					var totalReadR = parsedRequestLine._2;
					if (totalReadR == 0) {
						buf.reset();
						break outer;
					}
					requestHandler.r  = new RequestEntity<>(requestLine.getMethod(), requestLine.getUrl());
					read += totalReadR;
					requestHandler.State = ParsingState.STATE_HEADERS;
					break;

				case STATE_HEADERS:
					var parsedHeader = headerHandler.Parse(buf);
					var totalReadH = parsedHeader._1;
					var done = parsedHeader._2;
					if (totalReadH == 0) break outer;
					read += totalReadH;
					if (done) requestHandler.State = ParsingState.STATE_BODY;
					break;

				case STATE_BODY:
					var length = RequestHandler.getLength(headerHandler, "content-length" , 0);
					if (length == 0) {
						requestHandler.State = ParsingState.STATE_DONE;
						break;
					}
					var serializer = new SerializationManager();
					var stillMissing = length - serializer.convert(requestHandler.r.getBody(), headerHandler.h.getContentType()).length;
					var available = buf.remaining();
					var remaining = Math.min(stillMissing, available);
					if (remaining > 0) {
						var chunk = new byte[remaining];
						buf.get(chunk);
						r.Body += new String(chunk);
						read += remaining;
					}
					if (length == serializer.convert(requestHandler.r.getBody(), headerHandler.h.getContentType()).length) {
						requestHandler.State = ParsingState.STATE_DONE;
					} else break outer;
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
