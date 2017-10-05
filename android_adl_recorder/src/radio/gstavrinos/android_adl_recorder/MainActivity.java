package radio.gstavrinos.android_adl_recorder;

import std_msgs.String;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import org.ros.node.Node;
import org.ros.node.NodeMain;

import android.os.Handler;
import android.support.annotation.IdRes;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.ros.node.ConnectedNode;
import org.ros.namespace.GraphName;
import org.ros.android.RosActivity;
import org.ros.node.topic.Publisher;
import org.ros.node.NodeMainExecutor;
import org.ros.node.NodeConfiguration;

import static com.google.common.primitives.Ints.min;

/**
 * Created by gstavrinos on 9/8/17.
 */

public class MainActivity extends RosActivity implements NodeMain{

    private Publisher<String> publisher;

    private EditText code_name, repetition;
    private RadioGroup adl_selection;
    private Button adl_start, start_timer;
    private TextView time;
    private TableLayout past_times;
    private ScrollView scroll;

    private java.lang.String adl_number, adl_code, adl_repetition, adl_name;
    private java.lang.String recording_topic = "android_app/record_adl";

    private Handler mHandler = new Handler();
    private long startTime;
    private long elapsedTime;
    private final int REFRESH_RATE = 1;
    private java.lang.String hours, minutes, seconds, milliseconds;
    private long hrs, secs, mins;


    private Runnable startTimer = new Runnable() {
        public void run() {
            elapsedTime = System.currentTimeMillis() - startTime;
            updateTimer(elapsedTime);
            mHandler.postDelayed(this,REFRESH_RATE);
        }
    };

    public MainActivity() {
        super("Android ADL Recorder", "Android ADL Recorder");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main_activity);

        adl_number = "";
        adl_code = "";
        adl_repetition = "";
        adl_name = "-";

        code_name = (EditText)findViewById(R.id.editText);
        repetition = (EditText)findViewById(R.id.editText2);
        adl_selection = (RadioGroup)findViewById(R.id.adl_selection);
        adl_start = (Button)findViewById(R.id.start_button);
        start_timer = (Button)findViewById(R.id.timer_start_button);
        time = (TextView)findViewById(R.id.time);
        past_times = (TableLayout)findViewById(R.id.past_times);
        scroll = (ScrollView) findViewById(R.id.scroll);

        start_timer.setBackgroundColor(Color.GREEN);
        start_timer.setEnabled(false);
        start_timer.setAlpha(0.5f);

        adl_selection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                adl_number = checkedId+"";
                adl_name = ((RadioButton)adl_selection.getChildAt(checkedId-1)).getText().toString();
            }
        });

        adl_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adl_start.getText().equals("Start Robot")){
                    if (publisher == null) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Connection error!")
                                .setMessage("There was a connection error. Please check your network settings!")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {}
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                    else{
                        if(!adl_number.isEmpty() && !adl_code.isEmpty() && !adl_repetition.isEmpty()) {
                            if(adl_code.contains("#") || adl_repetition.contains("#")){
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Wrong input!")
                                        .setMessage("None of the values can contain the '#' character!")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {}
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                            else {
                                start_timer.setEnabled(true);
                                start_timer.setAlpha(1f);
                                adl_start.setEnabled(false);
                                adl_start.setAlpha(0.5f);
                                adl_code = code_name.getText().toString();
                                adl_repetition = repetition.getText().toString();
                                String s_msg = publisher.newMessage();
                                s_msg.setData(adl_number + "#" + adl_code + "#" + adl_repetition);
                                publisher.publish(s_msg);
                                adl_start.setText("Stop Robot");
                                for(int i=0;i < adl_selection.getChildCount();i++){
                                    if(Integer.parseInt(adl_number)-1 == i){
                                        (adl_selection.getChildAt(i)).setEnabled(true);
                                    }
                                    else{
                                        (adl_selection.getChildAt(i)).setEnabled(false);
                                    }
                                }
                                code_name.setEnabled(false);
                                repetition.setEnabled(false);
                            }
                        }
                        else{
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Wrong input!")
                                    .setMessage("Please check your selected values!")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {}
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                }
                else {
                    String s_msg = publisher.newMessage();
                    //pill intake has a number of 4, but it stops at 13, so
                    java.lang.String adl_number_ = min(Integer.parseInt(adl_number) + 10, 13)+"";
                    s_msg.setData(adl_number_ + "#" + adl_code + "#" + adl_repetition);
                    publisher.publish(s_msg);
                    adl_start.setText("Start Robot");
                    for(int i=0;i < adl_selection.getChildCount();i++){
                        if(Integer.parseInt(adl_number) == i){
                            (adl_selection.getChildAt(i)).setEnabled(true);
                        }
                        else{
                            (adl_selection.getChildAt(i)).setEnabled(true);
                        }
                    }
                    code_name.setEnabled(true);
                    repetition.setEnabled(true);
                }
            }
        });
    }

    private void updateTimer (float time_){
        secs = (long)(time_ / 1000);
        mins = (long)((time_ / 1000) / 60);
        hrs = (long)(((time_ / 1000) / 60) / 60);

        secs = secs % 60;
        seconds = java.lang.String.valueOf(secs);
        if(secs == 0){
            seconds = "00";
        }
        if(secs < 10 && secs > 0){
            seconds = "0" + seconds;
        }

        mins = mins % 60;
        minutes = java.lang.String.valueOf(mins);
        if(mins == 0){
            minutes = "00";
        }
        if(mins < 10 && mins > 0){
            minutes = "0" + minutes;
        }

        hours = java.lang.String.valueOf(hrs);
        if(hrs == 0){
            hours = "00";
        }
        if(hrs < 10 && hrs > 0){
            hours = "0" + hours;
        }

        milliseconds = java.lang.String.valueOf((long)time_);
        if(milliseconds.length() == 2){
            milliseconds = "0" + milliseconds;
        }
        else if(milliseconds.length() == 1){
            milliseconds = "00" + milliseconds;
        }
        else if(milliseconds.length() > 3){
            milliseconds =  milliseconds.substring(milliseconds.length()-3);
        }

        time.setText(hours + ":" + minutes + ":" + seconds + "." + milliseconds);
    }

    public void handleTimer(View v){
        if(start_timer.getText().equals("Start Timer")){
            startTime = System.currentTimeMillis();
            mHandler.removeCallbacks(startTimer);
            mHandler.postDelayed(startTimer, 0);
            start_timer.setText("Stop Timer");
            start_timer.setBackgroundColor(Color.YELLOW);
            adl_start.setEnabled(false);
            adl_start.setAlpha(0.5f);
        }
        else if(start_timer.getText().equals("Stop Timer")){
            start_timer.setText("Save");
            mHandler.removeCallbacks(startTimer);
            start_timer.setBackgroundColor(Color.RED);
        }
        else{
            adl_code = code_name.getText().toString();
            adl_repetition = repetition.getText().toString();
            start_timer.setBackgroundColor(Color.GREEN);
            TableRow tr = new TableRow(this);
            android.widget.TableRow.LayoutParams p =new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            p.rightMargin = DisplayHelper.dpToPixel(20, this);
            TextView tv1 = new TextView(this);
            tv1.setText(adl_name);
            tv1.setLayoutParams(p);
            TextView tv2 = new TextView(this);
            if(adl_code.isEmpty()){
                adl_code= "-";
            }
            tv2.setText(adl_code);
            tv2.setLayoutParams(p);
            TextView tv3 = new TextView(this);
            if(adl_repetition.isEmpty()){
                adl_repetition = "-";
            }
            tv3.setText(adl_repetition);
            tv3.setLayoutParams(p);
            TextView tv4 = new TextView(this);
            tv4.setText(time.getText());
            tr.addView(tv1);
            tr.addView(tv2);
            tr.addView(tv3);
            tr.addView(tv4);
            past_times.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            scroll.post(new Runnable() {
                @Override
                public void run() {
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            });
            start_timer.setText("Start Timer");
            time.setText("00:00:00.000");
            start_timer.setEnabled(false);
            start_timer.setAlpha(0.5f);
            adl_start.setEnabled(true);
            adl_start.setAlpha(1f);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(this, nodeConfiguration);
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android_adl_recorder");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher(GraphName.of(recording_topic), String._TYPE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onShutdown(Node node) {}

    @Override
    public void onShutdownComplete(Node node) {}

    @Override
    public void onError(Node node, Throwable throwable) {}
}
