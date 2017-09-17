package com.fnt.handler;

import java.util.List;

import com.fnt.server.Response;

public class DNIDHandler {

	private List<String> arguments;

	public DNIDHandler(List<String> arguments) {
		this.arguments = arguments;

	}
	
	public boolean verify(){
		return true;
	}
	
	public Response execute(){
		return new Response(arguments.get(0) + " DNID");
	}


}
