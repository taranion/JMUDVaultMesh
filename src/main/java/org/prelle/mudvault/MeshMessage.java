package org.prelle.mudvault;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 */
@ToString
@Getter
public class MeshMessage {

	protected String version;
	protected UUID id;
	protected String timestamp;
	protected MessageType type;
	protected MUDUser from;
	protected MUDUser to;
	protected Metadata metadata;
	
	//-------------------------------------------------------------------
	/**
	 */
	public MeshMessage() {
		// TODO Auto-generated constructor stub
	}

}
