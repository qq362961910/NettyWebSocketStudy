package com.wxsk.pusher.launcher;

import com.wxsk.pusher.config.AppConfig;
import com.wxsk.pusher.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class DefaultLauncher {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLauncher.class);
    private static final int PORT_DEFAULT = 8888;
    public static int PORT;

    public static void main(String[] args) {
        String portStr = System.getProperty("port");
        if (portStr == null || portStr.trim().length() == 0) {
            PORT = PORT_DEFAULT;
        }
        else {
            try {
                PORT = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                PORT = PORT_DEFAULT;
            }
        }
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        WebSocketServer launcher = context.getBean(WebSocketServer.class);
        launcher.setPort(PORT);
        new Thread(launcher).start();
        logger.info("App start with PORT: " + PORT);
    }
}
