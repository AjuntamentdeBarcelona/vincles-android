package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

import cat.bcn.vincles.mobile.Client.Business.MediaCallbacks;
import cat.bcn.vincles.mobile.Client.Business.MediaManager;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Gallery.ZoomContentActivity;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class ContentDetailAugmentedPagerFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ContentDetailAugmentedPagerFragment.class.getName();

    boolean isAutoDownload;
    String filePath, mimeType;
    ProgressBar progressBar;
    ImageView videoHint;
    ImageView downloadIV;
    PhotoView imageView;
    private int position;
    private DownloadRequestAugmented listener;
    private int idContent;

    public static ContentDetailAugmentedPagerFragment newInstance(String filePath, String mimeType, int position, int idContent) {
        ContentDetailAugmentedPagerFragment fragment = new ContentDetailAugmentedPagerFragment();
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
        filePath = args.getString("filePath");
        mimeType = args.getString("mimeType");
        position = args.getInt("position");
        isAutoDownload = new UserPreferences(getContext()).getIsAutodownload();
        idContent = args.getInt("idContent");

        boolean isVideo = (mimeType != null && mimeType.startsWith("video"));
        View rootView = inflater.inflate(R.layout.fragment_content_detail_augmented_pager_picture, container, false);

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


    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.imageView:
                boolean isVideo = (mimeType != null && mimeType.startsWith("video"));

                if (isVideo) {
                    Intent intent = new Intent(getContext(), ZoomContentActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("filePath", filePath);
                    bundle.putString("mimeType", mimeType);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.videoView:
                Intent intent = new Intent(getContext(), ZoomContentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("filePath", filePath);
                bundle.putString("mimeType", mimeType);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.download:
                downloadIV.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                addContent(true);
                break;
        }
    }

    private void addContent(boolean downloadClicked) {
        if (filePath==null||filePath.equals("") && progressBar.getVisibility()!=View.VISIBLE){
            progressBar.setVisibility(View.VISIBLE);
        }
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

    public void refreshPicture(String filePath) {
        progressBar.setVisibility(View.GONE);
        downloadIV.setVisibility(View.GONE);
        ImageUtils.setImageToImageView(new File(filePath), imageView, getContext(), false);
        imageView.setOnClickListener(this);
    }

    public void setListener(DownloadRequestAugmented listener) {
        this.listener = listener;
    }

    interface DownloadRequestAugmented {
        void onDownloadRequestAugmented(int position);
    }


}
