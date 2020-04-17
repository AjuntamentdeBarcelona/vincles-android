package cat.bcn.vincles.mobile.UI.Chats;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.design.widget.TabLayout;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.MediaCallbacks;
import cat.bcn.vincles.mobile.Client.Business.MediaCallbacksGallery;
import cat.bcn.vincles.mobile.Client.Business.MediaManager;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessage;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessageMedia;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.ImageUtils;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement.TYPE_ME_AUDIO_FIRST;
import static cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement.TYPE_ME_TEXT;
import static cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement.TYPE_USER_AUDIO_FIRST;
import static cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement.TYPE_USER_TEXT;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.BaseViewHolder> {

    public static final int USER_ME = -1;


    boolean isAutodownload;
    Context context;
    List<ChatElement> elementsList;
    ChatAdapterListener listener;
    private boolean deleteVisibility;
    SparseArray<Contact> users;

    HashMap<Integer, Integer> audioPlayPositions;
    HashMap<Integer, Integer> audioPlayDurations;
    int audioPlayingId = -1;
    String audioPath;
    int audioDuration;
    MediaPlayer mediaPlayer;
    AudioViewHolder playingAudioVh;
    CountDownTimer audioTimer;
    private String idChat;
    MediaManager mediaManager;
    private Boolean isGroupChat;

    public class BaseViewHolder extends RecyclerView.ViewHolder {

        TextView message;


        public BaseViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
        }

        @CallSuper
        public void fillInfo(ChatElement chatElement, BaseViewHolder holder) {
            message.setText(chatElement.getText());
        }

    }

    public class CallAlertViewHolder extends BaseViewHolder {

        ImageView avatar;


        public CallAlertViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
        }

        @CallSuper
        public void fillInfo(ChatElement chatElement, BaseViewHolder holder) {
            super.fillInfo(chatElement, holder);

            ChatMessage chatMessage = (ChatMessage) chatElement;
            Contact user = users.get(chatMessage.getIdUserFrom());

            if (user != null) {
                holder.itemView.setTag(user.getId());
                setUserAvatarForId(avatar, ((ChatMessage) chatElement).getIdUserFrom());
                mediaManager.downloadContactItem(holder, -1, user.getPath(), user.getId(),
                        Contact.TYPE_CIRCLE_USER, user.getIdContentPhoto(), true, new MediaCallbacksGallery() {
                            @Override
                            public void onSuccess(HashMap<Integer, Object> response) {

                                CallAlertViewHolder holder = (CallAlertViewHolder) response.get(0);
                                ImageView avatar = (ImageView) holder.avatar;
                                String theFilePath = (String) response.get(1);
                                int theIdContent = (int) response.get(3);

                                int userId = (int) response.get(3);
                                if (holder.itemView.getTag() == null || !holder.itemView.getTag().equals(userId)) {
                                    return;
                                }

                                Log.d("tag", String.valueOf(holder.avatar.getTag()));

                                if (holder.avatar.getTag() == null) {
                                    return;
                                }

                                if (!holder.avatar.getTag().equals(theIdContent)) {
                                    return;
                                }

                                if (holder.getAdapterPosition() == NO_POSITION) {
                                    Log.d("downloadGalleryItem", "NO_POSITION");
                                    return;
                                }

                                putImage(theFilePath, avatar);

                            /*ImageView theHolder = (ImageView) response.get(0);
                            String theFilePath = (String) response.get(1);

                            putImage(theFilePath, theHolder);*/
                            }

                            @Override
                            public void onFailure(HashMap<Integer, Object> response) {

                            }
                        });
            } else {
                setUserAvatarForId(avatar, ((ChatMessage) chatElement).getIdUserFrom());
            }
            listener.putNotificationOnWatched((ChatMessage) chatElement);
        }

    }

    public class MessageViewHolder extends BaseViewHolder {
        ImageView avatar;
        TextView contactName;
        TextView time;
        View bubble;


        public MessageViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            contactName = itemView.findViewById(R.id.contact_name);
            time = itemView.findViewById(R.id.time);
            bubble = itemView.findViewById(R.id.bubble);
        }

        @Override
        public void fillInfo(ChatElement chatElement, BaseViewHolder holder) {
            super.fillInfo(chatElement, holder);

            // Make text messages containing a URL linkable.
            Linkify.addLinks(message, Linkify.WEB_URLS);

            ChatMessage chatMessage = (ChatMessage) chatElement;

            int id = chatMessage.getType() >= TYPE_ME_TEXT ? USER_ME : chatMessage.getIdUserFrom();
            contactName.setText(getUserNameForId(id, chatMessage.getFullNameUserSender()));

            Contact user = users.get(chatMessage.getIdUserFrom());
            if (user != null) {
                contactName.setTag(user.getId());
                mediaManager.downloadContactItem(holder, -1, user.getPath(), user.getId(),
                        Contact.TYPE_CIRCLE_USER, user.getIdContentPhoto(), true, new MediaCallbacksGallery() {
                            @Override
                            public void onSuccess(HashMap<Integer, Object> response) {
                                MessageViewHolder holder = (MessageViewHolder) response.get(0);
                                ImageView avatar = (ImageView) holder.avatar;
                                String theFilePath = (String) response.get(1);

                                int theIdContent = (int) response.get(3);
                                if (!holder.contactName.getTag().equals(theIdContent)) {
                                    return;
                                }

                                if (holder.getAdapterPosition() == NO_POSITION) {
                                    Log.d("downloadGalleryItem", "NO_POSITION");
                                    return;
                                }

                                putImage(theFilePath, avatar);
                            }

                            @Override
                            public void onFailure(HashMap<Integer, Object> response) {

                            }
                        });
            } else {
                setUserAvatarForId(avatar, id);
            }


            time.setText(DateUtils.getFormattedHourMinutesFromMillis(context, chatElement.getSendTime()));
            if (chatElement.getType() >= TYPE_ME_TEXT && chatElement.getType() <= TYPE_ME_AUDIO_FIRST) {
                Drawable background = bubble.getBackground();
                Drawable wrapDrawable = DrawableCompat.wrap(background);
                DrawableCompat.setTint(wrapDrawable, context.getResources()
                        .getColor(R.color.chat_bubble_grey_dark));
                bubble.setBackground(wrapDrawable);

                message.setTextColor(context.getResources().getColor(R.color.colorWhite));
            } else if (!((ChatMessage) chatElement).isWatched()
                    && chatElement.getType() >= TYPE_USER_TEXT
                    && chatElement.getType() <= TYPE_USER_AUDIO_FIRST) {
                Drawable background = bubble.getBackground();
                Drawable wrapDrawable = DrawableCompat.wrap(background);
                DrawableCompat.setTint(wrapDrawable, context.getResources()
                        .getColor(R.color.chat_bubble_pink));
                bubble.setBackground(wrapDrawable);
            } else {
                Drawable background = bubble.getBackground();
                Drawable wrapDrawable = DrawableCompat.wrap(background);
                DrawableCompat.setTint(wrapDrawable, context.getResources()
                        .getColor(R.color.chat_bubble_grey_light));
                bubble.setBackground(wrapDrawable);
            }

            if (chatMessage.getNotificationId() != -1) {
                listener.putNotificationOnWatched(chatMessage);
            }
        }

    }

    public class ImageViewHolder extends MessageViewHolder implements ChatImagePagerAdapter.Callback {
        ViewPager viewPager;
        TabLayout tabLayout;


        public ImageViewHolder(View itemView) {
            super(itemView);
            viewPager = itemView.findViewById(R.id.pager);
            tabLayout = itemView.findViewById(R.id.tablayout);
        }

        @Override
        public void fillInfo(ChatElement chatElement, BaseViewHolder holder) {
            super.fillInfo(chatElement, holder);
            if (chatElement.getText() == null || chatElement.getText().length() <= 0) {
                message.setVisibility(View.GONE);
            }

            ChatMessageMedia message = (ChatMessageMedia) chatElement;
            if (message.getMediaFiles().size() == 0) {
                message.getMediaFiles().add("");
            }
            if (message.getIsVideo().size() == 0) {
                message.getIsVideo().add(false);
            }

            ChatImagePagerAdapter adapter = new ChatImagePagerAdapter(message.getMediaFiles(), message.getIsVideo(), context, this, (int) message.getId(), idChat, isGroupChat);
            viewPager.setAdapter(adapter);
            if (adapter.getCount() < 2) {
                tabLayout.setVisibility(View.GONE);
            } else {
                tabLayout.setVisibility(View.VISIBLE);
                tabLayout.setupWithViewPager(viewPager, true);
            }
        }

        @Override
        public void onMediaClicked(String path, boolean isVideo) {
            listener.onChatElementMediaClicked(path, isVideo ? "video" : "image");
        }
    }

    public class AudioViewHolder extends MessageViewHolder {
        ImageView audioButton;
        ImageView download;
        ProgressBar progressBar;
        SeekBar seekBar;
        TextView time;
        int duration;


        public AudioViewHolder(View itemView) {
            super(itemView);
            audioButton = itemView.findViewById(R.id.play_iv);
            progressBar = itemView.findViewById(R.id.progressbar);
            seekBar = itemView.findViewById(R.id.seekbar);
            time = itemView.findViewById(R.id.proggress_time);
            download = itemView.findViewById(R.id.download);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (seekBar.getTag() != null) {
                        int id = (int) seekBar.getTag();
                        audioPlayPositions.put(id, progress);
                        if (id == audioPlayingId && fromUser) {
                            if (mediaPlayer != null) {
                                mediaPlayer.seekTo(progress);
                            }
                            if (audioTimer != null) {
                                audioTimer.cancel();

                                progressBar.setEnabled(true);
                                seekBar.setEnabled(true);
                            } else {
                                resetAudioSeekBar(seekBar);
                            }
                            createCountdownTimer(progress);
                        // If the user chose to move the seekbar progress manually
                        } else if (fromUser) {
                            if (audioPlayingId != -1) stopPlayingAudio();
                            if (!audioPlayDurations.containsKey(id)) {
                                for (ChatElement element : elementsList) {
                                    if (element instanceof ChatMessageMedia &&
                                            ((ChatMessageMedia) element).getId() == id) {
                                        String path = ((ChatMessageMedia) element).getMediaFiles().get(0);
                                        // Only move the progress if the path is not null or blank
                                        if (path != null && !"".equals(path)) {
                                            MediaPlayer mediaPlayer = new MediaPlayer();
                                            try {
                                                progressBar.setEnabled(true);
                                                seekBar.setEnabled(true);

                                                // In Samsung devices this fails sometimes if the path
                                                // is not transformed into a Uri first
                                                mediaPlayer.setDataSource(Uri.parse(path).toString());
                                                mediaPlayer.prepare();
                                                int duration = mediaPlayer.getDuration();
                                                audioPlayDurations.put(id, duration);
                                                setDuration(duration);
                                                setPlayPosition(progress * duration / 100);
                                            } catch (IOException e) {
                                                resetAudioSeekBar(seekBar);
                                            }
                                        }
                                        break;
                                    }
                                }
                            } else {
                                setPlayPosition(progress);
                            }
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        private void resetAudioSeekBar(SeekBar seekBar) {
            progressBar.setEnabled(false);
            seekBar.setEnabled(false);
            seekBar.setProgress(0);
            progressBar.setProgress(0);
            Toast.makeText(context, R.string.error_1001, Toast.LENGTH_SHORT).show();
        }

        public void setPlayPosition(int position) {
            seekBar.setProgress(position);
            time.setText(DateUtils.getFormatedTimeFromMillis(position));
        }

        public void setPausedState() {
            audioButton.setImageDrawable(context.getResources()
                    .getDrawable(R.drawable.play));
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
            seekBar.setMax(duration);
        }

        @Override
        public void fillInfo(final ChatElement chatElement, BaseViewHolder holder) {
            super.fillInfo(chatElement, holder);
            if (chatElement.getText() == null || chatElement.getText().length() <= 0) {
                message.setVisibility(View.GONE);
            }
            if (chatElement instanceof ChatMessageMedia) {
                final ChatMessageMedia chatMessageMedia = (ChatMessageMedia) chatElement;
                seekBar.setTag((int) chatMessageMedia.getId());

                if (chatMessageMedia.getMediaFiles().size() == 0) {
                    chatMessageMedia.getMediaFiles().add("");
                }
                final String fileName = chatMessageMedia.getMediaFiles().get(0);

                addAudio(fileName, chatMessageMedia, 0, false);

            }
        }

        private void addAudio(final String fileName, final ChatMessageMedia chatMessageMedia, int position, boolean downloadClicked) {

            int playPosition = 0;
            if (audioPlayPositions.containsKey((int) chatMessageMedia.getId())) {
                playPosition = audioPlayPositions.get((int) chatMessageMedia.getId());
            }


            final int finalPlayPosition = playPosition;
            mediaManager.downloadMessageItem(fileName, (int) chatMessageMedia.getId(), idChat, position, isGroupChat, new MediaCallbacks() {
                @Override
                public void onSuccess(final String response) {

                    download.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    audioButton.setVisibility(View.VISIBLE);
                    //setPlayPosition(playPosition);
                    Log.d("audch", "cha id:" + chatMessageMedia.getId() + ", audioPlaID:" + audioPlayingId + "savedPosition:" + audioPlayPositions);
                    if (audioPlayPositions.containsKey((int) chatMessageMedia.getId()) &&
                            audioPlayDurations.containsKey((int) chatMessageMedia.getId())) {
                        setDuration(audioPlayDurations.get((int) chatMessageMedia.getId()));
                        Log.d("audch", "setPos, pos:" + audioPlayPositions.get((int) chatMessageMedia.getId()) + ", dura:" + audioPlayDurations.get((int) chatMessageMedia.getId()));
                        setPlayPosition(finalPlayPosition);
                    }
                    if (chatMessageMedia.getId() == audioPlayingId) {
                        audioButton.setImageDrawable(context.getResources()
                                .getDrawable(R.drawable.exo_controls_pause));
                        playingAudioVh = AudioViewHolder.this;
                    } else {
                        audioButton.setImageDrawable(context.getResources()
                                .getDrawable(R.drawable.play));
                    }

                    if (audioPlayingId == (int) chatMessageMedia.getId() && mediaPlayer == null) {
                        playingAudioVh = AudioViewHolder.this;
                        audioPath = response;
                        playAudio(chatMessageMedia.getId());
                    }

                    audioPath = response;
                    String extension = FilenameUtils.getExtension(audioPath);

                    if (extension.equals("") && isAutodownload) {
                        audioButton.setVisibility(View.GONE);
                        download.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        addAudio(fileName, chatMessageMedia, 0, true);

                        /*if (fileRequestListener != null) {
                            fileRequestListener.onFileRequest((int) chatMessageMedia.getId(), 0);
                        }*/
                    }
                    audioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            download.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);

                            progressBar.setEnabled(true);
                            seekBar.setEnabled(true);

                            //TODO anything else to do?
                            if (chatMessageMedia.getId() == audioPlayingId) {
                                stopPlayingAudio();
                            } else if (!ChatAudioRecorderFragment.RECORDING_AUDIO) {
                                stopPlayingAudio(); //check if -1
                                playingAudioVh = AudioViewHolder.this;
                                audioButton.setImageDrawable(context.getResources().getDrawable(R.drawable.exo_controls_pause));
                                audioPlayingId = (int) chatMessageMedia.getId();
                                if (!audioPlayPositions.containsKey(audioPlayingId))
                                    audioPlayPositions.put(audioPlayingId, 0);
                                audioPath = response;
                                playAudio(chatMessageMedia.getId());
                            }
                        }
                    });

                }

                @Override
                public void onFailure(String response) {

                    //if (fileName == null || fileName.length() == 0)
                    if (isAutodownload) {
                        progressBar.setVisibility(View.VISIBLE);
                        audioButton.setVisibility(View.GONE);
                        download.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        audioButton.setVisibility(View.GONE);
                        download.setVisibility(View.VISIBLE);
                    }
                }
            }, downloadClicked);


            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    download.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    addAudio(fileName, chatMessageMedia, 0, true);
                    //   fileRequestListener.onFileRequest((int) chatMessageMedia.getId(), 0);


                }
            });
        }
    }

    public void stopPlayingAudio() {
        if (audioTimer != null) audioTimer.cancel();
        if (mediaPlayer != null) mediaPlayer.release();
        mediaPlayer = null;
        if (playingAudioVh != null) playingAudioVh.setPausedState();
        playingAudioVh = null;
        audioPlayingId = -1;
    }

    private void playAudio(long id) {
        mediaPlayer = new MediaPlayer();

        mediaManager.downloadMessageItem(audioPath, (int) id, idChat, 0, isGroupChat, new MediaCallbacks() {
            @Override
            public void onSuccess(String response) {
                try {
                    File file = new File(response);

                    String extension = FilenameUtils.getExtension(response);
                    if (!file.exists() || (!extension.equals("aac") && !extension.equals("mp3"))) {
                        Toast.makeText(context, R.string.error_audio_format, Toast.LENGTH_SHORT).show();
                    }

                    mediaPlayer.setDataSource(audioPath);
                    mediaPlayer.prepare();
                    audioPlayDurations.put(audioPlayingId, mediaPlayer.getDuration());
                    if (playingAudioVh != null) {
                        playingAudioVh.setDuration(mediaPlayer.getDuration());
                    }
                    int currentProgress = 0;
                    if (audioPlayPositions.containsKey(audioPlayingId) && audioPlayPositions.get(audioPlayingId) != null) {
                        try {
                            currentProgress = audioPlayPositions.get(audioPlayingId);
                        } catch (NullPointerException ignored) {

                        }
                    }
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(currentProgress);
                        mediaPlayer.start();
                        createCountdownTimer(currentProgress);
                    }
                } catch (IOException e) {
                    Log.e("media_player", "prepare() failed");
                }

            }

            @Override
            public void onFailure(String response) {
                Toast.makeText(context, R.string.error_audio_format, Toast.LENGTH_SHORT).show();

            }
        }, false);
    }

    private void createCountdownTimer(int currentProgress) {
        if (mediaPlayer==null)return;
        audioTimer = new CountDownTimer(mediaPlayer.getDuration() - currentProgress,
                16) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (playingAudioVh != null) {
                    playingAudioVh.setPlayPosition((int) (playingAudioVh.getDuration()
                            - millisUntilFinished));
                }
            }

            @Override
            public void onFinish() {
                if (playingAudioVh != null) playingAudioVh.setPlayPosition(0);
                stopPlayingAudio();
                if (audioPlayPositions.containsKey(audioPlayingId)) {
                    audioPlayPositions.remove(audioPlayingId);
                }
            }
        }.start();
    }


    ChatAdapter(Context context, List<ChatElement> elementsList, ChatAdapterListener listener,
                SparseArray<Contact> users, Bundle savedState, String idChat, boolean isGroupChat) {
        this.context = context;
        this.elementsList = elementsList;
        this.listener = listener;
        this.users = users;
        this.idChat = idChat;
        this.mediaManager = new MediaManager(context);
        this.isGroupChat = isGroupChat;
        isAutodownload = new UserPreferences(context).getIsAutodownload();
        audioPlayPositions = new HashMap<>();
        audioPlayDurations = new HashMap<>();
        loadSavedState(savedState);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("audioPlayingId", audioPlayingId);
        outState.putSerializable("audioPlayPositions", audioPlayPositions);
        outState.putSerializable("audioPlayDurations", audioPlayDurations);
        outState.putString("audioPath", audioPath);

        if (audioTimer != null) audioTimer.cancel();
        if (mediaPlayer != null) mediaPlayer.release();
        mediaPlayer = null;
    }

    private void loadSavedState(Bundle state) {
        if (state != null) {
            audioPlayingId = state.getInt("audioPlayingId");
            audioPlayPositions = (HashMap<Integer, Integer>) state.getSerializable("audioPlayPositions");
            audioPlayDurations = (HashMap<Integer, Integer>) state.getSerializable("audioPlayDurations");
            if (audioPlayPositions == null) audioPlayPositions = new HashMap<>();
            if (audioPlayDurations == null) audioPlayDurations = new HashMap<>();
            audioPath = state.getString("audioPath");
        }
    }

    @Override
    public int getItemViewType(int position) {

        return elementsList.get(position).getType();

    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(context).inflate(getLayoutId(viewType), parent, false);

        switch (viewType) {
            case ChatElement.TYPE_ALERT_DATE:
            default:
                return new BaseViewHolder(v);
            case ChatElement.TYPE_ALERT_MISSED_CALL:
                return new CallAlertViewHolder(v);
            case ChatElement.TYPE_USER_TEXT:
            case ChatElement.TYPE_USER_TEXT_FIRST:
            case ChatElement.TYPE_ME_TEXT:
            case ChatElement.TYPE_ME_TEXT_FIRST:
                return new MessageViewHolder(v);
            case ChatElement.TYPE_USER_AUDIO:
            case ChatElement.TYPE_USER_AUDIO_FIRST:
            case ChatElement.TYPE_ME_AUDIO:
            case ChatElement.TYPE_ME_AUDIO_FIRST:
                return new AudioViewHolder(v);
            case ChatElement.TYPE_USER_IMAGE:
            case ChatElement.TYPE_USER_IMAGE_FIRST:
            case ChatElement.TYPE_ME_IMAGE:
            case ChatElement.TYPE_ME_IMAGE_FIRST:
                return new ImageViewHolder(v);
        }
    }

    private int getLayoutId(int viewType) {
        switch (viewType) {
            case ChatElement.TYPE_ALERT_DATE:
            default:
                return R.layout.chat_element_alert;
            case ChatElement.TYPE_ALERT_MISSED_CALL:
                return R.layout.chat_element_call_alert;
            case ChatElement.TYPE_USER_TEXT_FIRST:
                return R.layout.chat_element_user_text_first;
            case ChatElement.TYPE_USER_TEXT:
                return R.layout.chat_element_user_text;
            case ChatElement.TYPE_USER_AUDIO_FIRST:
                return R.layout.chat_element_user_audio_first;
            case ChatElement.TYPE_USER_AUDIO:
                return R.layout.chat_element_user_audio;
            case ChatElement.TYPE_ME_IMAGE:
                return R.layout.chat_element_me_image;
            case ChatElement.TYPE_ME_IMAGE_FIRST:
                return R.layout.chat_element_me_image_first;
            case ChatElement.TYPE_ME_TEXT_FIRST:
                return R.layout.chat_element_me_text_first;
            case ChatElement.TYPE_ME_TEXT:
                return R.layout.chat_element_me_text;
            case ChatElement.TYPE_ME_AUDIO_FIRST:
                return R.layout.chat_element_me_audio_first;
            case ChatElement.TYPE_ME_AUDIO:
                return R.layout.chat_element_me_audio;
            case ChatElement.TYPE_USER_IMAGE:
                return R.layout.chat_element_user_image;
            case ChatElement.TYPE_USER_IMAGE_FIRST:
                return R.layout.chat_element_user_image_first;
        }
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        holder.fillInfo(elementsList.get(position), holder);

       /* if (holder instanceof  CallAlertViewHolder){
            CallAlertViewHolder callAlertViewHolder = (CallAlertViewHolder) holder;

            ChatMessage chatElement = (ChatMessage)elementsList.get(position);
            Contact user = users.get(chatElement.getIdUserFrom());

            this.mediaManager.downloadContactItem(callAlertViewHolder.avatar,-1,user.getPath(),user.getId(),
                    Contact.TYPE_CIRCLE_USER, user.getIdContentPhoto(), true, new MediaCallbacksGallery() {
                        @Override
                        public void onSuccess(HashMap<Integer, Object> response) {
                            ImageView theHolder = (ImageView) response.get(0);
                            String theFilePath = (String) response.get(1);

                            putImage(theFilePath, theHolder);
                        }

                        @Override
                        public void onFailure(HashMap<Integer, Object> response) {

                        }
                    });
        }*/


    }

    private void putImage(final String theFilePath, final ImageView theHolder) {
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                ImageUtils.setImageToImageView(theFilePath, theHolder, context, true);
            }
        });

    }


    @Override
    public int getItemCount() {

        return elementsList.size();

    }

    public interface ChatAdapterListener {
        void onChatElementMediaClicked(String path, String mimeType);

        void putNotificationOnWatched(ChatMessage chatMessage);
    }

    private String getUserNameForId(int id, String fullname) {
        if (id == USER_ME) return context.getResources()
                .getString(R.string.chat_username_you);
        if (users != null) {
            Contact user = users.get(id);
            if (user != null) {
                return user.getName() + " " + user.getLastname();
            }
        }
        if (fullname != null && !fullname.isEmpty()) return fullname;

        return "" + id;
    }

    private void setUserAvatarForId(ImageView avatar, int id) {
        boolean avatarSet = false;
        if (users != null) {

            Contact user = users.get(id);
            if (user != null) {
                String path = user.getPath();
                Log.d("qwe", "setAvatar, id:" + id + " path:" + path);
                if (path != null && path.length() > 0 && !path.equals("placeholder")) {
                    path = path.replace("file://", "");
                    Log.d("qwe", "entra glide");
                    avatarSet = true;

                    ImageUtils.setImageToImageView(path, avatar, context, true);

                }
            }
        }
        if (!avatarSet)
            avatar.setImageDrawable(context.getResources().getDrawable(R.drawable.user));
    }

    public void setUsers(SparseArray<Contact> users) {
        this.users = users;
    }


}
