package com.example.embers;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends AppCompatActivity {

    private TextView scoreLabel;
    private int playerScore;
    private int playerHealth;
    private FrameLayout playFrameLayout;
    final ArrayList<Orb> orbsList = new ArrayList<>();

    private Handler handler = new Handler();
    private Timer positionTimer = new Timer();
    private Timer orbSpawnTimer = new Timer();

    private class Orb {
        private int xPosition;
        private int yPosition;
        private boolean scored;
        private int points;
        private boolean damaged;
        private int damage;
        private int size;
        private int speed;
        ImageView ball;

        @SuppressLint("ClickableViewAccessibility")
        Orb(Context context, int x, int y, int orbSize, int orbSpeed) {
            ball = new ImageView(context);
            ball.setImageResource(R.drawable.ball_shape);
            playFrameLayout.addView(ball);
            scored = false;
            damaged = false;
            size = orbSize;
            speed = orbSpeed;
            ball.getLayoutParams().height = size;
            ball.getLayoutParams().width = size;
            xPosition = x;
            yPosition = y;
            ball.setX(x);
            ball.setY(y);

            //TODO Calculate points and damage based on size and speed
            points = 50;
            damage = 100;

            ball.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    speed = 0;
                    scored = true;
                    playFrameLayout.removeView(ball);
                    return true;
                }
            });
        }

        void updatePosition(int x, int y) {
            xPosition = x;
            yPosition = y;
            ball.setX(x);
            ball.setY(y);
        }
    }

    // TODO: Override the back navigation button to end the game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        playerScore = 0;
        playerHealth = 1000;
        scoreLabel = findViewById(R.id.current_score);
        playFrameLayout = findViewById(R.id.play_frame);
        // TODO: Add health bar at the bottom
        // TODO: Add restart button and move it off screen

        positionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updatePositions();
                    }
                });
            }
        }, 0, 20);

        orbSpawnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        spawnOrbs();
                    }
                });
            }
        }, 0, 1000); //TODO: experiment with this value
    }

    public void updatePositions() {
        boolean removeOrb = false;
        int index = 0;
        for (Orb orb : orbsList) {
            if (orb.speed != 0) {
                orb.updatePosition(orb.xPosition + orb.speed, orb.yPosition);
                if (!removeOrb) {
                    index++;
                }
            } else {
                removeOrb = true;
            }

            if (orb.xPosition > getDisplayWidth() + orb.size) {
                orb.speed = 0;
                Log.e("TATE_TAG", "Setting orb status as damaged");
                orb.damaged = true;
            }
        }
        if (removeOrb) {
            Orb orb = orbsList.get(index);
            if (orb.damaged) {
                playerHealth = playerHealth - orb.damage;
                Log.e("TATE_TAG", "Health = " + playerHealth);
            } else if (orb.scored) {
                playerScore = playerScore + orb.points;
                scoreLabel.setText("Score : " + playerScore);
                Log.e("TATE_TAG", "Score = " + playerScore);
            }
            orbsList.remove(index);
        }
        if (playerHealth <= 0) {
            //TODO: END THE GAME and MOVE RESTART BUTTON
            Log.e("TATE_TAG", "GAME OVER!!!!!");
            orbSpawnTimer.cancel();
            for (Orb orb : orbsList) {
                playFrameLayout.removeView(orb.ball);
            }
            orbsList.clear();
            positionTimer.cancel();
        }
    }

    public void spawnOrbs() {
        int orbSize = 120; // TODO: Randomize this size
        int orbSpeed = 6; // TODO: Randomize the speed
        int y = (int) Math.floor(Math.random() * (getDisplayHeight() - orbSize));
        orbsList.add(new Orb(this, -50, y, orbSize, orbSpeed));
    }

    private int getDisplayWidth() {
        return this.getResources().getDisplayMetrics().widthPixels;
    }

    private int getDisplayHeight() {
        return this.getResources().getDisplayMetrics().heightPixels;
    }
}
