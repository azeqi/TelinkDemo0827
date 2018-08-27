package worker.seedmorn.com.telinkdemo0827.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.telink.bluetooth.TelinkLog;
import com.telink.bluetooth.event.DeviceEvent;
import com.telink.bluetooth.event.LeScanEvent;
import com.telink.bluetooth.event.MeshEvent;
import com.telink.bluetooth.event.NotificationEvent;
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
import worker.seedmorn.com.telinkdemo0827.IMeshView;
import worker.seedmorn.com.telinkdemo0827.R;
import worker.seedmorn.com.telinkdemo0827.app.TelinkMyApplication;
import worker.seedmorn.com.telinkdemo0827.model.Light;
import worker.seedmorn.com.telinkdemo0827.model.Lights;
import worker.seedmorn.com.telinkdemo0827.model.Mesh;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        this.mApplication = (TelinkMyApplication) this.getApplication();
        //监听事件
        this.mApplication = (TelinkMyApplication) this.getApplication();
        this.mApplication.addEventListener(LeScanEvent.LE_SCAN, this);
        this.mApplication.addEventListener(LeScanEvent.LE_SCAN_TIMEOUT, this);
        this.mApplication.addEventListener(LeScanEvent.LE_SCAN_COMPLETED, this);
        this.mApplication.addEventListener(DeviceEvent.STATUS_CHANGED, this);
        this.mApplication.addEventListener(MeshEvent.UPDATE_COMPLETED, this);
        this.mApplication.addEventListener(MeshEvent.ERROR, this);
        mMeshPresenter = new MeshPresenter(this, this);
    }

    @OnClick({R.id.btn_create_mash, R.id.btn_search, R.id.btn_connect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_create_mash:
                mMeshPresenter.showCreateMeshDialog();
                break;
            case R.id.btn_search:
                if ("".equals(SharedPreferencesHelper.getMeshName(this)) || "".equals(SharedPreferencesHelper.getMeshName(this))) {
                    Toast.makeText(this, " mesh name or password can not be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                this.startScan(0);
                break;
            case R.id.btn_connect:
                break;
        }
    }

    private void checkMesh() {

    }

    @Override
    public void updateTip() {
        StringBuilder tipBuilder = new StringBuilder();
        tipBuilder.append("当前 mesh:  " + SharedPreferencesHelper.getMeshName(this));
        tipBuilder.append("\n当前密码:  " + SharedPreferencesHelper.getMeshPassword(this));
        tvContent.setText(tipBuilder.toString());
        this.mApplication.doInit();//初始化 mesh 和 启动 server
    }

    @Override
    public void performed(Event<String> event) {
        Log.i(TAG, "performed: ----〉" + event.getType() + "   " + ((NotificationEvent) event).getArgs());
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
                this.onDeviceStatusChanged((DeviceEvent) event);
                break;

            case MeshEvent.UPDATE_COMPLETED:
                this.startScan(1000);
                break;
            case MeshEvent.ERROR:
                this.onMeshEvent((MeshEvent) event);
                break;
        }
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
    private void startScan(int delay) {
//        scanedList.clear();
        TelinkLightService.Instance().idleMode(true);
        this.mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mApplication.isEmptyMesh())
                    return;
                Mesh mesh = mApplication.getMesh();
                //扫描参数
                LeScanParameters params = LeScanParameters.create();
                params.setMeshName("telink_mesh1");
                params.setOutOfMeshName("telink_mesh1");
                params.setTimeoutSeconds(10);
                params.setScanMode(false);
                TelinkLightService.Instance().startScan(params);
            }
        }, delay);
    }
}
