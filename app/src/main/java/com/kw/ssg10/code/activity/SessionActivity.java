package com.kw.ssg10.code.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kw.ssg10.R;
import com.kw.ssg10.code.session.TcpServerSession;
import com.kw.ssg10.code.session.UdpClientSession;
import com.kw.ssg10.code.utils.SessionConfig;
import com.kw.ssg10.code.utils.SessionManager;
import com.kw.ssg10.code.adapter.ItemAdapter;
import com.kw.ssg10.code.session.Session;
import com.kw.ssg10.code.session.TcpClientSession;
import com.kw.ssg10.code.session.task.LogTask;
import com.kw.ssg10.code.session.task.SendStringTask;
import com.kw.ssg10.code.session.task.SessionTask;
import com.kw.ssg10.code.session.task.UploadFileTask;


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;


public class SessionActivity extends AppCompatActivity {
    private static final String TAG = "kwdebug";

    private int pos = 0;
    private SessionConfig config;

    public EditText editSend;
    public ImageView imageViewUpload;
    Button btnSend;
    public ArrayList<SessionTask> tasks = new ArrayList<>();
    public RecyclerView rvOutput;
    private FloatingActionButton fabBottom, fabTop;
    private Menu menu;


    // 消息处理类
    public class UpdateUIHandler extends Handler {
        public static final int MSG_UPDATE_OUTPUT = 0; // 更新输出列表
        public static final int MSG_ONESTABLISH = 1; // 连接建立
        public static final int MSG_ONCLOSE = 2; // 连接关闭
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_OUTPUT:
                    updateOutput();
                    break;
                case MSG_ONESTABLISH:
                    if (menu.size() != 0) {
                        menu.getItem(0).setIcon(R.drawable.ic_power_green_24dp);
                    }
                    break;
                case MSG_ONCLOSE:
                    if (menu.size() != 0) {
                        menu.getItem(0).setIcon(R.drawable.ic_power_white_24dp);
                    }
                    break;
            }
        }
    }
    private UpdateUIHandler handler = new UpdateUIHandler();

    private Session session; // 会话类
    private boolean autoScrollToBottom = true; // 自动滑动到底部


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        Intent intent = getIntent();
        pos = intent.getIntExtra("pos", 0);
        config = SessionManager.get(pos);

        Toolbar toolbar = findViewById(R.id.toolbar_session);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (config.getCSMode() == 0) {
            getSupportActionBar().setTitle(SessionManager.get(pos).getServerHost() + ":" + SessionManager.get(pos).getServerPort());
        } else {
            getSupportActionBar().setTitle(SessionManager.get(pos).getListenHost() + ":" + SessionManager.get(pos).getListenPort());
        }

        menu = toolbar.getMenu();


        if (config.getCSMode() == 0 && config.getProtocol() == 0) session = new TcpClientSession(handler, config, tasks);
        else if (config.getCSMode() == 0 && config.getProtocol() == 1) session = new UdpClientSession(handler, config, tasks);
        else if (config.getCSMode() == 1 && config.getProtocol() == 0) session = new TcpServerSession(handler, config, tasks);
//        else if (config.getCSMode() == 1 && config.getProtocol() == 1) session = new UdpServerSession();


        editSend = findViewById(R.id.edit_send);
        imageViewUpload = findViewById(R.id.iv_upload);
        btnSend = findViewById(R.id.btn_send);

        rvOutput = findViewById(R.id.rv_output);
        rvOutput.setLayoutManager(new LinearLayoutManager(this));
        rvOutput.setAdapter(new ItemAdapter(tasks));

        fabBottom = findViewById(R.id.fab_scroll_to_bottom);
        fabTop = findViewById(R.id.fab_scroll_to_top);
        fabBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoScrollToBottom = true;
                rvOutput.scrollToPosition(tasks.size() - 1); // 瞬移到最后一项
                fabBottom.hide();
                fabTop.hide();
            }
        });
        fabTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoScrollToBottom = false;
                rvOutput.scrollToPosition(0); // 瞬移到第一项
                fabBottom.hide();
                fabTop.hide();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String strSend = editSend.getText().toString();
                if (strSend.isEmpty()) return;
                editSend.setText("");
                try {
                    SessionTask task = new SendStringTask(strSend, config.getEncodeSend(), config.getEOL());
                    session.postTask(task);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    log(e.getMessage());
                }

                rvOutput.scrollToPosition(tasks.size() - 1);
            }
        });

        imageViewUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File sdCard = Environment.getExternalStorageDirectory();
                File path = new File(sdCard.getPath() + File.separator + "SocketSG"); // 在sd卡根目录创建了一个 SocketSG 目录
                if (!path.isDirectory()) path.mkdir();
                // 使用第三方文件选择库 ExFilePicker
                ExFilePicker exFilePicker = new ExFilePicker();
                exFilePicker.setNewFolderButtonDisabled(true); // 禁用新建目录
                exFilePicker.setStartDirectory(path.getPath()); // 默认路径
                exFilePicker.setQuitButtonEnabled(true); // 退出按钮
                exFilePicker.setUseFirstItemAsUpEnabled(true); // 第一个item是返回上级

                exFilePicker.start(SessionActivity.this, 100); // 选择需要发送的文件
            }
        });



        editSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    btnSend.setVisibility(View.GONE);
                    imageViewUpload.setVisibility(View.VISIBLE);
                } else {
                    btnSend.setVisibility(View.VISIBLE);
                    imageViewUpload.setVisibility(View.GONE);
                }
            }
        });

        rvOutput.addOnScrollListener(new RecyclerView.OnScrollListener() {
            // 状态监听，目的是维护一个 boolean 标记，用户往上翻的时候不强制滚动到底部
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { // 停止状态
                    if (!rvOutput.canScrollVertically(1)) { // 到达底部
                        autoScrollToBottom = true;
                    }
                    // 静止时隐藏
                    fabBottom.hide();
                    fabTop.hide();
                }
            }

            // 上下滑监听
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy < 0) { // 当前处于上滑状态
                    autoScrollToBottom = false;
                    fabTop.show();
                    fabBottom.hide();
                } else if (dy > 0) { // 当前处于下滑状态
                    fabTop.hide();
                    fabBottom.show();
                }
            }

        });

        rvOutput.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (i3 < i7) { // bottom < oldBottom
                    rvOutput.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rvOutput.scrollToPosition(tasks.size() - 1);
                        }
                    }, 100);
                }
            }
        });

        if (session instanceof TcpClientSession) {
            if (config.isAutoConnect()) session.establish();
        } else if (session instanceof TcpServerSession) {
            if (config.isAutoListen()) session.establish();
        }

    }

    // 子活动调用完成回调，此处唯一目的是发送文件
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // 上传文件
        if (requestCode == 100) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() > 0) {
                // Here is object contains selected files names and path
                if (result.getCount() >= 0) {
                    List<String> filePaths = result.getNames();
                    //String filePath = result.getPath() + result.getNames().get(0);
                    String path = result.getPath();
                    try {
                        for (int i = 0; i < filePaths.size(); i++) {
                            String filePath = path + filePaths.get(i);
                            File file = new File(filePath);
                            SessionTask task = new UploadFileTask(file);
                            session.postTask(task);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "onActivityResult: " + e.getMessage());
                    }
                }
            }
        }
    }

    // 打印日志
    private void log(String text) {
        SessionTask task = new LogTask(text);
        session.postTask(task);
    }

    // 更新输出列表
    private void updateOutput() {
        try {
            // 刷新列表
            rvOutput.getAdapter().notifyDataSetChanged();
            if (!tasks.isEmpty() && autoScrollToBottom) rvOutput.scrollToPosition(tasks.size() - 1); // 瞬移到底部
//            if (!tasks.isEmpty() && autoScrollToBottom) rvOutput.smoothScrollToPosition(tasks.size() - 1); // 滑动到底部
        } catch (Exception e) {
            Log.d(TAG, "updateOutput: " + e.getMessage());
        }
    }

    // 设置菜单布局
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_session, menu);
        return true;
    }

    // 如果连接已经建立，且菜单还未初始化完成，就需要在这里修改图标颜色
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (session.isEstablish()) {
            menu.getItem(0).setIcon(R.drawable.ic_power_green_24dp);
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_power_white_24dp);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // 菜单点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_toggle:
                // 连接/监听
                if (session.isEstablish()) {
                    session.close();
                } else {
                    session.establish();
                }

                break;
            case R.id.item_clear:
                // 清屏
                //Toast.makeText(SessionActivity.this, "不存在的功能", Toast.LENGTH_SHORT).show();
                tasks.clear();
                onResume();
                break;
            default:
        }
        return true;
    }

    // 界面更新时自动刷新输出列表
    @Override
    protected void onResume() {
        updateOutput();
        super.onResume();
    }

    // 活动销毁
    @Override
    protected void onDestroy() {
        session.close();
        super.onDestroy();
    }

    // 下面三个函数实现了点击键盘外区域收起键盘功能
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                // 我的修改：只要满足top bottom边界都认为是edittext，为的是点击发送键不收起
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
