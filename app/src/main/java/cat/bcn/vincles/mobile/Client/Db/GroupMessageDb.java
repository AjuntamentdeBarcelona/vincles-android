package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class GroupMessageDb extends BaseDb {
    Context context;

    public GroupMessageDb(Context context) {
        super(context);
        this.context = context;

    }

    @Override
    public void dropTable() {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(GroupMessageRest.class).findAll().deleteAllFromRealm();
                    realm.where(ChatMessageRest.class).findAll().deleteAllFromRealm();
                }
            });
        }
    }

    public GroupMessageRest findMessageUnmanaged(long id) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            GroupMessageRest groupMessageRest = realmInstance.where(GroupMessageRest.class).equalTo("id", id).findFirst();
            if (groupMessageRest==null)return null;
            return realmInstance.copyFromRealm(groupMessageRest);
        }
    }

    public ArrayList<GroupMessageRest> findAllMessagesForGroupUnmanaged(int groupId) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            RealmResults<GroupMessageRest> messagesList = realmInstance.where(GroupMessageRest.class)
                    .equalTo("idChat",groupId)
                    .sort("sendTime", Sort.DESCENDING)
                    .findAll();
            if (messagesList==null)return new ArrayList<>();
            return new ArrayList<>(realmInstance.copyFromRealm(messagesList));
        }
    }

    public void saveGroupMessageRest(final GroupMessageRest groupMessageRest) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(groupMessageRest);
                }
            });
        }
    }

    public void saveGroupMessageRestList(final List<GroupMessageRest> messages) {
        if(messages.size() == 0){
            return;
        }
        final int idChat = messages.get(0).getIdChat();
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GroupRealm group = realm.where(GroupRealm.class)
                            .equalTo("idChat", idChat)
                            .findFirst();
                    long lastAccess = 0;
                    if (group != null) {
                        lastAccess = group.getLastAccess();
                    }
                    else{
                        Dynamizer dynamizer = realm.where(Dynamizer.class).equalTo("idChat", idChat).findFirst();
                        if (dynamizer!=null)
                            lastAccess = dynamizer.getLastAccess();
                    }
                    for (GroupMessageRest messageRest : messages) {
                        messageRest.setWatched(messageRest.getSendTime() < lastAccess);
                        realm.copyToRealmOrUpdate(messageRest);
                    }
                }
            });
        }
    }


    public void setGroupMessageListWatchedTrue(final int idChat) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<GroupMessageRest> messages = realm.where(GroupMessageRest.class)
                            .equalTo("idChat",idChat)
                            .sort("sendTime", Sort.DESCENDING)
                            .findAll();
                    for (GroupMessageRest messageRest : messages) {
                        messageRest.setWatched(true);
                    }
                }
            });
        }
    }

    public void setMessageFile(int contentID, final String path, final long messageID, final String metadata) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GroupMessageRest message = realm.where(GroupMessageRest.class).equalTo("id", messageID).findFirst();
                    if (message == null) return;
                    message.setPathContent(path);
                    message.setMetadataContent(metadata);
                }
            });
        }
    }

    public int getNumberUnreadMessagesReceived(int idMe, int idOther) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            RealmResults<GroupMessageRest> groupMessageRest = realmInstance.where(GroupMessageRest.class)
                    .equalTo("idChat", idOther)
                    .equalTo("watched", false)
                    .notEqualTo("idUserSender", idMe)
                    .findAll();
            if (groupMessageRest == null) return 0;
            return groupMessageRest.size();
        }
    }

    public int getTotalNumberMessages(int idChat) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            RealmResults<GroupMessageRest> groupMessageRest = realmInstance.where(GroupMessageRest.class)
                    .equalTo("idChat",idChat)
                    .findAll();
            if (groupMessageRest == null) return 0;
            return groupMessageRest.size();
        }
    }

    public long getLastMessageFromGroup(int idChat) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            GroupMessageRest groupMessage = realmInstance.where(GroupMessageRest.class)
                    .equalTo("idChat",idChat)
                    .sort("sendTime", Sort.DESCENDING)
                    .findFirst();

            return (groupMessage == null ? 0 : groupMessage.getSendTime()) ;
        }
    }
}
