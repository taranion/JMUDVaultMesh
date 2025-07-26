package org.prelle.mudvault;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class Who extends MeshMessage {
	
	public static record WhoPayload(boolean request, String sort, String format, PlayerInfo[] users) {
		public WhoPayload(PlayerInfo[] users) {
			this(false, null, null, users);
		}
		public WhoPayload(String sort, String format) {
			this(true, sort, format, null);
		}
	}
	
	@Getter @Setter
	private WhoPayload payload;

	//-------------------------------------------------------------------
	public Who() {
		type=MessageType.WHO;
	}

	//-------------------------------------------------------------------
	public Who(String sort, String format) {
		type=MessageType.WHO;
		payload = new WhoPayload(sort, format);
	}

	//-------------------------------------------------------------------
	public Who(PlayerInfo[] users) {
		type=MessageType.WHO;
		payload = new WhoPayload(users);
	}

}
