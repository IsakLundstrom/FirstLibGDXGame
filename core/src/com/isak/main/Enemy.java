package com.isak.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Enemy {

    private Vector2 pos;
    private Vector2 vel;
    private Texture texture;
    private Sprite sprite;
    private Circle collision;

    private int radius;
    private String imagePath;

    private SpriteBatch batch;

    /**
     * Constructor for an enemy with default radius (64px) and image path ("clown-pixel.png")
     * @param pos Starting position in pixels
     * @param vel Starting velocity
     */
    public Enemy(Vector2 pos, Vector2 vel){
        batch = new SpriteBatch();
        radius = 64;
        imagePath = "clown-pixel.png";
        setup(pos, vel, radius, imagePath);
    }

    /**
     * Constructor for an enemy
     * @param pos Starting position in pixels
     * @param vel Starting velocity
     * @param radius Radius in pixels
     * @param imagePath Path to the image
     */
    public Enemy(Vector2 pos, Vector2 vel, int radius, String imagePath){
        batch = new SpriteBatch();
        setup(pos, vel, radius, imagePath);
    }

    private void setup(Vector2 pos, Vector2 vel, int radius, String imagePath) {
        this.pos = pos;
        this.vel = vel;
        this.radius = radius;
        this.imagePath = imagePath;
        texture = new Texture(Gdx.files.internal(imagePath));
        sprite = new Sprite(texture);
        sprite.setSize(2*radius, 2*radius);
        sprite.setOrigin(radius, radius);
        float angle = MathUtils.atan2(vel.y, vel.x);
        sprite.setRotation(angle/ MathUtils.PI2*360 - 90);
        collision = new Circle(pos.x, pos.y, radius);
    }

    /**
     * Move the enemy position, collision circle and sprite
     */
    public void movement(){
        pos.set(pos.x + vel.x, pos.y + vel.y);
        collision.setPosition(pos);
        sprite.setPosition(pos.x - radius, pos.y - radius);
    }

    /**
     * Draw the enemy sprite using SpriteBatch
     */
    public void render(){
        batch.begin();
        sprite.draw(batch);
        batch.end();
    }

    public Vector2 getPos() {
        return pos;
    }

    public Circle getCollision() {
        return collision;
    }
}
