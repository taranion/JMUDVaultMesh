package org.prelle.mudvault;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.prelle.mudvault.Authentication.AuthPayload;
import org.prelle.mudvault.Tell.TellPayload;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * 
 */
public class MUDVaultMesh extends WebSocketClient {
	
	private final static Logger logger = System.getLogger("mudvault");
	
	public final static String VERSION = "1.0";
	
	private Gson gson;
	
	private VaultMeshConfig config;
	private String mudName;
	private String apiKey;
	
	private transient MeshConnectionState state;
	private transient String token;
	private transient MUDVaultListener callback;
	
	//-------------------------------------------------------------------
	public MUDVaultMesh(VaultMeshConfig config, MUDVaultListener callback) throws URISyntaxException {
		super(new URI("ws://"+config.getMeshServer()+":"+config.getWebsocketPort()), Map.of("User-Agent",config.getUserAgent()));
		this.mudName = config.getMudName();
		this.apiKey  = config.getApiKey();
		this.config  = config;
		this.callback= callback;
		gson = new Gson();
		state = MeshConnectionState.DISCONNECTED;
	}
	
	//-------------------------------------------------------------------
	private void changeState(MeshConnectionState newState) {
		Objects.requireNonNull(newState);
		if (newState==state) return;
		logger.log(Level.INFO, "Change state from {0} to {1}", state, newState);
		state = newState;
		if (callback!=null)
			callback.meshStateChanged(newState);
	}
	
	//-------------------------------------------------------------------
	private void send(MeshMessage mess) {
		mess.timestamp = Instant.now().toString();
		mess.version=VERSION;
        String json = gson.toJson(mess);
        logger.log(Level.INFO, "SND: "+json);
        super.send(json);
	}
	
	//-------------------------------------------------------------------
	private void authenticate() throws IOException {
		changeState(MeshConnectionState.AUTHENTICATING);
		HttpClient client = HttpClient.newBuilder()
        	.connectTimeout(Duration.ofSeconds(10))
        	.build();
		
		String jsonBody = String.format(
	            "{\"mudName\": \"%s\", \"apiKey\": \"%s\"}", 
	            mudName, apiKey
	        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://"+config.getMeshServer()+":"+config.getJwtPort()+config.getJwtPath()))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .timeout(Duration.ofSeconds(30))
            .build();
            
        try {
			HttpResponse<String> response = client.send(request, 
			    HttpResponse.BodyHandlers.ofString());
			    
			if (response.statusCode() == 200) {
			    logger.log(Level.DEBUG, "JWT authentication returned: {0}",response.body() );
			    AuthenticationJWT jwt = gson.fromJson(response.body(), AuthenticationJWT.class);
			    if (jwt.token()==null) {
					changeState(MeshConnectionState.DISCONNECTED);
			    	throw new IOException("Authentication error: "+jwt.message());
			    }
			    logger.log(Level.INFO, "Got token {0}", jwt.token() );
			    this.token = jwt.token();
				changeState(MeshConnectionState.CONNECTED);
			} else {
				changeState(MeshConnectionState.DISCONNECTED);
			    throw new RuntimeException("Login failed: " + response.statusCode());
			}
		} catch (InterruptedException e) {
			logger.log(Level.ERROR, "Internal error during JWT token authentication",e);
		}
	        
	    // Now Websocket Authentication
        Authentication auth = new Authentication();
        auth.id = UUID.randomUUID();
        auth.timestamp = Instant.now().toString();
        auth.from=new MUDUser(mudName, null,null);
        auth.to  =new MUDUser("Gateway", null,null);
        auth.payload  = new AuthPayload(token, mudName);
        auth.metadata = Metadata.builder().priority(5).ttl(300).build();
        send(auth);
	}
	
	//-------------------------------------------------------------------
	@Override
	public void onOpen(ServerHandshake handshakedata) {
		try {
			authenticate();
		} catch (IOException e) {
			logger.log(Level.ERROR, "Error authenticating",e);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.java_websocket.client.WebSocketClient#onClose(int, java.lang.String, boolean)
	 */
	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("closed with exit code " + code + " additional info: " + reason);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.java_websocket.client.WebSocketClient#onMessage(java.lang.String)
	 */
	@Override
	public void onMessage(String message) {
		logger.log(Level.INFO, "received message: " + message);
		JsonObject json = gson.fromJson(message, JsonObject.class);
		String type = json.get("type").getAsString();
		logger.log(Level.DEBUG, "Got "+type);
		MeshMessage mess = gson.fromJson(message, MeshMessage.class);
		logger.log(Level.DEBUG, "Got "+mess);
		
		switch (mess.type) {
		case CHANNEL   -> rcvChannel(gson.fromJson(message,  Channel.class));
		case ERROR     -> rcvError(gson.fromJson(message, ErrorResponse.class));
		case PING      -> send(new Pong());
		case TELL      -> rcvTell(gson.fromJson(message,  Tell.class));
		case WHO       -> rcvWho(gson.fromJson(message,  Who.class));
		case WHO_REPLY -> rcvWhoReply(gson.fromJson(message,  WhoReply.class));
		default -> {}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.java_websocket.client.WebSocketClient#onMessage(java.nio.ByteBuffer)
	 */
	@Override
	public void onMessage(ByteBuffer message) {
		logger.log(Level.INFO,"received ByteBuffer");
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.java_websocket.client.WebSocketClient#onError(java.lang.Exception)
	 */
	@Override
	public void onError(Exception ex) {
		logger.log(Level.INFO,"an error occurred:", ex);
	}

	//-------------------------------------------------------------------
	private void rcvError(ErrorResponse error) {
		logger.log(Level.ERROR,"Received error "+error.getPayload());
		switch (error.getPayload().getErrorCode()) {
		case MISSING_MUD_NAME:
		default:
		}
	}

	//-------------------------------------------------------------------
	private void rcvTell(Tell msg) {
		logger.log(Level.INFO,"Received tell from {0}@{1} to {2}", msg.from.user(), msg.from.mud(), msg.to.user());
		if (callback!=null) {
			callback.meshOnTell(msg.from.mud(), msg.from.mud(), msg.to.user(), msg.getPayload().message());
		}
	}

	//-------------------------------------------------------------------
	private void rcvChannel(Channel msg) {
		logger.log(Level.INFO,"Received message on {2} channel from {0}@{1}", msg.from.user(), msg.from.mud(), msg.to.channel());
		if (callback!=null) {
			callback.meshOnTell(msg.from.mud(), msg.from.mud(), msg.to.user(), msg.getPayload().message());
		}
	}

	//-------------------------------------------------------------------
	private void rcvWho(Who msg) {
		logger.log(Level.INFO,"User {0}@{1} requested a who-list", msg.from.user(), msg.from.mud());
		if (callback!=null) {
			List<PlayerInfo> userList = callback.meshOnWho(msg.from.mud(), msg.from.mud());
			WhoReply reply = new WhoReply();
			reply.getPayload().users().addAll(userList);
			reply.to = msg.from;
			reply.from=new MUDUser(mudName);
			send(reply);
		}
	}
	
	//-------------------------------------------------------------------
	public void requestwhoList(String toMUD, String fromUser) {
		Who msg = new Who();
		msg.to = new MUDUser(toMUD);
		msg.from = new MUDUser(mudName, fromUser);
		send(msg);
	}

	//-------------------------------------------------------------------
	private void rcvWhoReply(WhoReply msg) {
		logger.log(Level.INFO,"MUD {0} returned a who-list for {1}", msg.from.mud(), msg.to.user());
		if (callback!=null) {
			callback.meshReceivedWhoList(msg.from.mud(), msg.getPayload().users());
		}
	}
	
	//-------------------------------------------------------------------
	public void tell(String toMUD, String toUser, String fromUser, String message) {
		Tell msg = new Tell();
		msg.to = new MUDUser(toMUD, toUser);
		msg.from = new MUDUser(mudName, fromUser);
		msg.setPayload(new TellPayload(message));
		send(msg);
	}

}