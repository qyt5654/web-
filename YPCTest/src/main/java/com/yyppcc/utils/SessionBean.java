package com.yyppcc.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

@AllArgsConstructor
@Data
public class SessionBean {
    private WebSocketSession webSocketSession;
    private Integer clientId;
}
