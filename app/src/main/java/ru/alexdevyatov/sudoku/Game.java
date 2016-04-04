package ru.alexdevyatov.sudoku;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class Game extends AppCompatActivity {
    private static final String TAG = "Sudoku";
    public static final String KEY_DIFFICULTY = "ru.alexdevyatov.sudoku.difficulty";
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;
    private int puzzle[];
    private PuzzleView puzzleView;
    private final int used[][][] = new int[9][9][];

    private final String easyPuzzle =
            "360000000004230800000004200" +
                    "070460003820000014500013020" +
                    "001900000007048300000000045" ;
    private final String mediumPuzzle =
            "650000070000506000014000005" +
            "007009000002314700000700800" +
            "500000630000201000030000097" ;
    private final String hardPuzzle =
            "009000000080605020501078000" +
                    "000000700706040102004000000" +
                    "000720903090301080000000600" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        int diff = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY_EASY);
        puzzle = getPuzzle(diff);
        puzzleView = new PuzzleView(this);
        setContentView(puzzleView);
        puzzleView.requestFocus();
    }

    public void showKeyPad(int x, int y) {
        int tiles[] = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        Log.d(TAG, "showKeypad: used=" + toPuzzleString(tiles));
        Dialog v = new Keypad(this, tiles, puzzleView);
        v.show();
    }

    public boolean setTileIfValid(int x, int y, int value) throws Exception {
        setTile(x, y, value);
        return !puzzleView.tryToFindCoincidences(x, y, value);
    }

    public void clearTile(int x, int y) {
        setTile(x, y, 0);
    }

    private int[] getPuzzle(int diff) {
        String puz;
        // Нужно сделать: продолжение предыдущей игры
        switch (diff) {
            case DIFFICULTY_HARD:
                puz = hardPuzzle;
                break;
            case DIFFICULTY_MEDIUM:
                puz = mediumPuzzle;
                break;
            case DIFFICULTY_EASY:
            default:
                puz = easyPuzzle;
                break;
        }
        return fromPuzzleString(puz);
    }

    private int[] fromPuzzleString(String string) {
        int[] puz = new int[string.length()];
        for (int i = 0; i < puz.length; i++) {
            puz[i] = string.charAt(i) - '0';
        }
        return puz;
    }

    private String toPuzzleString(int[] puz) {
        StringBuilder buf = new StringBuilder();
        for (int element : puz) {
            buf.append(element);
        }
        return buf.toString();
    }

    public int getTile(int x, int y) {
        return puzzle[y * 9 + x];
    }

    private void setTile(int x, int y, int value) {
        if (y * 9 + x < puzzle.length)
            puzzle[y * 9 + x] = value;
    }

    protected String getTileString(int x, int y) {
        int v = getTile(x, y);
        if (v == 0)
            return "" ;
        else
            return String.valueOf(v);
    }
}
