package com.jwlks.healthble.play;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.jwlks.healthble.R;
import com.jwlks.healthble.util.Dlog;

import static com.jwlks.healthble.main.NavigationActivity.mBluetoothLeService;
import static com.jwlks.healthble.main.NavigationActivity.mWriteCharacteristic;

public class PlayExerciseMain extends AppCompatActivity {
    ProgressBar readyProgressBar, playProgressBar, restProgressBar;
    Button startButton;

    InputMethodManager inputMethodManager;
    EditText readyTimeOutMin, readyTimeOutSec;
    EditText playTimeOutMin, playTimeOutSec;
    EditText restTimeOutMin, restTimeOutSec;

    final static int INIT = 0;
    final static int RUN = 1;
    final static int PAUSE = 2;
    int cur_status = INIT;
    long readyBaseTime;
    long playBaseTime;
    long restBaseTime;

    long readySetTime;
    long playSetTime;
    long restSetTime;

    String readyTime;
    Handler readyHandler;
    String playTime;
    Handler playHandler;
    String restTime;
    Handler restHandler;

    boolean actionTrigger = false;
    boolean finishTrigger = true;
    boolean readyTrigger = false;
    boolean playTrigger = false;
    boolean restTrigger = false;


    Drawable imagePlay;
    Drawable imagePause;


    byte[] readyData = {(byte)0xF1, 0x06, 0x00, 0x01, (byte)0x0F2};
    byte[] playData = {(byte)0xF1, 0x06, 0x00, 0x02, (byte)0x0F2};
    byte[] restData = {(byte)0xF1, 0x06, 0x00, 0x03, (byte)0x0F2};
    byte[] stopData = {(byte)0xF1, 0x06, 0x00, 0x30, (byte)0x0F2};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_exercise_main_sub);
        imagePlay = getBaseContext().getResources().getDrawable( R.drawable.ic_baseline_play_arrow_48_white, null);
        imagePause = getBaseContext().getResources().getDrawable( R.drawable.ic_baseline_pause_48_white, null );
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        readyProgressBar = findViewById(R.id.progress_bar_ready);
        playProgressBar = findViewById(R.id.progress_bar_play);
        restProgressBar = findViewById(R.id.progress_bar_rest);

        readyTimeOutMin = findViewById(R.id.ready_time_out_min);
        readyTimeOutSec = findViewById(R.id.ready_time_out_sec);
        playTimeOutMin = findViewById(R.id.play_time_out_min);
        playTimeOutSec = findViewById(R.id.play_time_out_sec);
        restTimeOutMin = findViewById(R.id.rest_time_out_min);
        restTimeOutSec = findViewById(R.id.rest_time_out_sec);

        readyChangeEvent();
        playChangeEvent();
        restChangeEvent();

        readyTimeHandler();
        playTimeHandler();
        restTimeHandler();
        setOnclickEvent();
    }

    @SuppressLint("HandlerLeak")
    public void readyTimeHandler() {
         readyHandler = new Handler() {
            @Override
            public void handleMessage(Message message){
                readyTime = getTimeOut(readyBaseTime, readySetTime, readyProgressBar, readyTimeOutMin, readyTimeOutSec);

                if(readyTime.equals("00:00")){
                    reset(readyHandler, readySetTime, readyProgressBar, readyTimeOutMin, readyTimeOutSec);
                    readyTrigger = false;

                    mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, playData);
                    //NEXT
                    playBaseTime = SystemClock.elapsedRealtime();
                    playHandler.sendEmptyMessage(0);
                } else {
                    readyHandler.sendEmptyMessage(0);
                    readyTrigger = true;

                }
            }
        };
    }

    @SuppressLint("HandlerLeak")
    public void playTimeHandler() {
        playHandler = new Handler() {
            @Override
            public void handleMessage(Message message){
                playTime = getTimeOut(playBaseTime, playSetTime, playProgressBar, playTimeOutMin, playTimeOutSec);

                if(playTime.equals("00:00")){
                    reset(playHandler, playSetTime, playProgressBar, playTimeOutMin, playTimeOutSec);
                    playTrigger = false;

                    mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, restData);
                    //NEXT
                    restBaseTime = SystemClock.elapsedRealtime();
                    restHandler.sendEmptyMessage(0);

                } else {
                    playHandler.sendEmptyMessage(0);
                    playTrigger = true;
                }
            }
        };
    }

    @SuppressLint("HandlerLeak")
    public void restTimeHandler() {
        restHandler = new Handler() {
            @Override
            public void handleMessage(Message message){
                restTime = getTimeOut(restBaseTime, restSetTime, restProgressBar, restTimeOutMin, restTimeOutSec);

                if(restTime.equals("00:00")){
                    reset(restHandler, restSetTime, restProgressBar, restTimeOutMin, restTimeOutSec);
                    restTrigger = false;
                    finishTrigger = true;
                    actionTrigger = false;
                    startButton.setText("시작하기");
                    startButton.setCompoundDrawablesWithIntrinsicBounds(imagePlay, null, null, null);

                    enableETControl(readyTimeOutMin, readyTimeOutSec, true);
                    enableETControl(playTimeOutMin, playTimeOutSec, true);
                    enableETControl(restTimeOutMin, restTimeOutSec, true);

                    mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, stopData);

                } else {
                    restHandler.sendEmptyMessage(0);
                    restTrigger = true;
                }
            }
        };
    }

    public String getTimeOut(long baseTime, long setTime, ProgressBar progressBar, EditText minET, EditText secET) {
        long now = SystemClock.elapsedRealtime();
        long outTime = baseTime - now + setTime;

        long sec = outTime/1000%60;
        long min = outTime/1000/60;

        //0.1초 단위가 남아있을때 초가 넘어가서 0.5초에도 0초로 표시 되기 때문에
        // 0.1초 단위를 계산해서 초가 60초 이하일때 0.1초 단위가 남아 있으면
        // 초가 변경되지 않도록 세팅

        if(outTime%1000/10 != 0 && sec < 60) {
            sec +=1;
            if(sec == 60) {
                sec = 0;
                min +=1;
            }
        }

        @SuppressLint("DefaultLocale")
        String easyOutTime = String.format("%02d:%02d", min, sec);
        String[] times = easyOutTime.split(":");

        minET.setText(times[0]);
        secET.setText(times[1]);

        progressBar.setProgress((int)((now-baseTime) + (setTime/1000)));

        return easyOutTime;
    }

    public void readyChangeEvent(){
        readyTimeOutMin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (readyTimeOutMin.hasFocus() && getEditTime(readyTimeOutMin, readyTimeOutSec) != 0) {
                    readySetTime = setTime(readyProgressBar, readyTimeOutMin, readyTimeOutSec);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        readyTimeOutSec.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(readyTimeOutSec.hasFocus() && getEditTime(readyTimeOutMin, readyTimeOutSec) != 0) {
                    readySetTime = setTime(readyProgressBar, readyTimeOutMin, readyTimeOutSec);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void playChangeEvent(){
        playTimeOutMin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(playTimeOutMin.hasFocus() && getEditTime(playTimeOutMin, playTimeOutSec) != 0) {
                    playSetTime = setTime(playProgressBar, playTimeOutMin, playTimeOutSec);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        playTimeOutSec.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(playTimeOutSec.hasFocus() && getEditTime(playTimeOutMin, playTimeOutSec) != 0) {
                    playSetTime = setTime(playProgressBar, playTimeOutMin, playTimeOutSec);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void restChangeEvent(){
        restTimeOutMin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(restTimeOutMin.hasFocus() && getEditTime(restTimeOutMin, restTimeOutSec) != 0) {
                    restSetTime = setTime(restProgressBar, restTimeOutMin, restTimeOutSec);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        restTimeOutSec.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(restTimeOutSec.hasFocus() && getEditTime(restTimeOutMin, restTimeOutSec) != 0) {
                    restSetTime = setTime(restProgressBar, restTimeOutMin, restTimeOutSec);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public long setTime(ProgressBar progressBar, EditText minET, EditText secET) {
        long mValue = Long.parseLong(minET.getText().toString()) * 1000 * 60 +
                Long.parseLong(secET.getText().toString()) * 1000;
        progressBar.setMax((int)mValue);

        return mValue;
    }

    public long getEditTime(EditText minET, EditText secET) {
        long min = 0;
        long sec = 0;
        String getMin = minET.getText().toString();
        String getSec = secET.getText().toString();

        if(getMin == null || getMin.equals("") || getSec == null || getSec.equals("") ){
            minET.setText("00");
            secET.setText("00");
        } else {
            min = Long.parseLong(getMin) * 60 * 1000;
            sec = Long.parseLong(getSec) * 1000;
        }
        return min + sec;
    }

    public void reset(Handler handler, long mValue, ProgressBar progressBar, EditText minET, EditText secET ){
        //핸들러 메세지 전달 종료
        handler.removeCallbacksAndMessages(null);

        //상태 변수 초기화
        cur_status = INIT;

        //long형으로 변환한 시간 초기화
        long sec = mValue/1000%60;
        long min = mValue/1000/60;
        String easyOutTime = String.format("%02d:%02d", min, sec);
        String[] times = easyOutTime.split(":");
        minET.setText(times[0]);
        secET.setText(times[1]);
        //EditText 시간 초기화
        minET.setEnabled(true);
        secET.setEnabled(true);

        progressBar.setProgress(0);
    }

    public void hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(readyTimeOutMin.getWindowToken(),0);
        inputMethodManager.hideSoftInputFromWindow(readyTimeOutSec.getWindowToken(),0);
    }

    public void enableETControl(EditText minET, EditText secET, boolean state){
        minET.setEnabled(state);
        secET.setEnabled(state);
    }

    public void setOnclickEvent(){
        startButton = findViewById(R.id.play_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(!actionTrigger){
                    actionTrigger = true;
                    startButton.setText("일시정지");
                    startButton.setCompoundDrawablesWithIntrinsicBounds(imagePause, null, null, null);
                    hideKeyboard();
                    enableETControl(readyTimeOutMin, readyTimeOutSec, false);
                    enableETControl(playTimeOutMin, playTimeOutSec, false);
                    enableETControl(restTimeOutMin, restTimeOutSec, false);
                    if(finishTrigger){
                        finishTrigger = false;
                        readyBaseTime = SystemClock.elapsedRealtime();
                        readyHandler.sendEmptyMessage(0);

                        long restTimeSum = readySetTime + playSetTime + restSetTime;
                        long sec = restTimeSum/1000%60;
                        long min = restTimeSum/1000/60;
                        long hour = restTimeSum/1000/3600;
                        String sumOutTime = String.format("%02d:%02d:%02d", hour, min, sec);
                        Dlog.d("sumOutTime : "+sumOutTime);
//                        String convertString = Long.toString(restTimeSum);
//                        byte[] convertByteData = hexStringToByte
//                        mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, convertByteData);
                        mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, readyData);
                    }

                    if(readyTrigger){
                        readyHandler.sendEmptyMessage(0);
                    }
                    if(playTrigger){
                        playHandler.sendEmptyMessage(0);
                    }
                    if(restTrigger){
                        restHandler.sendEmptyMessage(0);
                    }

                } else {
                    actionTrigger = false;
                    startButton.setText("시작하기");
                    startButton.setCompoundDrawablesWithIntrinsicBounds(imagePlay, null, null, null);

                    enableETControl(readyTimeOutMin, readyTimeOutSec, true);
                    enableETControl(playTimeOutMin, playTimeOutSec, true);
                    enableETControl(restTimeOutMin, restTimeOutSec, true);

                    if(readyTrigger){
                        readyHandler.removeMessages(0);
                    }
                    if(playTrigger){
                        playHandler.removeMessages(0);
                    }
                    if(restTrigger){
                        restHandler.removeMessages(0);
                    }

                }
            }
        });
    }



//    @SuppressLint("HandlerLeak")
//    public void readyHandlerControl() {
//        progressBar = findViewById(R.id.progress_bar_ready);
//        progressBar.setMax(100);
//
//        handler = new Handler() {
//            @Override
//            public void handleMessage(Message message){
//                if(counter < 100) {
//                    counter++;
//                    progressBar.setProgress(counter);
//                    PlayExerciseMain.this.sendMessage();
//                } else {
//                    handler.removeCallbacksAndMessages(null);
//                }
//
//                if(counter == 100){
//                    handler.removeCallbacksAndMessages(null);
//                    startButton.setText("시작하기");
//                    startButton.setCompoundDrawablesWithIntrinsicBounds(imagePlay, null, null, null);
//                    actionTrigger = false;
//                }
//
//            }
//        };
//    }
//
//    public void sendMessage() {
//        Message message = new Message();
//        handler.sendMessageDelayed(message, 10);
//    }


}
