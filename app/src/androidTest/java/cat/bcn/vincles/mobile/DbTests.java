package cat.bcn.vincles.mobile;

public class DbTests {
/*
    private GetUser getUser() {
        return new GetUser();
    }

    @Test
    public void insertContentInGalleryDbGetBackAllTheContentInTheGalleryThatShouldContain() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        GalleryDb galleryDb = new GalleryDb(appContext);

        GetUser user = getUser();
        GalleryContentRealm galleryContent = new GalleryContentRealm(1,"image/jpeg",user,1519897391);
        String galleryContentPath = "fakepath1";
        galleryContent.setPath(galleryContentPath);

        galleryDb.insertContent(galleryContent);

        Boolean result = false;
        RealmResults<GalleryContentRealm> allGalleryContentsPath = galleryDb.findAll();
        for (int i = 0; i < allGalleryContentsPath.size() && !result; i++) {
            if (allGalleryContentsPath.get(i).equals(galleryContentPath)) {
                result = true;
            }
        }
        assertEquals(true,result);
    }

    @Test
    public void insertContentInGalleryAndGetThatSpecificContent () throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        GalleryDb galleryDb = new GalleryDb(appContext);

        GetUser user = getUser();
        int gallertContetnID = 123;
        GalleryContentRealm galleryContent = new GalleryContentRealm(gallertContetnID,"image/jpeg",user,1519897391);
        String galleryContentPath = "fakepath2";
        galleryContent.setPath(galleryContentPath);

        galleryDb.insertContent(galleryContent);

        boolean result = galleryDb.existsContentById(gallertContetnID);
        assertEquals(true,result);
    }

    @Test
    public void insertContentInGalleryAsNotDownloadedAndGetItBackAsQueryAkingForAllContentsNotDownloaded () throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        GalleryDb galleryDb = new GalleryDb(appContext);
        Realm realm = Realm.getDefaultInstance();
        GetUser user = getUser();
        int gallertContetnID = 123;
        GalleryContentRealm galleryContent = new GalleryContentRealm(gallertContetnID,"image/jpeg",user,1519897391);

        galleryDb.insertContent(galleryContent);

        RealmResults<GalleryContentRealm> contentNotDownloaded = galleryDb.findContentNotDownloaded(realm);
        boolean result = false;
        for (int i = 0; i < contentNotDownloaded.size() && !result; i++) {
            if (gallertContetnID == contentNotDownloaded.get(i).getId()) {
                result = true;
            }
        }
        realm.close();
        assertEquals(true,result);
    }

    @Test
    public void setPathToAContentAlreadySavedAndCheckThatItsBeenSettingProperly () throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        GalleryDb galleryDb = new GalleryDb(appContext);

        GetUser user = getUser();
        int gallertContetnID = 123;
        GalleryContentRealm galleryContent = new GalleryContentRealm(gallertContetnID,"image/jpeg",user,1519897391);

        galleryDb.insertContent(galleryContent);
        String fakePath = "fakePath3";
        galleryDb.setPathToFile(gallertContetnID,fakePath);
        RealmResults<GalleryContentRealm> contentPaht = galleryDb.getContentsPathByUserID();

        boolean result = false;
        for (int i = 0; i < contentPaht.size() && !result; i++) {
            if (fakePath.equals(contentPaht.get(i).getPath())) {
                result = true;
            }
        }
        assertEquals(true,result);
    }

    @Test
    public void insertContentInThaGalleryAsItWereSentByOtherUserAnGetThatContentBack () throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        int userId = 1;
        int otherUerId = 2;
        UserPreferences userPreferences = new UserPreferences(appContext);
        userPreferences.setUserID(userId);

        GetUser user = getUser();
        user.setId(otherUerId);

        GalleryDb galleryDb = new GalleryDb(appContext);
        GalleryContentRealm galleryContent = new GalleryContentRealm(123,"image/jpeg",user,1519897391);
        String fakePath = "fakePath4";
        galleryContent.setPath(fakePath);

        galleryDb.insertContent(galleryContent);

        RealmResults<GalleryContentRealm> recivedContents = galleryDb.getRecivedContentsPath();
        boolean result = false;
        for (int i = 0; i < recivedContents.size() && !result; i++) {
            if (fakePath.equals(recivedContents.get(i).getPath())) {
                result = true;
            }
        }
        assertEquals(true,result);
    }

    @Test
    public void insertContentInThaGalleryAsItWereInsertByTheUserAndDontGetThatContentBackWhenDoingQueryForRecivedContetns () throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        int userId = 1;
        int otherUerId = 2;
        UserPreferences userPreferences = new UserPreferences(appContext);
        userPreferences.setUserID(userId);

        GetUser user = getUser();
        user.setId(otherUerId);

        GalleryDb galleryDb = new GalleryDb(appContext);
        GalleryContentRealm galleryContent = new GalleryContentRealm(123,"image/jpeg",user,1519897391);
        String fakePath = "fakePath5";

        galleryDb.insertContent(galleryContent);
        galleryDb.getRecivedContentsPath();

        galleryDb.getRecivedContentsPath();
        RealmResults<GalleryContentRealm> recivedContents = galleryDb.getRecivedContentsPath();
        boolean result = false;
        for (int i = 0; i < recivedContents.size() && !result; i++) {
            if (fakePath.equals(recivedContents.get(i))) {
                result = true;
            }
        }
        assertEquals(false,result);
    }
*/
}
