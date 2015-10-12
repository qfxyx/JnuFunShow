package liangbin.funshow.manage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2015/9/2.
 */
public class FunShowDatabaseHelper extends SQLiteOpenHelper {

    private Context mycontext;
    public static final String CREATE_SCHEDULE_TIMETABLE="create table class_timetable("+
            "id integer primary key autoincrement,"+"stu_num text,"+"stu_name text,"+
            "class_mon text,"+ "class_tus text,"+"class_wed text,"+"class_thu text,"+
            "class_fri text,"+ "class_sat text,"+ "class_sun text,"+"claas_week text)";

    public static final String CREATE_SCHEDULE_EXAM="create table exam_timetable("+
            "id integer primary key autoincrement,"+"stu_num text,"+"stu_name text,"+
            "exam_mon text,"+"exam_tus text,"+"exam_wed text,"+"exam_thu text,"
            +"exam_fri text,"+"exam_sat text,"+"exam_sun text,"+"exam_weeks text)";

    public FunShowDatabaseHelper(Context context,String name,SQLiteDatabase.
            CursorFactory cursorFactory,int version){
        super(context,name,cursorFactory,version);
        mycontext=context;
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_SCHEDULE_TIMETABLE);
        db.execSQL(CREATE_SCHEDULE_EXAM);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("drop table if exists class_timetable");
        db.execSQL("drop table if exists exam_timetable");
        onCreate(db);

    }
}
