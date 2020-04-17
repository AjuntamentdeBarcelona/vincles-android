package cat.bcn.vincles.mobile.UI.Chats;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.MediaCallbacks;
import cat.bcn.vincles.mobile.Client.Business.MediaManager;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import io.realm.Realm;

public class ChatImagePagerAdapter extends PagerAdapter {

    boolean isAutoDownload;
    int messageId;
    private List<String> imagePathsList;
    private List<Boolean> isVideoList;
    private String idChat;
    private Boolean isGroupChat;
    Context context;
    Callback listener;
    private static Integer IS_VIDEO = 0;
    private static Integer IS_IMAGE = 1;

    public ChatImagePagerAdapter(List<String> imagePathsList, List<Boolean> isVideoList,
                                 Context context, Callback listener, int messageId,
                                 String idChat, Boolean isGroupChat) {
        super();
        this.imagePathsList = imagePathsList;
        this.isVideoList = isVideoList;
        this.context = context;
        this.listener = listener;
        this.messageId = messageId;
        this.isGroupChat = isGroupChat;
        this.idChat = idChat;
        isAutoDownload = new UserPreferences(context).getIsAutodownload();
    }


    @Override
    public int getCount() {
        return imagePathsList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, final int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.chat_element_image_adapter_item, collection, false);
        collection.addView(layout);

        final String path = imagePathsList.get(position);
        final Boolean isVideo = isVideoList.get(position);
        Log.d("imgpag","instantiate item, path:"+path);

        addContent(path, position, layout, isVideo, false, this.messageId, this.idChat, this.isGroupChat);

        ImageView videoHintIV = layout.findViewById(R.id.video_hint);

        Realm realm = Realm.getDefaultInstance();
        ChatMessageRest message = realm.where(ChatMessageRest.class).equalTo("id", messageId).findFirst();
        videoHintIV.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        if (message != null && message.getMetadataAdjuntContents() != null){

            if(message.getMetadataAdjuntContents().size() < position && message.getMetadataAdjuntContents().get(position).toLowerCase().contains("video")) {
                videoHintIV.setVisibility(View.VISIBLE);

            }
        }

        return layout;
    }

    private void addContent(String path, final int position, final ViewGroup layout, final Boolean isVideo, final Boolean fromDownload, final int messageId, final String idChat , final Boolean isGroupChat) {
        if (path==null)path="";
        final ImageView imageView = layout.findViewById(R.id.imageview);
        final ImageView download = layout.findViewById(R.id.download);
        final ImageView videoHintIV = layout.findViewById(R.id.video_hint);

        Log.d("addcontent", String.valueOf(position));

        if(!path.equals("") || layout.findViewById(R.id.progressbar).getVisibility() != View.VISIBLE){
            layout.findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        }

        download.setVisibility(View.GONE);

        MediaManager mediaManager = new MediaManager(context);
        mediaManager.downloadMessageItem(path, messageId, idChat, position, isGroupChat, new MediaCallbacks() {
            @Override
            public void onSuccess(final String response) {
                Log.d("addcontent", response);
                layout.findViewById(R.id.progressbar).setVisibility(View.GONE);
                if (response != null && response.length()>0 && !response.equals("placeholder")) {
                    final Integer mediaType = (response.contains(".jpeg") || response.contains(".jpg") || response.contains(".png") ? IS_IMAGE : IS_VIDEO);
                    ImageUtils.setImageToImageView(response, imageView, context, true);
                    boolean isVid = mediaType.equals(IS_VIDEO);
                    videoHintIV.setVisibility(isVid ? View.VISIBLE : View.GONE);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onMediaClicked(response, (mediaType.equals(IS_VIDEO)));
                        }
                    });
                    imagePathsList.set(position, response);

                }
                else if (response!= null && response.length()>0 && response.equals("placeholder")) {
                    imageView.setImageDrawable(layout.getContext()
                            .getResources().getDrawable(R.drawable.user));
                    imageView.setOnClickListener(null);
                }

            }

            @Override
            public void onFailure(final String response) {
                if (isAutoDownload) {
                    if(layout.findViewById(R.id.progressbar).getVisibility() != View.VISIBLE){
                        layout.findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
                    }
                    imageView.setOnClickListener(null);
                } else {
                    layout.findViewById(R.id.progressbar).setVisibility(View.GONE);
                    download.setVisibility(View.VISIBLE);
                    download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addContent("", position, layout, isVideo, true, messageId, idChat, isGroupChat);

                        }
                    });
                }

            }
        }, fromDownload);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    public interface Callback {
        void onMediaClicked(String path, boolean isVideo);
    }


}
