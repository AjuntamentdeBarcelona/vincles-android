package cat.bcn.vincles.mobile.UI.About;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class AboutFragment extends BaseFragment {



    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance(FragmentResumed listener) {
        AboutFragment fragment = new AboutFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_ABOUT);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_about));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        TextView versionTV = v.findViewById(R.id.app_version_tv);
        versionTV.setText(getString(R.string.about_app_version, BuildConfig.VERSION_NAME) + " (" + BuildConfig.VERSION_CODE +")" );

        TextView bodyText = v.findViewById(R.id.about_tv);
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());


        //((TextView)v.findViewById(R.id.about_tv)).setMovementMethod(LinkMovementMethod.getInstance());

        v.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return v;
    }


}
