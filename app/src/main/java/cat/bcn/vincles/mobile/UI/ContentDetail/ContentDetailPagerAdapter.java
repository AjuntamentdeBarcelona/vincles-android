package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import io.realm.RealmResults;

public class ContentDetailPagerAdapter extends FragmentStatePagerAdapter implements ContentDetailPagerFragment.ViewRequest{

    private RealmResults<GalleryContentRealm> galleryContentsRealm;
    ContentDetailPagerFragment contentDetailPagerFragment;
    private ContentDetailPagerFragment.ViewRequest viewListener;

    public ContentDetailPagerAdapter(FragmentManager fragmentManager,
                                     RealmResults<GalleryContentRealm> galleryContentsRealm,
                                      ContentDetailPagerFragment.ViewRequest viewListener) {
        super(fragmentManager);
        this.galleryContentsRealm = galleryContentsRealm;
        this.viewListener = viewListener;
    }


    @Override
    public int getCount() {
        return galleryContentsRealm.size();
    }


    @Override
    public Fragment getItem(int position) {
        GalleryContentRealm galleryContent = galleryContentsRealm.get(position);

        contentDetailPagerFragment = ContentDetailPagerFragment.newInstance(galleryContent.getPath(),
                galleryContent.getMimeType(), position, galleryContent.getIdContent());
        return contentDetailPagerFragment;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object item = super.instantiateItem(container, position);
        if (item instanceof ContentDetailPagerFragment) {
            // Set the ViewListener for this fragment. Done here (and not in getItem()) because during
            // configuration changes all the fragments in the PagerAdapter are discarded and recreated
            // when needed based on their state.
            ((ContentDetailPagerFragment)item).setViewListener(this);
        }
        return item;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;

    }

    @Override
    public void onViewRequest(int position) {
        if (viewListener != null) viewListener.onViewRequest(position);
    }
}
