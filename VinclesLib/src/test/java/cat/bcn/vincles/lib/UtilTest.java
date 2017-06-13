/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib;

import android.graphics.Bitmap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.VinclesConstants;

public class UtilTest {

    @Test
    public void copyArray() {
        List<Integer> originList = Arrays.asList(1, 2, 3);
        List<Integer> targetList = new ArrayList<Integer>(originList);
        Assert.assertEquals(originList.size(), targetList.size());
    }

    @Test
    public void addArray() {
        //First ArrayList
        ArrayList<String> arraylist1 = new ArrayList<String>();
        arraylist1.add("AL1: E1");
        arraylist1.add("AL1: E2");
        arraylist1.add("AL1: E3");

        //Second ArrayList
        ArrayList<String> arraylist2 = new ArrayList<String>();
        arraylist2.add("AL2: E1");
        arraylist2.add("AL2: E2");
        arraylist2.add("AL2: E3");

        arraylist1.addAll(0, arraylist2);

        //New ArrayList
        ArrayList<String> al = new ArrayList<String>();
        al.addAll(arraylist1);
        al.addAll(arraylist2);
    }
}
