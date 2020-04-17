package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.CircleRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.CircleUserRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.UserCircleRealm;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.CircleUser;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Model.UserCircle;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessage;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class UserMessageDb extends BaseDb {

    Context context;

    public UserMessageDb(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void dropTable() {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(ChatMessageRest.class).findAll().deleteAllFromRealm();
                }});
        }
    }

    public ChatMessageRest findMessage(long id) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            ChatMessageRest chatMessageRest = realmInstance.where(ChatMessageRest.class).equalTo("id", id).findFirst();
            if (chatMessageRest==null)return null;
            return realmInstance.copyFromRealm(chatMessageRest);
        }
    }

    public ArrayList<ChatMessageRest> findAllMessagesBetween(int senderId, int receiverId) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            RealmResults<ChatMessageRest> messagesList = realmInstance.where(ChatMessageRest.class)
                    .beginGroup()
                    .equalTo("idUserFrom",senderId)
                    .equalTo("idUserTo",receiverId)
                    .endGroup()
                    .or()
                    .beginGroup()
                    .equalTo("idUserFrom",receiverId)
                    .equalTo("idUserTo",senderId)
                    .endGroup()
                    .sort("sendTime", Sort.DESCENDING)
                    .findAll();
            if (messagesList==null)return new ArrayList<>();
            return new ArrayList<>(realmInstance.copyFromRealm(messagesList));
        }
    }

    public ArrayList<ChatMessageRest> getUnreadMessagesReceived(int idMe, int idOther) {
        try(Realm realmInstance = Realm.getDefaultInstance()) {
            RealmResults<ChatMessageRest> chatmessageRest = realmInstance.where(ChatMessageRest.class)
                    .equalTo("idUserFrom", idOther)
                    .equalTo("idUserTo", idMe)
                    .equalTo("watched", false)
                    .sort("sendTime", Sort.DESCENDING)
                    .findAll();
            if (chatmessageRest==null)return new ArrayList<>();
            return  new ArrayList<>(realmInstance.copyFromRealm(chatmessageRest));
        }
    }

    public int getNumberUnreadMessagesReceived(int idMe, int idOther) {
        //Realm realm = Realm.getDefaultInstance();
        try(Realm realmInstance = Realm.getDefaultInstance()) {
            return realmInstance.where(ChatMessageRest.class)
                    .equalTo("idUserFrom", idOther)
                    .equalTo("idUserTo", idMe)
                    .equalTo("watched", false)
                    .findAll().size();
        }
    }

    public int getTotalNumberMessages(int idMe, int idOther) {
        try(Realm realmInstance = Realm.getDefaultInstance()) {
            RealmResults<ChatMessageRest> chatMessageRest = realmInstance.where(ChatMessageRest.class)
                    .beginGroup()
                    .equalTo("idUserFrom",idMe)
                    .equalTo("idUserTo",idOther)
                    .endGroup()
                    .or()
                    .beginGroup()
                    .equalTo("idUserFrom",idOther)
                    .equalTo("idUserTo",idMe)
                    .endGroup()
                    .findAll();
            return chatMessageRest.size();
        }
    }

    public long getLastMessage(int idMe, int idOther) {
        try(Realm realmInstance = Realm.getDefaultInstance()) {
            RealmResults<ChatMessageRest> messageRests = realmInstance.where(ChatMessageRest.class)
                    .beginGroup()
                    .equalTo("idUserFrom", idMe)
                    .equalTo("idUserTo", idOther)
                    .endGroup()
                    .or()
                    .beginGroup()
                    .equalTo("idUserFrom", idOther)
                    .equalTo("idUserTo", idMe)
                    .endGroup()
                    .sort("sendTime", Sort.DESCENDING)
                    .findAll();
            ChatMessageRest chatMessageRest = messageRests.size() > 0 ? messageRests.get(0) : null;
            return chatMessageRest == null ? 0 : chatMessageRest.getSendTime();
        }
    }


    public void saveChatMessageRest(final ChatMessageRest chatMessageRest) {
        try(Realm realmInstance = Realm.getDefaultInstance()) {
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(chatMessageRest);
                }
            });
        }
    }

    public void saveChatMessageRestList(final List<ChatMessageRest> messages) {
        try(Realm realmInstance = Realm.getDefaultInstance()) {
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (ChatMessageRest m : messages){
                        if (m.getIdAdjuntContents()!=null){
                            for (Integer adjunt : m.getIdAdjuntContents()){
                                m.getMetadataAdjuntContents().add("");
                                m.getPathsAdjuntContents().add("");
                            }
                        }

                    }
                    realm.copyToRealmOrUpdate(messages);
                }
            });
        }
    }

    public void setMessageFile(final int contentID, final String path, final long messageID, final String metadata) {

        try(Realm realmInstance = Realm.getDefaultInstance()) {
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ChatMessageRest message = realm.where(ChatMessageRest.class).equalTo("id", messageID).findFirst();
                    if (message==null)return;
                    RealmList<Integer> contentIDs = message.getIdAdjuntContents();
                    for (int i = 0; i<contentIDs.size(); i++) {
                        if (contentIDs.get(i) != null && contentIDs.get(i).equals(contentID)) {
                            RealmList<String> paths = message.getPathsAdjuntContents();
                            if (paths.size() > 0) paths.remove(i);
                            paths.add(i, path);
                            RealmList<String> metadatas = message.getMetadataAdjuntContents();
                            if (metadatas.size() > 0) metadatas.remove(i);
                            metadatas.add(i, metadata);
                            break;
                        }
                    }
                    realm.copyToRealmOrUpdate(message);

                }
            });
        }

        NotificationsDb notificationsDb = new NotificationsDb(context);
        notificationsDb.setMessageNotificationWatched(messageID);
    }

    public void setMessageWatched(final long id) {

        try(Realm realmInstance = Realm.getDefaultInstance()) {
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ChatMessageRest chatMessageRest = realm.where(ChatMessageRest.class)
                            .equalTo("id", id).findFirst();
                    if (chatMessageRest != null) {
                        chatMessageRest.setWatched(true);
                    }
                }
            });
        }
    }
}
