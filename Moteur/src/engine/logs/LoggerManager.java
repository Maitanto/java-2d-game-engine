package engine.logs;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * @author Pr. Olivier Gruber
 */
public class LoggerManager {
	private static final PrintStream PS;
	private static Map<String, Logger> loggers;
	static {
		PS = System.out;
		loggers = new Hashtable<String, Logger>();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			PS.println("Shutting down logging...");
			for (Logger logger : loggers.values()) {
				for (Handler h : logger.getHandlers()) {
					try {
						h.close();
					} catch (Exception e) {
						// nothing to do here.
					}
				}
			}
			PS.println("--> handlers are flushed and closed.");
			PS.println("REMINDER : All logs are in the logs files, they are not in the console anymore.");
		}));
	}

	/**
	 * Get a logger by name
	 * 
	 * @param s like a class name for example
	 * @return a logger
	 */
	public static Logger getLogger(String s) {
		Logger l = loggers.get(s);
		if (l == null) {
			l = Logger.getLogger(s);
			loggers.put(s, l);
		}
		return l;
	}
}