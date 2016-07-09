/*
 * Note: this code is a fork of Goodow realtime-channel project https://github.com/goodow/realtime-channel
 */

/*
 * Copyright 2013 Goodow.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package naga.commons.bus.websocket;

import naga.commons.bus.spi.Message;
import naga.commons.json.Json;
import naga.commons.json.spi.JsonObject;
import naga.commons.json.spi.WritableJsonObject;
import naga.commons.websocket.spi.WebSocket;
import naga.commons.websocket.spi.WebSocketListener;
import naga.platform.spi.ClientPlatform;
import naga.platform.spi.Platform;
import naga.commons.scheduler.spi.Scheduled;
import naga.commons.util.async.Handler;

import java.util.HashMap;
import java.util.Map;

/*
 * @author 田传武 (aka Larry Tin) - author of Goodow realtime-channel project
 * @author Bruno Salmon - fork, refactor & update for the naga project
 *
 * <a href="https://github.com/goodow/realtime-channel/blob/master/src/main/java/com/goodow/realtime/channel/impl/WebSocketBus.java">Original Goodow class</a>
 */
@SuppressWarnings("rawtypes")
public class WebSocketBus extends SimpleClientBus {
    private static final String ON_OPEN = "@realtime/bus/onOpen";
    static final String ON_CLOSE = "@realtime/bus/onClose";
    static final String ON_ERROR = "@realtime/bus/onError";

    private static final String SESSION = "_session";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String TOPIC_LOGIN = "vertx.basicauthmanager.login";

    private static final String BODY = "body";
    private static final String TOPIC = "address";
    private static final String REPLY_TOPIC = "replyAddress";
    protected static final String TYPE = "type";

    private final WebSocketListener internalWebSocketHandler;
    String serverUri;
    WebSocket webSocket;
    private int pingInterval;
    private Scheduled pingScheduled;
    private String sessionId;
    private String username;
    private String password;
    final Map<String, Integer> handlerCount = new HashMap<>();
    // Possible external web socket listener to observe web socket connection state
    private WebSocketListener webSocketListener;

    public WebSocketBus(WebSocketBusOptions options) {
        options.turnUnsetPropertiesToDefault(); // should be already done by the platform but just in case
        String serverUri =
                (options.getProtocol() == WebSocketBusOptions.Protocol.WS ? "ws" : "http")
                + (options.isServerSSL() ? "s://" : "://")
                + options.getServerHost()
                + ':' + options.getServerPort()
                + '/' + options.getBusPrefix()
                + (options.getProtocol() == WebSocketBusOptions.Protocol.WS ? "/websocket" : "");
        internalWebSocketHandler = new WebSocketListener() {
            @Override
            public void onOpen() {
                if (webSocketListener != null)
                    webSocketListener.onOpen();
                else
                    Platform.log("Connection open");
                // sendLogin(); // Disabling the auto logic mechanism
                // Send the first ping then send a ping every 5 seconds
                sendPing();
                cancelPingTimer();
                pingScheduled = Platform.schedulePeriodic(pingInterval, WebSocketBus.this::sendPing);
                if (hook != null)
                    hook.handleOpened();
                publishLocal(ON_OPEN, null);
            }

            @Override
            public void onMessage(String msg) {
                //Platform.log("Received message = " + msg);
                if (webSocketListener != null)
                    webSocketListener.onMessage(msg);
                JsonObject json = Json.parseObject(msg);
                @SuppressWarnings({"unchecked"})
                ClientMessage message = new ClientMessage(false, false, WebSocketBus.this, json.getString(TOPIC), json.getString(REPLY_TOPIC), json.get(BODY));
                internalHandleReceiveMessage(message);
            }

            @Override
            public void onError(String error) {
                if (webSocketListener != null)
                    webSocketListener.onError(error);
                else
                    Platform.log("Connection error = " + error);
                publishLocal(ON_ERROR, Json.createObject().set("message", error));
            }

            @Override
            public void onClose(JsonObject reason) {
                if (webSocketListener != null)
                    webSocketListener.onClose(reason);
                else
                    Platform.log("Connection closed, reason = " + reason);
                cancelPingTimer();
                publishLocal(ON_CLOSE, reason);
                if (hook != null)
                    hook.handlePostClose();
            }
        };

        connect(serverUri, options);
    }

    public void setWebSocketListener(WebSocketListener webSocketListener) {
        this.webSocketListener = webSocketListener;
    }

    private void cancelPingTimer() {
        if (pingScheduled != null)
            pingScheduled.cancel();
        pingScheduled = null;
    }

    public void connect(String serverUri, WebSocketBusOptions options) {
        this.serverUri = serverUri;
        pingInterval = options.getPingInterval();
        sessionId = options.getSessionId();
        if (sessionId == null)
            sessionId = idGenerator.next(23);
        username = options.getUsername();
        password = options.getPassword();

        if (webSocketListener == null)
            Platform.log("Connecting to " + serverUri);

        webSocket = ClientPlatform.createWebSocket(serverUri, options.getSocketOptions());
        webSocket.setListener(internalWebSocketHandler);
    }

    public WebSocket.State getReadyState() {
        return webSocket.getReadyState();
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    protected void doClose() {
        subscribeLocal(ON_CLOSE, event -> {
            clearHandlers();
            handlerCount.clear();
        });
        webSocket.close();
    }

    @Override
    protected boolean doSubscribe(boolean local, String topic, Handler<? extends Message> handler) {
        boolean subscribed = super.doSubscribe(local, topic, handler);
        if (local || !subscribed || (hook != null && !hook.handlePreSubscribe(topic, handler)))
            return false;
        if (handlerCount.containsKey(topic)) {
            handlerCount.put(topic, handlerCount.get(topic) + 1);
            return false;
        }
        handlerCount.put(topic, 1);
        sendSubscribe(topic);
        return true;
    }

    @Override
    protected <T> void doSendOrPub(boolean local, boolean send, String topic, Object msg, Handler<Message<T>> replyHandler) {
        checkNotNull(TOPIC, topic);
        if (local) {
            super.doSendOrPub(local, send, topic, msg, replyHandler);
            return;
        }
        WritableJsonObject envelope = Json.createObject().set(TYPE, send ? "send" : "publish").set(TOPIC, topic).set(BODY, msg);
        if (replyHandler != null) {
            String replyTopic = makeUUID();
            envelope.set(REPLY_TOPIC, replyTopic);
            replyHandlers.put(replyTopic, (Handler) replyHandler);
        }
        send(envelope);
    }

    @Override
    protected boolean doUnsubscribe(boolean local, String topic, Handler<? extends Message> handler) {
        boolean unsubscribed = super.doUnsubscribe(local, topic, handler);
        if (local || !unsubscribed || (hook != null && !hook.handleUnsubscribe(topic)))
            return false;
        handlerCount.put(topic, handlerCount.get(topic) - 1);
        if (handlerCount.get(topic) == 0) {
            handlerCount.remove(topic);
            sendUnsubscribe(topic);
            return true;
        }
        return false;
    }

    protected void send(JsonObject msg) {
        if (getReadyState() != WebSocket.State.OPEN)
            throw new IllegalStateException("INVALID_STATE_ERR");
        String data = msg.toJsonString();
        //Platform.log("Sending data: " + data);
        webSocket.send(data);
    }

    protected void sendLogin() {
        WritableJsonObject msg = Json.createObject().set(SESSION, sessionId);
        if (username != null) {
            msg.set(USERNAME, username);
            if (password != null)
                msg.set(PASSWORD, password);
        }
        send(TOPIC_LOGIN, msg, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                if (message.body() != null && message.body().getInt("code") != 0)
                    throw new RuntimeException(message.body().toJsonString());
            }
        });
    }

    protected void sendPing() {
        send(Json.createObject().set(TYPE, "ping"));
    }

    /*
     * First handler for this topic so we should register the connection
     */
    protected void sendSubscribe(String topic) {
        //assert topic != null : "topic shouldn't be null";
        send(Json.createObject().set(TYPE, "register").set(TOPIC, topic));
    }

    /*
     * No more handlers so we should unregister the connection
     */
    protected void sendUnsubscribe(String topic) {
        send(Json.createObject().set(TYPE, "unregister").set(TOPIC, topic));
    }
}