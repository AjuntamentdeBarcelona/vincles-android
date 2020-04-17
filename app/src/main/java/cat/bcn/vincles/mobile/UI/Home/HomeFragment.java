package cat.bcn.vincles.mobile.UI.Home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.StartGuide.StartGuideActivity;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;
import io.realm.RealmResults;


public class HomeFragment extends BaseFragment implements View.OnClickListener, HomeFragmentView {

    private OnFragmentInteractionListener mListener;
    HomePresenterContract presenter;
    UserPreferences userPreferences;
    View galeriView, calendarView, bellView;
    TextView calendarNumber, notificationsNumber;
    ViewGroup rootView;
    List<Contact> contacts;
    boolean isUserSenior;
    TextView noContactsError;
    public static boolean needCallRest = false;
    public static HomeFragment shared;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance(FragmentResumed listener) {
        HomeFragment fragment = new HomeFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_HOME);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        HomeFragment.shared = fragment;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("homeimg", "onCreate HomeFragment");
        userPreferences = new UserPreferences(getContext());
        isUserSenior = userPreferences.getIsUserSenior();
        contacts = new ArrayList<>();
        presenter = new HomePresenter((BaseRequest.RenewTokenFailed) getActivity(),this);
    }

    @Override
    public void onStart() {
        super.onStart();

        this.updateDynamizerPictures();
    }

    private void updateDynamizerPictures(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Dynamizer> x = realm.where(Dynamizer.class).findAll();
        for(int i = 0; i<x.size(); i++){
            Dynamizer d = x.get(i);
            Log.d("dynamizer name", d.getName());
            String accessToken = new UserPreferences().getAccessToken();


            GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(new BaseRequest.RenewTokenFailed() {
                @Override
                public void onRenewTokenFailed() {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.close_session)
                            .setMessage(R.string.close_session_token_message)
                            .setNegativeButton(android.R.string.ok, null)
                            .show();
                }
            }, String.valueOf(d.getId()));
            getUserPhotoRequest.addOnOnResponse(new GetUserPhotoRequest.OnResponse() {
                @Override
                public void onResponseGetUserPhotoRequest(Uri photo, String userID, int viewID, int contactType) {
                    new UserGroupsDb(MyApplication.getAppContext()).setGroupDynamizerAvatarPath(Integer.parseInt(userID), photo.getPath());
                    new UsersDb(MyApplication.getAppContext()).setPathAvatarToUser(Integer.parseInt(userID), photo.getPath());
                    //returnHash.put(1,photo.getPath());
                    //mediaCallbacks.onSuccess(returnHash);

                }

                @Override
                public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
                    //returnHash.put(1, UserPreferences.AUTO_DOWNLOAD);
                    //mediaCallbacks.onFailure(returnHash);
                }
            });
            getUserPhotoRequest.doRequest(accessToken);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_home));

        Log.d("homeimg", "onResume HomeFragment");
        if (presenter != null) {
            presenter.updateNotificationsNumber();
            presenter.getContacts(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("homeimg", "onCreateActivity HomeFragment");
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);
        noContactsError = rootView.findViewById(R.id.no_contacts_error);

        setupAvatarSizes();

        bellView =  rootView.findViewById(R.id.bell_background);
        galeriView =  rootView.findViewById(R.id.album);
        calendarView = rootView.findViewById(R.id.calendar);
        calendarNumber = rootView.findViewById(R.id.calendar_number);
        notificationsNumber = rootView.findViewById(R.id.numberAlerts);

        ViewGroup contactsView = rootView.findViewById(R.id.see_contacts_parent);
        if (isUserSenior) {
            contactsView.findViewById(R.id.family_button).setOnClickListener(this);
            contactsView.findViewById(R.id.groups_button).setOnClickListener(this);
        } else {
            contactsView.removeAllViews();
            inflater.inflate(R.layout.fragment_home_see_contacts, contactsView);
            contactsView.findViewById(R.id.see_contacts).setOnClickListener(this);
        }

        ImageView avatar = rootView.findViewById(R.id.userAvatar);

        bellView.setOnClickListener(this);
        galeriView.setOnClickListener(this);
        calendarView.setOnClickListener(this);

        String pathAvatar = userPreferences.getUserAvatar();
       if (!pathAvatar.equals("")) {
            Uri avatarUri = Uri.parse(pathAvatar);
           avatar.setImageURI(avatarUri);
           if(avatar.getDrawable() == null) {
               avatar.setImageResource(R.drawable.user);
           }

       }
        avatar.setOnClickListener(this);

        final TextView userWelcome = rootView.findViewById(R.id.textView);
        userWelcome.setText(getResources().getString(userPreferences.getGender()
                .equalsIgnoreCase("male") ? R.string.welcome_male
                : R.string.welcome_female, userPreferences.getName()));
        userWelcome.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d("ctout","on global layout!");
                if (userWelcome.getWidth() != 0) {
                    ImageUtils.drawCutoutBackground(userWelcome);
                    ViewTreeObserver viewTreeObserver = userWelcome.getViewTreeObserver();
                    viewTreeObserver.removeOnGlobalLayoutListener(this);
                }
            }
        });

        boolean isFirstTimeUserAccessApp = userPreferences.isFirstTimeUserAccessApp();
        if (isFirstTimeUserAccessApp) {
            userPreferences.setFirstTimeUserAccessApp(false);
            Intent intent = new Intent(getContext(), StartGuideActivity.class);
            startActivity(intent);
        }
        //TODO: modify when notifications enabled
        presenter.getContacts(needCallRest);
        needCallRest = false;

        presenter.onCreateView();

        return rootView;
    }

    @Override
    protected void processPendingChanges(Bundle bundle){
        Log.d("notman2","chat processPendingChanges");
        int changesType = bundle.getInt(BaseFragment.CHANGES_TYPE);
        if (changesType == BaseFragment.CHANGES_OTHER_NOTIFICATION) {
            String type = bundle.getString("type");
            switch (type) {
                case "USER_UPDATED":
                case "USER_LINKED":
                case "ADDED_TO_GROUP":
                case "GROUP_UPDATED":
                case "REMOVED_FROM_GROUP":
                case "USER_UNLINKED":
                case "USER_LEFT_CIRCLE":
                case "NEW_MESSAGE":
                case "NEW_CHAT_MESSAGE":
                case "MISSED_CALL":
                    presenter.getContacts(false);
                    break;
            }
        } else if (bundle.getInt(BaseFragment.CHANGES_TYPE) == BaseFragment.CHANGES_ANY_NOTIFICATION) {
            if (presenter != null) presenter.updateNotificationsNumber();
            String type = bundle.getString("type");
            if (type.equals("NEW_MESSAGE") || type.equals("NEW_CHAT_MESSAGE")) {
                presenter.getContacts(false);
            }
        }
        pendingChangeProcessed();
    }


    public void showAvatar (Bitmap bm, int viewId) {
        ImageView avatar = rootView.findViewById(viewId);
        avatar.setImageBitmap(bm);

        //Setting margins so that it is circular
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) avatar.getLayoutParams();
        int sideMargin = (avatar.getWidth() - avatar.getHeight())/2;
        params.rightMargin = sideMargin;
        params.leftMargin = sideMargin;
        avatar.setLayoutParams(params);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.userAvatar) {
            mListener.onUserAvatarClicked();
        } else if (view.getId() == R.id.album) {
            mListener.onGalleryClicked();
        } else if (view.getId() == R.id.calendar) {
            mListener.onCalendarClicked();
        } else if (view.getId() == R.id.see_contacts) {
            mListener.onContactsClicked(OnFragmentInteractionListener.BUTTON_ALL_CONTACTS);
        } else if (view.getId() == R.id.family_button) {
            mListener.onContactsClicked(OnFragmentInteractionListener.BUTTON_FAMILY);
        } else if (view.getId() == R.id.groups_button) {
            mListener.onContactsClicked(OnFragmentInteractionListener.BUTTON_GROUPS);
        } else if (view.getId() == R.id.bell_background) {
            mListener.onNotificationsClicked();
        }


    }

    @Override
    public void onContactsReady(List<Contact> contacts) {
        if (!isAdded()) return;
        for (int i = 0; i < 6; i++) {
            int position = i + 1;
            Contact contact = i >= contacts.size() ? null : contacts.get(i);
            ContactCompoundView contactCompoundView = rootView.findViewById(getResources().getIdentifier(
                    "contact" + position, "id", getContext().getPackageName()));
            if (contactCompoundView == null) {
                break;
            }
            // If there is no contact for this position, clear the information in the view, in case
            // there was some previous contact loaded
            if (contact == null) {
                contactCompoundView.clearView();
            } else {
                if (!this.contacts.contains(contact)) {
                    this.contacts.add(contact);
                }

                TextView name = contactCompoundView.getNameTV();
                if (name == null) break;
                if (contactCompoundView.getTag() != null &&
                        ((Contact)contactCompoundView.getTag()).getId() == contact.getId()) {
                    Log.d("usrhm", "fillContactsHome user is the same");
                } else {
                    Log.d("usrhm", "fillContactsHome user is different");
                }

                contactCompoundView.setTag(contact);
                if (contact.getLastname() != null && contact.getLastname().length() > 0) {
                    name.setText(contact.getName() + " " + contact.getLastname());
                } else {
                    name.setText(contact.getName());
                }

                if (contact.getPath() == null || contact.getPath().equals("")) {
                    Log.d("homecnt", "photo empty, load");
                    presenter.getContactPicture(contact.getId(), contact.getType());
                    getContactProggressbarVisibility(position, true);
                } else {
                    loadAvatarIntoContact(getContactAvatar(position), contact.getPath());
                    getContactProggressbarVisibility(position, false);
                }

                contactCompoundView.setNotificationsNumber(contact.getNumberNotifications());

                contactCompoundView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.getTag() != null && v.getTag() instanceof Contact) {

                            mListener.onContactSelected(String.valueOf(((Contact) v.getTag()).getIdChat()),
                                    ((Contact) v.getTag()).getType() == Contact.TYPE_GROUP,
                                    ((Contact) v.getTag()).getType() == Contact.TYPE_DYNAMIZER);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onUserPictureLoaded(int id, String path) {
        Log.d("homecnt", "onUserPictureLoaded homeFrag");
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getId() == id) {
                contacts.get(i).setPath(path);
                int position = getContactPositionViewInGrid(id);
                getContactProggressbarVisibility(position, false);
                loadAvatarIntoContact(getContactAvatar(position), path);
                break;
            }
        }
    }

    // Retrieve the position (from 1 to 6, both included) of the View containing tha contact with id
    // equals to the contactId param.
    private int getContactPositionViewInGrid(int contactId) {
        Activity activity = getActivity();
        if (isAdded() && activity != null) {
            for (int i = 1; i < 7; i++) {
                View contactView = rootView.findViewById(getResources().getIdentifier("contact" + i, "id", getContext().getPackageName()));
                if (contactView != null) {
                    Contact contact = (Contact) contactView.getTag();
                    if (contact != null && contact.getId() == contactId) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public void showNoContactsError() {
        if (noContactsError!= null) noContactsError.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoContactsError() {
        if (noContactsError!= null) noContactsError.setVisibility(View.GONE);
    }

    @Override
    public void setCalendarNumber(final int number) {
        if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (number > 0) {
                    calendarNumber.setVisibility(View.VISIBLE);
                    calendarNumber.setText(number+"");
                } else {
                    calendarNumber.setVisibility(View.GONE);
                }

            }
        });
    }

    @Override
    public void setNotificationsNumber(final int number) {
        if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notificationsNumber.setVisibility(number==0 ? View.INVISIBLE : View.VISIBLE);
                notificationsNumber.setText(number+"");
            }
        });
    }

    private ImageView getContactAvatar(int which) {
        if (!isAdded()) return null;
        ContactCompoundView contactCompoundView = rootView.findViewById(getResources().getIdentifier(
                "contact" + (which), "id", getContext().getPackageName()));
        if (contactCompoundView == null) return null;
        return contactCompoundView.getAvatarIV();
    }

    private void getContactProggressbarVisibility(int which, final boolean isVisible) {
        if (!isAdded()) return;
        if (getContext() != null) {
            final ContactCompoundView contactCompoundView = rootView.findViewById(getResources().getIdentifier(
                    "contact" + (which), "id", getContext().getPackageName()));
            if (contactCompoundView != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactCompoundView.getProgressBar().setVisibility(isVisible ? View.VISIBLE : View.GONE);
                        if (isVisible) contactCompoundView.avatarIV.setImageDrawable(null);
                    }
                });
            }
        }

    }

    private void loadAvatarIntoContact(final ImageView imageView, final String path) {
        //Setting margins so that it is circular

        Log.d("qwe","load image, path:"+path);
        if (imageView != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    /*
                    final SimpleTarget simpleTarget = new SimpleTarget<BitmapDrawable>() {
                        @Override
                        public void onResourceReady(@NonNull BitmapDrawable resource, @Nullable Transition<? super BitmapDrawable> transition) {
                            Glide.with(imageView.getContext())
                                    .load(path.equals("placeholder") ?
                                            getResources().getDrawable(R.drawable.user)
                                            : new File(path))
                                    .apply(new RequestOptions()
                                            .centerCrop())
                                    .into(imageView);

                        }
                    };
                    Glide.with(getContext())
                            .load(path.equals("placeholder") ?
                                    getResources().getDrawable(R.drawable.user)
                                    : new File(path))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, final Target<Drawable> target, boolean isFirstResource) {
                                    if (isAdded()) {
                                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.user));
                                    }
                                    return true;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .apply(new RequestOptions().overrideOf(200, 200)
                                    .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(simpleTarget);
                            */
                    ImageUtils.setImageToImageViewWithCallbacks(path.equals("placeholder") ?
                            getResources().getDrawable(R.drawable.user)
                            : new File(path), imageView,  imageView.getContext(), R.drawable.user);
                    /*Glide.with(getContext())
                            .load(new File(path))
                            .apply(RequestOptions.overrideOf(200, 200))
                            .into(imageView);*/
                }
            });
        }

    }

    private void setupAvatarSizes() {
        //ImageView avatar = rootView.findViewById(getContactAvatar(1));
        //setViewTreeForAvatar(avatar);


        /*for (int i = 0; i < 6; i++) {

            if (avatar == null) {
                break;
            } else {
                setViewTreeForAvatar(avatar);
            }
        }*/
    }

    private void setAvatarMargin(int sideMargin) {
        /*for (int i = 0; i < 6; i++) {
            ImageView avatar = rootView.findViewById(getContactAvatar(i+1));
            if (avatar != null) {
                *//*ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) avatar.getLayoutParams();
                params.rightMargin = sideMargin;
                params.leftMargin = sideMargin;
                avatar.setLayoutParams(params);*//*

                //avatar.setVisibility(View.VISIBLE);
            }
        }*/
    }

    private void setViewTreeForAvatar(final ImageView avatar) {
        Log.d("homeimg", "setViewTreeForAvatar");
        ViewTreeObserver vto = avatar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (avatar.getWidth() != 0) {
                    Log.d("homeimg", "load avatar, width:"+avatar.getWidth());
                    avatar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int sideMargin = (avatar.getWidth() - avatar.getHeight()) / 2;
                    setAvatarMargin(sideMargin);
                }
            }
        });
    }


    /**
     *
     * Methods for showing help guide
     *
     */
    @Override
    protected boolean shouldShowMenu() {
        return true;
    }

    @Override
    protected String getTextForPage(int page) {
        switch (page) {
            case 0:
                return getString(R.string.context_help_home_notifications);
            case 1:
                return getString(R.string.context_help_home_see_contacts);
            case 2:
                return getString(R.string.context_help_home_album);
            case 3:
                return getString(R.string.context_help_home_calendar);
            default:
                return null;
        }
    }

    @Override
    protected View getViewForPage(int page) {
        switch (page) {
            case 0:
                return rootView.findViewById(R.id.bell_background);
            case 1:
                return rootView.findViewById(R.id.see_contacts_parent);
            case 2:
                return rootView.findViewById(R.id.album);
            case 3:
                return rootView.findViewById(R.id.calendar);
            default:
                return null;
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        static final int BUTTON_ALL_CONTACTS = 0;
        static final int BUTTON_FAMILY = 1;
        static final int BUTTON_GROUPS = 2;

        public void onUserAvatarClicked();
        public void onGalleryClicked();
        public void onNotificationsClicked();
        public void onCalendarClicked();
        public void onContactsClicked(int which);
        void onContactSelected(String idUserSender, boolean isGroupChat, boolean isDynamizer);
    }
}
