/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.tablet.R;

public class DefaultNotificationFragment extends NotificationFragmentTemplate {
    TextView timeText;
    Timer timer;

    public static DefaultNotificationFragment newInstance() {
        DefaultNotificationFragment fragment = new DefaultNotificationFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification_default, container, false);

        // DATE STRINGS:
        String dateString, timeString;
        Calendar cal = Calendar.getInstance();
        dateString = VinclesConstants.getDateString(
                    cal.getTime(),
                    getActivity().getResources().getString(R.string.dateLargeformat),
                    new Locale(getActivity().getResources().getString(R.string.locale_language),
                            getActivity().getResources().getString(R.string.locale_country)));

        ((TextView)rootView.findViewById(R.id.texDay)).setText(dateString);
        timeText = (TextView)rootView.findViewById(R.id.texHour);
        updateClock();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
        timer = new Timer();
        startClockUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.purge();
        timer.cancel();
        timer = null;
    }

    private void startClockUpdate() {
        int secsUntilOnTheMinute=60-Calendar.getInstance().get(Calendar.SECOND);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateClock();
            }
        }, secsUntilOnTheMinute*1000, 60000);
    }

    private void updateClock() {
        Calendar cal = Calendar.getInstance();
        final String timeString = VinclesConstants.getDateString(
                cal.getTime(),
                getActivity().getResources().getString(R.string.timeformat),
                new Locale(getActivity().getResources().getString(R.string.locale_language),
                        getActivity().getResources().getString(R.string.locale_country)));

        if (timeText != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeText.setText(timeString);
                }
            });
        }
    }
}
