package com.kodak.rss.core.n2r.bean;

public class AuthorizationToken {
	
	public static final String ACCESS_TOKEN_RESPONSE = "AccessTokenResponse";
	public static final String ACCESS_TOKEN = "AccessToken";
	public static final String TOKEN_TYPE = "TokenType";
	public static final String EXPIRES_IN = "ExpiresIn";
	public static final String REFRESH_TOKEN = "RefreshToken";
	
	public String accessToken = "";
	public String tokenType = "";
	public int expiresIn = -1;
	public String refreshToken = "";
}
