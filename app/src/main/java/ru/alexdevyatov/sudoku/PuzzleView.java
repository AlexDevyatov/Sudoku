package ru.alexdevyatov.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.support.v4.content.ContextCompat;
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
        background.setColor(ContextCompat.getColor(getContext(), R.color.puzzle_background));
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);

        Paint dark = new Paint();
        dark.setColor(ContextCompat.getColor(getContext(), R.color.puzzle_dark));

        Paint hilite = new Paint();
        hilite.setColor(ContextCompat.getColor(getContext(), R.color.puzzle_hilite));

        Paint light = new Paint();
        light.setColor(ContextCompat.getColor(getContext(), R.color.puzzle_light));

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
        foreground.setColor(ContextCompat.getColor(getContext(), R.color.puzzle_foreground));
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
        selected.setColor(ContextCompat.getColor(getContext(), R.color.puzzle_selected));
        canvas.drawRect(selRect, selected);

        Paint mistake = new Paint();
        int mistake_color = ContextCompat.getColor(getContext(), R.color.mistake_background);
        Rect r = new Rect();
        int value = game.getTile(selX, selY);
        try {
            if (tryToFindCoincidences(selX, selY, value)) {
                getRect(selX, selY, r);
                mistake.setColor(mistake_color);
                canvas.drawRect(r, mistake);
                game.clearTile(selX, selY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDraw(canvas);
    }

    private int getNumberOfSector (int x, int y) {
        if (x >= 0 && y >= 0 && x <= 2 && y <= 2)
            return 0;
        if (x >= 3 && y >= 0 && x <= 5 && y <= 2)
            return 1;
        if (x >= 6 && y >= 0 && x <= 8 && y <= 2)
            return 2;
        if (x >= 0 && y >= 3 && x <= 2 && y <= 5)
            return 3;
        if (x >= 3 && y >= 3 && x <= 5 && y <= 5)
            return 4;
        if (x >= 6 && y >= 3 && x <= 8 && y <= 5)
            return 5;
        if (x >= 0 && y >= 6 && x <= 2 && y <= 8)
            return 6;
        if (x >= 3 && y >= 6 && x <= 5 && y <= 8)
            return 7;
        if (x >= 6 && y >= 6 && x <= 8 && y <= 8)
            return 8;
        return -1;
    }

    private Point getStartPoint (int x, int y) throws Exception {
        int sector = getNumberOfSector(x, y);
        switch (sector) {
            case 0:
                return new Point(0, 0);
            case 1:
                return new Point(3, 0);
            case 2:
                return new Point(6, 0);
            case 3:
                return new Point(0, 3);
            case 4:
                return new Point(3, 3);
            case 5:
                return new Point(6, 3);
            case 6:
                return new Point(0, 6);
            case 7:
                return new Point(3, 6);
            case 8:
                return new Point(6, 6);
        }
        Log.d("getStartPoint", "x = " + x + "y = " + y);
        throw new Exception("Fail to get a start point");
    }

    private boolean checkSector (int x, int y, int value) throws Exception {
        Point startPoint = getStartPoint(x, y);
        for (int i = startPoint.x; i < startPoint.x + 3; i++)
            for (int j = startPoint.y; j < startPoint.y + 3; j++) {
                if (i == x && j == y)
                    continue;
                int cellValue = game.getTile(i, j);
                if (cellValue == 0)
                    continue;
                if (cellValue == value)
                    return true;
            }
        return false;
    }

    private boolean checkRow (int x, int y, int value) {
        for (int j = 0; j < 9; j++) {
            if (j == y)
                continue;
            int cellValue = game.getTile(x, j);
            if (cellValue == 0)
                continue;
            if (cellValue == value)
                return true;
        }
        return false;
    }

    private boolean checkColumn (int x, int y, int value) {
        for (int i = 0; i < 9; i++) {
            if (i == x)
                continue;
            int cellValue = game.getTile(i, y);
            if (cellValue == 0)
                continue;
            if (cellValue == value)
                return true;
        }
        return false;
    }

    public boolean tryToFindCoincidences (int x, int y, int value) throws Exception {
        return checkColumn(x, y, value) || checkRow(x, y, value) || checkSector(x, y, value);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return super.onTouchEvent(event);
        select((int) (event.getX() / width),
                (int) (event.getY() / height));
        game.showKeyPad(selX, selY);
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

    public void setSelectedTile(int tile) throws Exception {
        invalidate();
        if (!game.setTileIfValid(selX, selY, tile))
            startAnimation(AnimationUtils.loadAnimation(game, R.anim.shake));
    }
}
