package cat.bcn.vincles.mobile.UI.Chats;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.Contacts.ContactsAdapter;
import cat.bcn.vincles.mobile.UI.Gallery.GridSpacingItemDecoration;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class GroupDetailFragment extends BaseFragment implements
        GroupDetailFragmentView, View.OnClickListener, AlertMessage.AlertMessageInterface,
        ContactsAdapter.ContactsAdapterListener, AlertMessage.DismissMessageInterface {

    private static final String PRESENTER_FRAGMENT_TAG = "presenter_group_detail_fragment_tag";

    View rootView;
    TextView description;
    ImageView avatar;
    TextView title;
    OnFragmentInteractionListener interactionListener;
    GroupDetailPresenterContract presenter;

    private AlertMessage invitationSentAlert;

    private  RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private ContactsAdapter contactsAdapter;
    private ArrayList<Contact> contacts;

    AlertNonDismissable alertNonDismissable;

    private int chatId;


    public GroupDetailFragment(){}

    public static GroupDetailFragment newInstance(FragmentResumed listener, int chatId) {
        GroupDetailFragment fragment = new GroupDetailFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CONTACTS);
        Bundle arguments = new Bundle();
        arguments.putInt("chatId", chatId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_group_detail));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            chatId = getArguments().getInt("chatId");
        }

        alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data), true);
        invitationSentAlert = new AlertMessage(this, AlertMessage.TITTLE_INVITATION);
        invitationSentAlert.setDismissMessageInterface(this);

        GroupDetailPresenter presenterFragment;
        if (getFragmentManager() != null) {
            presenterFragment = (GroupDetailPresenter)
                    getFragmentManager().findFragmentByTag(PRESENTER_FRAGMENT_TAG);

            if (presenterFragment != null && savedInstanceState == null) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.remove(presenterFragment).commit();
                presenterFragment = null;
            }
            if (presenterFragment == null) {
                presenterFragment = GroupDetailPresenter.newInstance((BaseRequest.RenewTokenFailed)
                        getActivity(), this, savedInstanceState, chatId);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(presenterFragment, PRESENTER_FRAGMENT_TAG).commit();
            } else {
                presenterFragment.setExternalVars((BaseRequest.RenewTokenFailed)
                        getActivity(), this, savedInstanceState);
            }
            presenter = presenterFragment;
        }

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_group_detail, container, false);
        title = rootView.findViewById(R.id.group_detail_title);
        description = rootView.findViewById(R.id.description);
        avatar = rootView.findViewById(R.id.avatar);
        rootView.findViewById(R.id.back).setOnClickListener(this);

        initRecyclerView();

        presenter.loadData();
        presenter.onCreateView();

        return rootView;
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.recyclerView);

        int numberOfColumns = getResources().getInteger(R.integer.contacts_number_of_columns);
        mLayoutManager = new GridLayoutManager(getContext(), numberOfColumns);
        if (itemDecoration == null) {
            int spacing = getResources().getDimensionPixelSize(R.dimen.adapter_image_spacing);
            itemDecoration = new GridSpacingItemDecoration(numberOfColumns, spacing, false);
        }
        recyclerView.removeItemDecoration(itemDecoration);
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
        contactsAdapter = new ContactsAdapter(getContext(), contacts, this);
        contactsAdapter.setShowNotificationsNumber(false);
        contactsAdapter.setDeleteVisibility(false);
        contactsAdapter.setContactSelectionEnabled(false);
        contactsAdapter.setGroupDetailIcons(true);
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setAdapter(contactsAdapter);
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChatFragment.OnFragmentInteractionListener) {
            interactionListener = (GroupDetailFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                }
                break;
        }
    }

    @Override
    public void showError(Object error) {
        AlertMessage alertMessage = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.dismissSafely();
                presenter.stoppedShowingErrorDialog();
            }
        }, AlertMessage.TITTLE_ERROR);
        alertMessage.setDismissMessageInterface(new AlertMessage.DismissMessageInterface() {
            @Override
            public void onDismissAlertMessage() {
                presenter.stoppedShowingErrorDialog();
            }
        });
        String errorMsg = ErrorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");

    }

    @Override
    public void showInvitationSent() {
        if (invitationSentAlert.alert!= null && invitationSentAlert.alert.isShowing()){return;}
        invitationSentAlert.showMessage(getActivity(), getResources()
                .getString(R.string.group_detail_send_invite_ok_message), "");
    }

    @Override
    public void showSendingData() {
        if(alertNonDismissable.alert!=null && alertNonDismissable.alert.isShowing()){return;}
        alertNonDismissable.showMessage(getActivity());
    }

    @Override
    public void hideSendingData() {
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
        }
    }

    @Override
    public void notifyContactChange() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(contactsAdapter != null){
                        contactsAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }


    @Override
    public void updateAvatar(final String path) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fillHostImage(path);
                }
            });
        }
    }

    @Override
    public void updateGroupName(final String name) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GroupDetailFragment.this.title.setText(name);
                }
            });
        }
    }

    @Override
    public void updateDescription(final String description) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GroupDetailFragment.this.description.setText(description);
                }
            });
        }
    }

    private void fillHostImage(String path) {
        if (path != null && path.length() > 0) {
            path = path.replace("file://","");
            ImageUtils.setImageToImageView(path.equals("placeholder") ?
                    avatar.getContext().getResources().getDrawable(R.drawable.user)
                    : path, avatar, getContext(), true);

            /*
            Glide.with(avatar.getContext())
                    .load(path.equals("placeholder") ?
                            avatar.getContext().getResources().getDrawable(R.drawable.user)
                            : new File(path))
                    .apply(RequestOptions.overrideOf(200, 200))
                    .into(avatar);
                    */
        }
    }


    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        alertMessage.dismissSafely();
        presenter.stoppedShowingMessageDialog();
    }

    @Override
    public void needContactPicturePath(int contactId, int contactType) {
        presenter.loadContactPicture(contactId);
    }

    @Override
    public void deleteCircle(int idUserToUnlink, String contactName) {
        presenter.clickedInvite(idUserToUnlink, Integer.parseInt(contactName));
    }

    @Override
    public void clickedCircle(String idUserSender, boolean isGroupChat, boolean isDynamizer) {
    }

    @Override
    public void updateShareTitle() {

    }

    @Override
    public void onDismissAlertMessage() {
        presenter.stoppedShowingMessageDialog();
    }


    public interface OnFragmentInteractionListener {
    }


}
