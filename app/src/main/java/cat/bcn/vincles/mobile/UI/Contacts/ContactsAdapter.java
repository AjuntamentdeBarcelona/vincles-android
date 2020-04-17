package cat.bcn.vincles.mobile.UI.Contacts;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.MediaCallbacksGallery;
import cat.bcn.vincles.mobile.Client.Business.MediaManager;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private Context context;
    private List<Contact> contactList;
    private ContactsAdapterListener listener;
    private boolean selectionEnabled;
    private List<OnItemClicked> onItemClickedListeners = new ArrayList<>();
    private boolean deleteVisibility;
    private boolean deleteIsInvite;
    private boolean groupDetailIcons;
    private List<Integer> selectedContactIds = new ArrayList<>();
    Integer maxItemsSelected = 5;
    private MediaManager mediaManager;

    private boolean showNotificationsNumber = true;

    public void setMaxItemsSelected(int max) {
        Log.d("maxItems", "setMaxItemsSelected: " + String.valueOf(max));
        maxItemsSelected = max;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView contactsIcon;
        TextView contactsText;
        TextView stateText;
        TextView notificationsNumber;
        ProgressBar progressBar;
        LinearLayout deleteLayout;
        RelativeLayout itemLayout;
        ImageView selected;
        ImageView deleteIcon;
        TextView deleteText;

        // Contact id, to keep track of the selected/unselected contacts
        Integer chatId;

        public ViewHolder(View itemView) {
            super(itemView);
            contactsIcon = itemView.findViewById(R.id.contactsIcon);
            contactsText = itemView.findViewById(R.id.contactsText);
            stateText = itemView.findViewById(R.id.state_text);
            progressBar = itemView.findViewById(R.id.progressbar);
            deleteLayout = itemView.findViewById(R.id.deleteLayout);
            deleteIcon = deleteLayout.findViewById(R.id.delete_iv);
            deleteText = deleteLayout.findViewById(R.id.delete_tv);
            itemLayout = itemView.findViewById(R.id.itemLayout);
            selected = itemView.findViewById(R.id.selected);
            notificationsNumber = itemView.findViewById(R.id.notifications_number);
            if (!selectionEnabled) {
                selected.setVisibility(View.GONE);
            }
        }

        public void onSelectItem() {
            Drawable.ConstantState selectedImageState = context.getResources().getDrawable(R.drawable.image_selected).getConstantState();
            Drawable.ConstantState actualState = selected.getDrawable().getConstantState();
            Log.d("maxItems", String.valueOf(maxItemsSelected));
            if(maxItemsSelected == -1 || selectedContactIds.size() < maxItemsSelected ||  actualState.equals(selectedImageState)) {
                for (int i = 0; i < onItemClickedListeners.size(); i++) {
                    Contact contact = contactList.get(getAdapterPosition());
                    // If the contact was selected, unselect it
                    if (selectedContactIds.contains(contact.getIdChat())) {
                        selectedContactIds.remove((Integer)contact.getIdChat());
                        onItemClickedListeners.get(i).onContactUnselected(contact);
                    // If the contact was unselected, select it
                    } else {
                        selectedContactIds.add(contact.getIdChat());
                        onItemClickedListeners.get(i).onContactSelected(contact);
                    }
                }


                Drawable unselectedImage = context.getResources().getDrawable(R.drawable.image_unselected);
                Drawable selectedImage = context.getResources().getDrawable(R.drawable.image_selected);
                Drawable stateBackground = actualState.equals(selectedImageState) ? unselectedImage : selectedImage;
                selected.setImageDrawable(stateBackground);
            } else {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(R.string.gallery_max_contacts)
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
            }

            if(maxItemsSelected != -1)
            listener.updateShareTitle();

        }

    }

    private void setSelectionDrawable(ImageView selectIndicator, boolean isSelected) {
        Drawable unselectedImage = context.getResources().getDrawable(R.drawable.image_unselected);
        Drawable selectedImage = context.getResources().getDrawable(R.drawable.image_selected);
        selectIndicator.setImageDrawable(isSelected ? selectedImage : unselectedImage);
    }

    public ContactsAdapter(Context context,List<Contact> contactList, ContactsAdapterListener listener){
        this.context = context;
        this.contactList = contactList;
        this.listener = listener;
        this.mediaManager = new MediaManager(context);
        Log.d("maxItems", String.valueOf(maxItemsSelected));

    }

    public ContactsAdapter(Context context, List<Contact> contacts, List<Integer> selected, ContactsAdapterListener listener){
        this.context = context;
        this.contactList = contacts;
        setSelectedContacts(selected);
        this.listener = listener;
        this.mediaManager = new MediaManager(context);
    }

    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(context).inflate(R.layout.contacts_adapter_item, parent, false);
        ContactsAdapter.ViewHolder vh = new ContactsAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ContactsAdapter.ViewHolder holder, final int position) {
        // Save the contactId for this ViewHolder
        holder.chatId = contactList.get(position).getIdChat();

        holder.itemView.setTag(contactList.get(position).getId());

        //Put Text
        holder.contactsIcon.setVisibility(View.GONE);
        putImage("bind", holder);
        holder.contactsText.setText(contactList.get(position).getName() + " "
                + contactList.get(position).getLastname());
        String state = contactList.get(position).getState();
        Log.d("contactType", "state: " + String.valueOf(state));
        Log.d("contactType", "getIdChat: " + String.valueOf(contactList.get(position).getIdChat()));
        Log.d("contactType", "getId: " + String.valueOf(contactList.get(position).getId()));


        if (state != null && state.length() > 0) {
            holder.stateText.setVisibility(View.VISIBLE);
            holder.stateText.setText(OtherUtils.getMeetingInvitationState(state, holder.stateText.getResources()));
        } else {
            holder.stateText.setVisibility(View.GONE);
        }
        //Put Image
        holder.progressBar.setVisibility(View.VISIBLE);


        boolean isUser = (deleteVisibility && (contactList.get(position).getType()
                == Contact.TYPE_USER_CIRCLE || contactList.get(position).getType()
                == Contact.TYPE_CIRCLE_USER) || (groupDetailIcons));
        Log.d("isUser", "isUser: "  + String.valueOf(isUser));

        this.mediaManager.downloadContactItem(holder,position,contactList.get(position).getPath(),contactList.get(position).getId(),
                contactList.get(position).getType(), contactList.get(position).getIdContentPhoto(), isUser, new MediaCallbacksGallery() {
            @Override
            public void onSuccess(HashMap<Integer, Object> response) {
                ContactsAdapter.ViewHolder theHolder = (ContactsAdapter.ViewHolder) response.get(0);
                String theFilePath = (String) response.get(1);

                int theIdContent = (int) response.get(3);
                if(!holder.itemView.getTag().equals(theIdContent)){
                    return;
                }

                if (theHolder.getAdapterPosition() == NO_POSITION){
                    Log.d("downloadGalleryItem", "NO_POSITION");
                    return;
                }
                contactList.get(position).setPath(theFilePath);
                putImage(theFilePath, theHolder);


            }

            @Override
            public void onFailure(HashMap<Integer, Object> response) {
                ContactsAdapter.ViewHolder theHolder = (ContactsAdapter.ViewHolder) response.get(0);
                putImage("placeholder", theHolder);
                ((Activity)context).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        holder.progressBar.setVisibility(View.GONE);

                    }
                });
            }
        });

        // If we are in selection mode
        if (selectionEnabled) {
            setSelectionDrawable(holder.selected, selectedContactIds != null && selectedContactIds.contains(holder.chatId));
        }


        holder.deleteLayout.setVisibility((deleteVisibility && (contactList.get(position).getType()
                == Contact.TYPE_USER_CIRCLE || contactList.get(position).getType()
                == Contact.TYPE_CIRCLE_USER) || (groupDetailIcons) ? View.VISIBLE : View.GONE));
        if (deleteIsInvite) {
            ((TextView)holder.deleteLayout.findViewById(R.id.delete_tv)).setText(R.string.calendar_stop_inviting);
        }

        //Click listeners if is in delete/selection mode or not

        if (deleteIsInvite) {
            holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.deleteCircle(contactList.get(position).getId(), contactList.get(position).getName() + " " + contactList.get(position).getLastname());
                }
            });
        } else if (groupDetailIcons) {
            holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.deleteCircle(contactList.get(position).getId(),
                            String.valueOf(contactList.get(position).getType()));
                }
            });
            Log.d("gdp","adapter type:"+contactList.get(position).getType());
            switch (contactList.get(position).getType()) {
                case Contact.TYPE_DYNAMIZER:
                    Drawable drawable = holder.deleteIcon.getResources().getDrawable(
                            R.drawable.ic_notification_small);
                    Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                    wrappedDrawable = wrappedDrawable.mutate();
                    DrawableCompat.setTint(wrappedDrawable, holder.deleteIcon.getResources()
                            .getColor(R.color.colorPrimary));
                    holder.deleteIcon.setImageDrawable(wrappedDrawable);
                    holder.deleteText.setText(holder.deleteIcon.getResources().getString(R.string.chat_button_dinamizer));
                    holder.deleteText.setTextColor(holder.deleteText.getResources().getColor(R.color.darkGray));
                    break;
                case Contact.TYPE_GROUP:
                    holder.deleteIcon.setImageDrawable(holder.deleteIcon.getResources().getDrawable(R.drawable.add_contact));
                    holder.deleteIcon.setBackground(holder.deleteIcon.getResources().getDrawable(R.drawable.red_circle_white_background));

                    holder.deleteText.setText(holder.deleteIcon.getResources().getString(R.string.group_detail_send_invite));
                    UserPreferences userPreferences = new UserPreferences(context);
                    if(userPreferences.getInvitedUsers().contains(String.valueOf(contactList.get(position).getId()))){
                        holder.deleteText.setText(holder.deleteIcon.getResources().getString(R.string.group_detail_resend_invite));
                    }

                    holder.deleteText.setTextColor(holder.deleteText.getResources().getColor(R.color.colorPrimary));
                    int padding = (int) holder.deleteIcon.getResources().getDimension(R.dimen.group_detail_invite_icon_padding);
                    holder.deleteIcon.setPadding(padding,padding,padding,padding);
                    break;
                default:
                    holder.deleteLayout.setVisibility(View.INVISIBLE);
                    break;
            }
            if (contactList.get(position).getId() == new UserPreferences().getUserID()) {
                holder.deleteLayout.setVisibility(View.INVISIBLE);
                holder.contactsText.setText(R.string.chat_username_you);
            }
        } else {
            holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.deleteLayout.getVisibility() == View.VISIBLE) {
                        listener.deleteCircle(contactList.get(position).getId(), contactList.get(position).getName() + " " + contactList.get(position).getLastname());
                    } else if (selectionEnabled){
                        if (holder.progressBar.getVisibility() == View.GONE) {
                            holder.onSelectItem();
                        }
                    } else {
                        boolean isGroupChat = contactList.get(position).getType() == Contact.TYPE_GROUP;
                        boolean isDynamizer = contactList.get(position).getType() == Contact.TYPE_DYNAMIZER;
                        listener.clickedCircle(String.valueOf(contactList.get(position).getIdChat()), isGroupChat,isDynamizer);
                    }
                }
            });
        }

        //Notifications

        if (showNotificationsNumber && contactList.get(position).getNumberNotifications() > 0) {
            holder.notificationsNumber.setVisibility(View.VISIBLE);
            holder.notificationsNumber.setText(String.valueOf(contactList.get(position).getNumberNotifications()));
            holder.contactsIcon.setBackground(context.getResources().getDrawable(R.drawable.red_circle_contact));
        } else {
            holder.notificationsNumber.setVisibility(View.GONE);
            holder.contactsIcon.setBackground(null);
        }

        //ContactsRepository.fillContacts(context, holder.contactsIcon, contactList.get(position).getPath());
    }

    private void putImage(final Object response, final ViewHolder holder) {
        Log.d("contectImage", "putImage response: " + response);
        if (response.equals("bind")){
            return;
        }
        ImageUtils.setImageToImageViewWithCallbacks(response.equals("placeholder") || response.equals("")  ?
                context.getResources().getDrawable(R.drawable.user)
                : new File(String.valueOf(response)), holder.contactsIcon,  context, R.drawable.userwhite);

        ((Activity)context).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                holder.contactsIcon.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
            }
        });

    }

    public void setDeleteVisibility(boolean deleteVisibility) {
        this.deleteVisibility = deleteVisibility;
    }

    public void setDeleteIsInvite(boolean deleteIsInvite) {
        this.deleteIsInvite = deleteIsInvite;
    }

    public void setContactSelectionEnabled(boolean enabled) {
        this.selectionEnabled = enabled;
    }

    public void setGroupDetailIcons(boolean groupDetailIcons) {
        this.groupDetailIcons = groupDetailIcons;
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void removeContact(int id) {
        for (Contact contact : contactList) {
            if (contact.getId() == id) {
                contactList.remove(contact);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public interface ContactsAdapterListener {
        void needContactPicturePath(int contactId, int contactType);
        void deleteCircle(int idUserToUnlink, String contactName);
        void clickedCircle(String idUserSender, boolean isGroupChat, boolean isDynamizer);
        void updateShareTitle();

    }

    public void addItemClickedListeners(OnItemClicked onItemClicked) {
        onItemClickedListeners.add(onItemClicked);
    }

    public interface OnItemClicked {
        void onContactSelected(Contact selectedContact);
        void onContactUnselected(Contact unselectedContact);
    }

    public void setShowNotificationsNumber(boolean showNotificationsNumber) {
        this.showNotificationsNumber = showNotificationsNumber;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public void setSelectedContacts(List<Integer> selectedContactIds) {
        this.selectedContactIds = selectedContactIds;
    }
}
