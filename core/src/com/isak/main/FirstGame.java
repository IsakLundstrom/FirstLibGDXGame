package com.isak.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class FirstGame extends ApplicationAdapter {
	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;
	private SpriteBatch batch;
	private Stage stage;
	private BitmapFont font;
	private TextButton button;

	//Touch variables
	private Vector3 touchPos;
	private Sprite touchSprite;

	final private int touchImageSize = 100;
	final private String touchImagePath = "cucumber-pixel.png";

	//Player variables
	private Vector2 playerPos;
	private Vector2 playerVel;
	private Vector2 playerAcc;
	private Sprite playerSprite;
	private Circle playerCollision;

	final private int playerRadius = 64;
	final private int playerAccConstant = 200; //Higher is slower
	final private int playerAccFriction = 50; //Higher is less friction
	final private String playerImagePath = "ratge-pixel.png";

	//Enemy variables
	private int enemyRadius = 64;
	final private String enemyImagePath = "clown-pixel.png";

	EnemySpawner enemySpawner;

	//Game variables
	private int score = 0;
	private int highScore = 0;
	final private float enemyStartSpeed = 4;
	private float enemySpeedIncrease = 0.1f;
	final private float enemyMaxSpeed = 10f;
	private int maxNumberEnemies = 5;
	private boolean isPlayerAlive = true;

	@Override
	public void create () {
		shapeRenderer = new ShapeRenderer();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();

		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);
		font = new BitmapFont();
		font.getData().setScale(Gdx.graphics.getWidth()/600f);

		resetButtonSetup();
		touchSetup();
		playerSetup();

		enemySpawner = new EnemySpawner(enemyStartSpeed, enemySpeedIncrease, enemyMaxSpeed,
				maxNumberEnemies, enemyRadius, enemyImagePath);
	}

	private void resetButtonSetup() {
		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.font = font;
		textButtonStyle.fontColor = Color.WHITE;
		textButtonStyle.downFontColor= Color.BLUE;
		button = new TextButton("Restart?", textButtonStyle);
		button.setSize(200, 100);
		button.setPosition(Gdx.graphics.getWidth()/2f - button.getWidth()/2, Gdx.graphics.getHeight()/2f - button.getHeight()/2);
		button.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				resetGame();
				stage.clear();
			}
		});
	}

	private void resetGame() {
		isPlayerAlive = true;
		playerPos.set(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f);
		playerVel.set(0,0);
		playerAcc.set(0,0);
		playerSprite.setRotation(0);

		for (int enemy = enemySpawner.getCurrentNumberEnemies() - 1; enemy >= 0; enemy--){
			enemySpawner.despawnEnemy(enemy);
			enemySpawner.setCurrentEnemySpeed(enemySpawner.getEnemyStartSpeed());
		}
		currentNumberEnemies = 0;
		currentEnemySpeed = enemyStartSpeed;
		score = 0;
	}

	private void touchSetup() {
		touchPos = new Vector3();
		Texture touchTexture = new Texture(Gdx.files.internal(touchImagePath));
		touchSprite = new Sprite(touchTexture);
		touchSprite.setSize(touchImageSize, touchImageSize);
	}

	private void playerSetup() {
		//player position and movement setup
		playerPos = new Vector2(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f);
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

		//player collision setup
		float playerCollisionRadius = playerRadius*0.8f;
		playerCollision = new Circle(playerPos.x, playerPos.y, playerCollisionRadius);
	}

	@Override
	public void render () {
		//### Game Logic ###
		//Camera and touch position
		camera.update();
		if(Gdx.input.isTouched()) touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(touchPos);

		//Check if enemies should spawn
		while(enemySpawner.getCurrentNumberEnemies() < enemySpawner.getMaxNumberEnemies()){
//			spawnEnemy();
			enemySpawner.spawnEnemy();
		}
		if (isPlayerAlive) {
			playerMovement();
			enemySpawner.moveEnemies();
		}
//		checkEnemyDespawn();
		enemySpawner.checkEnemyDespawn();
		checkPlayerEnemyCollision();
		bouncePlayerOnWallHit();

		//### Drawing ###
		//Background Color
		Gdx.gl.glClearColor(119f/255f,136/255f,153f/255f,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (isPlayerAlive) drawTouch();
		drawPlayer();
		enemySpawner.drawEnemies();

		//Draw enemies colliders
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		for (int enemy = enemySpawner.getCurrentNumberEnemies() - 1; enemy >= 0; enemy--){
			shapeRenderer.setColor(Color.GREEN);
			shapeRenderer.circle(enemySpawner.getEnemy(enemy).getPos().x,
					enemySpawner.getEnemy(enemy).getPos().y, enemyRadius);
		}
		shapeRenderer.end();

		if (!isPlayerAlive){
			drawResetButton();
		}
		drawScore();
		if (isPlayerAlive) score++;
	}

	private void drawScore() {
		batch.begin();
		font.draw(batch, "Score: " + score, 50, Gdx.graphics.getHeight() - 20);
		font.draw(batch, "High Score: " + highScore, Gdx.graphics.getWidth() - Gdx.graphics.getWidth()/4f, Gdx.graphics.getHeight() - 20);
		batch.end();
	}

	private void drawResetButton() {
		if(score > highScore) highScore = score;
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0,0,0,0.6f);
		shapeRenderer.rect(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		stage.draw();
	}

	private void drawPlayer() {
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

//		//Player radius and collision
//		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//		//radius
//		shapeRenderer.setColor(Color.WHITE);
//		shapeRenderer.circle(playerPos.x, playerPos.y, playerRadius);
//
//		//collision
//		shapeRenderer.setColor(Color.GREEN);
//		shapeRenderer.circle(playerPos.x, playerPos.y, playerCollision.radius);
//		shapeRenderer.end();
	}

	private void drawTouch() {
		if(Gdx.input.isTouched()) {
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

			//Draw line between circle and touch
			Color green = new Color(119f/255f,178f/255f,85f/255f,1);
			shapeRenderer.setColor(green);
			shapeRenderer.line(touchPos.x, touchPos.y, playerPos.x, playerPos.y);
			shapeRenderer.end();

			//Render cucumber
			touchSprite.setPosition(touchPos.x - touchImageSize/2f,
					touchPos.y - touchImageSize/2f);
			batch.begin();
			touchSprite.draw(batch);
			batch.end();
		}
	}

	private void checkPlayerEnemyCollision() {
		boolean isColliding;
//		System.out.println(enemySpawner.getCurrentNumberEnemies());
		for (int enemy = enemySpawner.getCurrentNumberEnemies() - 1; enemy >= 0; enemy--){
//			isColliding = playerCollision.overlaps(enemyCollision.get(enemy));
			isColliding = playerCollision.overlaps(enemySpawner.getEnemy(enemy).getCollision());
			if(isColliding) {
				isPlayerAlive = false;
				stage.addActor(button);
			}
		}
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
	public void pause () {

	}

	@Override
	public void resume () {

	}

	@Override
	public void dispose () {

	}
}
