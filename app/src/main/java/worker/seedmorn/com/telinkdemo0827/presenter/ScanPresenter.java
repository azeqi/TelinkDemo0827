package worker.seedmorn.com.telinkdemo0827.presenter;

import android.content.Context;

import worker.seedmorn.com.telinkdemo0827.app.TelinkMyApplication;

/*
*============================================
*Author:  zhangyn
*Time:   2018/8/27
*Version: 1.0
*Description:This is ${DATA}
*============================================
*/
public class ScanPresenter {
    private TelinkMyApplication myApplication;
    private Context mContext;

    public ScanPresenter(Context context, TelinkMyApplication myApplication) {
        this.myApplication=myApplication;
        this.mContext=context;
    }
}
