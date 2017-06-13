/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.dao;

import cat.bcn.vincles.lib.vo.Note;

public class NoteDAOImpl extends GenericDAOImpl<Note> implements NoteDAO {
    public NoteDAOImpl() {
        super(Note.class);
    }
}