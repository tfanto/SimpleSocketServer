package com.fnt.handler;

import java.io.UnsupportedEncodingException;

import com.fnt.server.Response;

public class DNIDHandler {

	private String arguments;

	public DNIDHandler(String arguments) {
		this.arguments = arguments;

	}

	public boolean verify() {
		return true;
	}

	public Response execute() throws UnsupportedEncodingException {
		return new Response((arguments + " DNID").getBytes("utf-8"));
	}

}
