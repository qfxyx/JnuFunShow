package liangbin.funshow.manage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/7/29.
 */
public class TitleListAdapter extends ArrayAdapter<TitleListView> {
    private int resourceId;
    public TitleListAdapter(Context context,int textViewResourceId,List<TitleListView> objects){
        super(context,textViewResourceId,objects);
        resourceId=textViewResourceId;
    }
    @Override
    public View getView(int position,View converView,ViewGroup parent){
        TitleListView titleListView=getItem(position);
        View view;
        ViewHolder viewHolder;
        if (converView==null){
            view= LayoutInflater.from(getContext()).inflate(resourceId,null);
            viewHolder=new ViewHolder();
            viewHolder.name=(TextView)view.findViewById(R.id.title_list_name);
            viewHolder.titleImage=(ImageView)view.findViewById(R.id.title_list_imageId);
            view.setTag(viewHolder);
        }else {
            view=converView;
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.titleImage.setImageResource(titleListView.getImageId());
        viewHolder.name.setText(titleListView.getName());
        return view;
    }
    class ViewHolder{
        ImageView titleImage;
        TextView name;
    }
}
