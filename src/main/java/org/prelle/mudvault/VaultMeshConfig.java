package org.prelle.mudvault;

import lombok.Builder;
import lombok.Getter;

/**
 * 
 */
@Getter
@Builder
public class VaultMeshConfig {
	
	private String mudName;
	private String apiKey;
	@Builder.Default
	private String userAgent="MUDVaultMesh Java";
	@Builder.Default
	private String meshServer="86.38.203.37";
	@Builder.Default
	private int websocketPort=8082;
	@Builder.Default
	private int jwtPort=8084;
	@Builder.Default
	private String jwtPath="/api/v1/auth/login";

}
