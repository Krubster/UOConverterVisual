package ru.alastar;

import ru.alastar.gui.GUICore;
import ru.alastar.gui.constructed.EntriesWindow;

import java.util.HashMap;

/**
 * Created by Alastar on 23.05.2016.
 */
public class WorkingEnvironment {
    public HashMap<Integer, LandInfo> blocks;
    public HashMap<Integer, Schema> schemas;
    public int editingSession = 0; // 0 is blocks 1 is land
    public int block_id = 0;
    public Entry currEntry = new Entry(-1);

    public WorkingEnvironment()
    {
        blocks = new HashMap<Integer, LandInfo>();
        schemas = new HashMap<Integer, Schema>();
    }

    public boolean containsKey(int id) {
        if(editingSession == 0)
            return schemas.containsKey(id);
        else
            return  blocks.containsKey(id);
    }

    public void setEntry(Entry ent) {
        currEntry = ent;
}

    public void removeEntry(int id)
    {

        if(editingSession == 0)
            schemas.remove(id);
        else
            blocks.remove(id);
    }


    public void update() {
        if(editingSession == 0)
        ((EntriesWindow)GUICore.getByName("EntryList")).updateSchema(schemas);

        if(editingSession == 1)
            ((EntriesWindow)GUICore.getByName("EntryList")).update(blocks);
    }
}
