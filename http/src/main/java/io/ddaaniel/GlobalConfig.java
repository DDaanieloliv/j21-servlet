package io.ddaaniel;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * GlobalConfig
 */
public class GlobalConfig {

	private static final Properties PROPERTIES = new Properties();
	private static int port = 42069;

	static {
		try (InputStream input = GlobalConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
			if (input != null) {
				PROPERTIES.load(input);
				port = Integer.parseInt(PROPERTIES.getProperty("server.port", "42069"));
			}

			String levelPropertie = PROPERTIES.getProperty("log.level", "INFO");
			Level configuredLevel = Level.parse(levelPropertie);

			Logger rootLogger = Logger.getLogger("");
			rootLogger.setLevel(configuredLevel);
			for (Handler h : rootLogger.getHandlers()) {
				rootLogger.removeHandler(h);
			}


			Formatter formatter = new DefaultLoggingFormatter();
			ConsoleHandler console = new ConsoleHandler();
			console.setFormatter(formatter);
			console.setLevel(configuredLevel);
			rootLogger.addHandler(console);

			String logFile = PROPERTIES.getProperty("log.file", "servlet.log");
			FileHandler file = new FileHandler(logFile, true);
			file.setFormatter(formatter);
			file.setLevel(configuredLevel);
			rootLogger.addHandler(file);

		} catch (Exception e) {
			throw new RuntimeException("Fatal error initializing application configurations", e);
		}
	}

	public static void initialize() {
		Logger.getLogger(GlobalConfig.class.getName()).info(" -> Application configurations loaded successfully.");
	}

	public static String getProperty(String key) {
		return PROPERTIES.getProperty(key);
	}

	public static int getPort() {
		return port;
	}
}


/**
 * DefaultLoggingFormatter
 */
class DefaultLoggingFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		String logMessage = String.format("[%tF %<tT] [Thread-%d] [%s] [%s#%s]: %s%n",
				record.getMillis(),
				record.getLongThreadID(),
				record.getLevel(),
				record.getLoggerName(),
				record.getSourceMethodName(),
				formatMessage(record)
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
}
