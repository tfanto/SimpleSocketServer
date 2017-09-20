package com.fnt.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Channel implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Channel.class);

	private Socket socket = null;
	private Server server = null;
	private int ALLOWED_TRIES;

	public Server getServer() {
		return server;
	}

	public Socket getSocket() {
		return socket;
	}

	public Channel(final Socket socket, final Server server) throws IOException {
		this.socket = socket;
		this.server = server;
		try {
			String allowed_logontriesStr = Main.getSettings().getProperty("server.allowedlogontries");
			ALLOWED_TRIES = Integer.parseInt(allowed_logontriesStr);
		} catch (NumberFormatException e) {
			ALLOWED_TRIES = 3;
		}
	}

	private void write(OutputStream out, Response response) throws UnsupportedEncodingException, IOException {
		out.write(response.toString().getBytes("UTF-8"));
	}

	private void write(OutputStream out, String msg) throws UnsupportedEncodingException, IOException {
		out.write(msg.getBytes("UTF-8"));
	}

	private String readLine(BufferedReader in) throws IOException {

		String line = null;
		while ((line = in.readLine()) != null) {
			if (line.length() > 0) {
				return line;
			}
		}
		return "";
	}

	// TODO make this correct later
	private boolean autenticate(String user, String pwd) throws IOException {
		if ((user == null) || (user.length() < 1))
			return false;
		if ((pwd == null) || (pwd.length() < 1))
			return false;

		String allowed_uid = Main.getSettings().getProperty("server.uid");
		String allowed_pwd = Main.getSettings().getProperty("server.pwd");
		if ((allowed_uid == null) || (allowed_uid.length() < 1))
			return false;
		if ((allowed_uid == null) || (allowed_uid.length() < 1))
			return false;
		return user.trim().equals(allowed_uid) && pwd.trim().equals(allowed_pwd);
	}

	private String getCommand(StringTokenizer st) {
		String command = st.nextToken();
		return command;
	}

	private String getArguments(StringTokenizer st) {

		String ret = "";

		int n = st.countTokens();
		if (n >= 1) {
			while (st.hasMoreTokens()) {
				String tok = st.nextToken().trim();
				ret += tok;
				ret += " ";
			}
			ret = ret.trim();
		}
		return ret;
	}

	public void run() {

		OutputStream out = null;
		BufferedReader in = null;
		String line = null;

		try {
			out = socket.getOutputStream();

			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

			int logonTries = 0;
			boolean isAuthenticated = false;
			while (!isAuthenticated && logonTries < ALLOWED_TRIES) {
				logonTries++;
				write(out, "name:");
				String user = readLine(in);
				write(out, "word:");
				String pwd = readLine(in);
				isAuthenticated = autenticate(user, pwd);
			}
			if (!isAuthenticated) {
				return;
			}
			write(out, ">");

			Response response = new Response();
			while ((line = in.readLine()) != null) {
				if (line == null || line.length() == 0) {
					response.set(Command.ERROR.getBytes("uf-8"));
				} else {
					StringTokenizer st = new StringTokenizer(line);
					String command = getCommand(st);
					command = command.toLowerCase();
					if (this.getServer().getCommands().containsKey(command)) {
						String arguments = getArguments(st);
						Command commandHandler = this.getServer().getCommands().get(command);
						response = commandHandler.handle(arguments);
					} else {
						response.set(Command.UNKNOWN.getBytes("utf-8"));
					}
				}
				write(out, response);
				write(out, Command.END);
				write(out, ">");
				out.flush();
				if (response.keepalive() == false) {
					break;
				}
			}

		} catch (IOException e) {
			LOGGER.error(e.toString(), e);
			return;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				LOGGER.info(e.toString(), e);
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				LOGGER.info(e.toString(), e);
			}
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				LOGGER.info(e.toString(), e);
			}
		}
	}
}
