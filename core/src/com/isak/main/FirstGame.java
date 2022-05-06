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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

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
	private Array<Vector2> enemyPos;
	private Array<Vector2> enemyVel;
	private Texture enemyTexture;
	private Array<Sprite> enemySprite;
	private Array<Circle> enemyCollision;

	private int enemyRadius = 64;
	final private String enemyImagePath = "clown.png";

	//Game variables
	private float enemySpeed = 2;
	private float enemySpeedIncrease = 0.01f;
	private int numberEnemies = 5;

	@Override
	public void create () {
		shapeRenderer = new ShapeRenderer();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();

		touchSetup();
		playerSetup();
		enemySetup();
	}

	private void enemySetup() {
		enemyPos = new Array<Vector2>();
		enemyVel = new Array<Vector2>();
		enemyTexture = new Texture(Gdx.files.internal(enemyImagePath));
		enemySprite = new Array<Sprite>();
		enemyCollision = new Array<Circle>();
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
		float playerSpriteStartPosX = playerPos.x - playerRadius;
		float playerSpriteStartPosY = playerPos.y - playerRadius;
		playerSprite.setPosition(playerSpriteStartPosX, playerSpriteStartPosY);
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

		//Enemy
		//Check if enemy should spawn
		if(numberEnemies > 0){
			numberEnemies--;
			spawnEnemy();
		}
		enemyMovement();
		checkEnemyDespawn();


		//check collision
//		boolean isOverlaping = playerCollision.overlaps(enemyCollision);
//		if(isOverlaping) {
//			System.out.println("overlap");
//		}

		//### Drawing ###
		//Background Color
		Gdx.gl.glClearColor(119f/255f,136/255f,153f/255f,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if(Gdx.input.isTouched()) {
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

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

		//player sprite
		playerSprite.setPosition(playerPos.x - playerRadius,
				playerPos.y - playerRadius);

		batch.begin();
		playerSprite.draw(batch);
		if (playerVel.x != 0) {
			float rotation = MathUtils.atan2(playerVel.y, playerVel.x) / MathUtils.PI2 * 360;
			playerSprite.setRotation(rotation);
		}
		batch.end();

		//Player radius and collision
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		//radius
		shapeRenderer.setColor(Color.WHITE);
		shapeRenderer.circle(playerPos.x, playerPos.y, playerRadius);

		//collision
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.circle(playerPos.x, playerPos.y, playerCollision.radius);
		shapeRenderer.end();

		//enemy
		batch.begin();
		for (int enemy = enemyPos.size - 1; enemy >= 0; enemy--){
			enemySprite.get(enemy).setPosition(enemyPos.get(enemy).x - enemyRadius,
					enemyPos.get(enemy).y - enemyRadius);
			enemySprite.get(enemy).draw(batch);
		}
		batch.end();

		//Draw enemies colliders
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		for (int enemy = enemyPos.size - 1; enemy >= 0; enemy--){
			shapeRenderer.setColor(Color.GREEN);
			shapeRenderer.circle(enemyPos.get(enemy).x, enemyPos.get(enemy).y, enemyCollision.get(enemy).radius);
		}
		shapeRenderer.end();
	}

	private void checkEnemyDespawn() {
		for (int enemy = enemyPos.size - 1; enemy >= 0; enemy--){
			//If outside screen => remove enemy
			if(enemyPos.get(enemy).x > Gdx.graphics.getWidth() + 1.1*enemyRadius ||
					enemyPos.get(enemy).x < -1.1*enemyRadius ||
					enemyPos.get(enemy).y > Gdx.graphics.getHeight() + 1.1*enemyRadius ||
					enemyPos.get(enemy).y < -1.1*enemyRadius){
				enemyPos.removeIndex(enemy);
				enemyVel.removeIndex(enemy);
				enemySprite.removeIndex(enemy);
				enemyCollision.removeIndex(enemy);
				numberEnemies++;
				enemySpeed += enemySpeedIncrease;
			}
		}
	}

	private void enemyMovement() {
		for (int enemy = enemyPos.size - 1; enemy >= 0; enemy--){
			enemyPos.get(enemy).set(enemyPos.get(enemy).x + enemyVel.get(enemy).x,
					enemyPos.get(enemy).y + enemyVel.get(enemy).y);
			//Move collision with enemy
			enemyCollision.get(enemy).setPosition(enemyPos.get(enemy));
		}
	}

	private void spawnEnemy() {
		//#Position
		float enemyStartPosX, enemyStartPosY;
		//Determine if enemy start above, below, right or left
		if(MathUtils.random() > 0.5) { //Spawns above or below
			enemyStartPosX = MathUtils.random() * Gdx.graphics.getWidth();
			if(MathUtils.random() > 0.5) { //Spawns above
				enemyStartPosY = Gdx.graphics.getHeight() + enemyRadius;
			}
			else { //Spawns below
				enemyStartPosY = -enemyRadius;
			}
		}
		else { //Spawns right or left
			enemyStartPosY = MathUtils.random() * Gdx.graphics.getHeight();
			if(MathUtils.random() > 0.5) { //Spawns right
				enemyStartPosX = Gdx.graphics.getWidth() + enemyRadius;
			}
			else { //Spawns left
				enemyStartPosX = -enemyRadius;
			}
		}
		enemyPos.add(new Vector2(enemyStartPosX, enemyStartPosY));

		//#Velocity
		//Angle the enemy towards the center
		float angle = MathUtils.atan2(Gdx.graphics.getHeight()/2 - enemyStartPosY,
				Gdx.graphics.getWidth()/2 - enemyStartPosX);
		angle += MathUtils.random(-1f, 1f) * MathUtils.PI/2; // add some randomness
		float enemyVelX = enemySpeed*MathUtils.cos(angle);
		float enemyVelY = enemySpeed*MathUtils.sin(angle);
		enemyVel.add(new Vector2(enemyVelX, enemyVelY));
//		System.out.println(new Vector2(enemyVelX, enemyVelY));

		//#Sprite
		int newestEnemy = enemyPos.size-1;
		enemySprite.add(new Sprite(enemyTexture));
		enemySprite.get(newestEnemy).setSize(2*enemyRadius, 2*enemyRadius);
		float enemySpritePosX = enemyStartPosX - enemyRadius;
		float enemySpritePosY = enemyStartPosY - enemyRadius;
		enemySprite.get(newestEnemy).setPosition(enemySpritePosX, enemySpritePosY);

		//#Rotation
		enemySprite.get(newestEnemy).setOrigin(enemyRadius, enemyRadius);
		enemySprite.get(newestEnemy).setRotation(angle/MathUtils.PI2*360 - 90);

		//#Collision
		enemyCollision.add(new Circle(enemyStartPosX, enemyStartPosY, enemyRadius));
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
