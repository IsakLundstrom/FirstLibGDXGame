package com.isak.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Player {
    private Vector2 playerPos;
    private Vector2 playerVel;
    private Vector2 playerAcc;
    private Sprite playerSprite;
    private Circle playerCollision;

    private int playerRadius;
    private int playerAccConstant; //Higher is slower
    private int playerAccFriction; //Higher is less friction
    private String playerImagePath;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    /**
     * Constructor for a player with given start position, radius in pixels, acceleration constant,
     * acceleration friction constant and the image path to use
     * @param playerPos Starting position in pixels
     * @param playerRadius Radius in pixels
     * @param playerAccConstant Acceleration constant (Higher is slower)
     * @param playerAccFriction Friction constant (Higher is less friction)
     * @param playerImagePath Path to the image to use
     */
    public Player(Vector2 playerStartPos, int playerRadius, int playerAccConstant,
                  int playerAccFriction, String playerImagePath) {
        this.playerPos = playerStartPos;
        this.playerRadius = playerRadius;
        this.playerAccConstant = playerAccConstant;
        this.playerAccFriction = playerAccFriction;
        this.playerImagePath = playerImagePath;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        setup();
    }

    private void setup() {
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

    /**
     * Reset player position, velocity, acceleration and rotation
     * @param resetPosition Position to reset to
     */
    public void reset(Vector2 resetPosition){
        playerPos.set(resetPosition);
        playerVel.set(0,0);
        playerAcc.set(0,0);
        playerSprite.setRotation(0);
    }

    /**
     * Render the player sprite using SpriteBatch
     */
    public void render() {
        batch.begin();
        playerSprite.draw(batch);
        batch.end();
    }

    /**
     * Draw collider
     */
    public void drawCollider(){
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.circle(playerPos.x, playerPos.y, playerCollision.radius);
		shapeRenderer.end();
    }
    /**
     * Draw radius
     */
    public void drawRadius(){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(playerPos.x, playerPos.y, playerRadius);
        shapeRenderer.end();
    }


    /**
     * Move player towards (touchPosX, touchPosY) using the acceleration constant, add friction
     * based on the current velocity, also move collision and sprite with the player position
     * @param touchPosX The x position to move the player
     * @param touchPosY The y position to move the player
     */
    public void movement(float touchPosX, float touchPosY) {
        movement(new Vector2(touchPosX, touchPosY));
    }

    /**
     * Move player towards touchPos using the acceleration constant, add friction based
     * on the current velocity, also move collision and sprite with the player position
     * @param touchPos The position to move the player
     */
    public void movement(Vector2 touchPos) {
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

        //Move player collision and sprite with the player pos
        playerCollision.setPosition(playerPos);
        playerSprite.setPosition(playerPos.x - playerRadius, playerPos.y - playerRadius);
        //Set sprite rotation
        if (playerVel.x != 0) {
            float rotation = MathUtils.atan2(playerVel.y, playerVel.x) / MathUtils.PI2 * 360;
            playerSprite.setRotation(rotation);
        }
    }

    /**
     * If player hits wall bounce on the wall, and to not get stuck in the wall place the player
     * next to the wall if it tries to jump past them
     */
    public void checkIfHitWall() {
        //Check walls for player, bounce if hit
        if(playerPos.x + playerRadius > Gdx.graphics.getWidth()
                || playerPos.x - playerRadius < 0) playerVel.set(-playerVel.x, playerVel.y);
        if(playerPos.y + playerRadius > Gdx.graphics.getHeight()
                || playerPos.y - playerRadius < 0) playerVel.set(playerVel.x, -playerVel.y);

        //If trying to move past the right wall
        if(playerPos.x + playerRadius > Gdx.graphics.getWidth())
            playerPos.set(Gdx.graphics.getWidth() - playerRadius , playerPos.y);
        //Left wall
        if(playerPos.x - playerRadius < 0) playerPos.set(playerRadius, playerPos.y);
        //Top wall
        if (playerPos.y + playerRadius > Gdx.graphics.getHeight())
            playerPos.set(playerPos.x, Gdx.graphics.getHeight() - playerRadius);
        //Bottom wall
        if(playerPos.y - playerRadius < 0) playerPos.set(playerPos.x, playerRadius);
    }

    public Vector2 getPlayerPos() {
        return playerPos;
    }

    public Circle getPlayerCollision() {
        return playerCollision;
    }
}
