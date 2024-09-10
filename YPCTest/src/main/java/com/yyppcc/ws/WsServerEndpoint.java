package com.yyppcc.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监听websocket地址/myWs
 */
@ServerEndpoint("/myWs")
@Component
@Slf4j
public class WsServerEndpoint {
    //保证线程安全
    static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session){
        sessionMap.put(session.getId(), session);
        log.info("连接了websocket");
    }

    @OnMessage
    public String onMessage(String message){
        log.info("收到了一条消息：{}", message);
        return "收到";
    }

    @OnClose
    public void OnClose(Session session){
        sessionMap.remove(session.getId());
        log.info("断开了websocket");
    }

    //每隔两秒向前端发送心跳
    @Scheduled(fixedRate = 2000)
    public void sendMsg() throws IOException {
        for(String key: sessionMap.keySet()){
            sessionMap.get(key).getBasicRemote().sendText("心跳");
        }
    }
}
