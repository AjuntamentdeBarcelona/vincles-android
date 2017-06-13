/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.ArrayAdapter;
import java.util.List;
import cat.bcn.vincles.lib.util.FontCache;
import cat.bcn.vincles.lib.util.VinclesConstants;

// Este adaptador actuará como un adaptador de 5 elementos siempre, pero guardar´a la groupList completa
// recorriendo únicamente desde el offset al que estemos apuntando y que agrandaremos o reduciremos
// con las funciones showMore y showLess

public class FixedItemsAdapterTemplate<T> extends ArrayAdapter<T> {
    protected Context mContext;
    protected Typeface tf;
    protected int itemHeight;
    protected List<T> itemsList;
    protected int listOffset = 0;
    protected int MAX_SPACE = 3;

    public FixedItemsAdapterTemplate(Context context, int resource, List<T> objects, int listHeight, int mMAX_ITEM) {
        super(context, resource, objects);
        mContext = context;
        MAX_SPACE = mMAX_ITEM;
        itemHeight = listHeight;
        itemsList = objects;
        tf = FontCache.get(VinclesConstants.TYPEFACE.REGULAR, context);
    }

    @Override
    public int getCount() {
        return MAX_SPACE;
    }

    public void showMore() {
        showMore(true);
    }

    public void showLess() {
        showLess(true);
    }

    public boolean isMore() {
        return showMore(false);
    }

    public boolean isLess() {
        return showLess(false);
    }

    private boolean showMore(boolean applyChange) {
        int space = 0, i = 0;
        for (i = 0; i <= MAX_SPACE; i++) {
            space += getFullRowSpace(i);
            if (space > MAX_SPACE) break;
        }

        if (listOffset+i >= itemsList.size()) return false;
        if (applyChange) {
            listOffset = listOffset+i;
            notifyDataSetChanged();
        }

        return space > MAX_SPACE;
    }

    private boolean showLess(boolean applyChange) {
        if (listOffset == 0) return false;
        int space = 0, i = 0;
        for (i = 1; i <= MAX_SPACE +1; i++) {
            if (listOffset-i < 0) {
                i = listOffset;
                break;
            }
            space++;
            if (isShowTitleMessage(listOffset-i)) space++;
            space += getMessageExtraSpace(itemsList.get(listOffset-i));
            if (space >= MAX_SPACE) break;
        }
        if (space > MAX_SPACE) i--;

        if (applyChange) {
            listOffset = listOffset - i;
            notifyDataSetChanged();
        }
        return listOffset != 0;
    }

    public void reset() {
        listOffset = 0;
    }

    // CALCULATE SPACE OF PREVIOUS ITEMS (AND ACTUAL ALSO) TO RETURN AN ANSWER
    protected boolean isShowed(int position) {
        int space = 0;
        for (int i = 0; i <= position; i++) {
            space += getFullRowSpace(i);
            if (space > MAX_SPACE) return false;
        }
        return true;
    }

    protected int getFullRowSpace(int position) {
        // DO NOT CRASH IN LAST ITEMS
        int realListPosition = getRealMessagePosition(position);
        if (realListPosition >= itemsList.size()) return 0;

        int space = 1;
        if (isShowTitleMessage(getRealMessagePosition(position))) space++;
        space += getMessageExtraSpace(itemsList.get(getRealMessagePosition(position)));
        return space;
    }

    protected boolean isShowTitleMessage(int listPosition) {
        return false;
    }

    protected int getMessageExtraSpace(T actual) {
        return 0;
    }

    public int getRealMessagePosition(int position) {
        return listOffset+position;
    }
}