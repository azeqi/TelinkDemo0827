package worker.seedmorn.com.telinkdemo0827.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.telink.bluetooth.LeBluetooth;
import com.telink.bluetooth.TelinkLog;
import com.telink.bluetooth.event.DeviceEvent;
import com.telink.bluetooth.event.LeScanEvent;
import com.telink.bluetooth.event.MeshEvent;
import com.telink.bluetooth.event.NotificationEvent;
import com.telink.bluetooth.event.ServiceEvent;
import com.telink.bluetooth.light.ConnectionStatus;
import com.telink.bluetooth.light.DeviceInfo;
import com.telink.bluetooth.light.LeScanParameters;
import com.telink.bluetooth.light.LightAdapter;
import com.telink.bluetooth.light.OnlineStatusNotificationParser;
import com.telink.util.Event;
import com.telink.util.EventListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import worker.seedmorn.com.telinkdemo0827.ErrorReportEvent;
import worker.seedmorn.com.telinkdemo0827.IMeshView;
import worker.seedmorn.com.telinkdemo0827.R;
import worker.seedmorn.com.telinkdemo0827.app.TelinkMyApplication;
import worker.seedmorn.com.telinkdemo0827.model.Light;
import worker.seedmorn.com.telinkdemo0827.model.Lights;
import worker.seedmorn.com.telinkdemo0827.model.SharedPreferencesHelper;
import worker.seedmorn.com.telinkdemo0827.presenter.MeshPresenter;
import worker.seedmorn.com.telinkdemo0827.server.TelinkLightService;

public class MainActivity extends TelinkBaseActivity implements IMeshView, EventListener<String> {
    private String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.btn_create_mash)
    Button btnCreateMash;
    @BindView(R.id.btn_search)
    Button btnSearch;
    @BindView(R.id.btn_connect)
    Button btnConnect;
    private MeshPresenter mMeshPresenter;
    private TelinkMyApplication mApplication;
    private static final int UPDATE_LIST = 0;
    private static final String DEFAULT_NAME = "telink_mesh1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    int PERMISSION_REQUEST_CODE = 0x10;

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    // 显示解释权限用途的界面，然后再继续请求权限
                } else {
                    // 没有权限，直接请求权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_CODE);
                }
            }
        }

    }


    private void init() {
        this.mApplication = (TelinkMyApplication) this.getApplication();
        //监听事件
        mMeshPresenter = new MeshPresenter(this, this, mApplication);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        registerReceiver(mReceiver, filter);
        checkPermission();
        this.mApplication.doInit();//初始化 mes

    }

    @Override
    protected void onStart() {
        super.onStart();
        this.mApplication.addEventListener(DeviceEvent.STATUS_CHANGED, this);
        this.mApplication.addEventListener(NotificationEvent.ONLINE_STATUS, this);
        this.mApplication.addEventListener(NotificationEvent.GET_ALARM, this);
        this.mApplication.addEventListener(NotificationEvent.GET_DEVICE_STATE, this);
        this.mApplication.addEventListener(ServiceEvent.SERVICE_CONNECTED, this);
        this.mApplication.addEventListener(MeshEvent.OFFLINE, this);
        this.mApplication.addEventListener(ErrorReportEvent.ERROR_REPORT, this);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);

                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "蓝牙开启");
                        TelinkLightService.Instance().idleMode(true);
//                        autoConnect();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "蓝牙关闭");
                        break;
                }
            }
        }
    };


    @OnClick({R.id.btn_create_mash, R.id.btn_search, R.id.btn_connect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_create_mash:
                mMeshPresenter.showCreateMeshDialog();
                break;
            case R.id.btn_search:
                TelinkLightService.Instance().idleMode(true);
                if ("".equals(SharedPreferencesHelper.getMeshName(this)) || "".equals(SharedPreferencesHelper.getMeshName(this))) {
                    Toast.makeText(this, " mesh name or password can not be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (mApplication.isEmptyMesh()) {
                    finish();
                    Toast.makeText(mApplication, "mesh info null", Toast.LENGTH_SHORT).show();
//            showToast("mesh info null!");
                    return;
                }
                startActivity(new Intent(this, ScanActivity.class));
                break;
            case R.id.btn_connect:
                break;
        }
    }

    private void checkMesh() {

    }

    @Override
    public void updateTip() {//h 和 启动 server
        StringBuilder tipBuilder = new StringBuilder();
        tipBuilder.append("当前 mesh:  " + SharedPreferencesHelper.getMeshName(this));
        tipBuilder.append("\n当前密码:  " + SharedPreferencesHelper.getMeshPassword(this));
        tvContent.setText(tipBuilder.toString());
    }

    @Override
    public void performed(Event<String> event) {
        Log.v("performed", "performed: ----〉" + event.getType() + "   ");
        switch (event.getType()) {
            case LeScanEvent.LE_SCAN:
//                this.onLeScan((LeScanEvent) event);
                Log.i(TAG, "le_scan: " + event);
                break;
            case LeScanEvent.LE_SCAN_TIMEOUT:
//                this.onLeScanTimeout((LeScanEvent) event);
                Log.i(TAG, "le_scan_timeout: " + event);
                break;
            case LeScanEvent.LE_SCAN_COMPLETED:
//                updateList();
                Log.i(TAG, "le_scan_completed: " + event);
                break;
            case DeviceEvent.STATUS_CHANGED:
                DeviceInfo deviceInfo = ((DeviceEvent) event).getArgs();
                int state = deviceInfo.status;
                if (state == LightAdapter.STATUS_CONNECTED) {
                    Log.i(TAG, "status_connected: " + event);
//                    onConnected(deviceInfo);
                } else if (state == LightAdapter.STATUS_LOGIN) {
                    Log.i(TAG, "status_login: " + event);

//                    onLogin(deviceInfo);
                } else if (state == LightAdapter.STATUS_LOGOUT) {
                    Log.i(TAG, "status_logout: " + event);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            btn_back.setEnabled(true);
                        }
                    });
                } else if (state == LightAdapter.STATUS_UPDATE_MESH_COMPLETED) {
//                    if (!updateComplete) {
//                        delayHandler.removeCallbacks(resetCompleteTask);
//                        delayHandler.postDelayed(resetCompleteTask, COMPLETE_DELAY);
//                    }
                }
                break;

            case MeshEvent.UPDATE_COMPLETED:
                this.startScan();
                break;
            case MeshEvent.ERROR:
                this.onMeshEvent((MeshEvent) event);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        this.mApplication.removeEventListener(this);
        Lights.getInstance().clear();
        this.mApplication.doDestroy();
        unregisterReceiver(mReceiver);
    }

    private void onMeshEvent(MeshEvent event) {
        new AlertDialog.Builder(this).setMessage("重启蓝牙,更好地体验智能灯!").show();
    }

    /**
     * 处理{@link NotificationEvent#ONLINE_STATUS}事件
     */
    private synchronized void onOnlineStatusNotify(NotificationEvent event) {

        TelinkLog.i("MainActivity#onOnlineStatusNotify#Thread ID : " + Thread.currentThread().getId());
        List<OnlineStatusNotificationParser.DeviceNotificationInfo> notificationInfoList;
        //noinspection unchecked
        notificationInfoList = (List<OnlineStatusNotificationParser.DeviceNotificationInfo>) event.parse();

        if (notificationInfoList == null || notificationInfoList.size() <= 0)
            return;

        /*if (this.deviceFragment != null) {
            this.deviceFragment.onNotify(notificationInfoList);
        }*/

        for (OnlineStatusNotificationParser.DeviceNotificationInfo notificationInfo : notificationInfoList) {

            int meshAddress = notificationInfo.meshAddress;
            int brightness = notificationInfo.brightness;

//            Light light = this.deviceFragment.getDevice(meshAddress);

//            if (light == null) {
//                light = new Light();
////                this.deviceFragment.addDevice(light);
//            }
//
//            light.meshAddress = meshAddress;
//            light.brightness = brightness;
//            light.connectionStatus = notificationInfo.connectionStatus;
//
//            if (light.meshAddress == this.connectMeshAddress) {
//                light.textColor = R.color.theme_positive_color;
//            } else {
//                light.textColor = R.color.black;
//            }
        }

        mHandler.obtainMessage(UPDATE_LIST).sendToTarget();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_LIST:
//                    deviceFragment.notifyDataSetChanged();
                    break;
            }
        }
    };
    private int connectMeshAddress;

    private void onDeviceStatusChanged(DeviceEvent event) {

        DeviceInfo deviceInfo = event.getArgs();

        switch (deviceInfo.status) {
            case LightAdapter.STATUS_UPDATE_MESH_COMPLETED:
                //加灯完成继续扫描,直到扫不到设备
                Log.i(TAG, "onDeviceStatusChanged: 加灯完成继续扫描,直到扫不到设备");
                int meshAddress = deviceInfo.meshAddress & 0xFF;
//                Light light = this.adapter.get(meshAddress);

//                if (light == null) {
//                    light = new Light();
//
//                    light.deviceName = deviceInfo.deviceName;
//                    light.meshName = deviceInfo.meshName;
//                    light.firmwareRevision = deviceInfo.firmwareRevision;
//                    light.longTermKey = deviceInfo.longTermKey;
//                    light.macAddress = deviceInfo.macAddress;
//                    light.meshAddress = deviceInfo.meshAddress;
//                    light.meshUUID = deviceInfo.meshUUID;
//                    light.productUUID = deviceInfo.productUUID;
//                    light.status = deviceInfo.status;
//                    light.textColor = R.color.black;
//                    light.selected = false;
////                    light.raw = deviceInfo;
//                    this.mApplication.getMesh().devices.add(light);
//                    this.mApplication.getMesh().saveOrUpdate(this);
//
//
//                    this.adapter.add(light);
//                    this.adapter.notifyDataSetChanged();
//                }

                break;

            case LightAdapter.STATUS_UPDATE_MESH_FAILURE:
                //加灯失败继续扫描
//                this.startScan(1000);
                TelinkLog.w("DeviceBatchScanningActivity#STATUS_UPDATE_MESH_FAILURE");
                break;

//            case LightAdapter.STATUS_ERROR_N:
//                this.onNError(event);
//                break;
        }
    }

    private void onLogout() {
        List<Light> lights = Lights.getInstance().get();
        for (Light light : lights) {
            light.connectionStatus = ConnectionStatus.OFFLINE;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run: 设备离线");
//                deviceFragment.notifyDataSetChanged();
            }
        });
    }

    /**
     * 开始扫描
     */
    private void startScan() {

        TelinkLightService.Instance().idleMode(true);

        if (mApplication.isEmptyMesh())
            return;

        //扫描参数
        LeScanParameters params = LeScanParameters.create();
        params.setMeshName(DEFAULT_NAME);
        params.setOutOfMeshName(DEFAULT_NAME);
        params.setTimeoutSeconds(10);
        params.setScanMode(true);
//        params.setScanTypeFilter(0x00);
        Log.i("scan", "startScan: " + DEFAULT_NAME + "" + DEFAULT_NAME);
        TelinkLightService.Instance().startScan(params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LeBluetooth.getInstance().isSupport(this)) {
            Toast.makeText(this, " ble not support", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!LeBluetooth.getInstance().isEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);


            builder.setMessage("开启蓝牙，体验智能灯!");
            builder.setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton("enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LeBluetooth.getInstance().enable(getApplicationContext());
                }
            });
            builder.show();
        }
        DeviceInfo deviceInfo = this.mApplication.getConnectDevice();
        if (null != deviceInfo)
            this.connectMeshAddress = this.mApplication.getConnectDevice().meshAddress & 0xFF;

    }

    @Override
    protected void onStop() {
        super.onStop();
        TelinkLightService.Instance().disableAutoRefreshNotify();
    }
}
