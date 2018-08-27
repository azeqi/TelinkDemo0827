package worker.seedmorn.com.telinkdemo0827.bean;

import android.os.Parcel;
import android.os.Parcelable;

/*
*============================================
*Author:  zhangyn
*Time:   2018/8/27
*Version: 1.0
*Description:This is ${DATA}
*============================================
*/
public class ErrorReportInfo implements Parcelable {


    /**
     * state code
     */
    public int stateCode;

    /**
     * error code
     */
    public int errorCode;

    public int deviceId;

    public ErrorReportInfo() {

    }

    public static final Creator<ErrorReportInfo> CREATOR = new Creator<ErrorReportInfo>() {
        @Override
        public ErrorReportInfo createFromParcel(Parcel in) {
            return new ErrorReportInfo(in);
        }

        @Override
        public ErrorReportInfo[] newArray(int size) {
            return new ErrorReportInfo[size];
        }
    };

    public ErrorReportInfo(Parcel in) {
        this.stateCode = in.readInt();
        this.errorCode = in.readInt();
        this.deviceId = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.stateCode);
        dest.writeInt(this.errorCode);
        dest.writeInt(this.deviceId);
    }

}

