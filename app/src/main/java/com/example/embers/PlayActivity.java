package com.example.embers;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends AppCompatActivity {

    private TextView scoreLabel;
    private int playerScore;
    private int playerHealth;
    private int startingHealth;
    private int spawnCounter;
    private int spawnPeriod;
    private int navBarHeight;
    private View gameOver;
    private View healthBar;
    private Button restartButton;
    private Button menuButton;
    private Button leaderboardButton;
    private FrameLayout playFrameLayout;
    final ArrayList<Orb> orbsList = new ArrayList<>();

    private Handler handler = new Handler();
    private Timer positionTimer = new Timer();
    private Timer orbSpawnTimer = new Timer();
    private TimerTask spawnTask;

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
            points = 1000*speed/size;
            damage = 10*size/speed;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        playerScore = 0;
        playerHealth = 1000;
        startingHealth = 1000;
        spawnCounter = 0;
        spawnPeriod = 1500;
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        navBarHeight = resources.getDimensionPixelSize(resourceId);
        scoreLabel = findViewById(R.id.current_score);
        playFrameLayout = findViewById(R.id.play_frame);
        gameOver = findViewById(R.id.game_over);
        gameOver.setVisibility(View.INVISIBLE);
        restartButton = findViewById(R.id.restart);
        restartButton.setEnabled(false);
        restartButton.setVisibility(View.INVISIBLE);
        menuButton = findViewById(R.id.main_menu);
        menuButton.setEnabled(false);
        menuButton.setVisibility(View.INVISIBLE);
        leaderboardButton = findViewById(R.id.leaderboard_button);
        leaderboardButton.setEnabled(false);
        leaderboardButton.setVisibility(View.INVISIBLE);
        healthBar = findViewById(R.id.health_bar);

        restartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                restartGame();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                openMainMenu();
            }
        });

        leaderboardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                openLeaderboard();
            }
        });

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

        spawnTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        spawnOrbs();
                    }
                });
            }
        };

        orbSpawnTimer.schedule(spawnTask, 0, spawnPeriod);
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
                float scalingFactor = (float) playerHealth/startingHealth;
                healthBar.setScaleX(scalingFactor);
                Log.e("TATE_TAG", "Health = " + playerHealth);
            } else if (orb.scored) {
                playerScore = playerScore + orb.points;
                scoreLabel.setText("Score : " + playerScore);
                Log.e("TATE_TAG", "Score = " + playerScore);
            }
            orbsList.remove(index);
        }
        if (playerHealth <= 0) {
            Log.e("TATE_TAG", "GAME OVER!!!!!");
            healthBar.setScaleX(0);
            orbSpawnTimer.cancel();
            for (Orb orb : orbsList) {
                playFrameLayout.removeView(orb.ball);
            }

            orbsList.clear();
            positionTimer.cancel();
            gameOver.setVisibility(View.VISIBLE);
            restartButton.setEnabled(true);
            restartButton.setVisibility(View.VISIBLE);
            menuButton.setEnabled(true);
            menuButton.setVisibility(View.VISIBLE);
            leaderboardButton.setEnabled(true);
            leaderboardButton.setVisibility(View.VISIBLE);
        }
    }

    public void spawnOrbs() {
        Random random = new Random();
        int orbSize = random.nextInt(120) + 80;
        int orbSpeed = random.nextInt(18) + 6;
        int max = getDisplayHeight() - orbSize - navBarHeight;
        int y = random.nextInt(max - scoreLabel.getHeight()) + scoreLabel.getHeight();
        //TODO: add ability for orbs to move diagonally
        orbsList.add(new Orb(this, -50, y, orbSize, orbSpeed));

        spawnCounter++;
        if (spawnCounter % 10 == 0) {
            spawnPeriod -= 100;
            if (spawnPeriod <= 250) {
                spawnPeriod = 250;
            }
            spawnTask.cancel();
            spawnTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            spawnOrbs();
                        }
                    });
                }
            };
            orbSpawnTimer.schedule(spawnTask, 0, spawnPeriod);
        }
    }

    private int getDisplayWidth() {
        return this.getResources().getDisplayMetrics().widthPixels;
    }

    private int getDisplayHeight() {
        return this.getResources().getDisplayMetrics().heightPixels;
    }

    public void restartGame() {
        startActivity(new Intent(this, PlayActivity.class));
    }

    public void openLeaderboard() {
        startActivity(new Intent(this, LeaderboardActivity.class));
    }

    public void openMainMenu() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
