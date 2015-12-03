package liangbin.funshow.manage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.jar.Attributes;

import liangbin.funshow.R;

/**
 * Created by Administrator on 2015/11/6.
 */
public class PullToRefleshLayout extends LinearLayout implements View.OnTouchListener{
    private final String TAG =".manage.PullToRefleshLayout";

    //the status of pulling to reflesh
    public static final int STATUS_PULL_TO_REFRESH = 0;
    // release to reflesh
    public static final int STATUS_RELEASE_TO_REFRESH = 1;
    //refleshing
    public static final int STATUS_REFRESHING = 2;
    //reflesh finish or nerver reflesh
    public static final int STATUS_REFRESH_FINISHED = 3;

    // the speed of the arrow rolls back
    public static final int SCROLL_SPEED = -20;

    //下拉刷新的回调接口
    private PullToRefreshListener mListener;

    //需要去下拉刷新的ListView
    private ListView listView;

    //the header of the view
    private View header;

    // 刷新时显示的进度条
    private ProgressBar progressBar;

    //指示下拉和释放的箭头
    private ImageView arrow;

    //指示下拉和释放的文字描述
    private TextView description;

    //下拉头的布局参数
    private MarginLayoutParams headerLayoutParams;

    //下拉头的高度
    private int hideHeaderHeight;

    //当前处理什么状态，可选值有STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
    // STATUS_REFRESHING 和 STATUS_REFRESH_FINISHED
    private int currentStatus = STATUS_REFRESH_FINISHED;

    //记录上一次的状态是什么，避免进行重复操作
    private int lastStatus = currentStatus;

    //手指按下时的屏幕纵坐标
    private float yDown;

    //在被判定为滚动之前用户手指可以移动的最大值。
    private  float touchSlop;

    //是否已加载过一次layout，这里onLayout中的初始化只需加载一次
    private boolean loadOnce;

    //当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
    private boolean ableToPull;

    public PullToRefleshLayout(Context context,AttributeSet attributes){
        super(context,attributes);
        headerInit(context);
    }
    @Override
    public void  onLayout(boolean change,int l,int t,int r,int b){
        super.onLayout(change, l, t, r, b);
        if (change&&!loadOnce){
            hideHeaderHeight=-header.getHeight();
            headerLayoutParams=(MarginLayoutParams)header.getLayoutParams();
            headerLayoutParams.topMargin=hideHeaderHeight;
            listView=(ListView)getChildAt(1);
            listView.setOnTouchListener(this);
            loadOnce=true;
            Log.i(TAG,"onLayout开始");
        }
    }
    @Override
    public boolean onTouch(View view,MotionEvent motionEvent){

        setIsAbleToPull(motionEvent);
        if(ableToPull){
            Log.i(TAG,String.valueOf(currentStatus));
            switch (motionEvent.getAction()){


                //表示刚按下时
                case MotionEvent.ACTION_DOWN:
                    //getRawY()表示从整个屏幕的左上角开始计算纵坐标
                    yDown=motionEvent.getRawY();
                    Log.i(TAG,"ACTION_DOWN");
                    break;

                //移动时
                case MotionEvent.ACTION_HOVER_MOVE:
                    Log.i(TAG,"移动");
                    float yMove=motionEvent.getRawY();
                    int distance=(int)(yMove-yDown);

                    // 如果手指是下滑状态，并且下拉头是完全隐藏的，就屏蔽下拉事件
                    if (distance <= 0 && headerLayoutParams.topMargin <= hideHeaderHeight) {
                        return false;
                    }

                    //触发移动事件的最短距离，如果小于这个距离就不触发移动控件
                    if (distance<touchSlop){
                        return false;
                    }
                    if (currentStatus!=STATUS_REFRESHING){
                        if (headerLayoutParams.topMargin>0){
                            currentStatus=STATUS_RELEASE_TO_REFRESH;
                        }else {
                            currentStatus=STATUS_PULL_TO_REFRESH;
                        }
                        // 通过偏移下拉头的topMargin值，来实现下拉效果
                        headerLayoutParams.topMargin = (distance / 2) + hideHeaderHeight;
                        header.setLayoutParams(headerLayoutParams);
                    }

                    break;

                //抬起,下面是两个case匹配同一个代码块
                case MotionEvent.ACTION_UP:
                default:
                    Log.i(TAG,"松开");
                    if (currentStatus==STATUS_RELEASE_TO_REFRESH){
                        //// 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
                        new RefreshingTask().execute();

                    }else if (currentStatus==STATUS_PULL_TO_REFRESH){
                        // 松手时如果是下拉状态，就去调用隐藏下拉头的任务
                        new HideHeaderTask().execute();

                    }
                    break;

            }
            // 时刻记得更新下拉头中的信息
            if (currentStatus == STATUS_PULL_TO_REFRESH
                    || currentStatus == STATUS_RELEASE_TO_REFRESH) {
                updateHeaderView();
                // 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
                listView.setPressed(false);
                listView.setFocusable(false);
                listView.setFocusableInTouchMode(false);
                lastStatus = currentStatus;
                // 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
                return true;
            }
        }
        return false;
    }
    //Header init
    private void headerInit(Context context){
        header= LayoutInflater.from(context).inflate(R.layout.pull_reflesh_header,null,true);
        progressBar=(ProgressBar)header.findViewById(R.id.pull_reflesh_progressBar);
        arrow=(ImageView)header.findViewById(R.id.pull_arrow);
        description=(TextView)header.findViewById(R.id.pull_reflesh_text);
        //the value has been defined in api sourceCode ,I think,maybe it is not ture
        touchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
        setOrientation(VERTICAL);
        //this is the method of ViewGroup and LinearLayout extends from ViewGroup
        addView(header,0);
        Log.i(TAG, "初始化header");



    }
    private void setIsAbleToPull(MotionEvent event) {

        View firstChild = listView.getChildAt(0);
        if (firstChild != null) {
            Log.i(TAG,"是否允许下拉开始设置");
            int firstVisiblePos = listView.getFirstVisiblePosition();
            if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
                if (!ableToPull) {
                    yDown = event.getRawY();
                }
                // 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
                ableToPull = true;
                Log.i(TAG,"允许下拉");
            } else {
                if (headerLayoutParams.topMargin != hideHeaderHeight) {
                    headerLayoutParams.topMargin = hideHeaderHeight;
                    header.setLayoutParams(headerLayoutParams);
                }
                ableToPull = false;
                Log.i(TAG,"不允许下拉");
            }
        } else {
            // 如果ListView中没有元素，也应该允许下拉刷新
            ableToPull = true;
            Log.i(TAG,"没有元素也允许下拉");
        }
    }
    private void updateHeaderView() {
        if (true) {
            if (currentStatus == STATUS_PULL_TO_REFRESH) {
                description.setText("下拉刷新");
                arrow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Log.i(TAG, "下拉刷新");
                rotateArrow();
            } else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                description.setText("松开刷新");
                arrow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Log.i(TAG, "松开刷新");
                rotateArrow();
            } else if (currentStatus == STATUS_REFRESHING) {
                description.setText("正在刷新");
                progressBar.setVisibility(View.VISIBLE);
                arrow.clearAnimation();
                arrow.setVisibility(View.GONE);
                Log.i(TAG, "正在刷新");
            }
        }


        }
        private void rotateArrow() {
            float pivotX = arrow.getWidth() / 2f;
            float pivotY = arrow.getHeight() / 2f;
            float fromDegrees = 0f;
            float toDegrees = 0f;
            if (currentStatus == STATUS_PULL_TO_REFRESH) {
                fromDegrees = 180f;
                toDegrees = 360f;
            } else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                fromDegrees = 0f;
                toDegrees = 180f;
            }
            RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
            animation.setDuration(100);
            animation.setFillAfter(true);
            arrow.startAnimation(animation);
            Log.i(TAG, "旋转header");
        }

    // 使当前线程睡眠指定的毫秒数。
    private void sleep(int time){
        try {
            Thread.sleep(time);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        }
    class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            int topMargin = headerLayoutParams.topMargin;
            while (true) {
                topMargin = topMargin + SCROLL_SPEED;
                if (topMargin <= hideHeaderHeight) {
                    //回到原始距离后循环终止
                    topMargin = hideHeaderHeight;
                    break;
                }
                //更新距离，执行中创建新线程处理onProgressUpdate()
                publishProgress(topMargin);
                sleep(10);
            }
            return topMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... topMargin) {
            headerLayoutParams.topMargin = topMargin[0];
            header.setLayoutParams(headerLayoutParams);
        }

        @Override
        protected void onPostExecute(Integer topMargin) {
            headerLayoutParams.topMargin = topMargin;
            header.setLayoutParams(headerLayoutParams);
            currentStatus = STATUS_REFRESH_FINISHED;
        }
    }
    //正在刷新的任务，在此任务中会去回调注册进来的下拉刷新监听器。
    class RefreshingTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            int topMargin = headerLayoutParams.topMargin;
            while (true) {
                topMargin = topMargin + SCROLL_SPEED;
                if (topMargin <= 0) {
                    topMargin = 0;
                    break;
                }
                publishProgress(topMargin);
                sleep(10);
            }
            currentStatus = STATUS_REFRESHING;
            publishProgress(0);
            if (mListener != null) {
                mListener.onRefresh();
            }
            return null;
        }
    }

        //下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
    public interface PullToRefreshListener{
        //刷新时会去回调此方法，在方法内编写具体的刷新逻辑。
        // 注意此方法是在子线程中调用的， 你可以不必另开线程来进行耗时操作。
        void onRefresh();
    }
    public void setOnRefreshListener(PullToRefreshListener listener) {
        mListener = listener;

    }
    public void finishRefreshing() {
        //当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于正在刷新状态。
        currentStatus = STATUS_REFRESH_FINISHED;
        new HideHeaderTask().execute();
    }

}

