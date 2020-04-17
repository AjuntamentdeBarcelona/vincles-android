package cat.bcn.vincles.mobile.Client.Model;

public class ChatMessageMulticastRest {

    private int idUserFrom;

    private int[] idUserToList;

    private int[] idChatToList;

    private String text;

    private int[] idAdjuntContents;

    private String metadataTipus;

    public int getIdUserFrom() {
        return idUserFrom;
    }

    public void setIdUserFrom(int idUserFrom) {
        this.idUserFrom = idUserFrom;
    }

    public int[] getIdUserToList() {
        return idUserToList;
    }

    public void setIdUserToList(int[] idUserToList) {
        this.idUserToList = idUserToList;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int[] getIdAdjuntContents() {
        return idAdjuntContents;
    }

    public void setIdAdjuntContents(int[] idAdjuntContents) {
        this.idAdjuntContents = idAdjuntContents;
    }

    public String getMetadataTipus() {
        return metadataTipus;
    }

    public void setMetadataTipus(String metadataTipus) {
        this.metadataTipus = metadataTipus;
    }

    public int[] getIdChatToList() {
        return idChatToList;
    }

    public void setIdChatToList(int[] idChatToList) {
        this.idChatToList = idChatToList;
    }
}
