/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.adapter.MessageAdapter;
import cat.bcn.vincles.tablet.model.MainModel;

public class TaskMessageListActivity extends TaskActivity {
    private final String TAG = this.getClass().getSimpleName();
    private TextView messageCounter;
    private ListView lisMessage;
    private View textErrorLayout;
    private MessageAdapter adapter;
    private int pag = 0;
    private List<Message> lista;
    private String fromDate = "";
    private int MAX_ITEM = 3;
    private LinearLayout ll_prev, ll_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_message);
        lisMessage = (ListView) findViewById(R.id.lisMessage);
        ll_next = (LinearLayout) findViewById(R.id.ll_next);
        ll_prev = (LinearLayout) findViewById(R.id.ll_prev);
        messageCounter = (TextView) findViewById(R.id.messageCounter);
        textErrorLayout = findViewById(R.id.texError);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lisMessage.post(new Runnable() {
            @Override
            public void run() {
                int listHeight = lisMessage.getMeasuredHeight() / MAX_ITEM;
                adapter = new MessageAdapter(getApplicationContext(), 0, new ArrayList<Message>());
                adapter.setFixedItemHeight(listHeight);
                updateViews();
                addListeners();
            }
        });
    }

    private void updateViews() {
        lista = taskModel.getMessageList();
        List<Message> lst_pag = new ArrayList<Message>();

        fillPaginateMessagesList(lst_pag);
        adapter.addAll(lst_pag);
        if (!MainModel.avoidServerCalls) showProgressBar(false,getString(R.string.general_download));
        else refreshCounter();
        taskModel.getMessageServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "getMessageServerList() - result");
                if (!MainModel.avoidServerCalls) hideProgressBar();

                // Now get update local list!!!
                lista = taskModel.getMessageList();
                adapter.clear();
                // Get last message which is first record!

                Message lastMessage = null;
                if (lista.size() > 0) {
                    lastMessage = lista.get(0);
                    fromDate = String.valueOf(lastMessage.sendTime.getTime() + 1);
                }

                updateNavigationButton();
                List<Message> lst_pag = new ArrayList<Message>();
                if (lista == null || lista.size() <= 0)
                    return;
                fillPaginateMessagesList(lst_pag);
                adapter.addAll(lst_pag);

                refreshCounter();
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getMessageServerList() - error: " + error);
                if (!MainModel.avoidServerCalls) hideProgressBar();
                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_messsage_load_list), Toast.LENGTH_SHORT);
                toast.show();
            }
        }, fromDate, "");
        updateNavigationButton();
    }

    private void refreshCounter() {
        int first = (pag * MAX_ITEM) + 1;
        int last = (pag * MAX_ITEM) + MAX_ITEM;
        if (last > lista.size()) last = lista.size();
        if (first == last) messageCounter.setText(  first + "/" + lista.size());
        else messageCounter.setText(  first + "-" + last + "/" + lista.size());
    }

    private void fillPaginateMessagesList(List<Message> lst_pag) {
        if (lista == null || lista.size() ==  0) textErrorLayout.setVisibility(View.VISIBLE);
        else textErrorLayout.setVisibility(View.GONE);

        for (int i = 0; i < Math.min(MAX_ITEM,lista.size()-pag*MAX_ITEM); i++) {
            lst_pag.add(lista.get(pag*MAX_ITEM + i));
        }
    }

    private void addListeners() {
        // ListView Item Click Listener
        lisMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ListView Clicked item value
                taskModel.setCurrentMessage((Message) lisMessage.getItemAtPosition(position));
                // Go to detail
                startActivity(new Intent(TaskMessageListActivity.this, TaskMessageDetailActivity.class));
            }
        });

        // Assign adapter to ListView
        lisMessage.setAdapter(adapter);
    }

    public void createMessage(View view) {
        Log.i(TAG, "createMessage()");

        taskModel.setCurrentMessage(new Message());
        startActivity(new Intent(this, TaskMessageUserListActivity.class));
    }

    public void prevMessage(View view) {
        if (pag <= 0)
            return;
        pag--;
        adapter.clear();
        List<Message> lst_pag = new ArrayList<Message>();
        for (int i = 0; i < Math.min(MAX_ITEM,lista.size()); i++) {
            lst_pag.add(lista.get(pag*MAX_ITEM + i));
        }
        adapter.addAll(lst_pag);
        refreshCounter();

        updateNavigationButton();
    }

    public void nextMessage(View view) {
        if (pag >= lista.size() / MAX_ITEM || (lista.size() <= (pag+1)*MAX_ITEM))
            return;
        pag++;
        adapter.clear();
        List<Message> lst_pag = new ArrayList<Message>();
        for (int i = 0; i < Math.min(MAX_ITEM,lista.size()) && (pag*MAX_ITEM + i < lista.size()); i++) {
            lst_pag.add(lista.get(pag*MAX_ITEM + i));
        }
        adapter.addAll(lst_pag);
        refreshCounter();

        updateNavigationButton();
    }

    private void updateNavigationButton() {
        if (lista == null || lista.size() <= MAX_ITEM) {
            ll_next.setEnabled(false);
            ll_next.setAlpha(0.4f);

            ll_prev.setEnabled(false);
            ll_prev.setAlpha(0.4f);
        } else if (pag ==0) {
            ll_prev.setEnabled(false);
            ll_prev.setAlpha(0.4f);

            if (lista.size() > MAX_ITEM) {
                ll_next.setEnabled(true);
                ll_next.setAlpha(1.0f);
            } else {
                ll_next.setEnabled(false);
                ll_next.setAlpha(0.4f);
            }
        } else if ((pag + 1) * MAX_ITEM < lista.size()) {
            ll_next.setEnabled(true);
            ll_next.setAlpha(1.0f);
            ll_prev.setEnabled(true);
            ll_prev.setAlpha(1.0f);
        } else {
            ll_next.setEnabled(false);
            ll_next.setAlpha(0.4f);
            ll_prev.setEnabled(true);
            ll_prev.setAlpha(1.0f);
        }
    }
}
