package liangbin.funshow.manage;

/**
 * Created by Administrator on 2015/7/29.
 */
public class TitleListView {
    private String name;
    private int imageId;
    public TitleListView(String name,int imageId)
    {       this.name=name;
            this.imageId=imageId;

    }
    public String getName(){
        return name;
    }
    public int getImageId(){
        return imageId;
    }
}
