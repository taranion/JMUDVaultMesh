package org.prelle.mudvault;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Getter
@Setter
public class Authentication extends MeshMessage {
	
	public static record AuthPayload(String token, String mudName) {};
	
	protected AuthPayload payload;

	//-------------------------------------------------------------------
	public Authentication() {
		version = MUDVaultMesh.VERSION;
		id      = UUID.randomUUID();
		type    = MessageType.AUTH;
	}

}
