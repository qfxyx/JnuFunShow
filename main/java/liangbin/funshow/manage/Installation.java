package liangbin.funshow.manage;

import cn.bmob.v3.BmobObject;

/**
 * Created by Administrator on 2015/9/21.
 */
public class Installation extends BmobObject {
    private String manufacturer;
    public  String deviceName;
    private String deviceVersion;
    private String deviceId;

    public void setManufacturer(String manufacturer){
        this.manufacturer=manufacturer;
    }
    public void setDeviceName(String deviceName){
        this.deviceName=deviceName;

    }
    public void setDeviceVersion(String deviceVersion){
        this.deviceVersion=deviceVersion;

    }
    public void setDeviceId(String deviceId){
        this.deviceId=deviceId;
    }
}
