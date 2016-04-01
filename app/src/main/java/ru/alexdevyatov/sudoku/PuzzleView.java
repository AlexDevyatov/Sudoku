package ru.alexdevyatov.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

public class PuzzleView extends View{
    private static final String TAG = "Sudoku";
    private final Game game;

    private float width;
    private float height;
    private int selX;
    private int selY;
    private final Rect selRect = new Rect();

    public PuzzleView(Context context) {
        super(context);
        this.game = (Game)context;
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w / 9f;
        height = h / 9f;
        getRect(selX, selY, selRect);
        Log.d(TAG, "onSizeChanged: width " + width + ", height " + height);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void getRect(int x, int y, Rect rect) {
        rect.set((int) (x * width), (int) (y * height), (int) (x * width + width), (int) (y * height + height));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint background = new Paint();
        background.setColor(getResources().getColor(R.color.puzzle_background));
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);

        Paint dark = new Paint();
        dark.setColor(getResources().getColor(R.color.puzzle_dark));

        Paint hilite = new Paint();
        hilite.setColor(getResources().getColor(R.color.puzzle_hilite));

        Paint light = new Paint();
        light.setColor(getResources().getColor(R.color.puzzle_light));

        for (int i = 0; i < 9; i++) {
            canvas.drawLine(0, i * height, getWidth(), i * height, light);
            canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, hilite);
            canvas.drawLine(i * width, 0, i * width, getHeight(), light);
            canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight(), hilite);
        }

        for (int i = 0; i < 9; i++) {
            if (i % 3 != 0)
                continue;
            canvas.drawLine(0, i * height, getWidth(), i * height, dark);
            canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, hilite);
            canvas.drawLine(i * width, 0, i * width, getHeight(), dark); canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight(), hilite);
        }

        Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
        foreground.setColor(getResources().getColor(R.color.puzzle_foreground));
        foreground.setStyle(Style.FILL);
        foreground.setTextSize(height * 0.75f);
        foreground.setTextScaleX(width / height);
        foreground.setTextAlign(Paint.Align.CENTER);

        FontMetrics fm = foreground.getFontMetrics();

        float x = width / 2;
        float y = height / 2 - (fm.ascent + fm.descent) / 2;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                canvas.drawText(this.game.getTileString(i, j), i * width + x, j * height + y, foreground);
            }
        }

        Log.d(TAG, "selRect=" + selRect);
        Paint selected = new Paint();
        selected.setColor(getResources().getColor(R.color.puzzle_selected));
        canvas.drawRect(selRect, selected);

        Paint mistake = new Paint();
        int mistake_color = getResources().getColor(R.color.mistake_background);
        Rect r = new Rect();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int value = game.getTile(i, j);
                int candY = checkRow(i, j, value);
                int candX = checkColumn(i, j, value);
                Log.d(TAG, "candX = " + candX);
                Log.d(TAG, "candY = " + candY);
                if (candX != -1 || candY != -1) {
                    getRect(i, j, r);
                    mistake.setColor(mistake_color);
                    canvas.drawRect(r, mistake);
                    canvas.drawText(this.game.getTileString(i, j), i * width + x, j * height + y, foreground);
                    if (candX != -1) {
                        Rect rx = new Rect();
                        getRect(candX, j, rx);
                        canvas.drawRect(rx, mistake);
                        canvas.drawText(this.game.getTileString(candX, j), candX * width + x, j * height + y, foreground);
                    }
                    if (candY != -1) {
                        Rect ry = new Rect();
                        getRect(i, candY, ry);
                        canvas.drawRect(ry, mistake);
                        canvas.drawText(this.game.getTileString(i, candY), i * width + x, candY * height + y, foreground);
                    }
                }
            }
        }


        super.onDraw(canvas);
    }

    private int checkRow (int x, int y, int value) { // Возвращает координату y ячейки, значение которой совпадает с value
        for (int j = 0; j < 9; j++) {
            if (j == y)
                continue;
            int cellValue = game.getTile(x, j);
            if (cellValue == 0)
                continue;
            if (cellValue == value)
                return j;
        }
        return -1;
    }

    private int checkColumn (int x, int y, int value) { // Возвращает координату x ячейки, значение которой совпадает с value
        for (int i = 0; i < 9; i++) {
            if (i == x)
                continue;
            int cellValue = game.getTile(i, y);
            if (cellValue == 0)
                continue;
            if (cellValue == value)
                return i;
        }
        return -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return super.onTouchEvent(event);
        select((int) (event.getX() / width),
                (int) (event.getY() / height));
        game.showKeyPadOrError(selX, selY);
        Log.d(TAG, "onTouchEvent: x " + selX + ", y " + selY);
        return true;
    }

    private void select(int x, int y) {
        invalidate(selRect);
        selX = Math.min(Math.max(x, 0), 8);
        selY = Math.min(Math.max(y, 0), 8);
        getRect(selX, selY, selRect);
        invalidate(selRect);
    }

    public void setSelectedTile(int tile) {
        invalidate();
        if (!game.setTileIfValid(selX, selY, tile))
            startAnimation(AnimationUtils.loadAnimation(game, R.anim.shake));
    }
}
