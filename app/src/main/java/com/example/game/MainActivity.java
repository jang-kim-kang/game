package com.example.game;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
//열거 지정

enum Line {
    Left, Top, Right, Bottom
}

public class MainActivity extends AppCompatActivity {

    final int REQUEST_ENABLE_BT = 1;
    final int REQUEST_ENABLE_LOCATION = 1;

    private SceneView view;
    private List<Fighter> fighters = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private List<Enemy> enemys = new ArrayList<>();
    private List<Bullet> enemybullets = new ArrayList<>();
    private List<Background> backgrounds = new ArrayList<>();

    private MoveHandler handler = new MoveHandler();
    private Size screenbounds;

    private GameBluetoothManager bluetoothManager;
    private int handlerCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenbounds = new Size(metrics.widthPixels, metrics.heightPixels);

        view = new SceneView(getBaseContext());
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        setContentView(view);

        Resources res = getResources();
        BitmapDrawable bd = (BitmapDrawable) res.getDrawable(R.drawable.background);
        Background b1 = new Background();
        Background b2 = new Background();
        b1.bitmap = bd.getBitmap();
        b2.bitmap = bd.getBitmap();
        b1.y = 0;
        b2.y = -metrics.heightPixels;
        backgrounds.add(b1);
        backgrounds.add(b2);


        bluetoothManager = new GameBluetoothManager(getBaseContext());
        if (GameBluetoothManager.isEnabledBluetooth()) {
            bluetoothManager.activeRecieve();
            checkPermission();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("블루투스를 사용할수 없는 기기").setMessage("혼자 진행?");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d("artcow", "확인 누름");
                    startSingleGame();
                }
            });
            builder.setNegativeButton("취소", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothManager != null) {
            bluetoothManager.activeRecieve();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothManager != null) {
            bluetoothManager.pauseReceive();
        }
    }

    private void showConnectedDevices() {

        final Set<BluetoothDevice> pairedDevices = bluetoothManager.pairedDevices();
        if (pairedDevices.size() == 0) {
            new AlertDialog.Builder(this).setTitle("연결된 장치가 없음").setPositiveButton("확인", null).create().show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("연결할 장치를 선택하세요");

            // 페어링 된 블루투스 장치의 이름 목록 작성
            final List<String> listItems = new ArrayList<>();
            for (BluetoothDevice bt_device : pairedDevices) {
                listItems.add(bt_device.getName());
            }
            listItems.add("Cancel");    // 취소 항목 추가

            final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Dialog dialog_ = (Dialog) dialog;

                    if (which == listItems.size()) {
                        // 연결할 장치를 선택하지 않고 '취소'를 누른 경우
                        Log.d("artcow", "아무것도 하지 않음.");
                    } else {
                        // 기기 이름을 선택한 경우 선택한 기기 이름과 같은 블루투스 객체를 찾아서 연결을 시도한다
                        for (BluetoothDevice bt_device : pairedDevices) {
                            if (bt_device.getName().equals(items[which].toString())) {
                                bluetoothManager.connect(bt_device);  //해당하는 블루투스 객체를 이용하여 연결 시도
                                break;
                            }
                        }
                    }

                }
            });
            builder.setCancelable(false);    // 뒤로 가기 버튼 사용 금지
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void checkPermission() {
        if (isCoarseLocationGranted()) {
            if (!GameBluetoothManager.isActiveBluetooth()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // 여기서부터 블루투스 페어링을 시작
                AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("모드선택").setMessage("검색을 하거나 검색을 허용").
                        setPositiveButton("검색하기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                bluetoothManager.discovery();


                                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
//                                b.

                            }
                        }).
                        setNeutralButton("검색허용", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent discoveryIntent = bluetoothManager.enabledDiscovery();
                                if (discoveryIntent != null) {
                                    startActivity(discoveryIntent);
                                } else {
                                    Toast.makeText(getBaseContext(), "이미 검색 활성화", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                if (bluetoothManager.pairedDevices().size() != 0) {
                    builder.setNegativeButton("기존 장치 연결", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showConnectedDevices();
                        }
                    });
                }
                builder.create().show();
            }
        } else {
            // 위치정보 허용 다이얼로그를 띄운다.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ENABLE_LOCATION);
        }
    }

    private boolean isCoarseLocationGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            if (isCoarseLocationGranted()) {

            } else {
                new AlertDialog.Builder(this).setTitle("위치정보 요청").setMessage("블루투스를 사용하기 위해선 위치정보 사용에 동의해야함").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkPermission();
                    }
                }).create().show();

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (!GameBluetoothManager.isActiveBluetooth()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("혼자할래?");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("artcow", "확인 누름");
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                checkPermission();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startSingleGame() {
        fighters.clear();
        enemys.clear();
        bullets.clear();
        enemybullets.clear();

        //비트맵 이미지 사용
        Resources res = getResources();
        BitmapDrawable bd = (BitmapDrawable) res.getDrawable(R.drawable.fighter);
        fighters.add(new Fighter(screenbounds, bd.getBitmap()));


        BitmapDrawable ebd = (BitmapDrawable) res.getDrawable(R.drawable.enemy);
        enemys.add(new Enemy(screenbounds, ebd.getBitmap()));
        enemys.add(new Enemy(screenbounds, ebd.getBitmap()));
        enemys.add(new Enemy(screenbounds, ebd.getBitmap()));
        enemys.add(new Enemy(screenbounds, ebd.getBitmap()));

        startgame();
    }

    private void startMultiGame() {

    }

    private void startgame() {
        // 기체를 화면에 그려주기 위해 핸들러를 즉시 실행
        handler.sendEmptyMessageDelayed(0, 0);
    }

    private void stopgame() {

        handler.removeCallbacksAndMessages(null);
        handler.removeMessages(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("게임끝").setMessage("다시할래?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startSingleGame();
            }
        });
        builder.setNegativeButton("취소", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    protected class SceneView extends View {

        private Fighter holder;

        protected SceneView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            for (Background background : backgrounds) {
                canvas.drawBitmap(background.bitmap, null, new Rect(0, (int) background.y, screenbounds.getWidth(), screenbounds.getHeight() + (int) background.y), null);
            }

            //canvas.drawColor(Color.WHITE);

            // 1. 총알 그려줌
            for (Bullet b : bullets) {
                Bitmap bit = b.bitmap;
                canvas.drawBitmap(bit, b.X, b.Y, null);
            }
            //2.적의 총알 그려줌
            for (Bullet eb : enemybullets) {
                Bitmap bit = eb.bitmap;
                canvas.drawBitmap(bit, eb.X, eb.Y, null);
            }

            // 3. 비행기를 화면에 그려줌
            for (Fighter fighter : fighters) {
                Bitmap bit = fighter.bitmap;
                canvas.drawBitmap(bit, fighter.X, fighter.Y, null);
            }

            //4. 적기 그려줌
            for (Enemy e : enemys) {
                Bitmap bit = e.bitmap;
                canvas.drawBitmap(bit, e.X, e.Y, null);
            }
            //나의 총알과 적의 총돌판정
            for (Bullet b : bullets) {
                for (Enemy e : enemys) {
                    for (Line l1 : Line.values()) {
                        Point[] enemyLine = e.getLine(l1);
                        for (Line l2 : Line.values()) {
                            Point[] line = b.getLine(l2);

                            float x1 = line[0].x, x2 = line[1].x, x3 = enemyLine[0].x, x4 = enemyLine[1].x;
                            float y1 = line[0].y, y2 = line[1].y, y3 = enemyLine[0].y, y4 = enemyLine[1].y;

                            float den = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y2);
                            if (den != 0) {
                                float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / den;
                                float ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / den;
                                if (0.0f <= ua && ua <= 1.0f && 0.0f <= ub && ub <= 1.0f) {
//                                    Log.d("artcow", "충돌");
                                    bullets.remove(b);//충돌시 총알과 적 삭제해줌
                                    enemys.remove(e);
                                    //적기가 파괴됐을 때 500sec 후에 적기 추가 실행
                                    final Enemy fe = e;
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            enemys.add(new Enemy(fe.bounds, fe.bitmap));
                                        }
                                    }, 500);

                                    return;
                                }
                            }
                        }

                    }
                }

            }

            //적의 총알과 내 캐릭터의 충돌판정
            for (Bullet eb : enemybullets) {
                for (Fighter fighter : fighters) {
                    for (Line l1 : Line.values()) {
                        Point[] enemyLine = fighter.getLine(l1);
                        for (Line l2 : Line.values()) {
                            Point[] line = eb.getLine(l2);

                            float x1 = line[0].x, x2 = line[1].x, x3 = enemyLine[0].x, x4 = enemyLine[1].x;
                            float y1 = line[0].y, y2 = line[1].y, y3 = enemyLine[0].y, y4 = enemyLine[1].y;

                            float den = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y2);
                            if (den != 0) {
                                float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / den;
                                float ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / den;
                                if (0.0f <= ua && ua <= 1.0f && 0.0f <= ub && ub <= 1.0f) {
                                    Log.d("artcow", "충돌");
                                    stopgame();
                                    return;
                                }
                            }
                        }

                    }
                }


            }
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // 터치 이벤트가 다운인 경우 현재 좌표에 볼이 위치하는지 판단
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                float x = event.getX(), y = event.getY();
                for (Fighter fighter : fighters) {
                    //바운더리 지정
                    int w = fighter.bitmap.getWidth();
                    int h = fighter.bitmap.getHeight();

                    float left = fighter.X;
                    float top = fighter.Y;
                    float right = fighter.X + w;
                    float bottom = fighter.Y + h;
                    //왼쪽위 꼭지점에서 시작


                    if (left <= x && x <= right && top <= y && y <= bottom) {
                        holder = fighter;
                        // 터치다운 이후의 작업 처리를 하기위해서는 true를 리턴 해주어야 함.
                        return true;
                    }
                }
            }

            // 터치다운 상태가 아닐땐 이전에 캐치해둔 홀더가 있는경우에만 작동
            if (holder == null) {
                Log.d("artcow", "holder is null");
                return super.onTouchEvent(event);
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                holder.setX(event.getX());
                //holder.setY(event.getY());


            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                holder = null;
            }

            return super.onTouchEvent(event);
        }
    }

    class MoveHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (handlerCount % 1 == 0) {
                for (int i = bullets.size() - 1; 0 <= i; i--) {
                    Bullet b = bullets.get(i);
                    if (b.moveUp()) {
                        bullets.remove(b);
                    }
                }

                for (int i = enemybullets.size() - 1; 0 <= i; i--) {
                    Bullet b = enemybullets.get(i);
                    if (b.moveDown()) {
                        bullets.remove(b);
                    }
                }

                for (Enemy e : enemys) {
                    e.moveRandom();
                }

                for (Background b : backgrounds) {
                    b.y += 1;
                    if (screenbounds.getHeight() <= b.y) {
                        b.y = -screenbounds.getHeight();
                    }
                }
            }

            for (Enemy e : enemys) {
                Log.d("artcow", "500 - e.speed*10 : " + (500 - e.speed*10));
            if (handlerCount % (30 - e.speed)  ==0) {
                Fighter me = fighters.get(0);
                Enemy you = enemys.get(0);
                Resources res = getResources();
                BitmapDrawable bd = (BitmapDrawable) res.getDrawable(R.drawable.bullet);
                Bitmap bitmap = bd.getBitmap();
                // x 는 현재 기체의 x 좌표를 기준으로 생성되는데,
                // 기체의 중심점과 총알의 중심점을 일치시키기 위해 기체의 w 의 절반을 더하고, 총알의 w의 절반을 빼서 x 좌표를 만듬.
                // y는 현재 기체의 머리부분과 동일한 좌표에서 시작해 총알이 기체의 머리에서 부터 발사.
                //나의 총알 생성
                Bullet b = new Bullet(me.X + me.bitmap.getWidth() / 2 - bitmap.getWidth() / 2, me.Y, bitmap, screenbounds);
                bullets.add(b);
                //적기 총알 생성
                    Bitmap eb = e.bitmap;
                    Bullet b2 = new Bullet(e.X + eb.getWidth() / 2 - eb.getWidth() / 2, e.Y, bitmap, screenbounds);
                    b2.speed = e.speed;
                    enemybullets.add(b2);
                }
            }
            Log.d("artcow" ,"fdjfkdjf : " + handlerCount);

            view.invalidate();
            handlerCount ++;
            if (1000000 < handlerCount ) {
                handlerCount = 0;
            }
            this.sendEmptyMessageDelayed(0, 15); //15밀리sec주기로 갱신

        }
    }

    interface Collisionable {
        public Point[] getLine(Line line);

    }


    class Background {
        private float y;
        private Bitmap bitmap;
    }

}