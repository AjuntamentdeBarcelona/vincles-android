/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.model;

import cat.bcn.vincles.lib.dao.NetworkDAO;
import cat.bcn.vincles.lib.dao.NetworkDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.vo.Network;
import java.util.List;

public class NetworkModel {
    private static final String TAG = "NetworkModel";
    protected MainModel mainModel = MainModel.getInstance();
    private boolean initialized;
    private static NetworkModel instance;
    private NetworkDAO networkDAO;
    private UserDAO userDAO;
    public String view;
    public List<Network> networkList;
    public Network current;
    public static final String NETWORK_JOIN = "networkJoin";
    public static final String NETWORK_SUCCESS = "networkSuccess";

    public static NetworkModel getInstance() {
        if (instance == null) {
            instance = new NetworkModel();
            instance.initialize();
        }
        return instance;
    }

    private NetworkModel() {
    }

    public void initialize() {
        if (!initialized) {
            initialized = true;
            view = "";
            networkDAO = new NetworkDAOImpl();
            userDAO = new UserDAOImpl();
        }
    }

    public void saveNetwork(Network item) {
        networkDAO.save(item);
    }

    public Network getNetwork(Long id) {
        return networkDAO.get(id);
    }
}