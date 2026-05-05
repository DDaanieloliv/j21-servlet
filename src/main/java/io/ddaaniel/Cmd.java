package io.ddaaniel;

import java.util.concurrent.LinkedBlockingQueue;

import io.ddaaniel.tcpListener.IOReader;

public class Cmd {
	public static void main(String[] args) {

		IOReader.readConn(new LinkedBlockingQueue<String>());
	}
}
