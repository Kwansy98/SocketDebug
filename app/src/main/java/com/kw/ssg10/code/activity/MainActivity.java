package com.kw.ssg10.code.activity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.kw.ssg10.R;
import com.kw.ssg10.code.utils.IpUtils;
import com.kw.ssg10.code.utils.SessionConfig;
import com.kw.ssg10.code.utils.SessionManager;
import com.kw.ssg10.code.adapter.SessionsAdapter;

import java.io.File;

// 主界面，负责加载会话列表，添加删除会话

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "kwdebug";

    RecyclerView recyclerView; // 会话列表
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_main); // 主界面菜单栏
        setSupportActionBar(toolbar);

        SessionManager.initSessionManager(this); // 加载会话列表
        recyclerView = findViewById(R.id.rv_sessions);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // 设置列表适配器，定义3种点击行为和1种长按行为
        recyclerView.setAdapter(new SessionsAdapter(SessionManager.getSessions(), new SessionsAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos) {
                //Log.d(TAG, "点击了root，即将连接会话...");
                SessionManager.moveToTop(pos);

                Intent intent = new Intent(MainActivity.this, SessionActivity.class);
                intent.putExtra("pos", 0);
                startActivity(intent);
            }
        }, new SessionsAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos) {
//                Log.d(TAG, "onClick: 点击了share");
                try {
                    String export = SessionManager.get(pos).toString();
                    Log.d(TAG, "onClick: 导出：" + export);
                    Toast.makeText(MainActivity.this, "已导出到粘贴板", Toast.LENGTH_SHORT).show();
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("export", export);
                    clipboardManager.setPrimaryClip(clipData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new SessionsAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos) {
                Intent intent = new Intent(MainActivity.this, SessionEditActivity.class);
                SessionConfig config = SessionManager.get(pos);
                SessionManager.remove(pos);
                intent.putExtra("config", config);
                startActivityForResult(intent, 101);
            }
        }, new SessionsAdapter.OnItemLongClickListener() {
            @Override
            public void onLongClick(final int pos, View v) {
                // 弹出菜单
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_session_longclick, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_session_longclick:
//                                Log.d(TAG, "onMenuItemClick: 点击删除菜单");
                                SessionManager.remove(pos);
                                onResume();
                                return true;
                        }
                        return false;
                    }
                });
            }
        }));

        showWelcomeDialog();

        // 动态权限获取，申请了写权限，自动就有读权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    // 申请IO权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 创建目录
                    File sdCard = Environment.getExternalStorageDirectory();
                    File path = new File(sdCard.getPath() + File.separator + "SocketSG"); // 在sd卡根目录创建了一个 SocketSG 目录
                    if (!path.isDirectory())  {
                        boolean b = path.mkdir();
                        if (!b) Log.d(TAG, "onCreate: failed mkdir");
                        else Log.d(TAG, "onCreate: successfully mkdir");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "获取文件读写权限失败，上传下载功能不可用", Toast.LENGTH_LONG).show();
                }
        }
    }

    // 创建菜单回调
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return true;
    }

    // 菜单点击处理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add:
                SessionConfig config = null; // SessionEditActivity 根据null得知这是新建配置，否则就是编辑已有配置
                Intent intent = new Intent(MainActivity.this, SessionEditActivity.class);
                intent.putExtra("config", config);
                startActivityForResult(intent, 101);
                break;
            case R.id.item_import_clipboard:
                // 获取粘贴板字符串
                try {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (!clipboardManager.hasPrimaryClip()) break;
                    ClipData clipData = clipboardManager.getPrimaryClip();
                    ClipDescription clipDescription = clipboardManager.getPrimaryClipDescription();
                    String importJson = clipData.getItemAt(0).getText().toString();
                    //Log.d(TAG, "onOptionsItemSelected: 粘贴板字符串：" + importJson);
                    SessionConfig newConfig = new SessionConfig();
                    boolean isParseSuccessful = newConfig.fromString(importJson);
                    if (isParseSuccessful) {
                        SessionManager.add(newConfig);
                        Toast.makeText(MainActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                        onResume();
                    } else {
                        Toast.makeText(MainActivity.this, "导入失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "导入失败", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.item_get_local_ip:
                Toast.makeText(this, "" + IpUtils.getIPAddress(this), Toast.LENGTH_LONG).show(); // 获取ip
                break;
            case R.id.item_dev_log:
                showWelcomeDialog();
                break;
            case R.id.item_about:
                Toast.makeText(this, "点击了关于", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

    // 子活动调用完成回调，此处唯一目的是对返回的config进行处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 101) {
            try {
                SessionConfig config = (SessionConfig) data.getSerializableExtra("config");
                if (config != null) {
                    SessionManager.add(config); // 当且仅当config非空，添加到列表
                }
                onResume();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 列表刷新
    @Override
    protected void onResume() {
        recyclerView.getAdapter().notifyDataSetChanged();
        super.onResume();
    }

    // 显示欢迎对话框
    private void showWelcomeDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("准备实现的功能");
        String []msgs = new String[100];
        msgs[0] = "长按配置多选\n";
        msgs[1] = "单击文件item浏览完整内容\n";
        msgs[2] = "长按文本复制\n";
        msgs[3] = "发送文件模板\n";

        String msg = "";
        for (int i = 0; i < 100; i++) if (msgs[i] != null) msg += (i+1 + "." + msgs[i]);
        dialog.setMessage(msg);

        dialog.setCancelable(true);
        dialog.setNegativeButton("好", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
