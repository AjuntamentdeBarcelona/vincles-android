/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.groups;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskMainActivity;
import cat.bcn.vincles.tablet.activity.operation.TaskMessageDetailActivity;
import cat.bcn.vincles.tablet.adapter.GroupsAdapter;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.MainModel;

public class GroupsListActivity extends GroupsActivity {
    private final String TAG = this.getClass().getSimpleName();
    private GroupModel groupModel = GroupModel.getInstance();
    private ListView lisGroups;
    private GroupsAdapter adapter;
    private int pag = 0;
    private View ll_prev, ll_next;

    public static final int GROUPS_SCREEN_DIVIDER = 5;
    public static final int MAX_ITEM = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_list);

        lisGroups = (ListView) findViewById(R.id.lisGroup);
        ll_next = findViewById(R.id.ll_next);
        ll_prev = findViewById(R.id.ll_prev);

        int height = getResources().getDisplayMetrics().heightPixels/GROUPS_SCREEN_DIVIDER*MAX_ITEM;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) lisGroups.getLayoutParams();
        lp.height = height;
        lisGroups.setLayoutParams(lp);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // User first local data
        groupModel.groupList = groupModel.getGroupList();
        adapter = new GroupsAdapter(this, 0, new ArrayList<VinclesGroup>());
        updateViews();
        addListeners();

        if (!MainModel.avoidServerCalls) {
            showProgressBar(false, getString(R.string.general_download));
            groupModel.getGroupServerList(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    Log.i(TAG, "getGroupServerList() - result");
                    hideProgressBar();

                    groupModel.groupList = groupModel.getGroupList();
                    updateViews();
                }

                @Override
                public void onFailure(Object error) {
                    Log.e(TAG, "getGroupServerList() - error: " + error);
                    hideProgressBar();
                    Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_messsage_load_list), Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
    }

    private void updateViews() {
        List<VinclesGroup> lst_pag = new ArrayList<VinclesGroup>();
        for (int i = 0; i < Math.min(MAX_ITEM,groupModel.groupList.size()); i++) {
            lst_pag.add(groupModel.groupList.get(pag*MAX_ITEM + i));
        }
        adapter.clear();
        adapter.addAll(lst_pag);
        adapter.notifyDataSetChanged();
        checkNavigationState();
    }

    private void addListeners() {
        // ListView Item Click Listener
        lisGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                groupModel.currentGroup = (VinclesGroup) lisGroups.getItemAtPosition(position);
                // Go to detail
                startActivity(new Intent(GroupsListActivity.this, GroupsChatActivity.class));
            }
        });

        // Assign adapter to ListView
        lisGroups.setAdapter(adapter);
    }

    public void prevMessage(View view) {
        if (pag <= 0)
            return;
        pag--;
        adapter.clear();
        List<VinclesGroup> lst_pag = new ArrayList<VinclesGroup>();
        for (int i = 0; i < Math.min(MAX_ITEM,groupModel.groupList.size()); i++) {
            lst_pag.add(groupModel.groupList.get(pag*MAX_ITEM + i));
        }
        adapter.addAll(lst_pag);
        adapter.notifyDataSetChanged();
        checkNavigationState();
    }

    public void nextMessage(View view) {
        if (pag >= groupModel.groupList.size() / MAX_ITEM || (groupModel.groupList.size() <= (pag+1)*MAX_ITEM))
            return;
        pag++;
        adapter.clear();
        List<VinclesGroup> lst_pag = new ArrayList<VinclesGroup>();
        for (int i = 0; i < Math.min(MAX_ITEM,groupModel.groupList.size()) && (pag*MAX_ITEM + i < groupModel.groupList.size()); i++) {
            lst_pag.add(groupModel.groupList.get(pag*MAX_ITEM + i));
        }
        adapter.addAll(lst_pag);
        adapter.notifyDataSetChanged();
        checkNavigationState();
    }

    private void checkNavigationState() {
        if (pag == 0) {
            ll_prev.setEnabled(false);
            if (groupModel.groupList.size()>MAX_ITEM) {
                ll_next.setEnabled(true);
            } else {
                ll_next.setEnabled(false);
            }
        } else {
            if (pag*MAX_ITEM >= groupModel.groupList.size()) {
                ll_next.setEnabled(false);
            } else {
                ll_next.setEnabled(false);
            }
            ll_prev.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, TaskMainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
