package cat.bcn.vincles.mobile.UI.Common;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.R;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;

public class BaseFragment extends Fragment {

    public static final String CHANGES_TYPE = "fragment_changes_type";
    public static final int CHANGES_SHARE_MEDIA = 0;
    public static final int CHANGES_NEW_MESSAGE = 1;
    public static final int CHANGES_NEW_GROUP_MESSAGE = 2;
    public static final int CHANGES_OTHER_NOTIFICATION = 3;
    public static final int CHANGES_ANY_NOTIFICATION = 4;

    FragmentResumed listener;
    int which;

    boolean fragmentIsAfterOnResume = false;
    boolean shouldProcessChanges = false;
    ArrayList<Bundle> pendingChanges = new ArrayList<>();
    //Bundle pendingChanges;

    GuideView guideView;

    private int currentHelpPage;

    @Override
    public void onResume() {
        fragmentIsAfterOnResume = true;
        if (listener != null) listener.onFragmentResumed(which);
        super.onResume();

        if (shouldProcessChanges) {
            if (pendingChanges.size() <= 1) shouldProcessChanges = false;
            if (pendingChanges.size() > 0) processPendingChanges(pendingChanges.remove(0));
        }
    }

    @Override
    public void onPause() {
        fragmentIsAfterOnResume = false;
        if (guideView != null) {
            currentHelpPage = Integer.MAX_VALUE;
            guideView.dismiss();
        }
        //if (guideView != null) while (guideView.isShowing()) guideView.dismiss();
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void setListener(FragmentResumed listener, int which) {
        this.listener = listener;
        this.which = which;
    }

    public interface FragmentResumed {
        public static final int FRAGMENT_HOME = 0;
        public static final int FRAGMENT_CONTACTS = 1;
        public static final int FRAGMENT_NOTIFICATIONS = 2;
        public static final int FRAGMENT_CALENDAR = 3;
        public static final int FRAGMENT_GALLERY= 4;
        public static final int FRAGMENT_CONFIGURATION = 5;
        public static final int FRAGMENT_ABOUT = 6;
        public static final int FRAGMENT_CALL = 7;
        void onFragmentResumed(int which);
    }

    public void notifyChanges(Bundle changes) {
        if (fragmentIsAfterOnResume) {
            shouldProcessChanges = false;
            processPendingChanges(changes);
        } else {
            shouldProcessChanges = true;
            pendingChanges.add(changes);
        }
    }

    protected void processPendingChanges(Bundle bundle){}

    protected void pendingChangeProcessed() {
        if (fragmentIsAfterOnResume && pendingChanges.size() > 0) {
            processPendingChanges(pendingChanges.remove(0));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment_manager, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help && shouldShowMenu()) {
            doHelpActions();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_help);
        if (item != null) item.setVisible(shouldShowMenu());
    }

    protected boolean shouldShowMenu() {
        return false;
    }

    protected void doHelpActions() {
        currentHelpPage = 0;
        runHelp();
    }

    private void runHelp() {
        String text = getTextForPage(currentHelpPage);
        if (text != null && text.length() > 0) {
            View view = getViewForPage(currentHelpPage);
            openGuideView(text, view);
        }
    }

    private void openGuideView(String text, View view) {
        if (view == null || !view.isShown()) return;
        guideView = new GuideView.Builder(getActivity())
                .setContentText(text)
                .setGravity(GuideView.Gravity.center)
                .setDismissType(GuideView.DismissType.anywhere)
                .setTargetView(view)
                .setContentTextSize(getResources().getInteger(R.integer.help_text_size))
                .setGuideListener(new GuideView.GuideListener() {
                    @Override
                    public void onDismiss(View view) {
                        currentHelpPage++;
                        runHelp();
                    }
                })
                .build();
        guideView.show();
    }

    protected String getTextForPage(int page) {
        return null;
    }

    protected View getViewForPage(int page) {
        return null;
    }



}
