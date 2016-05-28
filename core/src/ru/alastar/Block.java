package ru.alastar;

import com.badlogic.gdx.graphics.Texture;

/**
 * Created by Alastar on 24.05.2016.
 */
public class Block {
    public int id;
    public Texture tex;
    public byte subid;
    public Block(int i, Texture t, byte subid)
    {
        id = i;
        tex = t;
        this.subid = subid;
    }
}
