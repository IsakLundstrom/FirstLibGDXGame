package com.isak.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class TouchElement {
    private Vector3 touchPos;
    private Sprite touchSprite;

    private int touchImageSize = 100;
    private String touchImagePath = "cucumber-pixel.png";

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    /**
     * Constructor for the touch position object
     * @param touchImageSize Width and height in pixels
     * @param touchImagePath Path to the touch image
     */
    public TouchElement(int touchImageSize, String touchImagePath) {
        this.touchImageSize = touchImageSize;
        this.touchImagePath = touchImagePath;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        touchSetup();
    }

    private void touchSetup() {
        touchPos = new Vector3();
        Texture touchTexture = new Texture(Gdx.files.internal(touchImagePath));
        touchSprite = new Sprite(touchTexture);
        touchSprite.setSize(touchImageSize, touchImageSize);
    }

    /**
     * If Gdx.input.isTouched() then move touchPos to that position
     */
    public void moveTouchPosition(){
        if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            touchSprite.setPosition(touchPos.x - touchImageSize / 2f,
                    touchPos.y - touchImageSize / 2f);
        }
    }


    /**
     * Set touchImage position to touchPos
     */
    public void setTouchImagePosition(){
        touchSprite.setPosition(touchPos.x - touchImageSize / 2f,
                touchPos.y - touchImageSize / 2f);
    }

    /**
     * If Gdx.input.isTouched() then render touchSprite using SpriteBatch
     */
    public void render() {
        if(Gdx.input.isTouched()) {
            batch.begin();
            touchSprite.draw(batch);
            batch.end();
        }
    }

    /**
     * If Gdx.input.isTouched() then draw a line between touchPos and given pos (player)
     * @param playerPos Position to draw line to from touchPos
     */
    public void drawLineBetweenTouchAndPlayer(Vector2 playerPos) {
        if (Gdx.input.isTouched()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Color green = new Color(119f / 255f, 178f / 255f, 85f / 255f, 1);
            shapeRenderer.setColor(green);
            shapeRenderer.line(touchPos.x, touchPos.y, playerPos.x, playerPos.y);
            shapeRenderer.end();
        }
    }

    public Vector3 getTouchPos() {
        return touchPos;
    }
}
