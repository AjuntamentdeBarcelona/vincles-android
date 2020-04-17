package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.github.chrisbanes.photoview.PhotoView;

import cat.bcn.vincles.mobile.Client.Business.MediaCallbacks;
import cat.bcn.vincles.mobile.Client.Business.MediaManager;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class ContentDetailPagerFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ContentDetailPagerFragment.class.getName();

    boolean isAutoDownload;
    String filePath, mimeType;
    ProgressBar progressBar;
    ImageView videoHint;
    ImageView downloadIV;
    PhotoView imageView;
    private int position;
    private int idContent;
    ViewRequest viewListener;

    public static ContentDetailPagerFragment newInstance(String filePath, String mimeType, int position, int idContent) {
        ContentDetailPagerFragment fragment = new ContentDetailPagerFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("filePath", filePath);
        args.putString("mimeType", mimeType);
        args.putInt("position", position);
        args.putInt("idContent", idContent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_gallery_detail));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        assert args != null;
        filePath = args.getString("filePath");
        mimeType = args.getString("mimeType");
        position = args.getInt("position");
        idContent = args.getInt("idContent");
        isAutoDownload = new UserPreferences(getContext()).getIsAutodownload();

        boolean isVideo = (mimeType != null && mimeType.startsWith("video"));
        View rootView = inflater.inflate(R.layout.fragment_content_detail_pager_picture, container, false);

        imageView = rootView.findViewById(R.id.imageView);
        progressBar = rootView.findViewById(R.id.progressbar);
        downloadIV = rootView.findViewById(R.id.download);
        downloadIV.setOnClickListener(this);

        addContent(false);

        if (isVideo) {
            videoHint = rootView.findViewById(R.id.video_hint);
            videoHint.setVisibility(View.VISIBLE);
        }


        return rootView;
    }

    private void addContent(boolean downloadClicked) {
        if (progressBar.getVisibility()!=View.VISIBLE){progressBar.setVisibility(View.VISIBLE);}
        downloadIV.setVisibility(View.GONE);

        MediaManager mediaManager = new MediaManager(getActivity());
        mediaManager.downloadGalleryItemDetail(filePath, idContent, mimeType, new MediaCallbacks() {
            @Override
            public void onSuccess(String response) {
                refreshPicture(response);
            }


            @Override
            public void onFailure(String response) {
                if (isAutoDownload) {
                    progressBar.setVisibility(View.VISIBLE);
                    downloadIV.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    downloadIV.setVisibility(View.VISIBLE);
                }
            }
        },downloadClicked);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView:
                if (viewListener != null) viewListener.onViewRequest(position);

            case R.id.videoView:

                if (viewListener != null) viewListener.onViewRequest(position);


                /*
                Intent intent = new Intent(getContext(), ZoomContentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("filePath", filePath);
                bundle.putString("mimeType", mimeType);
                intent.putExtras(bundle);
                startActivity(intent);
                */
                break;
            case R.id.download:
                downloadIV.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                addContent(true);
                break;
        }
    }

    public void refreshPicture(String filePath) {
        progressBar.setVisibility(View.GONE);
        downloadIV.setVisibility(View.GONE);
        ImageUtils.setImageToImageViewFromPath(filePath, imageView, getContext(), false);
        imageView.setOnClickListener(this);
    }

    public void setViewListener(ViewRequest listener) {
        this.viewListener = listener;
    }

    interface ViewRequest {
        void onViewRequest(int position);
    }



}
