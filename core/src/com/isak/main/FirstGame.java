package com.isak.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class FirstGame extends ApplicationAdapter {
	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;
	private SpriteBatch batch;

	//Touch variables
	private Vector3 touchPos;
	private Sprite touchSprite;

	final private int touchImageSize = 128;
	final private String touchImagePath = "cucumber.png";

	//Player variables
	private Vector2 playerPos;
	private Vector2 playerVel;
	private Vector2 playerAcc;
	private Sprite playerSprite;
	private Circle playerCollision;

	final private int playerRadius = 64;
	final private int playerAccConstant = 1000; //Higher is slower
	final private int playerAccFriction = 50; //Higher is less friction
	final private String playerImagePath = "ratge.png";

	//Enemy variables
	private Vector2 enemyPos;
	private Vector2 enemyVel;
	private Sprite enemySprite;
	private Circle enemyCollision;

	private int enemyRadius = 128;
	final private String enemyImagePath = "clown.png";

	@Override
	public void create () {
		shapeRenderer = new ShapeRenderer();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();

		touchSetup();
		playerSetup();

		//enemy pos
		enemyPos = new Vector2(300,300);

		//enemny sprite setup
		Texture enemyTexture = new Texture(Gdx.files.internal(enemyImagePath));
		enemySprite = new Sprite(enemyTexture);
		enemySprite.setSize(enemyRadius, enemyRadius);
		float enemyStartPosX = enemyPos.x - enemySprite.getWidth()/2;
		float enemyStartPosY = enemyPos.y - enemySprite.getHeight()/2;
		enemySprite.setPosition(enemyStartPosX, enemyStartPosY);

		enemyCollision = new Circle(enemyPos.x, enemyPos.y, enemySprite.getWidth()/1.5f);
	}

	private void touchSetup() {
		touchPos = new Vector3();
		Texture touchTexture = new Texture(Gdx.files.internal(touchImagePath));
		touchSprite = new Sprite(touchTexture);
		touchSprite.setSize(touchImageSize, touchImageSize);
	}

	private void playerSetup() {
		//player position and movement setup
		playerPos = new Vector2(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		playerVel = new Vector2(0,0);
		playerAcc = new Vector2(0,0);

		//player sprite setup
		Texture playerTexture = new Texture(Gdx.files.internal(playerImagePath));
		playerSprite = new Sprite(playerTexture);
		playerSprite.setSize(playerRadius*2, playerRadius*2);
		float playerStartPosX = playerPos.x - playerRadius;
		float playerStartPosY = playerPos.y - playerRadius;
		playerSprite.setPosition(playerStartPosX, playerStartPosY);
		playerSprite.setOrigin(playerRadius, playerRadius); //Set origin for rotation

		//player collsion setup
		float playerCollsionRadius = playerRadius*0.8f;
		playerCollision = new Circle(playerPos.x, playerPos.y, playerCollsionRadius);
	}

	@Override
	public void render () {
		//### Game Logic ###
		//Camera and touch position
		camera.update();
		if(Gdx.input.isTouched()) touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(touchPos);

		//Player
		playerMovement();
		bouncePlayerOnWallHit();

		//check collision
		boolean isOverlaping = playerCollision.overlaps(enemyCollision);
		if(isOverlaping) {
			System.out.println("overlap");
		}

		//### Drawing ###
		//Background Color
		Gdx.gl.glClearColor(119f/255f,136/255f,153f/255f,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if(Gdx.input.isTouched()) {
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

//			//Draw touch pos circle
//			shapeRenderer.setColor(Color.RED);
//			shapeRenderer.circle(touchPos.x, touchPos.y, 32);

			//Draw line between circle and touch
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.line(touchPos.x, touchPos.y, playerPos.x, playerPos.y);

			shapeRenderer.end();

			//Render cucumber
			touchSprite.setPosition(touchPos.x - touchImageSize/2,
					touchPos.y - touchImageSize/2);
			batch.begin();
			touchSprite.draw(batch);
			batch.end();

		}


		//Draw player radius and collsion
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		//radius
		shapeRenderer.setColor(Color.WHITE);
		shapeRenderer.circle(playerPos.x, playerPos.y, playerRadius);

		//collision
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.circle(playerPos.x, playerPos.y, playerCollision.radius);
		shapeRenderer.end();

		//player sprite
		playerSprite.setPosition(playerPos.x - playerRadius,
				playerPos.y - playerRadius);

		batch.begin();
		playerSprite.draw(batch);
		if (playerVel.x != 0) {
			float rotation = (float) (Math.atan(playerVel.y / playerVel.x) * 360 / (2 * Math.PI));
			if (playerVel.x < 0) rotation += 180;
			playerSprite.setRotation(rotation);
		}
		batch.end();

		//enemy

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.ORANGE);
		shapeRenderer.circle(enemyPos.x, enemyPos.y, enemyCollision.radius);
		shapeRenderer.end();

		batch.begin();
		enemySprite.draw(batch);
		batch.end();

	}

	private void bouncePlayerOnWallHit() {
		//Check walls for player, bounce if hit
		if(playerPos.x + playerRadius > Gdx.graphics.getWidth()
				|| playerPos.x - playerRadius < 0)
			playerVel.set(-playerVel.x, playerVel.y);
		if(playerPos.y + playerRadius > Gdx.graphics.getHeight()
				|| playerPos.y - playerRadius < 0)
			playerVel.set(playerVel.x, -playerVel.y);
	}

	private void playerMovement() {
		if(Gdx.input.isTouched()) {
			//Move player towards touchPos
			playerAcc.set((touchPos.x - playerPos.x) / playerAccConstant,
					(touchPos.y - playerPos.y)/ playerAccConstant);
			playerVel.set(playerVel.x + playerAcc.x, playerVel.y + playerAcc.y);
			playerPos.set(playerPos.x + playerVel.x, playerPos.y + playerVel.y);
		}

		//Add friction depending on velocity
		playerAcc.set(-playerVel.x/playerAccFriction, -playerVel.y/playerAccFriction);
		playerVel.set(playerVel.x + playerAcc.x, playerVel.y + playerAcc.y);
		playerPos.set(playerPos.x + playerVel.x, playerPos.y + playerVel.y);

		//Move player collision with the player
		playerCollision.setPosition(playerPos);
	}

	@Override
	public void dispose () {

	}
}
