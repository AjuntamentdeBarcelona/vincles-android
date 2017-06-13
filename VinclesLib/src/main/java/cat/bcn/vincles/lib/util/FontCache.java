/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

public class FontCache {
	
	private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();
	
	public static Typeface get(String name, Context context) {
		Typeface tf = fontCache.get(name);
		
		if (tf == null) {
			try {
				tf = Typeface.createFromAsset(context.getAssets(), name);
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			fontCache.put(name, tf);
		}
		
		return tf;
	
	}
	
}
