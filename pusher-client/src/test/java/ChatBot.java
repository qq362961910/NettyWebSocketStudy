import java.io.StringReader;
import java.net.URI;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * ChatBot
 * @author Jiji_Sasidharan
 */
public class ChatBot {

    /**
     * main
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final ChatClientEndpoint clientEndPoint = new ChatClientEndpoint(new URI("ws://192.168.14.62:8888"));
        clientEndPoint.addMessageHandler(new ChatClientEndpoint.MessageHandler() {
            public void handleMessage(String message) {
                JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
                String userName = jsonObject.getString("user");
                if (!"bot".equals(userName)) {
                    clientEndPoint.sendMessage(getMessage("Hello " + userName +", How are you?"));
                    // other dirty bot logic goes here.. :)
                }
            }
        });

        while (true) {
            clientEndPoint.sendMessage(getMessage("Hi There!!"));
            Thread.sleep(30000);
        }
    }

    /**
     * Create a json representation.
     *
     * @param message
     * @return
     */
    private static String getMessage(String message) {
        return Json.createObjectBuilder()
                .add("user", "bot")
                .add("message", message)
                .build()
                .toString();
    }
}