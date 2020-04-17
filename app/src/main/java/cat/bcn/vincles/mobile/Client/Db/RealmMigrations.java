package cat.bcn.vincles.mobile.Client.Db;

import android.util.Log;

import java.util.Objects;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;

public class RealmMigrations implements RealmMigration {

    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();

        final RealmObjectSchema meetingRealmSchema = schema.get("MeetingRealm");

        if(meetingRealmSchema != null && !meetingRealmSchema.hasField("alertShown")){
            meetingRealmSchema.addField("alertShown", boolean.class);
        }

        final RealmObjectSchema mgalleryRealmSchema = schema.get("GalleryContentRealm");

        if(mgalleryRealmSchema != null && !mgalleryRealmSchema.hasField("thumbnailPath")){
            mgalleryRealmSchema.addField("thumbnailPath", String.class);
        }

        final RealmObjectSchema mChatRealmSchema = schema.get("ChatMessage");

        if(mChatRealmSchema != null && !mChatRealmSchema.hasField("notificationId")){
            mChatRealmSchema.addField("notificationId", Integer.class);
        }

        final RealmObjectSchema mChatMessageRestRealmSchema = schema.get("ChatMessageRest");

        if(mChatMessageRestRealmSchema != null && mChatMessageRestRealmSchema.hasField("notificationId")){
            mChatMessageRestRealmSchema.removeField("notificationId");
        }

       final RealmObjectSchema mGalleryContentRealmSchema = schema.get("GalleryContentRealm");
        final RealmObjectSchema mGetUserRealmSchema = schema.get("GetUser");

        RealmResults<DynamicRealmObject> projectResults = realm.where("GalleryContentRealm").findAll();

        if (!Objects.requireNonNull(mGalleryContentRealmSchema).hasField("userCreator")){
            mGalleryContentRealmSchema.addRealmObjectField("userCreator", mGetUserRealmSchema);
        }

        if (!Objects.requireNonNull(mGalleryContentRealmSchema).hasPrimaryKey()){
            mGalleryContentRealmSchema.addPrimaryKey("id");
        }

        mGalleryContentRealmSchema.transform(new RealmObjectSchema.Function() {
            @Override
            public void apply(DynamicRealmObject obj) {
                int userId = obj.getInt("userId");
                // If the userCreator field of the GalleryContentRealm object is null, try to set it
                if (obj.get("userCreator") == null) {
                    DynamicRealmObject creatorUser = realm.where("GetUser").equalTo("id", userId).findFirst();
//                    Log.d("realmMigration", String.format("GalleryContentRealm.id=%s GalleryContentRealm.userId=%s GalleryContentRealm.userCreator=%s",
//                            obj.get("id"),
//                            obj.get("userId"),
//                            obj.get("userCreator") == null ? "null" : "not null"));
                    // Set the userCreator to the corresponding user object in the GetUser table (if it exists)
                    if (creatorUser != null) {
                        obj.set("userCreator", creatorUser);
                    }
                }
            }
        });


        //ADD host and guests as users
        if (meetingRealmSchema!=null){
            final RealmObjectSchema getUserShema = schema.get("GetUser");

            if (!meetingRealmSchema.hasField("host")){
                if (getUserShema!=null)
                meetingRealmSchema.addRealmObjectField("host", getUserShema);
            }
            if (!meetingRealmSchema.hasField("guests")){
                if (getUserShema!=null)
                meetingRealmSchema.addRealmListField("guests", getUserShema);
            }
        }
    }
}