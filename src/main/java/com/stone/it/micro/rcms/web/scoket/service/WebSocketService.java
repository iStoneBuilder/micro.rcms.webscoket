package com.stone.it.micro.rcms.web.scoket.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * webSocket
 *
 */
@Component
@ServerEndpoint("/ws")
public class WebSocketService {

    //记录在线连接数
    private static Long onlineCount = 0L;
    //concurrent包的线程安全Set，用来存放每个客户端对应的WebSocket对象。
    private static final CopyOnWriteArraySet<WebSocketService> webSocketSet = new CopyOnWriteArraySet<WebSocketService>();
    //日志记录器
    private final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    public static synchronized Long getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketService.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketService.onlineCount--;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        logger.info("新建连接");
        //设置Session
        this.session = session;
        //添加到线程安全
        webSocketSet.add(this);
        //在线数加1
        addOnlineCount();
        logger.info("当前连接数："+getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        logger.info("关闭当前连接");
        //从set中删除
        webSocketSet.remove(this);
        //在线数减1
        subOnlineCount();
        logger.info("当前连接数："+getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("接收客户端消息："+message);
        for (WebSocketService item : webSocketSet) {
            if(item.session == session){
                //执行收到信息
                processShell(message);
            }
        }
    }

    /**
     * 发生错误时调用
     */
     @OnError
     public void onError(Session session, Throwable error) {
         logger.info("调用时发生错误："+error.getMessage());
         for (WebSocketService item : webSocketSet) {
             if(item.session == session){
                 item.sendMessage("调用时发生错误："+error.getMessage());
             }
         }
     }

    /**
     * 发送消息返回客户端
     * @param message
     * @throws IOException
     */
     public void sendMessage(String message) {
         try{
             this.session.getBasicRemote().sendText(message);
         }catch (IOException e){
             logger.info("返回客户端消息发生异常：："+e.getMessage());
         }
     }

    public void processShell(String shell){
        try {
            final Process process = Runtime.getRuntime().exec(shell);
            processMessage(process.getInputStream());
            processMessage(process.getErrorStream());
        }catch (Exception e){
            sendMessage("执行命令报错："+e.getMessage());
        }
    }

    private void processMessage(InputStream inputStream) {
        new Thread(new Runnable() {
            public void run() {
                Reader reader = new InputStreamReader(inputStream);
                BufferedReader bf = new BufferedReader(reader);
                String line = null;
                try {
                    while ((line = bf.readLine()) != null) {
                        sendMessage(line);
                    }
                } catch (IOException e) {
                    sendMessage("解析消息报错："+e.getMessage());
                }
            }
        }).start();
    }

}
