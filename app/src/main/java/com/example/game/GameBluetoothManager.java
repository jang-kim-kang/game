package com.example.game;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

// 참고 블로그
// http://jinyongjeong.github.io/2018/09/27/bluetoothpairing/
// https://ghj1001020.tistory.com/291

// https://bugwhale.tistory.com/entry/android-bluetooth-application

public class GameBluetoothManager {
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Context context;

    public GameBluetoothManager(Context context) {
        this.context = context;
    }


    public static boolean isEnabledBluetooth() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean isActiveBluetooth() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public void activeRecieve() {
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //연결 끊김 확인
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND);    //기기 검색됨
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);   //기기 검색 시작
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  //기기 검색 종료
        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        context.registerReceiver(mBluetoothStateReceiver, stateFilter);
    }

    public void pauseReceive() {
        context.unregisterReceiver(mBluetoothStateReceiver);
    }

    public Set<BluetoothDevice> pairedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    public void discovery() {
        mBluetoothAdapter.startDiscovery(); //블루투스 기기 검색 시작
        Log.d("artcow", "discovery");
    }

    public void connect(BluetoothDevice device) {
        mDevices = mBluetoothAdapter.getBondedDevices();
        mPairedDeviceCount = mDevices.size();

        //pairing되어 있는 기기의 목록을 가져와서 연결하고자 하는 기기가 이전 기기 목록에 있는지 확인
        boolean already_bonded_flag = false;
        if(mPairedDeviceCount > 0){
            for (BluetoothDevice bonded_device : mDevices) {
                if(device.getName().equals(bonded_device.getName())){
                    already_bonded_flag = true;
                    break;
                }
            }
        }
        //pairing process
        //만약 pairing기록이 있으면 바로 연결을 수행하며, 없으면 createBond()함수를 통해서 pairing을 수행한다.
        if(!already_bonded_flag){
            try {
                //pairing수행
                device.createBond();
                mRemoteDevice = device;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            connectToSelectedDevice(device);
        }
    }

    private void connectToSelectedDevice(BluetoothDevice device) {
        try {
            //선택한 디바이스 페어링 요청
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            mRemoteDevice = device;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Intent enabledDiscovery() {
        Log.d("artcow", "mBluetoothAdapter.getScanMode() = " + mBluetoothAdapter.getScanMode());
        if(mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){ //검색응답 모드가 활성화이면 하지 않음
            Log.d("artcow", "dddd");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60 * 30);  //60초 동안 상대방이 나를 검색할 수 있도록한다
//            startActivity(intent);
            return intent;
        }
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60 * 30);  //60초 동안 상대방이 나를 검색할 수 있도록한다
        return intent;
    }


    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();   //입력된 action
//            Toast.makeText(context, "받은 액션 : " + action, Toast.LENGTH_SHORT).show();
            Log.d("artcow", action);
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String name = null;
            if (device != null) {
                name = device.getName();    //broadcast를 보낸 기기의 이름을 가져온다.
            }
            //입력된 action에 따라서 함수를 처리한다
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED: //블루투스의 연결 상태 변경
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:

                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:

                            break;
                        case BluetoothAdapter.STATE_ON:

                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:

                            break;
                    }

                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:  //블루투스 기기 연결

                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:

                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:   //블루투스 기기 끊어짐

                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED: //블루투스 기기 검색 시작

                    break;
                case BluetoothDevice.ACTION_FOUND:  //블루투스 기기 검색 됨, 블루투스 기기가 근처에서 검색될 때마다 수행됨
                    String device_name = device.getName();
                    String device_Address = device.getAddress();
                    //본 함수는 블루투스 기기 이름의 앞글자가 "GSM"으로 시작하는 기기만을 검색하는 코드이다
                    if (device_name != null && device_name.length() > 4) {
                        Log.d("Bluetooth Name: ", device_name);
                        Log.d("Bluetooth Mac Address: ", device_Address);
                        if (device_name.substring(0, 3).equals("GSM")) {
//                            bluetooth_device.add(device);
                        }
                    }
                    break;
                case "android.bluetooth.adapter.action.DISCOVERY_FINISHED": //블루투스 기기 검색 종료
                    Log.d("artcow", "jfkdlfjldkjflkdjf");

                    Log.d("artcow", "");
//                    StartBluetoothDeviceConnection();   //원하는 기기에 연결
                    break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:

                    break;
            }

        }
    };


    private static final int REQUEST_ENABLE_BT = 3;
    //    public BluetoothAdapter mBluetoothAdapter = null;
    Set<BluetoothDevice> mDevices;
    int mPairedDeviceCount;
    BluetoothDevice mRemoteDevice;
    BluetoothSocket mSocket;
    InputStream mInputStream;
    OutputStream mOutputStream;
    Thread mWorkerThread;
    int readBufferPositon;      //버퍼 내 수신 문자 저장 위치
    byte[] readBuffer;      //수신 버퍼
    byte mDelimiter = 10;
}
