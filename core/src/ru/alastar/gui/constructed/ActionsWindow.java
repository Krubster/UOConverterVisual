package ru.alastar.gui.constructed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import ru.alastar.MainScreen;
import ru.alastar.gui.ConstructedGUI;
import ru.alastar.gui.GUICore;
import ru.alastar.lang.LanguageManager;

/**
 * Created by Alastar on 23.05.2016.
 */
public class ActionsWindow extends ConstructedGUI {

    private static Window actionsWindow;


    public ActionsWindow()
    {
        Table createTWindowTable = new Table();
        TextButton LoadSchemaBtn = new TextButton(LanguageManager.getLocalizedMessage("LoadSchema"), GUICore.getSelectedSkin());
        LoadSchemaBtn.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                    MainScreen.openLoadSchema();
            }});
        TextButton LoadBlocksBtn = new TextButton(LanguageManager.getLocalizedMessage("LoadBlocks"), GUICore.getSelectedSkin());
        LoadBlocksBtn.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                    MainScreen.openLoadBlocks();
            }});
       /// LoadBlocksBtn.setDisabled(true);
        createTWindowTable.padTop(10);
        createTWindowTable.add(new Label(" Load:", GUICore.getSelectedSkin())).fill();
        createTWindowTable.row();
        createTWindowTable.add(LoadBlocksBtn).fill();
        createTWindowTable.row();
        createTWindowTable.add(LoadSchemaBtn).fill();
        createTWindowTable.setFillParent(true);

        actionsWindow = new Window(LanguageManager.getLocalizedMessage("File"), GUICore.getSelectedSkin());
        actionsWindow.setMovable(true);
        actionsWindow.setResizeBorder(10);
        actionsWindow.pad(1);
        actionsWindow.padTop(28);
        actionsWindow.add(createTWindowTable).fill();
        actionsWindow.setPosition(100, 100);
        actionsWindow.setVisible(true);
        actionsWindow.pack();
        actionsWindow.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, Align.center);
}


    @Override
    public Actor getByName(String s) {
        if(s.equals("window"))
            return actionsWindow;
        return null;
    }

    @Override
    public void register(Stage s) {
        s.addActor(actionsWindow);
    }

    @Override
    public void show() {
        actionsWindow.setVisible(true);

    }

    @Override
    public void hide() {
        actionsWindow.setVisible(false);
    }
}
