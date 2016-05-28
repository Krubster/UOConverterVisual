package ru.alastar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import ru.alastar.game.GameObject;
import ru.alastar.game.components.GCamera;
import ru.alastar.game.components.MeshRenderer;
import ru.alastar.game.components.MouseInput;
import ru.alastar.graphics.GDirectionalLight;
import ru.alastar.gui.GUICore;
import ru.alastar.gui.constructed.ActionsWindow;
import ru.alastar.gui.constructed.BlocksWindow;
import ru.alastar.gui.constructed.EntriesWindow;
import ru.alastar.utils.GDebugger;

import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mick on 03.05.15.
 */
public class MainScreen implements Screen, InputProcessor {

    private static CameraInputController cameraInputController;
    public static WorkingEnvironment workEnvironment;
    private static Model cubeModel = null;
    private static ModelBuilder modelBuilder = null;
    private static GameObject central;
    public static HashMap<Integer, Texture> blocksTypes = new HashMap<Integer, Texture>();
    public static Block brush = null;
    private static GCamera camera = null;
    private static String version = "v2.1 Dither";
    private static GameObject intersected = null;

    public MainScreen() {
        Gdx.input.setInputProcessor(this);
        Engine.setDebug(true);
        Engine.setUseBulletPhysics(false);
        Engine.setDraw2D(false);
        Engine.setDraw3D(true);
        Engine.setDoPhysics(false);
        Engine.setFarCulling(true);
        Engine.set_clearColor(Color.TEAL);
        Engine.init();

        workEnvironment = new WorkingEnvironment();
        //EXAMPLE CODE 3D
        GUICore.addGUI("ActionsWindow", new ActionsWindow()).register(Engine.getStage());
        GUICore.addGUI("EntryList", new EntriesWindow()).register(Engine.getStage());
        GUICore.addGUI("BlocksList", new BlocksWindow()).register(Engine.getStage());

        modelBuilder = new ModelBuilder();


        camera = new GCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.far = 500f;
        camera.position.set(-3, 5, -3);
        camera.lookAt(0,0,0);
        MouseInput mouse = new MouseInput();
        mouse.setActive(true);
        createCentral();

        Engine.getEnvironment().set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f,
                0.4f, 1f));
        cameraInputController = new CameraInputController(camera);

        Engine.registerInputProcessor(cameraInputController);
        Engine.getEnvironment().set(new ColorAttribute(ColorAttribute.AmbientLight, .4f, .4f, .4f, 1f));
        //TODO: add shadow mapping options that will affect quality of the shadowing
        GDirectionalLight d = new GDirectionalLight( Gdx.graphics.getWidth()*2, Gdx.graphics.getHeight()*2, Gdx.graphics.getWidth()/16, Gdx.graphics.getHeight()/16, 0.01f, 300f, new Vector3(-2,5,-2), new Vector3(0,0,0)).set(new Color(0.8f, 0.8f, 0.8f, 1.0f));

        d.lookAt(new Vector3(0,0,0));
        GDirectionalLight d2 = new GDirectionalLight( Gdx.graphics.getWidth()*2, Gdx.graphics.getHeight()*2, Gdx.graphics.getWidth()/16, Gdx.graphics.getHeight()/16, 0.01f, 300f, new Vector3(2,5,2), new Vector3(0,0,0)).set(new Color(0.8f, 0.8f, 0.8f, 1.0f));
        GDirectionalLight d3 = new GDirectionalLight( Gdx.graphics.getWidth()*2, Gdx.graphics.getHeight()*2, Gdx.graphics.getWidth()/16, Gdx.graphics.getHeight()/16, 0.01f, 300f, new Vector3(3,0,-1), new Vector3(0,0,0)).set(new Color(0.8f, 0.8f, 0.8f, 1.0f));

        d.lookAt(new Vector3(0,0,0));
        d2.lookAt(new Vector3(0,0,0));
        d3.lookAt(new Vector3(0,0,0));
        Engine.getEnvironment().add(d);
        Engine.getEnvironment().add(d2);
        Engine.getEnvironment().add(d3);

        Engine.addDebugger(new GDebugger() {
            @Override
            public String process() {
                return version;
            }
        });
          Engine.addDebugger(new GDebugger() {
              @Override
              public String process() {
                  if(brush != null)
                  return "Selected ID: " + brush.id + ":" + brush.subid;
                  return "None selected!";
              }
          });
    }

    private static void createCentral() {
            Material mat = new Material();
            mat.set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));
            Pixmap white = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
            white.setColor(Color.WHITE);
            white.fill();
            mat.set(new TextureAttribute(TextureAttribute.Diffuse, new Texture(white)));

            cubeModel = modelBuilder.createBox(1.0f, 1.0f, 1.0f, GL20.GL_TRIANGLES, mat, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
            central = new GameObject(cubeModel, "center", "center");
            central.setPosition(new Vector3(0, 0, 0));
            central.getTransform().setToTranslation(0, 0, 0);

            central.setCastingShadows(false);
    }

    public static void openLoadSchema() {
        JFileChooser load = new JFileChooser();
        load.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = load.showOpenDialog(null);
        if (ret == JFileChooser.APPROVE_OPTION) {
         try{
             File dir =  load.getSelectedFile();
             FileInputStream fis;
             BufferedInputStream bis;
             int id;
             Schema schem;
             String n;
             File f;
             for(int i = 0; i < dir.listFiles().length; ++i)
             {
                 f = dir.listFiles()[i];
                 n =f.getName();
                 id = Integer.parseInt(n.substring(0, n.lastIndexOf(".")));
                 fis = new FileInputStream(f);
                 bis = new BufferedInputStream(fis);
                 schem = new Schema();
                 schem.deserialize(bis);
                 workEnvironment.schemas.put(id, schem);
                 bis.close();
                 fis.close();
             }
             workEnvironment.editingSession = 0;

             Engine.debug("loaded " + workEnvironment.schemas.size() +" entries");
             workEnvironment.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    }

    public static void openLoadBlocks() {
        JFileChooser load = new JFileChooser();
        load.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int ret = load.showOpenDialog(null);

        if (ret == JFileChooser.APPROVE_OPTION) {
            try {
               File dir =  load.getSelectedFile();
                FileInputStream fis;
                BufferedInputStream bis;
                int id;
                LandInfo schem;
                String n;
                for(File f: dir.listFiles())
                {
                    n =f.getName();
                  //  Engine.debug("Loading Land " + n + "...");
                    id = Integer.parseInt(n.substring(0, n.lastIndexOf(".")));
                    fis = new FileInputStream(f);
                    bis = new BufferedInputStream(fis);
                    schem = new LandInfo();
                    schem.deserialize(bis);
                    workEnvironment.blocks.put(id, schem);
                    bis.close();
                    fis.close();

                }
                workEnvironment.editingSession = 1;
                workEnvironment.update();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float v) {
        //Don't touch this
        Engine.render(v);
    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyDown(int i) {
        Engine.keyDown(i);
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        Engine.keyUp(i);
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        Engine.keyTyped(c);
        return false;
    }
    private Vector3 intersectPoint = new Vector3();
    private BoundingBox tmpBB = new BoundingBox();

    boolean intersect(Ray ray) {
        intersectPoint.set(0,0,0);
        ArrayList<GameObject> interse = new ArrayList<GameObject>();

        for(GameObject go: Engine.getWorld().instances) {
            tmpBB.set(go.getComponent(MeshRenderer.class).getBounds());
            tmpBB.mul(go.getTransform());
            if (Intersector.intersectRayBounds(ray, tmpBB, intersectPoint)) {
                interse.add(go);
            }
        }
        float dst = 100000000.f;
        GameObject nearest = null;
        Vector3 pos = new Vector3();
        for(GameObject go: interse)
        {
            go.getPosition(pos);
             if(pos.dst(camera.position) < dst)
             {
                 dst = pos.dst(camera.position);
                 nearest = go;
             }
        }
        if(nearest != null)
        {
            intersected = nearest;
            Engine.debug("Intersected: " + intersectPoint.toString());
            nearest.getPosition(pos);
            intersectPoint = intersectPoint.sub(pos);
            intersectPoint.nor();
            face(intersectPoint);
            return true;
        }
        return false;
    }

    private void face(Vector3 ip) {
        float x = Math.abs(ip.x);
        float y = Math.abs(ip.y);
        float z = Math.abs(ip.z);

        float a, b;
        a = Math.max(x, y);
        b = Math.max(a, z);
        if(b == x)
        {
            ip.y = 0;
            ip.z = 0;
            if(ip.x > 0)
                ip.x = 1;
            else
                ip.x = -1;
        }
        else if(b == z)
        {
            ip.y = 0;
            ip.x = 0;
            if(ip.z > 0)
                ip.z = 1;
            else
                ip.z = -1;
        }
        else
        {
            ip.x = 0;
            ip.z = 0;
            if(ip.y > 0)
                ip.y = 1;
            else
                ip.y = -1;
        }
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        Engine.touchDown(i, i1, i2, i3);
        if (intersect(camera.getPickRay(i, i1))) {
           if(i3 == 1)
               Delete();
           else
            Paint();

            return false;
        }
        return true;
    }

    private void Delete() {
        if (!intersected.tag.equalsIgnoreCase("center")) {
            Vector3 pos = new Vector3();
            intersected.getPosition(pos);
                workEnvironment.currEntry.blocks.remove(new UOVector((int) pos.x, (int) pos.y, (int) pos.z));
            Engine.getWorld().removeGameObject(intersected);
            Engine.debug(intersected.tag);
            Engine.debug(pos.toString());
            Engine.debug("removed!");
        }
        if(workEnvironment.currEntry.blocks.get(new UOVector(0,0,0)) == null)
        {
            createCentral();
        }
        saveEntry();
    }

    private void Paint() {
        if(brush != null) {
            Material mat = new Material();
            mat.set(new TextureAttribute(TextureAttribute.Diffuse, brush.tex));
            cubeModel = modelBuilder.createBox(1.0f, 1.0f, 1.0f, GL20.GL_TRIANGLES, mat, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
            GameObject go = new GameObject(cubeModel, "block", "block");
            if (!intersected.tag.equalsIgnoreCase("center")) {
                Vector3 pos = new Vector3();
                intersected.getPosition(pos);
                intersectPoint.add(pos);
                go.setPosition(intersectPoint);
                go.getTransform().setToTranslation(intersectPoint.x, intersectPoint.y, intersectPoint.z);
            } else {
                go.setPosition(new Vector3(0, 0, 0));
                go.getTransform().setToTranslation(0, 0, 0);
                Engine.getWorld().removeGameObject(central);
            }

            go.setCastingShadows(false);
            LandInfo info = new LandInfo();
            info.matId = brush.id;
            info.biomeid = 1;
            info.fill = false;
            info.useModifier = false;
            info.subId = brush.subid;
            Vector3 pos = new Vector3();
            go.getPosition(pos);
            workEnvironment.currEntry.blocks.put(new UOVector((int) pos.x, (int)pos.y, (int) pos.z), info);
            saveEntry();
        }
    }

    private void saveEntry() {
        if(workEnvironment.editingSession == 0) {
            File f = new File("out/schemas");
            if(f.exists())
            {
                File to = new File("out/schemas/" + workEnvironment.currEntry.id + ".bin");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(to);
                    BufferedOutputStream str = new BufferedOutputStream(fos);
                    Engine.debug("Size: " + workEnvironment.currEntry.blocks.size());
                    for(Map.Entry<UOVector, LandInfo> entry: workEnvironment.currEntry.blocks.entrySet())
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
                File to = new File("out/blocks/" + workEnvironment.currEntry.id + ".bin");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(to);
                    BufferedOutputStream str = new BufferedOutputStream(fos);
                    if(workEnvironment.currEntry.blocks.get(new UOVector(0,0,0)) != null)
                         workEnvironment.currEntry.blocks.get(new UOVector(0,0,0)).serialize(str);
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

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        Engine.touchUp(i, i1, i2, i3);
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        Engine.touchDragged(i, i1, i2);
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        Engine.mouseMoved(i, i1);
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        Engine.scrolled(i);
        return false;
    }

    public static void cantAddMore() {
        JOptionPane.showConfirmDialog(null, "This element already exists!");
    }

    public static void loadBlocks(Entry ent) {
        Engine.debug("Blocks count: " +  ent.blocks.size());
        clearCurrent();
        for(Map.Entry<UOVector, LandInfo> info: ent.blocks.entrySet()) {
            Material mat = new Material();
            Texture t = null;
            if (org.bukkit.Material.getMaterial(info.getValue().matId) != null)
                t = getTexById(org.bukkit.Material.getMaterial(info.getValue().matId),info.getValue().subId);
            else
                t = getTexById(info.getValue().matId,info.getValue().subId);
            if (t != null) {
                mat.set(new TextureAttribute(TextureAttribute.Diffuse, t));

                cubeModel = modelBuilder.createBox(1.0f, 1.0f, 1.0f, GL20.GL_TRIANGLES, mat, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
                GameObject block = new GameObject(cubeModel, "block", "block");
                block.setPosition(new Vector3(info.getKey().getBlockX(), info.getKey().getBlockY(), info.getKey().getBlockZ()));                       block.getTransform().setToTranslation(info.getKey().getBlockX(), info.getKey().getBlockY(), info.getKey().getBlockZ());

                block.setCastingShadows(false);
            }
            else
            {
                Engine.debug("No texture!");
            }
        }
    }

    private static void clearCurrent() {
        createCentral();
        GameObject go;
       for(int i = Engine.getWorld().instances.size() - 1; i >= 0; --i)
       {
           go = Engine.getWorld().instances.get(i);
           if(go != central)
           {
               Engine.getWorld().removeGameObject(go);
           }
       }
    }

    private static Texture getTexById(org.bukkit.Material m, byte b) {
        String name = m.name();
        Engine.debug(name);
        Texture tex = null;
        if(name.toLowerCase().contains("stairs")){
            tex = Engine.getTexture(name.toLowerCase() +"-east.png");
        }
        else {
            try {
                if (b > 0)
                    tex = Engine.getTexture(name.toLowerCase() + "," + b + ".png");
                else
                    tex = Engine.getTexture(name.toLowerCase() + ".png");
            } catch (Exception e) {
                Engine.logException(e);
            }
        }
        return tex;
    }
    private static Texture getTexById(int m,  byte b) {
        String name = String.valueOf(m);
        Engine.debug(name);
        Texture tex = null;

        if(name.toLowerCase().contains("stairs")){
            tex = Engine.getTexture(name.toLowerCase() +"-east.png");
        }else {
            try {
                if (b > 0)
                    tex = Engine.getTexture(name.toLowerCase() + "," + b + ".png");
                else
                    tex = Engine.getTexture(name.toLowerCase() + ".png");
            } catch (Exception e) {
                Engine.logException(e);
            }
        }
        return tex;
    }
}
