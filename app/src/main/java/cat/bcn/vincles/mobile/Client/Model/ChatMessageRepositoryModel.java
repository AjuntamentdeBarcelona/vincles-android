package cat.bcn.vincles.mobile.Client.Model;

import android.util.Log;

import java.util.ArrayList;

public class ChatMessageRepositoryModel {

    private long id;

    private int idSender;

    private String fullNameUserSender;

    private int idChat;

    private String text;

    private boolean watched;

    private long sendTime;
    private String metadataTipus;

    private ArrayList<String> pathsAdjuntContents = new ArrayList<>();

    private ArrayList<String> metadataAdjuntContents = new ArrayList<>();

    private ArrayList<Integer> idAdjuntContents = new ArrayList<>();

    public ChatMessageRepositoryModel(int idChat, int idSender, String text, ArrayList<Integer> idAdjuntContents,
                                      String metadataTipus) {
        this.idChat = idChat;
        this.idSender = idSender;
        this.text = text;
        this.idAdjuntContents = idAdjuntContents;
        this.metadataTipus = metadataTipus;
    }

    public ChatMessageRepositoryModel(int idChat, int idSender, String text,
                                      ArrayList<Integer> idAdjuntContents,
                                      ArrayList<String> pathsAdjuntContents,
                                      ArrayList<String> metadataAdjuntContents,
                                      String metadataTipus) {
        this(idChat, idSender, text, idAdjuntContents, metadataTipus);
        setPathsAdjuntContents(pathsAdjuntContents);
        setMetadataAdjuntContents(metadataAdjuntContents);
    }


    public ChatMessageRepositoryModel(ChatMessageRest chatMessageRest) {
        this.id = chatMessageRest.getId();
        this.idSender = chatMessageRest.getIdUserFrom();
        this.idChat = chatMessageRest.getIdUserFrom();
        this.text = chatMessageRest.getText();
        this.watched = chatMessageRest.isWatched();
        this.sendTime = chatMessageRest.getSendTime();
        this.metadataTipus = chatMessageRest.getMetadataTipus();
        this.pathsAdjuntContents = new ArrayList<>(chatMessageRest.getPathsAdjuntContents());
        this.metadataAdjuntContents = new ArrayList<>(chatMessageRest.getMetadataAdjuntContents());
        this.idAdjuntContents = new ArrayList<>(chatMessageRest.getIdAdjuntContents());
    }

    public ChatMessageRepositoryModel(GroupMessageRest groupMessageRest) {
        this.id = groupMessageRest.getId();
        this.idSender = groupMessageRest.getIdUserSender();
        this.fullNameUserSender = groupMessageRest.getFullNameUserSender();
        this.idChat = groupMessageRest.getIdChat();
        this.text = groupMessageRest.getText();
        this.watched = groupMessageRest.isWatched();
        this.sendTime = groupMessageRest.getSendTime();
        this.metadataTipus = groupMessageRest.getMetadataTipus();
        this.pathsAdjuntContents = new ArrayList<>();
        this.metadataAdjuntContents = new ArrayList<>();
        this.idAdjuntContents = new ArrayList<>();
        if (groupMessageRest.getIdContent() != null) {
            this.idAdjuntContents.add(groupMessageRest.getIdContent());
        }
        pathsAdjuntContents.add(groupMessageRest.getPathContent());
        metadataAdjuntContents.add(groupMessageRest.getMetadataContent());
        Log.d("chatmess", "break");

    }

    public long getId() {
        return id;
    }

    public int getIdSender() {
        return idSender;
    }

    public int getIdChat() {
        return idChat;
    }

    public String getText() {
        return text;
    }

    public boolean isWatched() {
        return watched;
    }

    public long getSendTime() {
        return sendTime;
    }

    public String getMetadataTipus() {
        return metadataTipus;
    }

    public ArrayList<String> getPathsAdjuntContents() {
        return pathsAdjuntContents;
    }

    public ArrayList<String> getMetadataAdjuntContents() {
        return metadataAdjuntContents;
    }

    public ArrayList<Integer> getIdAdjuntContents() {
        return idAdjuntContents;
    }

    public void setPathsAdjuntContents(ArrayList<String> pathsAdjuntContents) {
        this.pathsAdjuntContents = pathsAdjuntContents;
    }

    public void setMetadataAdjuntContents(ArrayList<String> metadataAdjuntContents) {
        this.metadataAdjuntContents = metadataAdjuntContents;
    }

    public String getFullNameUserSender() {
        return fullNameUserSender;
    }

    public void setFullNameUserSender(String fullNameUserSender) {
        this.fullNameUserSender = fullNameUserSender;
    }
}
