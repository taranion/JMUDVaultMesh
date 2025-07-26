package org.prelle.mudvault;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class Finger extends MeshMessage {
	
	public static record FingerPayload(String user, boolean request, PlayerInfo info) {
	}
	
	@Getter
	@Setter
	private FingerPayload payload;

	//-------------------------------------------------------------------
	public Finger() {
		type=MessageType.FINGER;
	}

}
