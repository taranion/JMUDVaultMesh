package org.prelle.mudvault;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 */
@ToString
@Getter
@Setter
@Builder
public class Metadata {
	
	private int priority;
	private int ttl;
	private String encoding;
	private String language;

}
