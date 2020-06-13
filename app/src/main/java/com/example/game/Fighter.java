package com.example.game;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Size;

import java.util.Random;

// fighter 객체
class Fighter implements MainActivity.Collisionable {

    public float X, Y;
    public Size bounds;
    public Bitmap bitmap;


    Fighter(Size bounds, Bitmap bitmap) {
        X = new Random().nextFloat();
        this.bounds = bounds;
        this.bitmap = bitmap;
        setX(new Random().nextInt(bounds.getWidth()));
        Y = bounds.getHeight() - 2 * bitmap.getHeight();//캐릭터를 아래에 배치시켜줌
    }

    public void setX(float x) {
        int R = bitmap.getWidth();
        float maxR = Math.min(bounds.getWidth() - R, x);//캐릭터의 마우스 커서를 중심점으로 위치 조정
        float maxL = Math.max(maxR, 0);
        X = maxL;
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