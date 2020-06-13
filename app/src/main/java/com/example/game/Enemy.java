package com.example.game;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Size;

import java.util.Random;

//적 개체
class Enemy implements MainActivity.Collisionable {
    public float X, Y;
    public Size bounds;
    public Bitmap bitmap;
    private int direction = 1;
    public int speed;


    Enemy(Size bounds, Bitmap bitmap) {
        X = new Random().nextFloat();
        speed = new Random().nextInt(5) + 1; // 적군의 속도는 기본 1에서 5 사이로 설정
        this.bounds = bounds;

        this.bitmap = bitmap;
        setX(new Random().nextInt(bounds.getWidth()));
        Y = Y + bitmap.getHeight();// enemy라 정의된 대상이 실체화가 됨
    }

    public void setX(float x) {
        int R = bitmap.getWidth();
        float maxR = Math.min(bounds.getWidth() - R, x);
        float maxL = Math.max(maxR, 0);
        X = maxL;
    }

    //적기 랜덤으로 좌우로 움직일 수 있게 설정
    public void moveRandom() {
        int r = new Random().nextInt(5) + 20 * direction;
        float next = X + r;
        setX(X + r);
        // 다음 이동해야 하는 위치와 실제로 이동된 위치가 다른 경우 방향을 반대로 설정해준다.
        if (next != X) {
            direction *= -1;
        }
    }

    //각 라인 지정 (lEFT-TOP-->RIGHT-BOTTOM)
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

}