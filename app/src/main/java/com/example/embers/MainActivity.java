package com.example.embers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                startGame();
            }
        });

        final Button leaderboardButton = findViewById(R.id.leaderboard_button);
        leaderboardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                openLeaderboard();
            }
        });
    }

    private void startGame() {
        startActivity(new Intent(this, PlayActivity.class));
    }

    private void openLeaderboard() {
        startActivity(new Intent(this, LeaderboardActivity.class));
    }
}
