package com.folder.Screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.folder.Boot;
import com.folder.UI.Status;

import com.folder.Object.*;
import com.folder.Object.Enemy.Enemy;
import com.folder.Tool.*;

public class GameScreen implements Screen {
    private Status hud;
    private SpriteBatch batch;

    public static OrthographicCamera camera;
    private Viewport gamePort;
    private Boot boot;

    private World world;

    private TmxMapLoader mapLoader;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;

    private Box2DDebugRenderer debug;

    private TextureAtlas atlas;
    private TextureAtlas enemyAtlas;
    private TextureAtlas hellDogAtlas;
    private TextureAtlas AnimationTileSetAtlas;
    private TextureAtlas bossAtlas;
    private TextureAtlas effectAtlas;

    //Object
    private ObjectCreate object;
    //ObjectTest objectTest;

    private MainCharacter player;

    public static GameScreen INSTANCE;

    LightHandle lightTest;

    public GameScreen(Boot boot) {
        world = new World(new Vector2(0, -10f), false);
        this.boot = boot;
        batch = boot.batch;

        camera = new OrthographicCamera();
        //gamePort = new StretchViewport(Boot.screenWidth / Boot.PPM, Boot.screenHeight / Boot.PPM, camera);
        gamePort = new StretchViewport(Boot.screenWidth / Boot.PPM - 1040 / Boot.PPM, Boot.screenHeight / Boot.PPM - 660 / Boot.PPM, camera);
        mapLoader = new TmxMapLoader();
        tiledMap = mapLoader.load("Map1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / Boot.PPM);

        debug = new Box2DDebugRenderer();

        atlas = new TextureAtlas("SpriteSheet/wizard.atlas");
        enemyAtlas = new TextureAtlas("SpriteSheet/werewolves.atlas");
        hellDogAtlas = new TextureAtlas("SpriteSheet/HellDog.atlas");
        AnimationTileSetAtlas = new TextureAtlas("SpriteSheet/AnimationTileset.atlas");
        bossAtlas = new TextureAtlas("SpriteSheet/ElementEnemy.atlas");
        effectAtlas = new TextureAtlas("SpriteSheet/effectAtlas.atlas");

        // object = new ObjectTest(this);dddd

        player = new MainCharacter(this);

        object = new ObjectCreate(this);

        hud = new Status(batch, this);

        world.setContactListener(new CollisionHandle());

        // lightTest = new LightHandle(this);

    }

    public static GameScreen screen() {
        return INSTANCE;
    }

    public void update(float deltaTime) {

        world.step(1 / 60f, 6, 2);

        camera.position.x = player.getBody().getPosition().x;

        if (!MainCharacter.isInMap2)
            if (camera.position.x <= 440 / Boot.PPM)
                camera.position.x = 440 / Boot.PPM;
            else if (camera.position.x >= 830 / Boot.PPM)
                camera.position.x = 830 / Boot.PPM;
        if (MainCharacter.isInMap2)
            if (camera.position.x <= 440 / Boot.PPM)
                camera.position.x = 440 / Boot.PPM;
            else if (camera.position.x >= 2111 / Boot.PPM)
                camera.position.x = 2111 / Boot.PPM;

        camera.update();

        mapRenderer.setView(camera);

        hud.update();

        player.update(deltaTime);

        for (Enemy enemy : object.getEnemyList())
            enemy.update(deltaTime);

        for (AnimationTileSetCreate animationTileSet : object.getAnimationTileSet())
            animationTileSet.update(deltaTime);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.render();

        if (MenuHandle.isMenu) {
            boot.setScreen(new MenuScreen(boot));
            MenuHandle.isMenu = false;
        }
        hud.getStage().draw();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();



        player.draw(batch);

        for (Enemy enemy : object.getEnemyList())
            enemy.draw(batch);

        for (AnimationTileSetCreate animationTileSet : object.getAnimationTileSet())
            animationTileSet.draw(batch);

        batch.end();
//        lightTest.getRayHandler().setCombinedMatrix(camera);
//        lightTest.getRayHandler().updateAndRender();
        debug.render(world, camera.combined);
    }


    public World getWorld() {
        return world;
    }

    public TiledMap getMap() {
        return tiledMap;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public TextureAtlas getEnemyAtlas() {
        return enemyAtlas;
    }

    public TextureAtlas getHellDogAtlas() {
        return hellDogAtlas;
    }

    public TextureAtlas getAnimationTileSetAtlas() {
        return AnimationTileSetAtlas;
    }

    public TextureAtlas getBossAtlas() {
        return bossAtlas;
    }

    public TextureAtlas getEffectAtlas() {
        return effectAtlas;
    }

    public void loadMap(String mapName) {
        tiledMap.dispose();
        tiledMap = mapLoader.load(mapName);
        mapRenderer.setMap(tiledMap);

        object.clear();
        object = new ObjectCreate(this);
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height, true);
    }

    @Override
    public void dispose() {
        atlas.dispose();
        world.dispose();
        batch.dispose();
        hud.dispose();
    }

    @Override
    public void show() {

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

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return gamePort;
    }

}
