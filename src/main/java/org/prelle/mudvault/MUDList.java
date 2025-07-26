package org.prelle.mudvault;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class MUDList extends MeshMessage {
	
	public static record MUDListPayload(boolean request, MUDInfo[] muds) {
		public MUDListPayload(MUDInfo[] muds) {
			this(false, muds);
		}
		public MUDListPayload() {
			this(true, null);
		}
	}
	
	@Getter @Setter
	private MUDListPayload payload;

	//-------------------------------------------------------------------
	public MUDList() {
		type=MessageType.MUDLIST;
		payload = new MUDListPayload();
	}

	//-------------------------------------------------------------------
	public MUDList(MUDInfo[] mudlist) {
		type=MessageType.MUDLIST;
		payload = new MUDListPayload(mudlist);
	}

}
