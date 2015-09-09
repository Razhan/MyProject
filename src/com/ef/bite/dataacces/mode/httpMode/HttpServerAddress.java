package com.ef.bite.dataacces.mode.httpMode;

public class HttpServerAddress extends HttpBaseMessage {
	
	public HttpHostData data;
	
	public static class HttpHostData{
		public String host;
		public String market;
	}
}
