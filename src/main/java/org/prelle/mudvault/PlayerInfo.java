package org.prelle.mudvault;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 */
@Getter
@Setter
@ToString
public class PlayerInfo {

	private String username;
	private String displayName;
	private String title;
	private String level;
	@SerializedName("class")
	private String clazz;
	private String location;
	private Integer idle;
	private String realName;
}
