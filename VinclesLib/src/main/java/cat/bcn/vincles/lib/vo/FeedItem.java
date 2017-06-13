/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.vo;

import com.orm.dsl.Ignore;

import java.util.Date;
import java.util.Objects;

public class FeedItem extends GenericObject {
    @Ignore
    public static final String FEED_TYPE_NEW_MESSAGE = "FEED_NEW_MESSAGE";
    @Ignore
    public static final String FEED_TYPE_NEW_AUDIO_MESSAGE = "FEED_NEW_AUDIO_MESSAGE";
    @Ignore
    public static final String FEED_TYPE_NEW_VIDEO_MESSAGE = "FEED_NEW_VIDEO_MESSAGE";
    @Ignore
    public static final String FEED_TYPE_NEW_IMAGE_MESSAGE = "FEED_NEW_IMAGES_MESSAGE";
    @Ignore
    public static final String FEED_TYPE_NEW_TEXT_MESSAGE = "FEED_NEW_TEXT_MESSAGE";
    @Ignore
    public static final String FEED_TYPE_EVENT_FROM_AGENDA = "FEED_EVENT_FROM_AGENDA";
    @Ignore
    public static final String FEED_TYPE_NEW_EVENT = "FEED_NEW_EVENT";
    @Ignore
    public static final String FEED_TYPE_REMEMBER_EVENT = "FEED_REMEMBER_EVENT";
    @Ignore
    public static final String FEED_TYPE_DELETED_EVENT = "FEED_EVENT_DELETED";
    @Ignore
    public static final String FEED_TYPE_EVENT_ACCEPTED = "FEED_EVENT_ACCEPTED";
    @Ignore
    public static final String FEED_TYPE_EVENT_REJECTED = "FEED_EVENT_REJECTED";
    @Ignore
    public static final String FEED_TYPE_EVENT_UPDATED = "FEED_EVENT_UPDATED";
    @Ignore
    public static final String FEED_TYPE_INCOMING_CALL = "FEED_INCOMING_CALL";
    @Ignore
    public static final String FEED_TYPE_LOST_CALL = "FEED_LOST_CALL";
    @Ignore
    public static final String FEED_TYPE_USER_LINKED = "FEED_USER_LINKED";
    @Ignore
    public static final String FEED_TYPE_USER_UNLINKED = "FEED_USER_UNLINKED";
    @Ignore
    public static final String FEED_TYPE_INVITATION_SENT = "FEED_INVITATION_SENDED";
    @Ignore
    public static final String FEED_TYPE_NEW_CHAT = "FEED_NEW_CHAT_MESSAGE";
    @Ignore
    public static final String FEED_TYPE_ADDED_TO_GROUP = "FEED_ADDED_TO_GROUP";

    private Long creationTime;
    private Long idData;
    private String type;        // STRING TYPE FROM PushMessage:Type
    private String info;
    private Boolean watched = false;

    // FIXED DATA:
    String text;
    String subtext;
    long idExtra;
    long itemDate;

    public FeedItem fromPushMessage(PushMessage push) {
        this.creationTime = push.getCreationTime();
        this.idData = push.getIdData();
        this.type = "FEED_" + push.getType();
        this.info = push.getInfo();
        this.idExtra = push.getIdExtra();
        return this;

    }

    public FeedItem setFixedData(String text, String subtext, long itemDate) {
        this.text = text;
        this.subtext = subtext;
        this.itemDate = itemDate;
        return this;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public FeedItem setCreationDate(Date creationDate) {
        setCreated(creationDate);
        return this;
    }

    public String getType() {
        return type;
    }

    public FeedItem setType(String type) {
        this.type = type;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public FeedItem setInfo(String info) {
        this.info = info;
        return this;
    }

    public Long getIdData() {
        return idData;
    }

    public FeedItem setIdData(Long idData) {
        this.idData = idData;
        return this;
    }

    public Boolean getWatched() {
        return watched;
    }

    public FeedItem setWatched(Boolean watched) {
        this.watched = watched;
        return this;
    }

    public long getExtraId() {
        return idExtra;
    }

    public FeedItem setExtraId(long idExtra) {
        this.idExtra = idExtra;
        return this;
    }

    public long getItemDate() {
        return itemDate;
    }

    public FeedItem setItemDate(long itemDate) {
        this.itemDate = itemDate;
        return this;
    }

    public String getSubtext() {
        return subtext;
    }

    public FeedItem setSubtext(String subtext) {
        this.subtext = subtext;
        return this;
    }

    public String getText() {
        return text;
    }

    public FeedItem setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedItem that = (FeedItem) o;
        return compare(getType(), that.getType()) &&  compare(getIdData(), that.getIdData());
    }

    public boolean compare(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
}
