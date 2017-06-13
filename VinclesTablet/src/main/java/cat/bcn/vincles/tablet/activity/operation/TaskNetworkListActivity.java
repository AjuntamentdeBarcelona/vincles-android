/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.List;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.model.FeedModel;
import cat.bcn.vincles.tablet.model.TaskModel;

public class TaskNetworkListActivity extends TaskActivity {
    public static final String OFFSET_STRING = "USERLIST_OFFSET";
    private static final String TAG = "TaskNetworkListActivity";
    private List<User> userlist;
    private int radius, totalWidth, totalHeight, layoutWidth, layoutHeight;
    private int centerOffset = -2, userListOffset;
    private int imageOffsetWidth = -0;
    private int imageOffsetHeight = -0;
    private ViewGroup roundListCanvas, roundListBackground;
    private View ll_prev, ll_next;
    private TextView messageCounter;

    private final int maxUsersInRoundview = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_network);
        taskModel.code = null;

        userListOffset = getIntent().getIntExtra(OFFSET_STRING, 0);
        Log.d(null, "USER OFFSET : " + userListOffset);

        roundListCanvas = (ViewGroup) findViewById(R.id.roundListCanvas);
        roundListBackground = (ViewGroup) findViewById(R.id.roundListBackground);
        messageCounter = (TextView) findViewById(R.id.messageCounter);

        ll_next = findViewById(R.id.ll_next);
        ll_prev = findViewById(R.id.ll_prev);

        imageOffsetWidth = -getResources().getDimensionPixelSize(R.dimen.task_network_roundlist_item_size)/2;
        imageOffsetHeight = -getResources().getDimensionPixelSize(R.dimen.task_network_roundlist_item_size)/2;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Use first local data
        userlist = taskModel.getUserList();

        initRoundList();

        taskModel.getUserServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "getUserServerList() - result");
                // Now, load from local user list updated!!!
                userlist = taskModel.getUserList();
                refreshList();
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getUserServerList() - error: " + error);
                String errorMessage = mainModel.getErrorByCode(error);
                Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void refreshList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                painthRoundList();
            }
        });
    }

    public void associate(View view) {
        Log.i(TAG, "associate()");

        taskModel.generateCode(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "generateCode() - code: " + result.toString());

                // Go to next screen
                taskModel.code = result.toString();
                taskModel.view = TaskModel.TASK_NETWORK_CODE;
                startActivity(new Intent(TaskNetworkListActivity.this, TaskNetworkDetailActivity.class));
            }

            @Override
            public void onFailure(Object error) {
                Log.i(TAG, "confirm() - error: " + error);
                String errorMessage = mainModel.getErrorByCode(error);
                Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }




    ////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    ////               ROUND LIST THINGS                    \\\\

    ////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    private void initRoundList() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        totalWidth = size.x;
        totalHeight = size.y;
        radius = size.y/2 - size.y/10 -40;
        Log.d(TAG, "TOTAL SIZE: " + totalWidth + ", " + totalHeight);

        ViewTreeObserver observer = roundListCanvas.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                layoutWidth = roundListCanvas.getWidth();
                layoutHeight = roundListCanvas.getHeight();
                Log.d(TAG, "CALCULATED VIEW SIZE: " + layoutWidth + ", " + layoutHeight);
                roundListCanvas.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                refreshList();
            }
        });


        // ADD MAIN PICTURE
        ImageView imgPhoto = (ImageView) findViewById(R.id.mainImgPhoto);
        if (mainModel.currentUser != null) {
            if (mainModel.currentUser.idContentPhoto != null) {
                if (!isFinishing())
                    Glide.with(this)
                        .load(VinclesConstants.getImageDirectory() + "/" + mainModel.currentUser.imageName)
                        .signature(new StringSignature(mainModel.currentUser.idContentPhoto.toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(imgPhoto);
            } else {
                Log.w(TAG, mainModel.currentUser.alias + " has idContentPhoto null!");
            }
        }
    }

    private void painthRoundList() {
        checkButtons();
        roundListCanvas.removeAllViews();
        View circle = roundListBackground.getChildAt(0);
        ViewGroup.LayoutParams params = circle.getLayoutParams();
        params.height = radius*2;
        params.width = radius*2;
        circle.setLayoutParams(params);

        LayoutInflater inflater = getLayoutInflater();

        int maxListed = maxUsersInRoundview;
        if (userListOffset + maxUsersInRoundview > userlist.size()) maxListed = userlist.size() - userListOffset;

        double userspace = Math.PI * 2 / (maxListed);
        double offset = 0;
        double[] position;
        int userlist_number = 0;
        for (int i = 0; i < maxUsersInRoundview; i++) {
            userlist_number = i + userListOffset;
            if (userlist_number > userListOffset + maxListed-1) break;
            position = getPosition(layoutWidth/2 + centerOffset, layoutHeight/2 + centerOffset,
                    radius, offset+(userspace*userlist_number));
            createUserAtPosition(inflater,
                    (int)position[0]+imageOffsetWidth+centerOffset,
                    (int)position[1]+imageOffsetHeight+centerOffset,
                    userlist_number);
        }
    }

    private View createUserAtPosition(LayoutInflater inflater, int posX, int posY, int userlist_position) {
        View user = inflater.inflate(R.layout.item_roundlist_user, null);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(posX, posY, 0, 0);
        user.setLayoutParams(layoutParams);
        roundListCanvas.addView(user);

        // ADD TAG
        user.setTag(userlist_position);

        // ADD TEXT
        ((TextView) user.findViewById(R.id.texUserName)).setText(userlist.get(userlist_position).name);

        // ADD PICTURE
        ImageView imgPhoto = (ImageView) user.findViewById(R.id.imgPhoto);
        if (!this.isFinishing()) {
            if (userlist.get(userlist_position) != null) {
                if (userlist.get(userlist_position).idContentPhoto != null) {
                    if (!isFinishing())
                        Glide.with(this)
                            .load(mainModel.getUserPhotoUrlFromUser(userlist.get(userlist_position)))
                            .signature(new StringSignature(userlist.get(userlist_position).idContentPhoto.toString()))
                            .error(R.drawable.user).placeholder(R.color.superlightgray)
                            .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                            .into(imgPhoto);
                } else {
                    Log.w(TAG, userlist.get(userlist_position).alias + " has idContentPhoto null!");
                }
            }
        }

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int)v.getTag();
                Log.d(null, "POSITION: " + position + " / USER: " + userlist.get(position).name);
                taskModel.currentUser = userlist.get(position);
                taskModel.view = TaskModel.TASK_DELETE_USER;
                startActivity(new Intent(TaskNetworkListActivity.this, TaskNetworkUserActionActivity.class));
            }
        });

        return user;
    }

    private double[] getPosition(int centerx, int centery, float radius, double angle) {
        double[] position = new double[2];
        position[0] = centerx + radius * Math.cos(angle);
        position[1] = centery + radius* Math.sin(angle);
        return position;
    }

    private void limitUsersInOrderToSeeRoundView() {
        int i = 0;
        while (i <= userlist.size()) {
            if (i > maxUsersInRoundview-2)
                userlist.remove(i-1);
            else i++;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //////////////// PAGINATION \\\\\\\\\\\\\\\\\\
    private void checkButtons() {
        ll_next.setEnabled(true);
        ll_prev.setEnabled(true);
        if (userListOffset + maxUsersInRoundview > userlist.size()-1) ll_next.setEnabled(false);
        if (userListOffset == 0) ll_prev.setEnabled(false);

        refreshCounter();
    }

    private void refreshCounter() {
        int first = userListOffset + 1;
        int last = userListOffset + maxUsersInRoundview;
        if (last > userlist.size()) last = userlist.size();
        if (first == last) messageCounter.setText(  first + "/" + userlist.size());
        else messageCounter.setText(  first + "-" + last + "/" + userlist.size());
    }

    public void showMore(View v) {
        final Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if(((String)v.getTag()).equalsIgnoreCase("less")) {
            userListOffset -= maxUsersInRoundview;
            if (userListOffset < 0 ) userListOffset = 0;
            intent.putExtra(OFFSET_STRING, userListOffset);
        } else {
            if (userListOffset + maxUsersInRoundview <= userlist.size()) userListOffset += maxUsersInRoundview;
            intent.putExtra(OFFSET_STRING, userListOffset);
        }

        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
