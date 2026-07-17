package io.ddaaniel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CountDownLatch;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import io.ddaaniel.listener.DefaultServletContainer;

public class Main {

	private static final Logger log = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {


		globalLog();
		var port = 42069;
		var keepAliveLatch = new CountDownLatch(1);

		try {
			var s = new DefaultServletContainer().hookUp(port);
			log.info(" -> Server started on port 42069 ");

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				log.info(" -> Signal received! Initiating graceful shutdown...");
				s.Close();

				keepAliveLatch.countDown(); 
			}));
			keepAliveLatch.await();
			log.info(" -> Server gracefully stopped");

		} catch (Exception e) { 
			log.log(Level.SEVERE, " -> Error starting server: " + e.getMessage(), e); 
			System.exit(1);	
		}
	}

	private static void globalLog() {
		Logger rootLogger = Logger.getLogger("");
		for (Handler h : rootLogger.getHandlers()) {
			rootLogger.removeHandler(h);
		}

		Formatter customFormatter = new Formatter() {
			@Override
			public String format(LogRecord record) {
				String logMessage = String.format("[%tF %<tT] [Thread-%d] [%s] [%s#%s]: %s%n",
						record.getMillis(),
						record.getLongThreadID(),
						record.getLevel(),
						record.getLoggerName(),
						record.getSourceMethodName(),
						record.getMessage()
						);

				if (record.getThrown() != null) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					pw.println();
					record.getThrown().printStackTrace(pw);
					logMessage += sw.toString() + "\n";
				}

				return logMessage;
			}
		};

		try {
			ConsoleHandler console = new ConsoleHandler();
			console.setFormatter(customFormatter);
			rootLogger.addHandler(console);

			FileHandler file = new FileHandler("servlet.log", true);
			file.setFormatter(customFormatter);
			rootLogger.addHandler(file);

			rootLogger.setLevel(Level.INFO);

		} catch (Exception e) {
			throw new RuntimeException(" Error when configuring log handlers: " + e.getMessage());
		}
	}

}
