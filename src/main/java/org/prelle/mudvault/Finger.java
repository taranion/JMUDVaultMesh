package org.prelle.mudvault;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class Finger extends MeshMessage {
	
	public static record FingerPayload() {
	}
	
	@Getter
	@Setter
	private FingerPayload payload = new FingerPayload();

	//-------------------------------------------------------------------
	public Finger() {
		type=MessageType.FINGER;
	}

}
