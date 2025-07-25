package org.prelle.mudvault;

/**
 * 
 */
public record MUDUser(String mud, String user, String channel) {

	public MUDUser(String mud, String user) {
		this(mud,user,null);
	}

	public MUDUser(String mud) {
		this(mud,null,null);
	}
}
