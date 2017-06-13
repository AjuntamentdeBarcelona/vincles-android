/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.util;

import cat.bcn.vincles.lib.util.VinclesConstants;

public class VinclesTabletConstants {
    public static final String APP_PREFERENCES = "cat.bcn.vincles.tablet.preferences";
    public static final String USER_ID = "cat.bcn.vincles.tablet.userVincles-id";
    public static final String USER_PHOTO = "cat.bcn.vincles.tablet.userVincles-photo" + VinclesConstants.IMAGE_EXTENSION;
    public static final String MESSAGE_PHOTO = "cat.bcn.vincles.tablet.message-photo";
    public static final String MESSAGE_AUDIO = "cat.bcn.vincles.tablet.message-audio";
    public static final String APP_LANGUAGE = "cat.bcn.vincles.tablet.app-language";
    public static final String APP_COUNTRY = "cat.bcn.vincles.tablet.app-country";
    public static final String APP_IMAGE_LIST = "cat.bcn.vincles.tablet.app-image_list";
    public static final String APP_THEME = "cat.bcn.vincles.tablet.app-theme";
    public static final String APP_LASTNOTIFICATIONCHECK = "cat.bcn.vincles.tablet.app-lastnotificationcheck";

    public static final String FONT_SIZE = "fontsize";
    public static final Float FONT_SMALL = 0.8f;
    public static final Float FONT_NORMAL = 1.0f;
    public static final Float FONT_LARGE = 1.2f;

    public static final String VOLUME = "volume";
    public static final int VOLUME_NORMAL = 50;

    public static final int BRIGHTNESS_NORMAL = 150;
    public static final String BRIGHTNESS = "brightness";
    public static final String BRIGHTNESS_AUTOMATIC = "brightness-automatic";

    public static final String MARGIN_LEFT = "marginleft";
    public static final String MARGIN_RIGHT = "marginright";
    public static final int MARGIN_LEFT_NORMAL = 10;
    public static final int MARGIN_LEFT_LARGE = 100;
    public static final int MARGIN_RIGHT_NORMAL = 10;
    public static final int MARGIN_RIGHT_LARGE = 100;

    public static final String NETWORK_CODE = "cat.bcn.vincles.tablet.network-code";

    public static final String FOSCA_THEME = "fosca";
    public static final String CLARA_THEME = "clara";

    public static final String TOUR = "cat.bcn.vincles.mobile.tour";

    public static final int VC_NOTIFICATION_TIMEOUT_MS = 25000;
    public static final int VC_OUTGOING_CALL_TIMEOUT_SECS = 25;
    public static final int VC_INCOMING_CALL_TIMEOUT_SECS = 25;
}