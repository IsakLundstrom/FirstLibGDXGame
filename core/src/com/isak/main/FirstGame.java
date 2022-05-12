package com.isak.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class FirstGame extends ApplicationAdapter {
	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;
	private SpriteBatch batch;
	private Stage stage;
	private BitmapFont font;
	private TextButton button;
	private GlyphLayout layout;

	//Touch variables
	private TouchElement touchElement;
	final private int touchImageSize = 100;
	final private String touchImagePath = "cucumber-pixel.png";

	//Player variables
	private Player player;
	private Vector2 playerStartPos;
	final private int playerRadius = 64;
	final private int playerAccConstant = 200; //Higher is slower
	final private int playerAccFriction = 50; //Higher is less friction
	final private String playerImagePath = "ratge-pixel.png";

	//Enemy variables
	private EnemySpawner enemySpawner;
	private int enemyRadius = 64;
	final private String enemyImagePath = "clown-pixel.png";

	//Game variables
	private int score = 0;
	private int highScore = 0;
	final private float enemyStartSpeed = 4;
	private float enemySpeedIncrease = 0.1f;
	final private float enemyMaxSpeed = 10f;
	private int maxNumberEnemies = 5;
	private boolean isPlayerAlive = false;
	private boolean hasPlayerDiedOnes = false;

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

		layout = new GlyphLayout();

		startAndResetButtonSetup();
		touchElement = new TouchElement(touchImageSize, touchImagePath);

		playerStartPos = new Vector2(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f);
		player = new Player(playerStartPos, playerRadius, playerAccConstant,
				playerAccFriction, playerImagePath);

		enemySpawner = new EnemySpawner(enemyStartSpeed, enemySpeedIncrease, enemyMaxSpeed,
				maxNumberEnemies, enemyRadius, enemyImagePath);
	}

	private void startAndResetButtonSetup() {
		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.font = font;
		textButtonStyle.fontColor = Color.WHITE;
		textButtonStyle.downFontColor= Color.BLUE;
		button = new TextButton("Start Game", textButtonStyle);
		button.setSize(200, 100);
		button.setPosition(Gdx.graphics.getWidth()/2f - button.getWidth()/2,
				Gdx.graphics.getHeight()/3.5f - button.getHeight()/2);
		button.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				resetGame();
				stage.clear();
			}
		});
		stage.addActor(button);
	}

	private void resetGame() {
		isPlayerAlive = true;
		player.reset(playerStartPos);
		enemySpawner.resetEnemies();
		score = 0;
	}

	@Override
	public void render () {
		//### Game Logic ###
		//Camera and touch position
		camera.update();
		touchElement.moveTouchPosition();
		camera.unproject(touchElement.getTouchPos());
		touchElement.setTouchImagePosition();

		//Check if enemies should spawn
		while(enemySpawner.getCurrentNumberEnemies() < enemySpawner.getMaxNumberEnemies()){
			enemySpawner.spawnEnemy();
		}

		if (isPlayerAlive) {
			player.movement(touchElement.getTouchPos().x, touchElement.getTouchPos().y);
			enemySpawner.moveEnemies();
		}

		checkPlayerEnemyCollision();
		player.checkIfHitWall();
		enemySpawner.checkEnemyDespawn();

		//### Drawing ###
		//Background Color
		Gdx.gl.glClearColor(119f/255f,136/255f,153f/255f,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		touchElement.drawLineBetweenTouchAndPlayer(player.getPlayerPos());
		if (isPlayerAlive) touchElement.render();
		player.render();
		player.drawCollider();
		player.drawRadius();
		enemySpawner.renderEnemies();
		enemySpawner.drawEnemiesColliders();

		if (!isPlayerAlive){
			drawStartAndResetScreen();
		}
		drawScore();
		if (isPlayerAlive) score++;
	}

	private void drawScore() {
		batch.begin();
		float scoreTextInsetPercentX = 0.97f;
		float scoreTextInsetPercentY = 0.97f;
		font.draw(batch, "Score: " + score, Gdx.graphics.getWidth()*(1 - scoreTextInsetPercentX),
				Gdx.graphics.getHeight() * scoreTextInsetPercentY);
		String highScoreText = "High Score: " + highScore;
		layout.setText(font, highScoreText);
		font.draw(batch, highScoreText, Gdx.graphics.getWidth() * scoreTextInsetPercentX - layout.width,
				Gdx.graphics.getHeight() * scoreTextInsetPercentY);
		batch.end();
	}

	private void drawStartAndResetScreen() {
		if(score > highScore) highScore = score;
		//Create a dark background overlay
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0,0,0,0.6f);
		shapeRenderer.rect(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		stage.draw();
		//If player has died ones draw reset message also
		if (!hasPlayerDiedOnes) return;
		batch.begin();
		String loseText = "Ratge got caught by the clowns...";
		font.getData().setScale(Gdx.graphics.getWidth()/500f);
		layout.setText(font, loseText);
		font.draw(batch, loseText, (Gdx.graphics.getWidth() - layout.width) / 2f,
				Gdx.graphics.getHeight() / 2f  + layout.height);
		loseText = "All he wanted was his cucumber...";
		font.draw(batch, loseText, (Gdx.graphics.getWidth() - layout.width) / 2f,
				Gdx.graphics.getHeight() / 2f  - layout.height);
		font.getData().setScale(Gdx.graphics.getWidth()/600f);
		batch.end();
	}

	private void checkPlayerEnemyCollision() {
		boolean isColliding;
		for (int enemy = enemySpawner.getCurrentNumberEnemies() - 1; enemy >= 0; enemy--){
			isColliding = player.getPlayerCollision().overlaps(enemySpawner.getEnemy(enemy).getEnemyCollision());
			if(isColliding) {
				playerLost();
			}
		}
	}

	private void playerLost() {
		isPlayerAlive = false;
		hasPlayerDiedOnes = true;
		button.setText("Restart?");
		stage.addActor(button);
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
