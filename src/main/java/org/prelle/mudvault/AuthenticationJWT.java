package org.prelle.mudvault;

/**
 * 
 */
public record AuthenticationJWT(String message, String token, String mudName, String expires) {

}
