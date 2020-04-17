package cat.bcn.vincles.mobile.UI.Calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Gallery.GalleryAdapter;

public class DurationPickerDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    DurationPicked listener;
    int which = 1;
    boolean pendingResult = false;

    public void setListener(DurationPicked listener) {
        this.listener = listener;
        if (pendingResult) {
            listener.onDurationPicked(which);
            pendingResult = false;
        }
    }

    public void setWhich(int which) {
        this.which = which;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String[] items = getResources().getStringArray(R.array.meeting_duration_entries);
        MyAdapter adapter = new MyAdapter(items, which);
        AlertDialog.Builder datePickerDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.calendar_date_length)
                .setSingleChoiceItems(adapter, which, this);
        return datePickerDialog.create();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (listener != null) {
            listener.onDurationPicked(which);
            pendingResult = false;
        } else {
            this.which = which;
            pendingResult = true;
        }
        dialog.dismiss();
    }


    public interface DurationPicked {
        void onDurationPicked(int which);
    }

    private class MyAdapter extends BaseAdapter {

        String[] items;
        int checkedPosition;

        MyAdapter(String[] items, int checkedPosition) {
            this.items = items;
            this.checkedPosition = checkedPosition;
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.duration_list_item, null);
                holder.textView = convertView.findViewById(R.id.text);
                holder.check = convertView.findViewById(R.id.check);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.textView.setText(items[position]);
            if (position == checkedPosition) {
                holder.check.setImageDrawable(getResources().getDrawable(R.drawable.check_filter));
                holder.textView.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                holder.check.setImageDrawable(getResources().getDrawable(R.drawable.check_filter_hover));
                holder.textView.setTextColor(getResources().getColor(R.color.colorBlack));
            }
            return convertView;
        }

    }

    private class ViewHolder {
        TextView textView;
        ImageView check;
    }

}
