package ru.alastar.gui.constructed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import ru.alastar.*;
import ru.alastar.gui.ConstructedGUI;
import ru.alastar.gui.GUICore;
import ru.alastar.lang.LanguageManager;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alastar on 23.05.2016.
 */
public class EntriesWindow extends ConstructedGUI {
    private static Window entryWindow;
    private  Table entrylist;
    private Entry selected = null;
    private ScrollPane pane = null;
    public EntriesWindow()
    {

        TextField.TextFieldFilter digitsFilter = new TextField.TextFieldFilter() {

            @Override
            public boolean acceptChar(TextField textField, char c) {
                if (c != '.' && c != '-')
                    if (!Character.isDigit(c))
                        return false;
                if (c == '-') {
                    return !(textField.getText().toCharArray().length != 0 && textField.getText().toCharArray()[0] == '-');
                }
                return !(c == '.' && textField.getText().contains("."));
            }

        };
        entrylist = new Table();
        pane = new ScrollPane(entrylist);
        final TextField textId = new TextField("0", GUICore.getSelectedSkin(), "default");
        textId.setTextFieldFilter(digitsFilter);

        final TextField toid = new TextField("0", GUICore.getSelectedSkin(), "default");
        toid.setTextFieldFilter(digitsFilter);

        TextButton AddEntry = new TextButton(LanguageManager.getLocalizedMessage("Add"), GUICore.getSelectedSkin());
        AddEntry.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int id = 0;
                try{
                    id = Integer.valueOf(textId.getText());
                }catch(Exception e){

                }
                if(!MainScreen.workEnvironment.containsKey(id)){
                     final Entry ent = new Entry(id);
                     MainScreen.workEnvironment.setEntry(ent);
                    TextButton entryBtn = new TextButton(Integer.toString(ent.id), GUICore.getSelectedSkin());
                    ent.btnRef = entryBtn;
                    entryBtn.addListener(new ChangeListener(){

                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            if(selected != null)
                             selected.btnRef.setColor(Color.LIGHT_GRAY);

                            selected = ent;
                            MainScreen.loadBlocks(ent);
                            ent.btnRef.setColor(Color.YELLOW);
                            MainScreen.workEnvironment.setEntry(ent);
                        }});
                     entrylist.row();
                     entrylist.add(entryBtn);
                    pane.setScrollY(entrylist.getHeight());
                    if(selected == null){
                        selected = ent;
                        ent.btnRef.setColor(Color.YELLOW);
                    }
                }
                else
                    MainScreen.cantAddMore();
            }});
        TextButton RemoveSelected = new TextButton(LanguageManager.getLocalizedMessage("Remove"), GUICore.getSelectedSkin());
        RemoveSelected.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                 if(selected != null)
                 {
                     entrylist.removeActor(selected.btnRef);
                     MainScreen.workEnvironment.removeEntry(selected.id);
                     selected = null;
                 }
            }});

        TextButton Copy = new TextButton(LanguageManager.getLocalizedMessage("Copy"), GUICore.getSelectedSkin());
        Copy.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(selected != null)
                {
                    int toi = Integer.parseInt(toid.getText());
                   if(selected.id < toi)
                   {
                       for(int i = selected.id; i <= toi; ++i)
                       {
                           if(!MainScreen.workEnvironment.containsKey(i))
                           {
                               final Entry ent = new Entry(i);
                               ent.blocks = MainScreen.workEnvironment.currEntry.blocks;
                               TextButton entryBtn = new TextButton(Integer.toString(ent.id), GUICore.getSelectedSkin());
                               ent.btnRef = entryBtn;
                               entryBtn.addListener(new ChangeListener(){

                                   @Override
                                   public void changed(ChangeEvent event, Actor actor) {
                                       if(selected != null)
                                           selected.btnRef.setColor(Color.LIGHT_GRAY);

                                       selected = ent;
                                       MainScreen.loadBlocks(ent);
                                       ent.btnRef.setColor(Color.YELLOW);
                                       MainScreen.workEnvironment.setEntry(ent);
                                   }});
                               entrylist.row();
                               entrylist.add(entryBtn);
                               if(MainScreen.workEnvironment.editingSession == 0) {
                                   File f = new File("out/schemas");
                                   if(f.exists())
                                   {
                                       File to = new File("out/schemas/" + i + ".bin");
                                       FileOutputStream fos = null;
                                       try {
                                           fos = new FileOutputStream(to);
                                           BufferedOutputStream str = new BufferedOutputStream(fos);
                                           for(Map.Entry<UOVector, LandInfo> entry: MainScreen.workEnvironment.currEntry.blocks.entrySet())
                                           {
                                               str.write(ByteBuffer.allocate(4).putInt(entry.getKey().getBlockX()).array());
                                               str.write(ByteBuffer.allocate(4).putInt(entry.getKey().getBlockX()).array());
                                               str.write(ByteBuffer.allocate(4).putInt(entry.getKey().getBlockX()).array());

                                               entry.getValue().serialize(str);

                                           }
                                           str.close();
                                           fos.close();
                                       } catch (FileNotFoundException e) {
                                           e.printStackTrace();
                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }
                                   }
                                   else
                                       try {
                                           Files.createDirectory(f.toPath());
                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }
                               } else
                               {
                                   File f = new File("out/blocks");
                                   if(f.exists())
                                   {
                                       File to = new File("out/blocks/" + i + ".bin");
                                       FileOutputStream fos = null;
                                       try {
                                           fos = new FileOutputStream(to);
                                           BufferedOutputStream str = new BufferedOutputStream(fos);
                                           if(MainScreen.workEnvironment.currEntry.blocks.get(new UOVector(0,0,0)) != null)
                                               MainScreen.workEnvironment.currEntry.blocks.get(new UOVector(0,0,0)).serialize(str);
                                           str.close();
                                           fos.close();
                                       } catch (FileNotFoundException e) {
                                           e.printStackTrace();
                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }
                                   }
                                   else
                                       try {
                                           Files.createDirectory(f.toPath());
                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }
                               }
                           }
                       }
                   }
                }
            }});

        entryWindow = new Window(LanguageManager.getLocalizedMessage("Entries"), GUICore.getSelectedSkin());
        entryWindow.setMovable(true);
        entryWindow.setResizeBorder(10);
        entryWindow.pad(1);
        entryWindow.padTop(28);
        entryWindow.add(new Label("ID:", GUICore.getSelectedSkin())).fill();
        entryWindow.add(textId).fill();
        entryWindow.row();
        entryWindow.add(AddEntry).fill();
        entryWindow.row();
        entryWindow.add(new Label("Copy to:", GUICore.getSelectedSkin())).fill();
        entryWindow.add(toid).fill();
        entryWindow.row();
        entryWindow.add(Copy).fill();
        entryWindow.row();
        entryWindow.add(RemoveSelected).fill();
        entryWindow.row();
        entryWindow.add(pane).fill();
        entryWindow.setPosition(100, 100);
        entryWindow.setVisible(true);
        entryWindow.validate();

        entryWindow.setWidth(300);
        entryWindow.setHeight(600);
        entryWindow.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, Align.center);
    }

    @Override
    public Actor getByName(String s) {
        if(s == "window")
        return entryWindow;
        if(s == "entrylist")
            return entrylist;
        return entryWindow;
    }

    @Override
    public void register(Stage s) {
        s.addActor(entryWindow);
    }

    @Override
    public void show() {
        entryWindow.setVisible(true);

    }

    @Override
    public void hide() {
        entryWindow.setVisible(false);
    }



    public void updateSchema(HashMap<Integer, Schema> schemas) {
        entrylist.clear();
        for(Map.Entry<Integer, Schema> en: schemas.entrySet())
        {
            int id = en.getKey();
            final Entry ent = new Entry(id);
         //  Engine.debug("Blocks count: " + en.getValue().blocks.size());
            ent.blocks = (HashMap<UOVector, LandInfo>) en.getValue().blocks.clone();

            TextButton entryBtn = new TextButton(Integer.toString(ent.id), GUICore.getSelectedSkin());
            ent.btnRef = entryBtn;
            entryBtn.addListener(new ChangeListener(){

                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if(selected != null)
                        selected.btnRef.setColor(Color.LIGHT_GRAY);

                    selected = ent;
                    MainScreen.loadBlocks(ent);
                    ent.btnRef.setColor(Color.YELLOW);
                    MainScreen.workEnvironment.setEntry(ent);
                }});
            entrylist.row();
            entrylist.add(entryBtn);
            pane.setScrollY(entrylist.getHeight());

            if(selected == null){
                selected = ent;
                ent.btnRef.setColor(Color.YELLOW);
            }
        }
    }

    public void update(HashMap<Integer, LandInfo> blocks) {
        entrylist.clear();
        for(Map.Entry<Integer, LandInfo> en: blocks.entrySet())
        {
            int id = en.getKey();
            final Entry ent = new Entry(id);
            ent.blocks = new HashMap<UOVector, LandInfo>();
            ent.blocks.put(new UOVector(0,0,0), en.getValue());
            TextButton entryBtn = new TextButton(Integer.toString(ent.id), GUICore.getSelectedSkin());
            ent.btnRef = entryBtn;
            entryBtn.addListener(new ChangeListener(){

                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if(selected != null)
                        selected.btnRef.setColor(Color.LIGHT_GRAY);

                    selected = ent;
                    MainScreen.loadBlocks(ent);
                    ent.btnRef.setColor(Color.YELLOW);
                    MainScreen.workEnvironment.setEntry(ent);
                }});
            entrylist.row();
            entrylist.add(entryBtn);
            pane.setScrollY(entrylist.getHeight());

            if(selected == null){
                selected = ent;
                ent.btnRef.setColor(Color.YELLOW);
            }
        }
    }
}
