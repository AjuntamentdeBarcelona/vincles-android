package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import io.realm.RealmResults;

public class ContentDetailAugmentedPagerAdapter extends FragmentStatePagerAdapter {

    private RealmResults<GalleryContentRealm> galleryContentsRealm;
    ContentDetailAugmentedPagerFragment contentDetailPagerFragment;

    public ContentDetailAugmentedPagerAdapter(FragmentManager fragmentManager,
                                     RealmResults<GalleryContentRealm> galleryContentsRealm) {
        super(fragmentManager);
        this.galleryContentsRealm = galleryContentsRealm;
    }


    @Override
    public int getCount() {
        return galleryContentsRealm.size();
    }


    @Override
    public Fragment getItem(int position) {
        GalleryContentRealm galleryContent = galleryContentsRealm.get(position);
        contentDetailPagerFragment = ContentDetailAugmentedPagerFragment.newInstance(galleryContent.getPath(),
                galleryContent.getMimeType(), position, galleryContent.getIdContent());
        return contentDetailPagerFragment;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;

    }


}
