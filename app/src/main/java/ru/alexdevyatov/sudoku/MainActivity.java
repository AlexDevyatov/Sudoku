package ru.alexdevyatov.sudoku;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button continue_button;
    private Button new_game_button;
    private Button about_button;
    private Button exit_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /*ActionBar ab = getActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));*/

        continue_button = (Button)findViewById(R.id.continue_button);
        new_game_button = (Button)findViewById(R.id.new_game_button);
        about_button = (Button)findViewById(R.id.about_button);
        exit_button = (Button)findViewById(R.id.exit_button);

        continue_button.setOnClickListener(this);
        new_game_button.setOnClickListener(this);
        about_button.setOnClickListener(this);
        exit_button.setOnClickListener(this);
    }

    private void openNewGameDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_game_title)
                .setItems(R.array.difficulty, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        startGame(i);
                    }
                })
                .show();
    }

    private void startGame(int i) {
        Intent intent = new Intent(this, Game.class);
        intent.putExtra(Game.KEY_DIFFICULTY, i);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_button:
                Intent i = new Intent(this, about.class);
                startActivity(i);
                break;
            case R.id.continue_button:

                break;
            case R.id.new_game_button:
                openNewGameDialog();
                break;
            case R.id.exit_button:
                finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, Prefs.class));
                return true;
        }
        return false;
    }
}
