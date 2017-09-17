package com.fnt.handler;

import java.util.List;

import com.fnt.server.Response;

public class POLLHandler {

	private List<String> arguments;

	public POLLHandler(List<String> arguments) {
		this.arguments = arguments;
	}
	
	public boolean verify(){
		
		return true;
	}
	
	public Response execute(){
		return new Response(arguments.get(0) + " POLL");
	}

}
