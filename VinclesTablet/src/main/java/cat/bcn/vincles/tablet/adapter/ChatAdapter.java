/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.component.ChatItemView;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.ResourceModel;

// Este adaptador actuará como un adaptador de 5 elementos siempre, pero guardar´a la groupList completa
// recorriendo únicamente desde el offset al que estemos apuntando y que agrandaremos o reduciremos
// con las funciones showMore y showLess

public class ChatAdapter extends FixedItemsAdapterTemplate<Chat> {
    private final String TAG = this.getClass().getSimpleName();
    private MainModel mainModel = MainModel.getInstance();
    private ViewHolder holder;

    // LOADING IMAGES DIALOG
    private ProgressDialog progressBar;
    private int imageFirstLoadings = 0;

    public ChatAdapter(Context context, int resource, List<Chat> objects, int listHeight, int mMAX_ITEM) {
        super(context, resource, objects, listHeight, mMAX_ITEM);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // DO NOT CRASH IN VOID LIST MESSAGES
        if (itemsList.size() <= 0) {
            rowView = new ChatItemView(getContext(), 0);
            rowView.setVisibility(View.GONE);
            return rowView;
        }

        // DO NOT CRASH IN LAST ITEMS
        int realListPosition = getRealMessagePosition(position);
        if (realListPosition >= itemsList.size()) realListPosition = itemsList.size() - 1;

        Chat item = itemsList.get(realListPosition);

        // GET ROW HEIGHT
        int viewRows = getFullRowSpace(position);

        // IS SHOWED NOT INTO THIS BUNDLE?
        if (!isShowed(position)) viewRows = 0;

        if (rowView == null) {
            rowView = new ChatItemView(getContext(), viewRows * itemHeight);
            rowView.setVisibility(View.VISIBLE);
            createHolder(rowView);
            rowView.setTag(holder);
        } else {
            if (holder == null) {
                createHolder(rowView);
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }
        }

        // SET VIEW HEIGHT
        ((ChatItemView)rowView).setHeight(viewRows*itemHeight);

        if (holder != null) {
            // DEFAULT STATE:
            rowView.setVisibility(View.VISIBLE);
            holder.texDay.setVisibility(View.GONE);
            holder.imgPicture.setImageResource(R.color.superlightgray);
            holder.imgPicture.setVisibility(View.GONE);
            holder.imgAudio.setVisibility(View.GONE);

            // DEFAULT AS OTHER USER
            holder.layout_imgMine.setVisibility(View.GONE);
            holder.layout_img.setVisibility(View.VISIBLE);
            holder.bubble.setBackgroundResource(R.drawable.speechbubble_left);

            // Bubble customization for other user / user mine
            if (item.idUserFrom.longValue() == MainModel.getInstance().getCurrentUserId().longValue()) {
                holder.imgPhotoWrapper = holder.imgPhotoMine;
                holder.texFullNameWrapper = holder.texFullNameMine;
                holder.layout_img.setVisibility(View.GONE);
                holder.layout_imgMine.setVisibility(View.VISIBLE);
                if (item.watched) {
                    holder.bubble.setBackgroundResource(R.drawable.speechbubble_right_readed);
                } else {
                    holder.bubble.setBackgroundResource(R.drawable.speechbubble_right);
                }
            } else {
                holder.imgPhotoWrapper = holder.imgPhoto;
                holder.texFullNameWrapper = holder.texFullName;
                holder.layout_img.setVisibility(View.VISIBLE);
                holder.layout_imgMine.setVisibility(View.GONE);
                if (item.watched) {
                    holder.bubble.setBackgroundResource(R.drawable.speechbubble_left_readed);
                } else {
                    holder.bubble.setBackgroundResource(R.drawable.speechbubble_left);
                }
            }

            // ADD DATE
            holder.position = position;
            holder.realPosition = getRealMessagePosition(position);
            if (isShowTitleMessage(holder.realPosition)) {
                holder.texDay.setVisibility(View.VISIBLE);
                holder.texDay.setText(VinclesConstants.getDateString(item.sendTime, getContext().getResources().getString(R.string.dateSmallformat), new Locale(getContext().getResources().getString(R.string.locale_language), getContext().getResources().getString(R.string.locale_country))));
            }

            if (item.userFrom != null) {
                if (item.userFrom.idContentPhoto != null) {
                    // Populate the data into the template view using the data object
                    try {
                        if (item.userFrom.usrImgStatus > 0) {
                            final Chat newItem = item;
                            Glide.with(getContext())
                                    .load(mainModel.getUserPhotoUrlFromUser(item.userFrom, new AsyncResponse() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            Glide.with(getContext())
                                                    .load(mainModel.getUserPhotoUrlFromUser(newItem.userFrom))
                                                    .signature(new StringSignature(newItem.userFrom.idContentPhoto.toString()))
                                                    .error(R.drawable.user).placeholder(R.color.superlightgray)
                                                    .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                                    .into(holder.imgPhotoWrapper);
                                        }

                                        @Override  public void onFailure(Object error) { }
                                    }))
                                    .signature(new StringSignature(item.userFrom.idContentPhoto.toString()))
                                    .error(R.drawable.user).placeholder(R.color.superlightgray)
                                    .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                    .into(holder.imgPhotoWrapper);
                        }
                        holder.texFullNameWrapper
                                .setText(item.userFrom.alias);
                    } catch (Exception e) { e.printStackTrace(); }
                } else {
                    Log.w(TAG, item.userFrom.alias + " has idContentPhoto null!");
                }
            }

            // Populate the time into the template
            if (DateUtils.isToday(item.sendTime.getTime())) {
                int min = VinclesConstants.getMinutesInterval(item.sendTime);

                String duration = mContext.getString(R.string.message_ago) + " ";
                if (min < 60) duration += min + " " + mContext.getString(R.string.minutes);
                else
                    duration += ((int) min / 60) + " " + mContext.getString(R.string.hours);

                holder.texTime.setText(duration);

            } else
                holder.texTime.setText(VinclesConstants.getDateString(item.sendTime, getContext().getResources().getString(R.string.timeformat), new Locale(getContext().getResources().getString(R.string.locale_language), getContext().getResources().getString(R.string.locale_country))));

            // Custom for message type:
            if (item.metadataTipus != null) {
                switch (item.metadataTipus) {
                    case VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE:
                        holder.texTitle.setVisibility(View.GONE);
                        if (item.getResources().size() > 0) {
                            holder.imgPicture.setVisibility(View.VISIBLE);
                            loadImage(holder, item.getResources().get(0), item);
                        }
                        break;
                    case VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE:
                        holder.texTitle.setVisibility(View.VISIBLE);
                        holder.imgAudio.setVisibility(View.VISIBLE);
                        holder.texTitle.setText(mContext.getString(R.string.task_groups_listen));
                        break;
                    case VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE:
                        holder.texTitle.setVisibility(View.VISIBLE);
                        holder.imgAudio.setVisibility(View.VISIBLE);
                        holder.texTitle.setText(mContext.getString(R.string.task_groups_video));
                        break;
                    case VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE:
                        holder.texTitle.setVisibility(View.VISIBLE);
                        holder.texTitle.setText(item.text);
                        break;
                }
            } else {
                holder.texTitle.setVisibility(View.VISIBLE);
                holder.texTitle.setText(item.text);
            }
        }

        return rowView;
    }


    @Override
    protected boolean isShowTitleMessage(int listPosition) {
        if (listPosition >= itemsList.size()) return false;

        Chat last;
        if (listPosition == 0) last = itemsList.get(0);
        else last = itemsList.get(listPosition-1);

        Chat actual = itemsList.get(listPosition);

        Calendar calLast = Calendar.getInstance();
        Calendar calActual = Calendar.getInstance();
        calLast.setTime(last.sendTime);
        calActual.setTime(actual.sendTime);
        return calLast.get(Calendar.YEAR) != calActual.get(Calendar.YEAR) ||
                calLast.get(Calendar.DAY_OF_YEAR) != calActual.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    protected int getMessageExtraSpace(Chat actual) {
        int extraSpace = 0;

        if (actual.metadataTipus != null) {
            // IMAGES FILL 2 SPACES
            if (actual.metadataTipus.equalsIgnoreCase(VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE))
                extraSpace++;

            // MORE THAN X TEXT CHARACTERS IS ALSO 2 ROWS
            if (actual.metadataTipus.equalsIgnoreCase(VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE) && actual.text.length() > 45)
                extraSpace++;
        } else {
            // If metadataTipus is null then is Text type
            if (actual.text.length() > 45) {
                extraSpace++;
            }
        }

        return extraSpace;
    }

    static class ViewHolder {
        public ImageView imgPhotoWrapper;
        public TextView texFullNameWrapper;

        public View layout_imgMine;
        public ImageView imgPhotoMine;
        public TextView texFullNameMine;

        public View layout_img;
        public ImageView imgPhoto;
        public TextView texFullName;

        public View bubble;

        public ImageView imgPicture;
        public ImageView imgAudio;
        public TextView texTitle;
        public TextView texTime;
        public TextView texDay;

        // Debug vars:
        public int realPosition = 0;
        public int position = 0;
        public boolean isLoadingImage = false;
    }

    private void loadImage(final ViewHolder holder, final Resource resource, final Chat chat) {
        boolean existe = false;
        if (resource.filename != null) {
            File file = new File(VinclesConstants.getImagePath() + "/" + resource.filename);
            existe = file.exists();
        }
        if (resource.filename != ""  && resource.filename != null && existe && chat.resStatus > 0) {
            try {
                Log.d(TAG, "LOAD IMAGE STARTED FOR position " + holder.realPosition + ", adapter postion " + holder.position + " and resource " + resource.filename + ", resStatus: " + chat.resStatus + ", is loading: " + holder.isLoadingImage);
                holder.isLoadingImage = true;
                final String filenameDelete = resource.filename;
                Glide.with(getContext())
                        .load(VinclesConstants.getImageDirectory() + "/" + resource.filename)
                        .into(holder.imgPicture);
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            if (chat.resStatus == 0) return;
            chat.resStatus = 0;
            Log.d(TAG, "LOAD IMAGE FIRST STARTED FOR position " + holder.realPosition + ", adapter postion " + holder.position + " and resource " + resource.filename + ", resStatus: " + chat.resStatus + ", is loading: " + holder.isLoadingImage);
            holder.isLoadingImage = true;
            imageFirstLoadings++;
            checkLoadingImagesDialog();

            ResourceModel.getInstance().loadChatGroupResource(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    imageFirstLoadings--;
                    checkLoadingImagesDialog();
                    chat.resStatus = 1;
                    try {
                        Resource resource = (Resource) result;
                        File imgFile = new File(VinclesConstants.getImagePath() + "/" + resource.filename);
                        if(imgFile.exists()) {
                            // BUG: THERE IS A BUG HERE WITH GLIDE
                            // IT LOADS A WHITE IMAGE I DON'T KNOW WHY, SO I WORKAROUND IT
                            // USING A TARGET INSTEAD OF AN GLIDEBITMAP AND LOAD IT MANUALLY
                            Glide.with(mContext)
                                    .load(imgFile)
                                    .asBitmap()
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                                            holder.imgPicture.setImageBitmap(bitmap);
                                        }
                                    });
//                                    .into(image); // THIS WILL CREATE THE WHITE IMAGE BUG
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }

                @Override
                public void onFailure(Object error) {
                    imageFirstLoadings--;
                    checkLoadingImagesDialog();
                    Log.d(TAG, "LOAD IMAGE FIRST FAILED FOR position " + holder.realPosition + ", adapter postion " + holder.position + " and resource " + resource.filename + ", resStatus: " + chat.resStatus);
                    chat.resStatus = 1;
                    Log.e(TAG, "loadChatGroupResource ERROR: " + error);
                }
            }, chat);
        }
    }

    private void checkLoadingImagesDialog() {
        Log.d(TAG, "LOAD IMAGE LOAD COUNTER: " + imageFirstLoadings);
        if (progressBar == null && imageFirstLoadings > 0) {
            progressBar = new ProgressDialog(mContext/*,R.style.DialogCustomTheme*/);
            progressBar.setMessage(mContext.getString(R.string.first_launch_configuration_step_5));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setInverseBackgroundForced(true);
            progressBar.setCancelable(false);
            progressBar.show();
        }
        else if (imageFirstLoadings <= 0) {
            imageFirstLoadings = 0;
            progressBar.dismiss();
            progressBar = null;
        }
    }

    private void createHolder(View rowView) {
        holder = new ViewHolder();
        // Lookup view for data population
        holder.layout_img = rowView.findViewById(R.id.item_message_photo_layout);
        holder.imgPhoto = (ImageView) rowView.findViewById(R.id.item_message_photo);
        holder.texFullName = (TextView) rowView.findViewById(R.id.item_message_fullname);

        holder.layout_imgMine = rowView.findViewById(R.id.item_message_photo_layout_mine);
        holder.imgPhotoMine = (ImageView) rowView.findViewById(R.id.item_message_photo_mine);
        holder.texFullNameMine = (TextView) rowView.findViewById(R.id.item_message_fullname_mine);

        holder.bubble = rowView.findViewById(R.id.layout_bubble);

        holder.imgPicture = (ImageView) rowView.findViewById(R.id.item_message_picture);
        holder.imgAudio = (ImageView) rowView.findViewById(R.id.item_message_audio);
        holder.texTitle = (TextView) rowView.findViewById(R.id.item_message_title);
        holder.texTime = (TextView) rowView.findViewById(R.id.item_message_time);
        holder.texDay = (TextView) rowView.findViewById(R.id.item_message_date);
    }
}
