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

	final private int touchImageSize = 128;
	final private String touchImagePath = "cucumber.png";

	//Player variables
	private Vector2 playerPos;
	private Vector2 playerVel;
	private Vector2 playerAcc;
	private Sprite playerSprite;
	private Circle playerCollision;

	final private int playerRadius = 64;
	final private int playerAccConstant = 200; //Higher is slower
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
	private int score = 0;
	private int highScore = 0;
	final private float enemyStartSpeed = 4;
	private float currentEnemySpeed = enemyStartSpeed;
	private float enemySpeedIncrease = 0.1f;
	final private float enemyMaxSpeed = 10f;
	private int maxNumberEnemies = 5;
	private int currentNumberEnemies = 0;
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
		enemySetup();
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
		for (int enemy = enemyPos.size - 1; enemy >= 0; enemy--){
			enemyPos.pop();
			enemyVel.pop();
			enemySprite.pop();
			enemyCollision.pop();
		}
		currentNumberEnemies = 0;
		currentEnemySpeed = enemyStartSpeed;
		score = 0;
	}

	private void enemySetup() {
		enemyPos = new Array<>();
		enemyVel = new Array<>();
		enemyTexture = new Texture(Gdx.files.internal(enemyImagePath));
		enemySprite = new Array<>();
		enemyCollision = new Array<>();
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
		while(currentNumberEnemies < maxNumberEnemies){
			spawnEnemy();
		}
		if (isPlayerAlive) {
			playerMovement();
			enemyMovement();
		}
		checkEnemyDespawn();
		checkPlayerEnemyCollision();
		bouncePlayerOnWallHit();

		//### Drawing ###
		//Background Color
		Gdx.gl.glClearColor(119f/255f,136/255f,153f/255f,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (isPlayerAlive) drawTouch();
		drawPlayer();
		drawEnemies();

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

	private void drawEnemies() {
		//enemy
		batch.begin();
		for (int enemy = enemyPos.size - 1; enemy >= 0; enemy--){
			enemySprite.get(enemy).setPosition(enemyPos.get(enemy).x - enemyRadius,
					enemyPos.get(enemy).y - enemyRadius);
			enemySprite.get(enemy).draw(batch);
		}
		batch.end();

		//Draw enemies colliders
//		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//		for (int enemy = enemyPos.size - 1; enemy >= 0; enemy--){
//			shapeRenderer.setColor(Color.GREEN);
//			shapeRenderer.circle(enemyPos.get(enemy).x, enemyPos.get(enemy).y, enemyCollision.get(enemy).radius);
//		}
//		shapeRenderer.end();
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
		for (int enemy = enemyPos.size - 1; enemy >= 0; enemy--){
			isColliding = playerCollision.overlaps(enemyCollision.get(enemy));
			if(isColliding) {
				isPlayerAlive = false;
				stage.addActor(button);
			}
		}
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
				currentNumberEnemies--;
				if(currentEnemySpeed < enemyMaxSpeed) currentEnemySpeed += enemySpeedIncrease;
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
		currentNumberEnemies++;
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
		float angle = MathUtils.atan2(Gdx.graphics.getHeight()/2f - enemyStartPosY,
				Gdx.graphics.getWidth()/2f - enemyStartPosX);
		angle += MathUtils.random(-1f, 1f) * MathUtils.PI/2; // add some randomness
//		angle += MathUtils.PI/8; // comment above, uncomment for a nice effect
		float enemyVelX = currentEnemySpeed * MathUtils.cos(angle) + MathUtils.random(-0.5f, 0.5f);
		float enemyVelY = currentEnemySpeed * MathUtils.sin(angle) + MathUtils.random(-0.5f, 0.5f);
		enemyVel.add(new Vector2(enemyVelX, enemyVelY));

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
	public void pause () {

	}

	@Override
	public void resume () {

	}

	@Override
	public void dispose () {

	}
}
