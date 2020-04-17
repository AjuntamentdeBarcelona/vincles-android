package cat.bcn.vincles.mobile.UI.ContentDetail;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGalleryContentsRequest;

public class ContentDetailMainPresenter implements ContentDetailMainPresenterContract, GetGalleryContentsRequest.OnResponse {

    ContentDetailView contentDetailView;
    ContentDetailAugmentedView contentDetailAugmentedView;

    GalleryDb galleryDb;
    UsersDb usersDb;
    UserPreferences userPreferences;
    String filterKind;
    BaseRequest.RenewTokenFailed listener;

    Boolean isLoadingMore = false;
    Boolean noMoreContent = false;

    boolean isAugmented = false;


    public ContentDetailMainPresenter(BaseRequest.RenewTokenFailed listener, ContentDetailView contentDetailView, GalleryDb galleryDb,
                                       UsersDb usersDb, UserPreferences userPreferences, String filterKind) {
        isAugmented = false;
        this.listener = listener;
        this.contentDetailView = contentDetailView;
        this.galleryDb = galleryDb;
        this.usersDb = usersDb;
        this.filterKind = filterKind;
        this.userPreferences = userPreferences;

    }

    public ContentDetailMainPresenter( ContentDetailAugmentedView contentDetailAugmentedView, GalleryDb galleryDb,
                                      UsersDb usersDb, UserPreferences userPreferences, String filterKind) {
        isAugmented = true;
       // this.listener = (BaseRequest.RenewTokenFailed) getActivity();
        this.contentDetailAugmentedView = contentDetailAugmentedView;
        this.galleryDb = galleryDb;
        this.usersDb = usersDb;
        this.filterKind = filterKind;
        this.userPreferences = userPreferences;

    }




    @Override
    public void getContent(long to) {

            if (isLoadingMore)return;
            if (noMoreContent)return;
            isLoadingMore = true;
            String accessToken = userPreferences.getAccessToken();
            GetGalleryContentsRequest getContentRequest = new GetGalleryContentsRequest(listener, to);
            getContentRequest.addOnOnResponse(this);
            getContentRequest.doRequest(accessToken);

    }

    @Override
    public void onResponseGetGalleryContentsRequest(ArrayList<GalleryContentRealm> galleryContentList) {
        isLoadingMore = false;

        if (galleryContentList == null)return;
        if (galleryContentList.size() == 0)return;
        if (galleryContentList.size() != 10) noMoreContent = true;

        saveNewGalleryContent(galleryContentList);

    }

    private void saveNewGalleryContent(List<GalleryContentRealm> galleryContentList) {
        galleryDb.insertMultipleContent(galleryContentList);
        /*AsyncTaskSaveGalleryContent asyncTask=new AsyncTaskSaveGalleryContent();
        asyncTask.execute(galleryContentList);*/
        if (!isAugmented){
            contentDetailView.contentAdded();
        }
        else{
            contentDetailAugmentedView.contentAdded();

        }
        contentDetailView.isLoadingMore(false);
    }


    @Override
    public void onFailureGetGalleryContentsRequest(Object error) {
        isLoadingMore = false;
    }

    /*private class AsyncTaskSaveGalleryContent extends AsyncTask<List<GalleryContent>, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @SafeVarargs
        @Override
        protected final Boolean doInBackground(List<GalleryContent>... galleryContentList) {

         //   galleryDb.insertMultipleContent(galleryContentList[0]);
            for (GalleryContent galleryContent :galleryContentList[0]) {
                if (!galleryDb.existsContentById(galleryContent.getId())) {

                    Realm realm = Realm.getDefaultInstance();

                    galleryDb.insertContent(new GalleryContentRealm(galleryContent.getId(),
                            galleryContent.getIdContent(), galleryContent.getMimeType(),
                            galleryContent.getUser().getId(), galleryContent.getInclusionTime()));

                    GetUser getUserRealm = realm.where(GetUser.class).equalTo("id", galleryContent.getUser().getId()).findFirst();

                    if(getUserRealm == null){

                        usersDb.saveGetUser(galleryContent.getUser(), true);
                    }
                }

            }

            return true;
        }
        @Override
        protected void onPostExecute(Boolean bitmap) {
            super.onPostExecute(bitmap);
            if (!isAugmented){
       //         contentDetailView.contentAdded();
            }
            else{
                contentDetailAugmentedView.contentAdded();

            }
            contentDetailView.isLoadingMore(false);

        }


    }*/
}
