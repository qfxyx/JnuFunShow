package liangbin.funshow.manage;

import cn.bmob.v3.BmobObject;

/**
 * Created by Administrator on 2015/10/26.
 */

public class FSAppInstallation extends BmobObject {
    private String DevicesManu ;
    private   String DeviceName;
    private String DeviceVersion;
    private String DeviceId;
    private String PhoneNum;
    private Integer UseTimes;

    public void setDevicesManu(String manufacturer){
        this.DevicesManu=manufacturer;
    }
    public void setDeviceName(String deviceName){
        this.DeviceName=deviceName;

    }
    public void setDeviceVersion(String deviceVersion){
        this.DeviceVersion=deviceVersion;

    }
    public void setDeviceId(String deviceId){
        this.DeviceId=deviceId;
    }
    public void setPhoneNum(String phoneNum){
        this.PhoneNum=phoneNum;

    }
    public void setUseTimes(Integer useTimes){
        this.UseTimes=useTimes;
    }
}
