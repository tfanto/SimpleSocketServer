package com.fnt.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fnt.handler.DNIDHandler;
import com.fnt.handler.POLLHandler;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static Properties settings;
	private static Object lock = new Object();

	/**
	 * accessible from all modules that needs it OBS reads from file since it is
	 * immutable
	 * 
	 * @return Settings.properties
	 * @throws IOException
	 */
	public static Properties getSettings() throws IOException {
		synchronized (lock) {
			return settings;
		}
	}

	/**
	 * Load properties file from classpath
	 * 
	 * @throws IOException
	 */
	private static void loadProperties() throws IOException {

		Properties properties = new Properties();
		InputStream is = Main.class.getClassLoader().getResourceAsStream("settings.properties");
		if (is == null) {
			throw new IOException();
		}
		properties.load(is);
		is.close();
		synchronized (lock) {
			settings = properties;
		}
	}

	/**
	 * Resolve port if not valid terminates immediately
	 * 
	 * @param portStr
	 * @return port
	 */
	private static int resolvePort(String portStr) {
		try {
			return Integer.parseInt(portStr);
		} catch (NumberFormatException nfe) {
			LOGGER.error("Invalid port");
			System.exit(-1);
			throw nfe;
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {

		try {
			loadProperties();
		} catch (IOException e) {
			LOGGER.error("settings.properties not found. cannot start");
			System.exit(-1);
		}

		String portFromPropertyFile = settings.getProperty("server.port");

		String portFromCommandLine = null;
		String dnidFromCommandLine = null;
		if (args.length > 0) {

			int numberOfArguments = args.length;
			for (int i = 0; i < numberOfArguments; i++) {
				String tmpArg = args[i];
				if (tmpArg.indexOf("=") < 0) {
					continue;
				}
				String[] buf = tmpArg.split("=");
				if (buf.length > 2) {
					continue;
				}
				String key = buf[0].trim().toLowerCase();
				String val = buf[1].trim();

				if (key.endsWith("port")) {
					portFromCommandLine = val;
				}
				if (key.endsWith("dnid")) {
					dnidFromCommandLine = val;
					settings.put("dnid", dnidFromCommandLine);
				}

			}
		}

		int port = Integer.MIN_VALUE;
		if (portFromCommandLine != null) {
			port = resolvePort(portFromCommandLine);
		} else {
			port = resolvePort(portFromPropertyFile);
		}

		Server server = null;
		try {
			server = new Server(port);

		} catch (IOException e) {
			LOGGER.error("Could not create server. cannot start");
			System.exit(-1);
		}

		// ADD handlers when needed

		server.registerCommand(new Command("POLL") {
			@Override
			public Response handle(List<String> arguments) {
				if (arguments.size() > 0) {
					POLLHandler pollHandler = new POLLHandler(arguments);
					if (pollHandler.verify()) {
						return pollHandler.execute();
					} else {
						return new Response("POLL request not OK");
					}
				} else
					return new Response("No arguments passed in POLL command");
			}

		});

		server.registerCommand(new Command("DNID") {
			@Override
			public Response handle(List<String> arguments) {
				if (arguments.size() > 0) {
					DNIDHandler dnidHandler = new DNIDHandler(arguments);
					if (dnidHandler.verify()) {
						return dnidHandler.execute();
					} else {
						return new Response("DNID request not OK");
					}
				} else
					return new Response("No arguments passed in DNID command");
			}

		});

		server.registerCommand(new Command("quit") {

			@Override
			public Response handle(List<String> arguments) {
				return new Response("bye", false);
			}

		});

		server.start();
		String msg = "Server running. Listening on port: " + port + " and dnid: [" + settings.getProperty("dnid") + "]";
		LOGGER.info(msg);

	}

}
