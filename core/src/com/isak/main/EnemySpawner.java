package com.isak.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class EnemySpawner {

    Array<Enemy> enemies;

    private float enemyStartSpeed;
    private float currentEnemySpeed;
    private float enemySpeedIncrease;
    private float enemyMaxSpeed;
    private int maxNumberEnemies;
    private int currentNumberEnemies;
    private int enemyRadius;
    private String imagePath;

    public EnemySpawner(float enemyStartSpeed, float enemySpeedIncrease, float enemyMaxSpeed,
                        int maxNumberEnemies, int enemyRadius, String imagePath){
        enemies = new Array<>();
        this.enemyStartSpeed = enemyStartSpeed;
        currentEnemySpeed = enemyStartSpeed;
        this.enemySpeedIncrease = enemySpeedIncrease;
        this.enemyMaxSpeed = enemyMaxSpeed;
        this.maxNumberEnemies = maxNumberEnemies;
        currentNumberEnemies = 0;
        this.enemyRadius = enemyRadius;
        this.imagePath = imagePath;
    }

    /**
     * Spawn a enemy with random position around and outside the screen, with a random velocity
     * angled towards the middle of the screen with a random change in angle applied
     */
    public void spawnEnemy() {
        //#Calcluate Position
        float enemyStartPosX, enemyStartPosY;
        //Determine if enemy start above, below, right or left
        if(MathUtils.random() > 0.5) { //Spawns above or below
            enemyStartPosX = MathUtils.random() * Gdx.graphics.getWidth();
            if(MathUtils.random() > 0.5) { //Spawns above
                enemyStartPosY = Gdx.graphics.getHeight() + 2*enemyRadius;
            }
            else { //Spawns below
                enemyStartPosY = -2*enemyRadius;
            }
        }
        else { //Spawns right or left
            enemyStartPosY = MathUtils.random() * Gdx.graphics.getHeight();
            if(MathUtils.random() > 0.5) { //Spawns right
                enemyStartPosX = Gdx.graphics.getWidth() + 2*enemyRadius;
            }
            else { //Spawns left
                enemyStartPosX = -2*enemyRadius;
            }
        }
        Vector2 startPos = new Vector2(enemyStartPosX, enemyStartPosY);

        //#Calculate Velocity
        //Angle the enemy towards the center
        float angle = MathUtils.atan2(Gdx.graphics.getHeight()/2f - enemyStartPosY,
                Gdx.graphics.getWidth()/2f - enemyStartPosX);
        angle += MathUtils.random(-1f, 1f) * MathUtils.PI/2; // add some randomness
//		angle += MathUtils.PI/8; // comment above, uncomment for a nice effect
        float enemyVelX = currentEnemySpeed * MathUtils.cos(angle) + MathUtils.random(-0.5f, 0.5f);
        float enemyVelY = currentEnemySpeed * MathUtils.sin(angle) + MathUtils.random(-0.5f, 0.5f);
        Vector2 startVel = new Vector2(enemyVelX, enemyVelY);

        enemies.add(new Enemy(startPos, startVel, enemyRadius, imagePath));
        currentNumberEnemies++;
//        System.out.println("spawned");
    }

    public void checkEnemyDespawn(){
        for (int enemy = currentNumberEnemies - 1; enemy >= 0; enemy--){
            //If outside screen => remove enemy
            if(enemies.get(enemy).getPos().x > Gdx.graphics.getWidth() + 2.01*enemyRadius ||
                    enemies.get(enemy).getPos().x < -2.01*enemyRadius ||
                    enemies.get(enemy).getPos().y > Gdx.graphics.getHeight() + 2.01*enemyRadius ||
                    enemies.get(enemy).getPos().y < -2.01*enemyRadius){
                despawnEnemy(enemy);
                if(currentEnemySpeed < enemyMaxSpeed) currentEnemySpeed += enemySpeedIncrease;
            }
        }
    }

    public void despawnEnemy(int i){
        enemies.removeIndex(i);
        currentNumberEnemies--;
    }

    public void moveEnemies(){
        for(Enemy enemy : enemies){
            enemy.movement();
//            System.out.println("moved");
        }
    }

    public void drawEnemies(){
        for(Enemy enemy : enemies){
            enemy.render();
        }
    }

    public float getEnemyStartSpeed() {
        return enemyStartSpeed;
    }

    public float getCurrentEnemySpeed() {
        return currentEnemySpeed;
    }

    public void setCurrentEnemySpeed(float currentEnemySpeed) {
        this.currentEnemySpeed = currentEnemySpeed;
    }

    public float getEnemyMaxSpeed() {
        return enemyMaxSpeed;
    }

    public float getEnemySpeedIncrease() {
        return enemySpeedIncrease;
    }

    public int getMaxNumberEnemies() {
        return maxNumberEnemies;
    }

    public int getCurrentNumberEnemies() {
        return currentNumberEnemies;
    }

    public Enemy getEnemy(int i){
        return enemies.get(i);
    }
}
