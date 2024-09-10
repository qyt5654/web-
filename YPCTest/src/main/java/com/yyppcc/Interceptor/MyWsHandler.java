package com.yyppcc.Interceptor;

import cn.hutool.db.Session;
import com.yyppcc.utils.SessionBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * web socket 主处理程序
 */
@Slf4j
@Component
public class MyWsHandler extends AbstractWebSocketHandler {
    private static Map<String, SessionBean> sessionBeanMap;
    private static AtomicInteger clientIdMaker;
    private static StringBuffer stringBuffer;
    static{
        sessionBeanMap = new ConcurrentHashMap<>();
        clientIdMaker = new AtomicInteger(0);
        stringBuffer = new StringBuffer();
    }

    //连接建立后
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        SessionBean sessionBean = new SessionBean(session, clientIdMaker.getAndIncrement());
        sessionBeanMap.put(session.getId(), sessionBean);
        log.info(sessionBeanMap.get(session.getId()).getClientId() + "建立了连接");
        stringBuffer.append(sessionBeanMap.get(session.getId()).getClientId()).append("进入了群聊<br/>");
        //向每个用户发送
        sendMessage(sessionBeanMap);
    }

    //收到消息
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        log.info(sessionBeanMap.get(session.getId()).getClientId() + ":" +message.getPayload());
        stringBuffer.append(sessionBeanMap.get(session.getId()).getClientId()).append(":").append(message.getPayload()).append("<br/>");
        sendMessage(sessionBeanMap);
    }

    //传输异常
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        if(session.isOpen()){
            session.close();
        }
        sessionBeanMap.remove(session.getId());
    }

    //连接关闭
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        int clientId = sessionBeanMap.get(session.getId()).getClientId();
        sessionBeanMap.remove(session.getId());
        log.info(clientId + "关闭了连接");
        stringBuffer.append(clientId).append("关闭了群聊<br/>");
        sendMessage(sessionBeanMap);
    }

    public void sendMessage(Map<String, SessionBean> sessionBeanMap){
        try{
            for(String key : sessionBeanMap.keySet()){
                sessionBeanMap.get(key).getWebSocketSession().sendMessage(new TextMessage(stringBuffer.toString()));
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }
}
