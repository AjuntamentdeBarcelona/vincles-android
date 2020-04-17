package cat.bcn.vincles.mobile.UI.Gallery;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.MediaCallbacksGallery;
import cat.bcn.vincles.mobile.Client.Business.MediaManager;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.ImageUtils;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder>{

    Context context;
    UserPreferences userPreferences;
    List<OnItemClicked> onItemClickedListeners = new ArrayList<>();
    private List<Integer> itemsSelected;
    private boolean isInSelectedMode;
    private boolean isInDeleteMode;

    List<GalleryContentRealm> galleryContents;
    Integer selectedContents = 0;
    Integer maxItemsSelected = 10;
    private MediaManager mediaManager;
    OnBottomReachedListener onBottomReachedListener;
    private OnCheckPermissionsCallback onCheckPermissionsCallback;


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageDetail;
        ImageView selectedImage;
        ImageView videoHint;
        ProgressBar progressBar;
        View backgroundView;

        ViewHolder(View v){
            super(v);
            imageDetail = v.findViewById(R.id.image);
            selectedImage = v.findViewById(R.id.selected);
            progressBar = v.findViewById(R.id.progressbar);
            videoHint = v.findViewById(R.id.video_hint);
            backgroundView = v.findViewById(R.id.background_view);

            backgroundView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isInDeleteMode){
                        onSelectItem();
                    }
                }
            });

            imageDetail.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            try{
                if (view == videoHint && (view.getTag() != null && view.getTag() instanceof Boolean && !(boolean)view.getTag())) {
                    videoHint.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                }else{
                    if (isInSelectedMode) {
                        onSelectItem();
                    } else {
                        onViewItem();
                    }
                }
            }catch(Exception e){
                System.out.println("Error " + e.getMessage());
            }
        }

        private void onViewItem() {
            try{
                for (int i = 0; i < onItemClickedListeners.size(); i++) {
                    int position = getAdapterPosition();
                    GalleryContentRealm galleryContentRealm = galleryContents.get(position);
                    onItemClickedListeners.get(i).onViewItem(galleryContentRealm, position);
                }
            }catch(Exception e){
                System.out.println("Error " + e.getMessage());
            }
        }

        public void onSelectItem() {
            try{
                Drawable.ConstantState selectedImageState = context.getResources().getDrawable(R.drawable.image_selected).getConstantState();
                Drawable.ConstantState actualState = selectedImage.getDrawable().getConstantState();

                Integer max = maxItemsSelected;
                if(isInDeleteMode){
                    max = Integer.MAX_VALUE;
                }

                if(selectedContents < max ||  actualState.equals(selectedImageState)){
                    if(actualState.equals(selectedImageState)){
                        selectedContents --;
                        Drawable unselectedIcon = context.getResources().getDrawable(R.drawable.image_unselected);
                        selectedImage.setImageDrawable(unselectedIcon);
                    } else{
                        selectedContents ++;
                        Drawable selectedIcon = context.getResources().getDrawable(R.drawable.image_selected);
                        selectedImage.setImageDrawable(selectedIcon);
                    }

                    int position = getAdapterPosition();
                    GalleryContentRealm galleryContentRealm = galleryContents.get(position);
                    for (int i = 0; i < onItemClickedListeners.size(); i++) {
                        onItemClickedListeners.get(i).onSelectItem(galleryContentRealm, position);
                    }
                }
                else{

                    new AlertDialog.Builder(context)
                            .setTitle(R.string.error)
                            .setMessage(R.string.gallery_max_items)
                            .setNegativeButton(android.R.string.ok, null)
                            .show();

                }


            }catch(Exception e){
                System.out.println("Error " + e.getMessage());
            }

        }

    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener){

        this.onBottomReachedListener = onBottomReachedListener;
    }

    public void emptyItemSelecteds() {
        selectedContents = 0;
        itemsSelected.clear();

    }

    public void selectAllItems(){
        emptyItemSelecteds();
        int i = 0;
        for (GalleryContentRealm content: galleryContents){
            itemsSelected.add(content.getId());
            selectedContents += 1;
        }
    }
    GalleryAdapter(Context context, List<GalleryContentRealm> galleryContents,
                   List<Integer> itemsSelected, boolean isInSelectedMode,  boolean isInDeleteMode, OnCheckPermissionsCallback onCheckPermissionsCallback){
        this.context = context;
        this.galleryContents = galleryContents;
        this.itemsSelected = itemsSelected;
        this.isInSelectedMode = isInSelectedMode;
        this.userPreferences = new UserPreferences(context);
        this.mediaManager = new MediaManager(context);
        this.onCheckPermissionsCallback = onCheckPermissionsCallback;
        this.isInDeleteMode = isInDeleteMode;

    }

    public void setItemsSelected(ArrayList<Integer> itemsSelected) {
        this.itemsSelected = itemsSelected;
    }

    public void setGalleryContents(List<GalleryContentRealm> galleryContents) {
        this.galleryContents = galleryContents;
    }

    public void setInSelectedMode(boolean inSelectedMode) {
        isInSelectedMode = inSelectedMode;
        notifyDataSetChanged();
    }

    public void setInDeleteMode(boolean inDeleteMode) {
        isInDeleteMode = inDeleteMode;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.gallery_adapter_item,parent,false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (position == galleryContents.size() - 2){

            onBottomReachedListener.onBottomReached(galleryContents);

        }
        holder.setIsRecyclable(false);
        holder.videoHint.setTag(false);
        holder.videoHint.setVisibility(View.INVISIBLE);
        holder.imageDetail.setImageDrawable(null);
        holder.imageDetail.setVisibility(View.INVISIBLE);

        holder.itemView.setTag(galleryContents.get(position).getIdContent());
        downloadGalleryItem(holder, position, false);

    }

    private void downloadGalleryItem(final ViewHolder tempHolder, final int position, final boolean downloadClicked) {
        if (downloadClicked && !onCheckPermissionsCallback.onCheckPermissionsCallback(position)){
            return;
        }

        tempHolder.progressBar.setVisibility(View.VISIBLE);
        this.mediaManager.downloadGalleryItem(tempHolder, position, galleryContents.get(position).getPath(),galleryContents.get(position).getIdContent(), galleryContents.get(position).getMimeType(), new MediaCallbacksGallery() {
            @Override
            public void onSuccess(HashMap<Integer, Object> response) {

                int theIdContent = (int) response.get(3);
                if(!tempHolder.itemView.getTag().equals(theIdContent)){
                    return;
                }

                String theFilePath = (String) response.get(1);


                if (tempHolder.getAdapterPosition() == NO_POSITION){
                    Log.d("downloadGalleryItem", "NO_POSITION");
                    return;
                }

                File file = new File(theFilePath);
                Uri imageUri = Uri.fromFile(file);
                Log.d("downloadGalleryItem", imageUri.getPath());


                if (!imageUri.toString().equals("file:///")) {
                    tempHolder.videoHint.setTag(true);
                    tempHolder.imageDetail.setVisibility(View.VISIBLE);
                    tempHolder.progressBar.setVisibility(View.INVISIBLE);
                    if(galleryContents == null || tempHolder.getAdapterPosition() >= galleryContents.size() ){
                        return;
                    }
                    GalleryContentRealm content =  galleryContents.get(tempHolder.getAdapterPosition());

                    String mimeType = content.getMimeType();

                    //VIDEO
                    if (mimeType != null && mimeType.startsWith("video")) {
                        tempHolder.videoHint.setImageDrawable(tempHolder.videoHint.getResources()
                                .getDrawable(R.drawable.video_hint));
                        tempHolder.videoHint.setVisibility(View.VISIBLE);
                        tempHolder.videoHint.setOnClickListener(tempHolder);

                        if(content.getThumbnailPath() == null || content.getThumbnailPath().equals("")){
                            GalleryDb galleryDb = new GalleryDb(context);
                            galleryDb.setPathFromIdContent(theIdContent, theFilePath);
                        }
                        tempHolder.imageDetail.setImageDrawable(new BitmapDrawable(tempHolder.imageDetail.getResources(), content.getThumbnailPath()));

                    }
                    else if(mimeType == null){
                        //
                    }
                    //IMAGE
                    else {
                        tempHolder.videoHint.setVisibility(View.INVISIBLE);
                        ImageUtils.setImageToImageView(imageUri, tempHolder.imageDetail, context, true);
                    }
                }

                Drawable selectedImage;
                int id = galleryContents.get(position).getId();
                if (itemsSelected.contains(id)) {
                    selectedImage = context.getResources().getDrawable(R.drawable.image_selected);
                } else {
                    selectedImage = context.getResources().getDrawable(R.drawable.image_unselected);
                }
                tempHolder.selectedImage.setImageDrawable(selectedImage);
                tempHolder.selectedImage.setVisibility(isInSelectedMode ? View.VISIBLE : View.INVISIBLE);

            }

            @Override
            public void onFailure(HashMap<Integer, Object> response) {

                String theFilePath = (String) response.get(1);
                final int position = (int) response.get(2);

                tempHolder.progressBar.setVisibility(View.INVISIBLE);

                //View download button
                if (theFilePath.equals(UserPreferences.AUTO_DOWNLOAD)){

                    tempHolder.videoHint.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tempHolder.progressBar.setVisibility(View.VISIBLE);
                            tempHolder.videoHint.setVisibility(View.INVISIBLE);
                            downloadGalleryItem(tempHolder, position, true);
                        }
                    });
                    tempHolder.videoHint.setVisibility(View.VISIBLE);
                    tempHolder.videoHint.setImageDrawable(tempHolder.videoHint.getResources().getDrawable(R.drawable.download));
                    tempHolder.selectedImage.setVisibility(isInDeleteMode ? View.VISIBLE : View.INVISIBLE);
                }
                Drawable selectedImage;
                int id = galleryContents.get(position).getId();
                if (itemsSelected.contains(id)) {
                    selectedImage = context.getResources().getDrawable(R.drawable.image_selected);
                } else {
                    selectedImage = context.getResources().getDrawable(R.drawable.image_unselected);
                }
                tempHolder.selectedImage.setImageDrawable(selectedImage);
                tempHolder.selectedImage.setVisibility(isInDeleteMode ? View.VISIBLE : View.INVISIBLE);
            }
        },downloadClicked);
    }

    @Override
    public int getItemCount() {
        return galleryContents.size();
    }

    public void addItemClickedListeners(OnItemClicked onItemClicked) {
        onItemClickedListeners.add(onItemClicked);
    }

    public interface OnItemClicked {
        void onViewItem(GalleryContentRealm galleryContentRealm, int index);
        void onSelectItem(GalleryContentRealm galleryContentRealm, int index);
    }

    public interface OnBottomReachedListener {
        void onBottomReached(List<GalleryContentRealm> position);
    }


    public interface OnCheckPermissionsCallback {
        boolean onCheckPermissionsCallback(int position);
    }

}
