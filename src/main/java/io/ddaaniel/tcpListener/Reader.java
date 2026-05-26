package io.ddaaniel.tcpListener;


import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;

import io.ddaaniel.internal.parser.request.Requests;
import io.ddaaniel.internal.parser.request.message.Request;

/**
 * Reader
 */
public abstract class Reader {

	public static void handleConnection() {
		try {

			var server = ServerSocketChannel.open();
			server.bind(new InetSocketAddress(42069));
			var request = new Request();
			var headers = new HashMap<String, String>();

			while (true) {
				var socket = server.accept();
				request = new Requests().RequestFromReader(socket);
				headers = request.Headers.h.map();
				break;
			}

			System.out.println("Request Line:");
			System.out.println(" - Method: " + request.RequestLine.Method); 
			System.out.println(" - Target: " + request.RequestLine.RequestTarget);
			System.out.println(" - Version: " + request.RequestLine.HttpVersion);
			System.out.println("Headers:");
			headers.forEach((key, value) -> System.out.println(" - " + key + ": " + value));
			System.out.println("Body:");
			System.out.println(" - " + request.Body + "\n");

		} catch (Exception e) { e.printStackTrace(); }
	}
}
