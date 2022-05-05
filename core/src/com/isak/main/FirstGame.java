package com.isak.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class FirstGame extends ApplicationAdapter {

	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;
	private Vector3 touchPos;

	private Vector2 playerPosition;
	private Vector2 velocity;
	private Vector2 acceleration;

	private int playerRadius = 64;

	private int accelerationWeight = 1;
	private int accelerationFriction = 1;

	
	@Override
	public void create () {
		shapeRenderer = new ShapeRenderer();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		touchPos = new Vector3(0, 0, 0);

		playerPosition = new Vector2(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		velocity = new Vector2(0,0);
		acceleration = new Vector2(0,0);

	}

	@Override
	public void render () {
		//Game Logic
		camera.update();

		if(Gdx.input.isTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);

			acceleration.set((Gdx.input.getX() - playerPosition.x) / 10000, (Gdx.graphics.getHeight() - Gdx.input.getY() - playerPosition.y)/10000);

//			velocity.set(velocity.x + acceleration.x, velocity.y + acceleration.y, 0);

			camera.unproject(touchPos);
		}
		else {
//			velocity.set(velocity.x - accelerationFriction, velocity.y - accelerationFriction, 0);
			acceleration.set(-velocity.x/100, -velocity.y/100);
		}
		velocity.set(velocity.x + acceleration.x, velocity.y + acceleration.y);
		playerPosition.set(playerPosition.x + velocity.x, playerPosition.y + velocity.y);

		//Check walls
//		if(playerPosition)

		//Drawing
		Gdx.gl.glClearColor(1,1,1,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//Draw moving circle
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.circle(playerPosition.x, playerPosition.y, playerRadius);


		if(Gdx.input.isTouched()) {
//			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			//Draw mouse pos
			shapeRenderer.setColor(Color.RED);
			shapeRenderer.circle(touchPos.x, touchPos.y, 32);

			//Draw line between circle and mouse
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.line(touchPos.x, touchPos.y, playerPosition.x, playerPosition.y);

			//Werid
//			shapeRenderer.setColor(Color.ORANGE);
//			shapeRenderer.line(Gdx.input.getX(), Gdx.input.getY(), position.x, position.y);
//			System.out.println("2 Gdx " + Gdx.input.getX() + " " + Gdx.input.getY());
//			System.out.println("mouse " + mousePos.x + " " + mousePos.y);

		}


		shapeRenderer.end();
	}
	
	@Override
	public void dispose () {

	}
}
