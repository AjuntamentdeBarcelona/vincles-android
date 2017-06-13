/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import com.orm.util.NamingHelper;

import cat.bcn.vincles.lib.vo.GenericObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenericDAOImpl<T extends GenericObject> implements GenericDAO<T> {
    private Class<T> type;

    public GenericDAOImpl(Class<T> type) {
        this.type = type;
    }

    @Override
    public Long save(T item) {
        Date now = new Date();
        if (item.getId() == null) {
            item.setCreated(now);
        }
        item.setUpdated(now);
        return item.save();
    }

    @Override
    public T get(Long id) {
        T item = T.findById(type, id);
        return item;
    }

    @Override
    public boolean delete(T item) {
        return item.delete();
    }

    @Override
    public List<T> getAll(int limit) {
        if (T.count(type) <= 0) return new ArrayList<T>();
        List<T> items = T.find(type, null, null, null, "ID DESC", "" + limit);
        if (items == null) {
            items = new ArrayList<T>();
        }
        return items;
    }

    @Override
    public List<T> getAll() {
        List<T> items = T.listAll(type);
        if (items == null) {
            items = new ArrayList<T>();
        }
        return items;
    }

    @Override
    public List<T> findByNetwork(Long code) {
        String query = "SELECT t1.* FROM " + NamingHelper.toSQLName(type) + " t1 " +
                "JOIN NETWORK t2 ON t1.network = t2.id AND t2.id = ?";
        List<T> items = T.findWithQuery(type, query, String.valueOf(code));
        return items;
    }
}