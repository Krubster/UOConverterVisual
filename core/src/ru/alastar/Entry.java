package ru.alastar;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import java.util.HashMap;

/**
 * Created by Alastar on 23.05.2016.
 */
public class Entry {
    public HashMap<UOVector, LandInfo> blocks; //pos mod, block info
    public int id;
    public TextButton btnRef = null;

    public Entry(int i)
    {
        this.id = i;
        blocks = new HashMap<UOVector, LandInfo>();
    }
}
