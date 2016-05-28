package ru.alastar.gui.constructed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import org.bukkit.Material;
import ru.alastar.Block;
import ru.alastar.Engine;
import ru.alastar.MainScreen;
import ru.alastar.gui.ConstructedGUI;
import ru.alastar.gui.GUICore;
import ru.alastar.lang.LanguageManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Alastar on 23.05.2016.
 */
public class BlocksWindow extends ConstructedGUI {

    private static Window window;

    public BlocksWindow()
    {
        Table table = new Table();
        table.pad(20);
        ScrollPane pane = new ScrollPane(table);
        pane.setFillParent(true);
        pane.setScrollingDisabled(true, false);
        File dir = new File(Engine.getDirectory() + "/res/blocks");
        if(dir.exists())
        {
            Texture tex = null;
            int id = 0;
            File f = null;
            String name = "";
            String facing = "";
            String subidstr = "0";
           for(int i = 0; i < dir.listFiles().length; ++i)
           {
                id = 0;
                name = "";
                facing = "";
                subidstr = "0";
               try {
                   f = dir.listFiles()[i];
                   name = f.getName();
                   name = name.substring(0, name.lastIndexOf("."));
                                                            //block name: name,subid-facing.png
                   if(name.split("-").length > 1) //we have faced block!
                   {
                       facing =  name.split("-")[1];
                       name = name.split("-")[0]; // only the name

                   }
                   if(name.split(",").length > 1) //we an subid block!
                   {
                       subidstr =  name.split(",")[1];
                       name = name.split(",")[0]; // only the name

                   }
                   if (Material.getMaterial(name.toUpperCase()) != null) {
                       id = Material.getMaterial(name.toUpperCase()).getId();
                   } else  //else id is the name of file(for modded blocks)
                   {
                       id = Integer.parseInt(name);
                   }

                   final int finalId = id;
                   byte subid = Byte.parseByte(subidstr);

                   if(facing.equalsIgnoreCase("east"))
                   {
                       subid = 1;
                       name += "-east";
                   }
                   else if(facing.equalsIgnoreCase("west"))
                   {
                       subid = 0;
                       name += "-west";

                   }
                   else if (facing.equalsIgnoreCase("south"))
                   {
                       subid = 3;
                       name += "-south";

                   }
                   else if(facing.equalsIgnoreCase("north"))
                   {
                       subid = 2;
                       name += "-north";

                   }
                   final byte finalSubid = subid;
                   tex = new Texture(f.getPath());
                   final Texture finalTex = tex;

                   ImageButton blockbtn = new ImageButton(new SpriteDrawable(new Sprite(tex)));
                   blockbtn.setWidth(100);
                   blockbtn.setHeight(25);
                   table.add(new Label(name + "(" + Integer.toString(id) + ":" + subid +")", GUICore.getSelectedSkin()));
                   table.add(blockbtn).maxHeight(50);
                   table.row();
                   pane.pack();;
                   pane.validate();;

                   blockbtn.addListener(new ChangeListener() {

                       @Override
                       public void changed(ChangeEvent event, Actor actor) {
                           MainScreen.brush = new Block(finalId, finalTex, finalSubid);
                       }
                   });
                   MainScreen.blocksTypes.put(id, tex);
               }catch(Exception e)
               {
               //  Engine.logException(e);
               }
           }
        }else
        {
            Engine.debug("No blocks dir, so create one at: " + dir.getAbsolutePath());
            try {
                Files.createDirectory(dir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        table.pack();

        window = new Window(LanguageManager.getLocalizedMessage("Blocks"), GUICore.getSelectedSkin());
        window.setMovable(true);
        window.setResizeBorder(10);
        window.pad(1);
        window.padTop(28);
        window.add(pane).fill();
        window.setPosition(100, 100);
        window.setVisible(true);
        window.pack();
        window.validate();
        window.setWidth(470);
        window.setHeight(600);

        window.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, Align.center);
    }

    @Override
    public Actor getByName(String s) {
        return window;
    }

    @Override
    public void register(Stage s) {
        s.addActor(window);
    }

    @Override
    public void show() {
        window.setVisible(true);

    }

    @Override
    public void hide() {
        window.setVisible(false);
    }
}
