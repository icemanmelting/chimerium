package pt.iceman.chimerium.request.body;

import com.google.gson.Gson;

public class GsonParser {
    private static Gson gson = null;

    public static Gson getGsonInstance() {
        if(gson == null) {
            gson = new Gson();
        }
        return gson;
    }
}
