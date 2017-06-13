/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import cat.bcn.vincles.lib.dao.FeedItemDAO;
import cat.bcn.vincles.lib.dao.FeedItemDAOImpl;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskCallActivity;
import cat.bcn.vincles.tablet.activity.operation.TaskMainActivity;
import static android.content.Context.NOTIFICATION_SERVICE;

public class FeedModel {
    private static int offsetEventWindowTime = 7200000; // 2 hours in milliseconds
    private static FeedModel instance;
    private List<FeedItem> feedList;
    private int messageLimit = 500;
    private FeedItemDAO feedItemDAO;

    public static FeedModel getInstance() {
        if (instance == null) {
            instance = new FeedModel();
        }
        return instance;
    }

    public FeedModel() {
        feedItemDAO = new FeedItemDAOImpl();
    }

    public FeedModel load() {
        refresh();
        return this;
    }

    public List<FeedItem> refresh() {
        return refresh(messageLimit);
    }

    public List<FeedItem> refresh(int limit) {
        feedList = feedItemDAO.getAll(limit);
        addEventItemsToList();
        orderFeedList();
        return feedList;
    }

    private void addEventItemsToList() {
        List<Task> tmpList = TaskModel.getInstance().getTodayTaskList();
        for (Task tmp: tmpList) {
            if (checkEventTime(tmp)) {
                if (tmp.owner == null) continue;
                FeedItem item = new FeedItem()
                        .setType(FeedItem.FEED_TYPE_EVENT_FROM_AGENDA)
                        .setIdData(tmp.getId())
                        .setExtraId(tmp.owner.getId())
//                        .setCreationDate()
                        .setFixedData(tmp.owner.alias, tmp.description, tmp.getDate().getTime());

                if (!feedList.contains(item)) {
                    addItemPrivate(item);
                    feedList.add(item);
                }
            }
        }
        orderFeedList();
    }

    private void orderFeedList() {
        Collections.sort(feedList, new Comparator<FeedItem>() {
            public int compare(FeedItem o1, FeedItem o2) {
                return o2.getCreated().compareTo(o1.getCreated());
            }
        });
    }

    public int count() {
        return feedList.size();
    }

    public List<FeedItem> getList(boolean refresh) {
        if (refresh) refresh();
        return feedList;
    }

    public FeedItem getLastItem(boolean refresh) {
        if (refresh) refresh();
        if (feedList.size() > 0) return feedList.get(0);
        else return null;
    }

    public FeedItem getItemWithId(Long id) {
        return feedItemDAO.get(id);
    }

    public List<FeedItem> addItem(FeedItem item) {
        // IF THERE IS AN EVENT REMOVE ALL PAST EVENT ITEMS EXCEPT MANUALLY ADDED ONES
        if (item.getType().contains("_EVENT") && !item.getType().equalsIgnoreCase(FeedItem.FEED_TYPE_EVENT_FROM_AGENDA)) {
            FeedItem.deleteAll(
                    FeedItem.class,
                    "id_data = ? AND type LIKE ? AND type NOT LIKE ?",
                    new String[] { item.getIdData().toString(), "%_EVENT%", FeedItem.FEED_TYPE_EVENT_FROM_AGENDA });
        }
        addItemPrivate(item);
        refresh();
        return feedList;
    }

    public List<FeedItem> remove(FeedItem item) {
        item.delete();
        return refresh();
    }

    public void setWatched(FeedItem item) {
        item.setWatched(true);
        item.save();
    }

    private void addItemPrivate(FeedItem item) {
        item.save();
    }

    public void clearFeed() {
        FeedItem.deleteAll(FeedItem.class);
    }

    public static boolean checkEventTime(Task tmp) {
        return tmp.getDate().before(new Date(new Date().getTime()+offsetEventWindowTime)) && tmp.getDate().after(new Date());
    }

    public static void addLostCall(Long callerId) {
        FeedModel.getInstance().addItem(
                new FeedItem().setType(FeedItem.FEED_TYPE_LOST_CALL)
                        .setIdData(callerId));
    }
}
