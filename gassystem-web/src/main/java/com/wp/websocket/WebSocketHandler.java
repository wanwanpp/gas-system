package com.wp.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

	private static Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

	private final EchoService echoService;

	public WebSocketHandler(EchoService echoService) {
		this.echoService = echoService;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		logger.info("Opened new session in instance " + this);
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws Exception {
		String echoMessage = this.echoService.getMessage(message.getPayload());
		logger.debug(echoMessage);
		session.sendMessage(new TextMessage(echoMessage));
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception)
			throws Exception {
		session.close(CloseStatus.SERVER_ERROR);
	}

}
