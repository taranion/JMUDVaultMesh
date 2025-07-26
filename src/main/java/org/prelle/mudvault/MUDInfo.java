package org.prelle.mudvault;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 */
@Getter
@Setter
@ToString
public class MUDInfo {

	private String name;
	private String host;
	private String version;
	private String admin;
	private String email;
	private int uptime;
	private int users;
	private String description;
	

}
