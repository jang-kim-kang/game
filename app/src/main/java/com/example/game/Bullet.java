package com.example.game;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Size;

// 총알을 그려주는 객체
class Bullet implements MainActivity.Collisionable {

    public float X, Y;
    public Bitmap bitmap;
    public int speed = 5; // 스피드의 기본값은 5
    private Size screenbounds;


    Bullet(float x, float y, Bitmap bitmap, Size screenbounds) {
        X = x;
        Y = y;
        this.bitmap = bitmap;
        this.screenbounds = screenbounds;
    }

    @Override
    public Point[] getLine(Line line) {
        if (line == Line.Left) {
            return new Point[]{new Point((int) X, (int) Y), new Point((int) X, (int) Y + bitmap.getHeight())};
        } else if (line == Line.Top) {
            return new Point[]{new Point((int) X, (int) Y), new Point((int) X + bitmap.getWidth(), (int) Y)};
        } else if (line == Line.Right) {
            return new Point[]{new Point((int) X + bitmap.getWidth(), (int) Y), new Point((int) X + bitmap.getWidth(), (int) Y + bitmap.getHeight())};
        } else if (line == Line.Bottom) {
            return new Point[]{new Point((int) X, (int) Y + bitmap.getHeight()), new Point((int) X + bitmap.getWidth(), (int) Y + bitmap.getHeight())};
        }
        return null;
    }

    public boolean moveUp() {
        Y -= speed;
        return Y + bitmap.getHeight() < 0;
    }

    public boolean moveDown() {
        Y += speed;
        return Y + screenbounds.getHeight() > Y;
    }


}