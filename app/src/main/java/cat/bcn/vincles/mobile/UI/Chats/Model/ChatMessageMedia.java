package cat.bcn.vincles.mobile.UI.Chats.Model;

import java.util.ArrayList;

public class ChatMessageMedia extends ChatMessage {

    ArrayList<String> mediaFiles;
    ArrayList<Boolean> isVideo;

    public ChatMessageMedia(int type, long sendTime, String text, long id, int idUserFrom, boolean watched, ArrayList<String> mediaFiles, String fullNameUserSender, ArrayList<Boolean> isVideo) {
        super(type, sendTime, text, id, idUserFrom, fullNameUserSender, watched);
        this.mediaFiles = mediaFiles;
        this.isVideo = isVideo;
    }

    public ArrayList<String> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(ArrayList<String> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    public ArrayList<Boolean> getIsVideo() {
        return isVideo;
    }

    public void setIsVideo(ArrayList<Boolean> isVideo) {
        this.isVideo = isVideo;
    }

}
