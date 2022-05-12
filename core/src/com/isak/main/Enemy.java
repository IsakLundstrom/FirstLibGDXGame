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

public class Enemy {

    private Vector2 enemyPos;
    private Vector2 enemyVel;
    private Texture enemyTexture;
    private Sprite enemySprite;
    private Circle enemyCollision;

    private int enemyRadius;
    private String enemyImagePath;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    /**
     * Constructor for an enemy
     * @param pos Starting position in pixels
     * @param vel Starting velocity
     * @param radius Radius in pixels
     * @param imagePath Path to the image
     */
    public Enemy(Vector2 pos, Vector2 vel, int radius, String imagePath){
        this.enemyPos = pos;
        this.enemyVel = vel;
        this.enemyRadius = radius;
        this.enemyImagePath = imagePath;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        setup();
    }

    private void setup() {
        enemyTexture = new Texture(Gdx.files.internal(enemyImagePath));
        enemySprite = new Sprite(enemyTexture);
        enemySprite.setSize(2* enemyRadius, 2* enemyRadius);
        enemySprite.setPosition(enemyPos.x - enemyRadius, enemyPos.y - enemyRadius);
        enemySprite.setOrigin(enemyRadius, enemyRadius);
        float angle = MathUtils.atan2(enemyVel.y, enemyVel.x);
        enemySprite.setRotation(angle/ MathUtils.PI2*360 - 90);
        enemyCollision = new Circle(enemyPos.x, enemyPos.y, enemyRadius);
    }

    /**
     * Move the enemy position, collision circle and sprite
     */
    public void movement(){
        enemyPos.set(enemyPos.x + enemyVel.x, enemyPos.y + enemyVel.y);
        enemyCollision.setPosition(enemyPos);
        enemySprite.setPosition(enemyPos.x - enemyRadius, enemyPos.y - enemyRadius);
    }

    /**
     * Render the enemy sprite using SpriteBatch
     */
    public void render(){
        batch.begin();
        enemySprite.draw(batch);
        batch.end();
    }

    public void drawCollider(){
        //Draw enemies colliders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(enemyPos.x, enemyPos.y, enemyRadius);
        shapeRenderer.end();
    }

    public Vector2 getEnemyPos() {
        return enemyPos;
    }

    public Circle getEnemyCollision() {
        return enemyCollision;
    }
}
