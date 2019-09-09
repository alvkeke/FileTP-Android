package com.alvkeke.tools.filetp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alvkeke.tools.filetp.listAdapter.CredibleListAdapter;

import java.util.HashSet;
import java.util.Set;

public class CredibleListActivity extends AppCompatActivity {

    private ListView mList;
    private CredibleListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credible_list);

        mList = findViewById(R.id.lv_credible_list);


        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CredibleListActivity.this);
                builder.setTitle("确定要删除：")
                        .setMessage(mAdapter.getItem(position))
                        .setNegativeButton(R.string.string_cancel, null)
                        .setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAdapter.delUser(mAdapter.getItem(position));
                                mAdapter.notifyDataSetChanged();
                            }
                        }).create().show();
                return true;
            }
        });

        SharedPreferences conf = getSharedPreferences(MainActivity.CONF_NAME, MODE_PRIVATE);
        Set<String> list = conf.getStringSet(MainActivity.CONF_KEY_CREDIBLE_USERS,
                new HashSet<String>());

        mAdapter = new CredibleListAdapter(this, list);

        mList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.credible_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_credible_add:
                final EditText et = new EditText(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("请输入需要添加的设备名称")
                        .setView(et)
                        .setNegativeButton(R.string.string_cancel, null)
                        .setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String s = et.getEditableText().toString();
                                mAdapter.addUser(s);
                                mAdapter.notifyDataSetChanged();
                            }
                        }).create().show();
                break;
            case R.id.menu_credible_cancel:
                finish();
                break;
            case R.id.menu_credible_clear:
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_credible_save:
                SharedPreferences conf = getSharedPreferences(MainActivity.CONF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = conf.edit();
                editor.remove(MainActivity.CONF_KEY_CREDIBLE_USERS);
                editor.apply();
                editor.putStringSet(MainActivity.CONF_KEY_CREDIBLE_USERS, mAdapter.getList());
                editor.apply();

                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
