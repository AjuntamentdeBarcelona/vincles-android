/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.util;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

public class FontManager {

	public static void setCustomFont(TextView textview, Context context, String name) {
		Typeface tf = FontCache.get(name, context);
		if (tf != null) {
			textview.setTypeface(tf);
		}
	}

}
