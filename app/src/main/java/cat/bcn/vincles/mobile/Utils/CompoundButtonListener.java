package cat.bcn.vincles.mobile.Utils;

import android.util.Log;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

public class CompoundButtonListener implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {

    boolean enabled = false;

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            Log.d("onSyncCalendarChanged", "Compoind isEnabled: " + isEnabled());
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

    }
}