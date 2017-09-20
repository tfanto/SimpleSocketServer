package com.fnt.handler;

import java.io.UnsupportedEncodingException;

import com.fnt.server.Response;

public class POLLHandler {

	private String arguments;

	public POLLHandler(String arguments) {
		this.arguments = arguments;
	}

	public boolean verify() {

		return true;
	}

	public Response execute() throws UnsupportedEncodingException {
		return new Response((arguments + " POLL").getBytes("utf-8"));
	}

}
