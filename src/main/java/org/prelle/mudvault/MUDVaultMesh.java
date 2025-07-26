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
import org.prelle.mudvault.Channel.ChannelAction;
import org.prelle.mudvault.Channel.ChannelPayload;
import org.prelle.mudvault.ErrorResponse.ErrorPayload;
import org.prelle.mudvault.Tell.TellPayload;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * 
 */
public class MUDVaultMesh extends WebSocketClient {
	
	private final static Logger logger = System.getLogger("mudvault");
	
	private final static String GLOBAL_USER = "Demon";
	
	public final static String VERSION = "1.0";
	
	private Gson gson;
	private Gson gsonPretty;
	
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
		Objects.requireNonNull(mudName, "mudName not configured");
		Objects.requireNonNull(apiKey, "apiKey not configured");
		this.config  = config;
		this.callback= callback;
		gson = new Gson();
		gsonPretty = new GsonBuilder().setPrettyPrinting().create();
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
		mess.id = UUID.randomUUID();
		if (mess.metadata==null) {
			mess.metadata = new Metadata(5,300,null,null);
		}
        String json = gson.toJson(mess);
        if (logger.isLoggable(Level.DEBUG)) {
        	String jsonPretty = gsonPretty.toJson(mess);
            logger.log(Level.DEBUG, "SND: "+jsonPretty);
        }
        super.send(json);
	}
	
	//-------------------------------------------------------------------
	private void reply(MeshMessage orig, MeshMessage mess) {
		mess.timestamp = Instant.now().toString();
		mess.from=new MUDUser(mudName, orig.to.user());
		mess.to=orig.from;
		mess.version=VERSION;
		mess.id = UUID.randomUUID();
		if (mess.metadata==null) {
			mess.metadata = new Metadata(5,300,null,null);
		}
        String json = gson.toJson(mess);
        if (logger.isLoggable(Level.DEBUG)) {
        	String jsonPretty = gsonPretty.toJson(mess);
            logger.log(Level.DEBUG, "SND: "+jsonPretty);
        }
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
		logger.log(Level.ERROR, "The websocket closed with exit code {0} and reason {1} ... was remote={2}", code, reason, remote);
		ErrorCode codes =ErrorCode.getByCode(code);
		changeState(MeshConnectionState.DISCONNECTED);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.java_websocket.client.WebSocketClient#onMessage(java.lang.String)
	 */
	@Override
	public void onMessage(String message) {
		logger.log(Level.DEBUG, "ENTER: onMessage");
		try {
		//logger.log(Level.INFO, "received message: " + message);
		JsonObject json = gson.fromJson(message, JsonObject.class);
		if (logger.isLoggable(Level.DEBUG)) {
			logger.log(Level.DEBUG, "received message: " + gsonPretty.toJson( gson.fromJson(message,JsonObject.class)));
		}
		
		String type = json.get("type").getAsString();
		logger.log(Level.DEBUG, "Got "+type);
		
		JsonObject payload = json.get("payload").getAsJsonObject();
		boolean isRequest = true;
		if (payload!=null && payload.get("request")!=null) {
			isRequest = payload.get("request").getAsBoolean();
		}
		logger.log(Level.DEBUG, "Got ''{0}'' , request={1}", type, isRequest);
		
		MeshMessage mess = gson.fromJson(message, MeshMessage.class);
		
		// Check if we are a valid target mud
		if (mess.to.mud()!=null && !"*".equals(mess.to.mud())) {
			if (!mess.to.mud().equalsIgnoreCase(mudName)) {
				// Don't answer an ERROR with an ERROR
				if (mess.type!=MessageType.ERROR) {
					ErrorResponse error = new ErrorResponse();
					error.to = mess.from;
					error.from = new MUDUser(mudName, null);
					error.setPayload(new ErrorPayload("No such MUD '"+mess.to.mud()+"' - I am '"+mudName+"'", ErrorCode.MUD_NOT_FOUND.code, null, message));
					send(error);
				}
				logger.log(Level.WARNING, "Ignore message directed at MUD ''{0}''", mess.to.mud());
				return;
			}
		}
		
		logger.log(Level.DEBUG, "<-- {0}", mess.type);
		switch (mess.type) {
		case CHANNEL   -> rcvChannel(gson.fromJson(message,  Channel.class));
		//case EMOTE_TO  -> rcvChannel(gson.fromJson(message,  Channel.class));
		case ERROR     -> rcvError(gson.fromJson(message, ErrorResponse.class));
		case FINGER    -> rcvFinger(gson.fromJson(message, Finger.class));
		case PING      -> send(new Pong());
		case TELL      -> rcvTell(gson.fromJson(message,  Tell.class));
		case WHO       -> rcvWho(gson.fromJson(message,  Who.class));
		case MUDLIST   -> rcvMUDList(gson.fromJson(message,  MUDList.class));
		default -> {}
		}
		} finally {
			logger.log(Level.DEBUG, "LEAVE: onMessage");
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
//		switch (error.getPayload().getErrorCode()) {
//		case MISSING_MUD_NAME:
//		default:
//		}
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
		if (msg.getPayload().request()) {
			// This is a WHO request
			logger.log(Level.INFO,"User {0}@{1} requested a who-list", msg.from.user(), msg.from.mud());
			if (callback!=null) {
				PlayerInfo[] userList = callback.meshOnWho(msg.from.mud(), msg.from.mud());
				logger.log(Level.INFO, "Player Info: "+userList);
				Who reply = new Who(userList);
				reply(msg,reply);
			}
		} else {
			// This is a WHO reply
			logger.log(Level.INFO,"MUD {0} returned a who-list for {1}", msg.from.mud(), msg.to.user());
			if (callback!=null) {
				callback.meshReceivedWhoList(msg.from.mud(), msg.getPayload().users());
			}
		}
		
	}
	
	//-------------------------------------------------------------------
	public void requestMUDs() {
		MUDList msg = new MUDList();
		msg.to = new MUDUser("Gateway");
		msg.from = new MUDUser(mudName, null);
		send(msg);
	}
	
	//-------------------------------------------------------------------
	public void rcvMUDList(MUDList response) {
		logger.log(Level.INFO, response.getPayload().muds().length+" muds found");
	}
	
	//-------------------------------------------------------------------
	public void requestWhoList(String toMUD, String fromUser) {
		Who msg = new Who(null,null);
		msg.to = new MUDUser(toMUD);
		msg.from = new MUDUser(mudName, fromUser);
		send(msg);
	}
	
	//-------------------------------------------------------------------
	public void tell(String toMUD, String toUser, String fromUser, String message) {
		Tell msg = new Tell();
		msg.to = new MUDUser(toMUD, toUser);
		msg.from = new MUDUser(mudName, fromUser);
		msg.setPayload(new TellPayload(message,null));
		send(msg);
	}
	
	//-------------------------------------------------------------------
	public void joinChannel(String channel) {
		Channel msg = new Channel();
		msg.to = new MUDUser("*", null, channel);
		msg.from = new MUDUser(mudName, GLOBAL_USER);
		msg.setPayload(new ChannelPayload(channel, null, ChannelAction.JOIN, null));
		send(msg);
	}
	
	//-------------------------------------------------------------------
	public void leaveChannel(String channel) {
		Channel msg = new Channel();
		msg.to = new MUDUser("*", null, channel);
		msg.from = new MUDUser(mudName, GLOBAL_USER);
		msg.setPayload(new ChannelPayload(channel, null, ChannelAction.LEAVE, null));
		send(msg);
	}
	
	//-------------------------------------------------------------------
	public void sendOnChannel(String channel, String fromUser, String message) {
		Channel msg = new Channel();
		msg.to = new MUDUser("*", null, channel);
		msg.from = new MUDUser(mudName, fromUser);
		msg.setPayload(new ChannelPayload(channel,message));
		send(msg);
	}
	
	//-------------------------------------------------------------------
	public void finger(String toMUD, String toUser, String fromMUD, String fromUser) {
		Finger msg = new Finger();
		msg.to = new MUDUser(toMUD, toUser);
		msg.from = new MUDUser(mudName, fromUser);
		send(msg);
	}

	//-------------------------------------------------------------------
	private void rcvFinger(Finger msg) {
		if (msg.getPayload().request()) {
			// This is a request
			logger.log(Level.INFO,"User {0}@{1} fingered our user {2}", msg.from.user(), msg.from.mud(), msg.to.user());
			if (callback!=null) {
				PlayerInfo info = callback.meshOnFinger(msg.from.mud(), msg.from.user(), msg.to.user());
				Finger reply = new Finger();
				reply.setPayload(new Finger.FingerPayload(msg.to.user(),false, info));
				reply.from=new MUDUser(mudName);
				reply(msg,reply);
			}
		} else {
			// This is a reponse
			logger.log(Level.INFO,"Got an finger answer from {0}@{1} for {2}", msg.from.user(), msg.from.mud(), msg.to.user());
			if (callback!=null) {
				
			}
		}
		
	}

}