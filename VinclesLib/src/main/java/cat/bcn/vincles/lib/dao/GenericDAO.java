/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import cat.bcn.vincles.lib.vo.GenericObject;

import java.util.List;

public interface GenericDAO<T extends GenericObject> {
    public Long save(T item);

    public T get(Long id);

    public List<T> getAll(int limit);

    public List<T> getAll();

    public boolean delete(T item);

    public List<T> findByNetwork(Long code);
}