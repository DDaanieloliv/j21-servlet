package io.ddaaniel;

import java.util.concurrent.LinkedBlockingQueue;

import io.ddaaniel.tcpListener.Reader;

public class Cmd {
	public static void main(String[] args) {

		Reader.readConn(new LinkedBlockingQueue<String>());
	}
}
