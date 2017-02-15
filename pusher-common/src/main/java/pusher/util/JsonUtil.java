package pusher.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();


    /**
     * Object ---> Json
     * */
    public static String formatObjectToJson(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    /**
     * Json ---> Object
     * */
    public static <T> T parseObjectFromJson(Class<T> clazz, String json) throws IOException {
        return mapper.readValue(json, clazz);
    }

}
