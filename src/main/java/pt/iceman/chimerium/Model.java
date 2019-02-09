package pt.iceman.chimerium;

public class Model {
    private String apiMessage;

    public Model() {}

    public Model(String apiMessage) {
        this.apiMessage = apiMessage;
    }

    public String getApiMessage() {
        return apiMessage;
    }

    public void setApiMessage(String apiMessage) {
        this.apiMessage = apiMessage;
    }
}
