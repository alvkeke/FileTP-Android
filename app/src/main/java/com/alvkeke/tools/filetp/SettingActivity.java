package com.alvkeke.tools.filetp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.alvkeke.tools.filetp.MainActivity.*;

public class SettingActivity extends AppCompatActivity {

    Button btnSetDeviceName;
    Button btnSetBeginPort;
    Switch switchHideFile;
    Button btnSetThreadNumber;
    Button btnShowCredibleList;
    Button btnShowSavePath;
    TextView tvHide;
    TextView tvShow;

    private String mLocalDeviceName;
    private String mAttendDeviceName;
    private int mBeginPort;
    private String mSavePath;
    private boolean mIsShowHideFile;
    private Set<String> mCredibleUsers;
    private int mAllowThreadNumber;

    private SharedPreferences conf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        btnSetBeginPort = findViewById(R.id.btn_set_begin_port);
        btnSetDeviceName = findViewById(R.id.btn_set_local_device_name);
        btnSetThreadNumber = findViewById(R.id.btn_set_thread_number);
        switchHideFile = findViewById(R.id.switch_show_hide_file);
        btnShowCredibleList = findViewById(R.id.btn_show_credible_list);
        btnShowSavePath = findViewById(R.id.btn_show_save_path);
        tvHide = findViewById(R.id.label_switch_left);
        tvShow = findViewById(R.id.label_switch_right);

        conf = getSharedPreferences(CONF_NAME, Context.MODE_PRIVATE);

        mCredibleUsers = conf.getStringSet(CONF_KEY_CREDIBLE_USERS, new HashSet<String>());
        mLocalDeviceName = conf.getString(CONF_KEY_DEVICE_NAME, "phone");
        mAttendDeviceName = conf.getString(CONF_KEY_ATTEND_DEVICE, "alv-manjaro");
        mBeginPort = conf.getInt(CONF_KEY_BEGIN_PORT, 10000);
        mSavePath = conf.getString(CONF_KEY_SAVE_PATH, "");
        mIsShowHideFile = conf.getBoolean(CONF_KEY_SHOW_HIDE_FILE, true);
        mAllowThreadNumber = conf.getInt(CONF_KEY_ALLOW_THREAD_NUMBER, -1);

        String title = "设备名称：" + mLocalDeviceName;
        btnSetDeviceName.setText(title);
        title = "端口：" + mBeginPort;
        btnSetBeginPort.setText(title);
        if (mAllowThreadNumber>0) {
            title = "线程数" + mAttendDeviceName;
        } else {
            title = "线程数：无限制";
        }
        btnSetThreadNumber.setText(title);

        switchHideFile.setChecked(mIsShowHideFile);

        btnShowSavePath.setText(mSavePath);

        btnSetDeviceName.setOnClickListener(new btnNameClick());
        btnSetBeginPort.setOnClickListener(new btnPortClick());
        btnSetThreadNumber.setOnClickListener(new btnThreadClick());
        btnShowSavePath.setOnClickListener(new btnPathClick());
        btnShowCredibleList.setOnClickListener(new btnCredibleClick());

        tvHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchHideFile.setChecked(false);
            }
        });

        tvShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchHideFile.setChecked(true);
            }
        });
    }

    class btnNameClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            final EditText et = new EditText(SettingActivity.this);
            et.setText(mLocalDeviceName);
            et.setSelectAllOnFocus(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle("设置设备名称")
                    .setView(et)
                    .setNegativeButton(R.string.string_cancel, null)
                    .setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mLocalDeviceName = et.getText().toString();
                            String title = "设备名称：" + mLocalDeviceName;
                            btnSetDeviceName.setText(title);
                        }
                    });
            builder.create().show();
        }
    }

    class btnPortClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            final EditText et = new EditText(SettingActivity.this);
            et.setText(String.valueOf(mBeginPort));
            et.setSelectAllOnFocus(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle("设置端口")
                    .setView(et)
                    .setNegativeButton(R.string.string_cancel, null)
                    .setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                mBeginPort = Integer.parseInt(et.getText().toString());
                                String title = "端口：" + mBeginPort;
                                btnSetBeginPort.setText(title);
                            }catch (NumberFormatException e){
                                e.printStackTrace();
                            }
                        }
                    });
            builder.create().show();
        }
    }

    class btnThreadClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            final EditText et = new EditText(SettingActivity.this);
            et.setText(String.valueOf(mAllowThreadNumber));
            et.setSelectAllOnFocus(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle("设置进程数")
                    .setView(et)
                    .setNegativeButton(R.string.string_cancel, null)
                    .setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                mAllowThreadNumber = Integer.parseInt(et.getText().toString());
                                String title = "进程数：" + mAllowThreadNumber;
                                btnSetBeginPort.setText(title);
                            }catch (NumberFormatException e){
                                e.printStackTrace();
                            }
                        }
                    });
            builder.create().show();

        }
    }

    class btnPathClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle("当前文件接收保存目录")  // todo: 修改此处提示
                    .setMessage(mSavePath + "\n如需修改请在主界面修改目录到需要的目录，然后")
                    .setPositiveButton(R.string.string_ok,null);
            builder.create().show();
        }
    }

    class btnCredibleClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            ListView listView = new ListView(SettingActivity.this);
            ArrayList<String> items = new ArrayList<>(mCredibleUsers);
            listView.setAdapter(new ArrayAdapter<>(SettingActivity.this,
                    android.R.layout.simple_expandable_list_item_1, items));

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle("可信列表")  // todo: 修改此处提示
                    .setMessage("如需修改请在主界面")
                    .setView(listView)
                    .setPositiveButton(R.string.string_ok,null);
            builder.create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_setting_cancel:
                SettingActivity.this.finish();
                break;
            case R.id.menu_setting_ok:

                SharedPreferences.Editor editor = conf.edit();

                // todo: 保存设置

                editor.apply();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
