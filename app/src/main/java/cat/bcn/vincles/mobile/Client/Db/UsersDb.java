package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.CircleRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.CircleUserRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.UserCircleRealm;
import cat.bcn.vincles.mobile.Client.Model.CircleUser;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.Serializers.AddUser;
import cat.bcn.vincles.mobile.Client.Model.UserCircle;
import io.realm.Realm;
import io.realm.RealmResults;

public class UsersDb extends BaseDb {

    public UsersDb(Context context) {
        super(context);
    }

    @Override
    public void dropTable() {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(GetUser.class).findAll().deleteAllFromRealm();
                    realm.where(CircleUserRealm.class).findAll().deleteAllFromRealm();
                    realm.where(UserCircleRealm.class).findAll().deleteAllFromRealm();
                    realm.where(CircleRealm.class).findAll().deleteAllFromRealm();

                }
            });
        }
    }

    //Realm Instance closed on Fragment
    public GetUser findUser(int id, Realm realm) {
        return realm.where(GetUser.class).equalTo("id", id).findFirst();
    }

    public GetUser findUserUnmanaged(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            GetUser getUser = realm.where(GetUser.class).equalTo("id", id).findFirst();
            if (getUser==null)return null;
            return realm.copyFromRealm(getUser);
        }
    }

    public Dynamizer findDynamizerUnmanaged(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            Dynamizer dinam = realm.where(Dynamizer.class).equalTo("id", id).findFirst();
            if (dinam==null)return null;
            return realm.copyFromRealm(dinam);
        }
    }

    public ArrayList<GetUser> findUsersUnmanaged(ArrayList<Integer> contactIds){
        Integer[] contactsIds = (Integer[]) contactIds.toArray(new Integer[contactIds.size()]);
        try (Realm realm = Realm.getDefaultInstance()) {
            List<GetUser> getUserList = realm.where(GetUser.class).in("id", contactsIds).findAll();
            if (getUserList==null)return null;
            return new ArrayList<GetUser>(realm.copyFromRealm(getUserList));
        }
    }

    public boolean userCircleORCircleUserAreNOTnull(int id){

        try (Realm realm = Realm.getDefaultInstance()) {
            UserCircleRealm userCircleRealm = realm.where(UserCircleRealm.class).equalTo("circleRealm.userId", id).findFirst();
            CircleUserRealm circleUserRealm = realm.where(CircleUserRealm.class).equalTo("userId", id).findFirst();
            return userCircleRealm != null || circleUserRealm != null;
        }
    }

    public void deleteUserCircle(final int id) {

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    UserCircleRealm userCircleRealm = realm.where(UserCircleRealm.class).equalTo("circleRealm.id", id).findFirst();
                    if (userCircleRealm == null) {
                        return;
                    }
                    userCircleRealm.deleteFromRealm();
                }
            });
        }
    }

    public void deleteCircleUser(final int id) {

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    CircleUserRealm circleUserRealm = realm.where(CircleUserRealm.class).equalTo("userId", id).findFirst();
                    if (circleUserRealm == null) {
                        return;
                    }
                    circleUserRealm.deleteFromRealm();
                }
            });
        }
    }


    public void deleteUserCircleIfExists(final int id) {

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<UserCircleRealm> userCircle = realm.where(UserCircleRealm.class)
                            .equalTo( "circleRealm.userId", id).findAll();
                    userCircle.deleteAllFromRealm();

                    RealmResults<CircleUserRealm> circleUser = realm.where(CircleUserRealm.class)
                            .equalTo( "userId", id).findAll();
                    circleUser.deleteAllFromRealm();
                }
            });
        }
    }

    public ArrayList<GetUser> findAllCircleUser() { //user is vincles
        try (Realm realm = Realm.getDefaultInstance()) {
            ArrayList<GetUser> getUserArrayList = new ArrayList<>();
            RealmResults<CircleUserRealm> circleUserRealmResults = realm.where(CircleUserRealm.class)
                    .findAll();
            if (circleUserRealmResults==null)return new ArrayList<>();
            List<CircleUserRealm> circleUserRealmArrayList = realm.copyFromRealm(circleUserRealmResults);
            GetUser getUser;
            for (CircleUserRealm circleUserRealm : circleUserRealmArrayList) {
                getUser = realm.where(GetUser.class).equalTo("id", circleUserRealm.getUserId()).findFirst();
                if(getUser != null){
                    GetUser getUserUmanaged = realm.copyFromRealm(getUser);
                    safeAddUserToList(getUserArrayList, getUserUmanaged);
                }

            }
            return getUserArrayList;
        }
    }

    public ArrayList<GetUser> findAllUserCircle() {
        try (Realm realm = Realm.getDefaultInstance()) {
            ArrayList<GetUser> getUserArrayList = new ArrayList<>();
            RealmResults<UserCircleRealm> userCircleRealmRealmResults = realm.where(UserCircleRealm.class)
                    .findAll();
            if (userCircleRealmRealmResults==null)return new ArrayList<>();
            List<UserCircleRealm> userCircleRealmList = realm.copyFromRealm(userCircleRealmRealmResults);

            GetUser getUser;
            for (UserCircleRealm userCircleRealm : userCircleRealmList) {
                getUser = realm.where(GetUser.class).equalTo("id", userCircleRealm.getCircleRealm().getUserId()).findFirst();
                if (getUser != null){
                    GetUser getUserUmanaged = realm.copyFromRealm(getUser);
                    safeAddUserToList(getUserArrayList, getUserUmanaged);
                }
            }
            return getUserArrayList;
        }
    }

    private void safeAddUserToList(ArrayList<GetUser> list, GetUser user) {
        if (list != null && user != null && !list.contains(user))
            list.add(user);
    }


    public void saveGetUserIfNotExists(final GetUser getUser) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GetUser getUserRealm = realm.where(GetUser.class).equalTo("id", getUser.getId()).findFirst();
                    if (getUserRealm == null) {
                        getUser.setIdCircle(-1);
                        Log.d("saveUser", "saveGetUserIfNotExists: " + getUser.getId() + " - " + getUser.getIdCircle());
                        realm.copyToRealmOrUpdate(getUser);
                    }

                }});
        }
    }
    public void saveGetUserListIfNotExists(final List<GetUser> getUserList) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (GetUser user : getUserList){
                        GetUser getUserRealm = realm.where(GetUser.class).equalTo("id", user.getId()).findFirst();
                        if (getUserRealm==null){
                            if(user.getIdCircle()==null)user.setIdCircle(-1);
                            Log.d("saveUser", "saveGetUserListIfNotExists: " + user.getId() + " - " + user.getIdCircle());
                            realm.copyToRealmOrUpdate(user);
                        }
                    }
                }});
        }
    }

    public void saveGetUser(final GetUser getUser, boolean beginTransaction) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GetUser getUserRealm = realm.where(GetUser.class).equalTo("id", getUser.getId()).findFirst();
                    if (getUserRealm != null) {
                        getUser.setPhoto(getUserRealm.getPhoto());
                        getUser.setNumberUnreadMessages(getUserRealm.getNumberUnreadMessages());
                        getUser.setLastInteraction(getUserRealm.getLastInteraction());
                        getUser.setIdCircle(getUserRealm.getIdCircle());
                    }
                    if(getUser.getIdCircle()==null)getUser.setIdCircle(-1);
                    Log.d("saveUser", "saveGetUser: " + getUser.getId() + " - " + getUser.getIdCircle());
                    realm.copyToRealmOrUpdate(getUser);

                }});
        }
    }

    public void updateGetUserWithPublicInfo(final GetUser publicInfoGetUser) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GetUser localGetUser = realm.where(GetUser.class).equalTo("id", publicInfoGetUser.getId()).findFirst();
                    if (localGetUser != null){
                        localGetUser.setName(publicInfoGetUser.getName());
                        localGetUser.setLastname(publicInfoGetUser.getLastname());
                        localGetUser.setAlias(publicInfoGetUser.getAlias());
                        localGetUser.setGender(publicInfoGetUser.getGender());
                        if (localGetUser.getIdContentPhoto() != null && !localGetUser.getIdContentPhoto().equals(publicInfoGetUser.getIdContentPhoto())) {
                            localGetUser.setIdContentPhoto(localGetUser.getIdContentPhoto());
                            localGetUser.setPhoto("");
                        }
                        localGetUser.setIdCircle(publicInfoGetUser.getId());
                        if(localGetUser.getIdCircle()==null)localGetUser.setIdCircle(-1);
                        Log.d("saveUser", "updateGetUserWithPublicInfo, localGetUser: "  + localGetUser.getId() + " - " + localGetUser.getIdCircle());
                        realm.copyToRealmOrUpdate(localGetUser);
                    }
                    else{
                        Dynamizer dinam = realm.where(Dynamizer.class).equalTo("id", publicInfoGetUser.getId()).findFirst();
                        if (dinam != null/* && getUserRealm.getIdContentPhoto() == getUser.getIdContentPhoto()*/) {
                            dinam.setName(publicInfoGetUser.getName());
                            dinam.setLastname(publicInfoGetUser.getLastname());
                            dinam.setAlias(publicInfoGetUser.getAlias());
                            dinam.setGender(publicInfoGetUser.getGender());
                            if (dinam.getIdContentPhoto() != publicInfoGetUser.getIdContentPhoto()) {
                                dinam.setIdContentPhoto(dinam.getIdContentPhoto());
                                dinam.setPhoto("");
                            }
                            realm.copyToRealmOrUpdate(dinam);
                        }
                    }
                }});
        }
    }

    public void updateUserName(final int userId, final String userName, final String lastname) {

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GetUser user = realm.where(GetUser.class).equalTo("id", userId).findFirst();
                    if (user != null) {
                        user.setName(userName);
                        user.setLastname(lastname);
                        realm.copyToRealmOrUpdate(user);
                    }
                }});
        }
    }

    public void addUser(final AddUser addUser) {

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    CircleUserRealm circleUser = new CircleUserRealm(addUser.getRelationship(),
                            addUser.getUserVincles().getId());

                    realm.copyToRealm(circleUser);
                    realm.copyToRealmOrUpdate(addUser.getUserVincles());

                }
            });
        }
    }

    public void saveCircleUsers(final List<CircleUser> circleUserList) {

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (CircleUser circleUser : circleUserList) {
                        CircleUserRealm circleUserRealm = realm.where(CircleUserRealm.class).equalTo("userId", circleUser.getUser().getId()).findFirst();
                        if (circleUserRealm != null) {
                            circleUserRealm.deleteFromRealm();
                        }
                        circleUserRealm = new CircleUserRealm(circleUser.getRelationship(), circleUser.getUser().getId());
                        realm.copyToRealm(circleUserRealm);

                        GetUser getUser = circleUser.getUser();
                        GetUser getUserRealm = realm.where(GetUser.class).equalTo("id", getUser.getId()).findFirst();
                        if (getUserRealm != null/* && getUserRealm.getIdContentPhoto() == getUser.getIdContentPhoto()*/) {
                            getUser.setPhoto(getUserRealm.getPhoto());
                            getUser.setNumberUnreadMessages(getUserRealm.getNumberUnreadMessages());
                            getUser.setLastInteraction(getUserRealm.getLastInteraction());
                        }
                        getUser.setIdCircle(circleUser.getUser().getIdCircle());
                        if (getUser.getIdCircle()==null)getUser.setIdCircle(-1);
                        Log.d("saveUser", "saveCircleUsers: " + getUser.getId() + " - " + getUser.getIdCircle());

                        realm.copyToRealmOrUpdate(getUser);
                    }

                }});
        }
    }

    public void saveUserCircles(final List<UserCircle> userCircleList) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (UserCircle userCircle : userCircleList) {
                        CircleRealm circleRealm = new CircleRealm(userCircle.getCircle().getId(), userCircle.getCircle().getUser().getId());

                        UserCircleRealm userCircleRealm = realm.where(UserCircleRealm.class).equalTo("circleRealm.userId", circleRealm.getId()).findFirst();
                        if (userCircleRealm != null) {
                            userCircleRealm.deleteFromRealm();
                        }
                        userCircleRealm = new UserCircleRealm(userCircle.getRelationship(), circleRealm);
                        realm.copyToRealm(userCircleRealm);

                        GetUser getUser = userCircle.getCircle().getUser();
                        GetUser getUserRealm = realm.where(GetUser.class).equalTo("id", getUser.getId()).findFirst();
                        if (getUserRealm != null/* && getUserRealm.getIdContentPhoto() == getUser.getIdContentPhoto()*/) {
                            getUser.setPhoto(getUserRealm.getPhoto());
                            getUser.setNumberUnreadMessages(getUserRealm.getNumberUnreadMessages());
                            getUser.setLastInteraction(getUserRealm.getLastInteraction());
                        }
                        getUser.setIdCircle(userCircle.getCircle().getId());
                        if (getUser.getIdCircle()==null)getUser.setIdCircle(-1);
                        Log.d("saveUser", "saveUserCircles: " + getUser.getId() + " - " + getUser.getIdCircle());

                        realm.copyToRealmOrUpdate(getUser);
                    }
                }
            });
        }
    }

    public void setPathAvatarToUser(final int userID, final String path) {

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GetUser user = realm.where(GetUser.class)
                            .equalTo("id", userID)
                            .findFirst();
                    if (user != null) {
                        user.setPhoto(path);
                    }
                }
            });
        }
    }

    public String getUserAvatarPath(int userID) {
        try(Realm realm = Realm.getDefaultInstance()){
            GetUser user = realm.where(GetUser.class)
                    .equalTo("id", userID)
                    .findFirst();
            if (user == null) return null;
            GetUser userUnmanaged = realm.copyFromRealm(user);
            return userUnmanaged.getPhoto();
        }
    }

    public void setMessagesInfo(final int userId, final int unreadMessages, final int unreadMissedCalls, final long lastInteraction) {
        try(Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GetUser user = realm.where(GetUser.class)
                            .equalTo("id", userId)
                            .findFirst();
                    if (user != null) {
                        user.setNumberUnreadMessages(unreadMessages+unreadMissedCalls);
                        user.setLastInteraction(lastInteraction);
                    }
                }
            });

        }
    }

}
