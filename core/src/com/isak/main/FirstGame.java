package com.isak.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import javax.swing.SpringLayout;

public class FirstGame extends ApplicationAdapter {

	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;
	private Vector3 touchPos;
	private AssetManager assetManager;
	private Texture ratgeTexture;
	private Sprite ratgeSprite;
	private SpriteBatch batch;

	private Vector2 playerPos;
	private Vector2 playerVel;
	private Vector2 playerAcc;

	private int playerRadius = 64;
	private int playerAccConstant = 1000; //Higher is slower
	private int playerAccFriction = 50; //Higher is less friction



	
	@Override
	public void create () {
		shapeRenderer = new ShapeRenderer();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		assetManager = new AssetManager();
		assetManager.load("ratge.png", Texture.class);
		assetManager.finishLoading();

		batch = new SpriteBatch();
		//ratgeTexture = assetManager.get("ratge.png");
		ratgeTexture = new Texture(Gdx.files.internal("ratge.png"));
		ratgeSprite = new Sprite(ratgeTexture);
		ratgeSprite.setPosition(Gdx.graphics.getWidth()/2 - ratgeSprite.getWidth()/2,
				Gdx.graphics.getHeight()/2 - ratgeSprite.getHeight()/2);

		touchPos = new Vector3(0, 0, 0);

		playerPos = new Vector2(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		playerVel = new Vector2(0,0);
		playerAcc = new Vector2(0,0);

	}

	@Override
	public void render () {
		//Game Logic
		camera.update();

		if(Gdx.input.isTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);

			//Move player towards touchPos
			playerAcc.set((touchPos.x - playerPos.x) / playerAccConstant,
					(Gdx.graphics.getHeight() - touchPos.y - playerPos.y)/ playerAccConstant);
			playerVel.set(playerVel.x + playerAcc.x, playerVel.y + playerAcc.y);
			playerPos.set(playerPos.x + playerVel.x, playerPos.y + playerVel.y);
			
			camera.unproject(touchPos);
		}
		//Friction
		playerAcc.set(-playerVel.x/playerAccFriction, -playerVel.y/playerAccFriction);
		playerVel.set(playerVel.x + playerAcc.x, playerVel.y + playerAcc.y);
		playerPos.set(playerPos.x + playerVel.x, playerPos.y + playerVel.y);

		//Check walls for player, bounce if hit
		if(playerPos.x + playerRadius > Gdx.graphics.getWidth() || playerPos.x - playerRadius < 0)
			playerVel.set(-playerVel.x, playerVel.y);
		if(playerPos.y + playerRadius > Gdx.graphics.getHeight() || playerPos.y - playerRadius < 0)
			playerVel.set(playerVel.x, -playerVel.y);

		//Drawing
		Gdx.gl.glClearColor(1,1,1,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//Draw moving circle
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.circle(playerPos.x, playerPos.y, playerRadius);


		if(Gdx.input.isTouched()) {
//			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			//Draw mouse pos
			shapeRenderer.setColor(Color.RED);
			shapeRenderer.circle(touchPos.x, touchPos.y, 32);

			//Draw line between circle and mouse
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.line(touchPos.x, touchPos.y, playerPos.x, playerPos.y);

			//Werid
//			shapeRenderer.setColor(Color.ORANGE);
//			shapeRenderer.line(Gdx.input.getX(), Gdx.input.getY(), position.x, position.y);
//			System.out.println("2 Gdx " + Gdx.input.getX() + " " + Gdx.input.getY());
//			System.out.println("mouse " + mousePos.x + " " + mousePos.y);

		}


		shapeRenderer.end();

		ratgeSprite.setPosition(playerPos.x - ratgeSprite.getWidth()/2,
				playerPos.y - ratgeSprite.getHeight()/2);

		batch.begin();
		ratgeSprite.draw(batch);
		batch.end();
	}
	
	@Override
	public void dispose () {

	}
}
