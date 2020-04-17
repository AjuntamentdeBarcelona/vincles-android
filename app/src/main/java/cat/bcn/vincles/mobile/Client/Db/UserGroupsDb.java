package cat.bcn.vincles.mobile.Client.Db;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.UserGroupRealm;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.UserGroup;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class UserGroupsDb extends BaseDb {

    Context context;

    public UserGroupsDb(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void dropTable() {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(UserGroupRealm.class).findAll().deleteAllFromRealm();
                    realm.where(GroupRealm.class).findAll().deleteAllFromRealm();
                    realm.where(Dynamizer.class).findAll().deleteAllFromRealm();

                }});
        }
    }

    public boolean checkIfDynamizerShouldBeShown(int dynamizerId) {
        try(Realm realmInstance = Realm.getDefaultInstance()) {
            RealmResults<GroupRealm> groupRealmRealmResults = realmInstance.where(GroupRealm.class)
                    .equalTo("shouldShow", true)
                    .equalTo("idDynamizer", dynamizerId)
                    .findAll();
            return groupRealmRealmResults != null && groupRealmRealmResults.size() != 0;
        }
    }

    public void deleteGroup(final int groupID) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GroupRealm groupRealm = realm.where(GroupRealm.class)
                            .equalTo("id", groupID)
                            .findFirst();
                    if (groupRealm != null) {
                        groupRealm.setShouldShow(false);
                    }
                }});
        }
    }


    public void setGroupLastAccess(final int chatId, final long lastAccess) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GroupRealm groupRealm = realm.where(GroupRealm.class)
                            .equalTo("idChat", chatId)
                            .findFirst();
                    if (groupRealm != null) {
                        groupRealm.setLastAccess(lastAccess);
                    }
                    else{
                        Dynamizer dynamizer = realm.where(Dynamizer.class).equalTo("idChat", chatId).findFirst();
                        if (dynamizer!=null){
                            dynamizer.setLastAccess(lastAccess);
                        }
                    }

                }});
        }

    }

    public void saveCurrentUsersGroups(final List<UserGroup> userGroupList) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (UserGroup userGroup : userGroupList) {
                        UserGroupRealm userGroupRealm = realm.where(UserGroupRealm.class).equalTo("idDynamizerChat", userGroup.getIdDynamizerChat()).findFirst();
                        if (userGroupRealm != null) {
                            userGroupRealm.deleteFromRealm();
                        }
                        userGroupRealm = new UserGroupRealm(userGroup.getIdDynamizerChat(), userGroup.getGroup().getIdGroup());
                        realm.copyToRealm(userGroupRealm);


                        GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", userGroup.getGroup().getIdGroup()).findFirst();
                        String groupPhoto =  groupRealm != null && groupRealm.getPhoto() != null ? groupRealm.getPhoto() : "";
                        if (groupRealm == null) {
                            groupRealm = new GroupRealm(userGroup.getGroup().getIdGroup(), userGroup.getGroup().getName(), userGroup.getGroup().getTopic(), userGroup.getGroup().getDescription(),
                                    groupPhoto, userGroup.getGroup().getDynamizer().getId(), userGroup.getGroup().getIdChat());
                        } else {
                            groupRealm.setName(userGroup.getGroup().getName());
                            groupRealm.setDescription(userGroup.getGroup().getDescription());
                            groupRealm.setIdDynamizer(userGroup.getGroup().getDynamizer().getId());
                            groupRealm.setIdChat(userGroup.getGroup().getIdChat());
                        }

                        realm.copyToRealmOrUpdate(groupRealm);

                        Dynamizer dynamizerRealm = realm.where(Dynamizer.class).equalTo("id", userGroup.getGroup().getDynamizer().getId()).findFirst();
                        Dynamizer dynamizer = userGroup.getGroup().getDynamizer();
                        if (dynamizerRealm != null && dynamizerRealm.getIdContentPhoto() == dynamizer.getIdContentPhoto()) {
                            dynamizer.setPhoto(dynamizerRealm.getPhoto());
                        }
                        dynamizer.setIdChat(userGroup.getIdDynamizerChat());
            /*if (dynamizerRealm != null) {
                dynamizerRealm.deleteFromRealm();
            }*/
                        realm.copyToRealmOrUpdate(dynamizer);
                    }

                }});
        }
    }

    public void addOrUpdateUserGroup(final UserGroup userGroup) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    UserGroupRealm userGroupRealm = realm.where(UserGroupRealm.class).equalTo(
                            "idGroup", userGroup.getGroup().getId()).findFirst();
                    if (userGroupRealm != null) {
                        if (userGroupRealm.getIdDynamizerChat() != userGroup.getIdDynamizerChat()) {
                            userGroupRealm.setIdDynamizerChat(userGroup.getIdDynamizerChat());
                        }
                    } else {
                        userGroupRealm = new UserGroupRealm(userGroup.getIdDynamizerChat(), userGroup.getGroup().getIdGroup());
                        realm.copyToRealm(userGroupRealm);
                    }
                    //add or modify group
                    GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", userGroup.getGroup().getIdGroup()).findFirst();
                    String groupPhoto =  groupRealm != null && groupRealm.getPhoto() != null ? groupRealm.getPhoto() : "";
                    RealmList<Integer> userIds =  groupRealm != null ? groupRealm.getUsers() : null;

                    groupRealm = new GroupRealm(userGroup.getGroup().getIdGroup(), userGroup.getGroup().getName(), userGroup.getGroup().getTopic(), userGroup.getGroup().getDescription(),
                            groupPhoto, userGroup.getGroup().getDynamizer().getId(), userGroup.getGroup().getIdChat());
                    groupRealm.setUsers(userIds);
                    realm.copyToRealmOrUpdate(groupRealm);

                    Dynamizer dynamizerRealm = realm.where(Dynamizer.class).equalTo("id", userGroup.getGroup().getDynamizer().getId()).findFirst();
                    Dynamizer dynamizer = userGroup.getGroup().getDynamizer();
                    if (dynamizerRealm != null && dynamizerRealm.getIdContentPhoto() == dynamizer.getIdContentPhoto()) {
                        dynamizer.setPhoto(dynamizerRealm.getPhoto());
                    }
                    dynamizer.setIdChat(userGroup.getIdDynamizerChat());
                    realm.copyToRealmOrUpdate(dynamizer);

                }});
        }
    }

    public void setShouldShowDynamizer(final int dynamizerId, final boolean shouldShow) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Dynamizer dynamizer = realm.where(Dynamizer.class).equalTo("id", dynamizerId).findFirst();
                    if (dynamizer!= null){
                        dynamizer.setShouldShow(shouldShow);
                    }
                }});
        }
    }


    public ArrayList<Dynamizer> findAllDynamizer() {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            RealmResults<Dynamizer> dynamizerRealmResults = realmInstance.where(Dynamizer.class)
                    .equalTo("shouldShow", true)
                    .findAll();

            ArrayList<Dynamizer> dinams = new ArrayList<>();

            RealmResults<GroupRealm> groupRealmRealmResults = realmInstance.where(GroupRealm.class)
                    .equalTo("shouldShow", true)
                    .findAll();
            for(Dynamizer dinam: dynamizerRealmResults){
                boolean add = false;
                for (GroupRealm group : groupRealmRealmResults) {
                    if (group.getIdDynamizer() == dinam.getId()){
                        add = true;
                    }
                }
                if(add){
                    dinams.add(realmInstance.copyFromRealm(dinam));
                }
            }

            return dinams;
        }

    }

    // Closed from fragment
    public Dynamizer findDynamizer(int id, Realm realm) {
        return realm.where(Dynamizer.class).equalTo("id", id).findFirst();
    }

    public Dynamizer findDynamizerUnmanaged(int id) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            Dynamizer dynamizer = realmInstance.where(Dynamizer.class).equalTo("id", id).findFirst();

            if (dynamizer == null) return null;
            return realmInstance.copyFromRealm(dynamizer);
        }
    }

    // Closed from fragment
    public Dynamizer findDynamizerFromChatId(int chatId, Realm realm) {
        return realm.where(Dynamizer.class).equalTo("idChat", chatId).findFirst();
    }

    public Dynamizer findDynamizerFromChatIdUnmanaged(int chatId) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            Dynamizer dynamizer = realmInstance.where(Dynamizer.class).equalTo("idChat", chatId).findFirst();

            if (dynamizer == null) return null;
            return realmInstance.copyFromRealm(dynamizer);
        }
    }


    public ArrayList<GroupRealm> findAllGroupRealm() {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            RealmResults<GroupRealm> groupRealmRealmResults = realmInstance.where(GroupRealm.class)
                    .equalTo("shouldShow", true)
                    .findAll();
            if (groupRealmRealmResults==null)return new ArrayList<>();
            return new ArrayList<>(realmInstance.copyFromRealm(groupRealmRealmResults));
        }

    }

    public void setUserGroupAvatarPath (final int groupID, final String path) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GroupRealm groupRealm = realm.where(GroupRealm.class)
                            .equalTo("id", groupID)
                            .findFirst();
                    if (groupRealm != null) {
                        groupRealm.setPhoto(path);
                    }
                }
            });
        }
    }

    public void setUserGroupUsersList(final int groupID, final ArrayList<Integer> userIDs) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GroupRealm groupRealm = realm.where(GroupRealm.class)
                            .equalTo("id", groupID)
                            .findFirst();
                    if (groupRealm != null) {
                        groupRealm.setUsers(new RealmList<Integer>(userIDs.toArray(new Integer[0])));
                    }
                }
            });
        }

    }

    public void addUserToGroupList(final int groupID, final int userId) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GroupRealm groupRealm = realm.where(GroupRealm.class)
                            .equalTo("id", groupID)
                            .findFirst();
                    if (groupRealm != null) {
                        ArrayList<Integer> userIDs = new ArrayList<>(groupRealm.getUsers());
                        if (!userIDs.contains(userId)) {
                            userIDs.add(userId);
                            groupRealm.setUsers(OtherUtils.convertIntegersToRealmList(userIDs));
                        }
                    }
                }
            });
        }
    }

    /**
     * Remove user and return whether it existed
     *
     * @param groupID
     * @param userId
     * @return          whether it existed
     */
    public void removeUserFromGroupList(final int groupID, final int userId) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GroupRealm groupRealm = realm.where(GroupRealm.class)
                            .equalTo("id", groupID)
                            .findFirst();
                    if (groupRealm != null) {
                        RealmList<Integer> userIDs = groupRealm.getUsers();
                        if (userIDs != null && userIDs.contains(userId)) {
                            userIDs.remove((Integer)userId);
                        }
                    }
                }
            });
        }
    }

    //Closed from fragment
    public GroupRealm getGroupFromIdChat(int idChat, Realm realm) {
        return realm.where(GroupRealm.class)
                .equalTo("idChat", idChat)
                .findFirst();
    }

    public GroupRealm getGroupFromIdChatUnmanaged(int idChat) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            GroupRealm groupRealm = realmInstance.where(GroupRealm.class)
                    .equalTo("idChat", idChat)
                    .findFirst();
            if (groupRealm == null)return null;
            return realmInstance.copyFromRealm(groupRealm);
        }
    }

    public GroupRealm getGroupUnmanaged(int idGroup){
        try(Realm realmInstance = Realm.getDefaultInstance()){
            GroupRealm groupRealm = realmInstance.where(GroupRealm.class)
                    .equalTo("id", idGroup)
                    .findFirst();
            if (groupRealm == null)return null;
            return realmInstance.copyFromRealm(groupRealm);
        }
    }


    public String getUserGroupAvatarPath (int groupID) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            GroupRealm groupRealm = realmInstance.where(GroupRealm.class)
                    .equalTo("id", groupID)
                    .findFirst();
            if (groupRealm == null)return null;
            return realmInstance.copyFromRealm(groupRealm).getPhoto();

        }

    }

    public void setGroupDynamizerAvatarPath (final int dynID, final String path) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Dynamizer dynamizer = realm.where(Dynamizer.class)
                            .equalTo("id", dynID)
                            .findFirst();

                    if (dynamizer != null) {
                        dynamizer.setPhoto(path);
                    }

                }
            });
        }
    }

    public String getGroupDynamizerAvatarPath (final int dynID) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
                    Dynamizer dynamizer = realmInstance.where(Dynamizer.class)
                            .equalTo("id", dynID)
                            .findFirst();
                    if (dynamizer == null) return null;
                    return realmInstance.copyFromRealm(dynamizer).getPhoto();
        }
    }


    public void setMessagesInfo(final int chatId, final int unreadMessages, final int interactions) {

        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GroupRealm groupRealm = realm.where(GroupRealm.class)
                            .equalTo("idChat", chatId)
                            .findFirst();
                    if (groupRealm != null) {
                        groupRealm.setNumberUnreadMessages(unreadMessages);
                        groupRealm.setNumberInteractions(interactions);
                    }
                }
            });
        }
    }

    public void setDynamizerMessagesInfo(final int chatId, final int unreadMessages, final int interactions) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Dynamizer dynamizer = realm.where(Dynamizer.class)
                            .equalTo("idChat", chatId)
                            .findFirst();

                    if (dynamizer != null) {
                        dynamizer.setNumberUnreadMessages(unreadMessages);
                        dynamizer.setNumberInteractions(interactions);
                    }

                }
            });
        }
    }
}
