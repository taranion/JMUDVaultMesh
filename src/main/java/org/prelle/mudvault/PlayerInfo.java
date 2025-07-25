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

	private String name;
	private String title;
	private Integer level;
	@SerializedName("class")
	private String clazz;
	private Integer idle;
}
