package com.kw.ssg10.code.activity;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kw.ssg10.R;
import com.kw.ssg10.code.utils.SessionConfig;

public class SessionEditActivity extends AppCompatActivity {
    private static final String TAG = "kwdebug";

    private SessionConfig config; // 是备份，进入活动时同步，离开活动时写入内存和存储
    private SessionConfig configPassing; // 用于传递的config

    // 服务器属性控件
    private RadioGroup radioGroupCS, radioGroupProtocol;
    private RadioButton rbtnClient, rbtnServer, rbtnTCP, rbtnUDP;

    // 客户端属性控件
    private CardView cardViewClient;
    private EditText editServerHost, editServerPort, editSourceHost, editSourcePort, editConnectTimeout;
    private CheckBox checkBoxAutoConnect;

    // 服务端属性控件
    private CardView cardViewServer;
    private EditText editListenHost, editListenPort;
    private CheckBox checkBoxAutoListen;


    // 通用属性控件
    private RadioGroup radioGroupIO, radioGroupEOL;
    private RadioButton rbtnRW, rbtnRO, rbtnWO, rbtnLF, rbtnCRLF, rbtnCR, rbtnNOEOF;
    private TextView tvEncodeSend, tvEncodeRecv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_edit);

        Toolbar toolbar = findViewById(R.id.toolbar_add_session);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 和按下返回键相同的处理
            }
        });

        radioGroupCS = findViewById(R.id.radiogroup_cs);
        radioGroupProtocol = findViewById(R.id.radiogroup_protocol);
        rbtnClient = findViewById(R.id.rbtn_client);
        rbtnServer = findViewById(R.id.rbtn_server);
        rbtnTCP = findViewById(R.id.rbtn_tcp);
        rbtnUDP = findViewById(R.id.rbtn_udp);

        cardViewClient = findViewById(R.id.card_client);
        editServerHost = findViewById(R.id.edit_server_host);
        editServerPort = findViewById(R.id.edit_server_port);
        editSourceHost = findViewById(R.id.edit_source_host);
        editSourcePort = findViewById(R.id.edit_source_port);
        editConnectTimeout = findViewById(R.id.edit_connect_timeout);
        checkBoxAutoConnect = findViewById(R.id.checkbox_auto_connect);

        cardViewServer = findViewById(R.id.card_server);
        editListenHost = findViewById(R.id.edit_listen_host);
        editListenPort = findViewById(R.id.edit_listen_port);
        checkBoxAutoListen = findViewById(R.id.checkbox_auto_listen);

        radioGroupIO = findViewById(R.id.radiogroup_io);
        radioGroupEOL = findViewById(R.id.radiogroup_eol);
        rbtnRW = findViewById(R.id.rbtn_rw);
        rbtnRO = findViewById(R.id.rbtn_ro);
        rbtnWO = findViewById(R.id.rbtn_wo);
        rbtnLF = findViewById(R.id.rbtn_lf);
        rbtnCRLF = findViewById(R.id.rbtn_crlf);
        rbtnCR = findViewById(R.id.rbtn_cr);
        rbtnNOEOF = findViewById(R.id.rbtn_no_eol);
        tvEncodeSend = findViewById(R.id.tv_encode_send);
        tvEncodeRecv = findViewById(R.id.tv_encode_recv);

        Intent intent = getIntent();
        configPassing = (SessionConfig) intent.getSerializableExtra("config");
        config = new SessionConfig();
        if (configPassing != null) config.copy(configPassing);
        syncUI();

        // 服务器属性控件监听事件
        rbtnClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbtnUDP.setVisibility(View.VISIBLE); // 允许选择UDP服务器
                cardViewServer.setVisibility(View.GONE);
                cardViewClient.setVisibility(View.VISIBLE);
                config.setCSMode(0);
            }
        });

        rbtnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbtnUDP.setVisibility(View.GONE); // 禁止选择UDP服务器
                rbtnTCP.setChecked(true); // 自动选上TCP协议
                cardViewClient.setVisibility(View.GONE);
                cardViewServer.setVisibility(View.VISIBLE);
                config.setCSMode(1);
            }
        });

        rbtnTCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setProtocol(0);
            }
        });

        rbtnUDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setProtocol(1);
            }
        });


        // 客户端属性控件监听事件
        editServerHost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String host = s.toString();
                if (host.isEmpty()) {
                    host = "0.0.0.0";
                }
                config.setServerHost(host);
            }
        });

        editServerPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String sPort = s.toString();
                if (sPort.isEmpty()) {
                    sPort = "2020";
                }
                config.setServerPort(Integer.parseInt(sPort));
            }
        });

        editSourceHost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String host = editable.toString();
                if (host.isEmpty()) {
                    host = "0.0.0.0";
                }
                config.setSourceHost(host);
            }
        });

        editSourcePort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String sPort = editable.toString();
                if (sPort.isEmpty()) {
                    sPort = "0";
                }
                config.setSourcePort(Integer.parseInt(sPort));
            }
        });

        editConnectTimeout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    config.setConnectTimeout(0);
                } else {
                    config.setConnectTimeout(Integer.parseInt(s.toString()));
                }
            }
        });

        checkBoxAutoConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                config.setAutoConnect(b);
            }
        });


        // 服务端属性控件监听事件
        editListenHost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String host = editable.toString();
                if (host.isEmpty()) {
                    host = "0.0.0.0";
                }
                config.setListenHost(host);
            }
        });

        editListenPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String sPort = editable.toString();
                if (sPort.isEmpty()) {
                    sPort = "2020";
                }
                config.setListenPort(Integer.parseInt(sPort));
            }
        });

        checkBoxAutoListen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                config.setAutoListen(b);
            }
        });


        // 通用属性控件监听事件

        rbtnRW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setIOMode(0);
            }
        });

        rbtnRO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setIOMode(1);
            }
        });

        rbtnWO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setIOMode(2);
            }
        });

        rbtnLF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setEOL(0);
            }
        });

        rbtnCRLF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setEOL(1);
            }
        });

        rbtnCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setEOL(2);
            }
        });

        rbtnNOEOF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setEOL(3);
            }
        });

        // 增删编码只需修改此两处
        tvEncodeSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 弹出菜单
                PopupMenu popupMenu = new PopupMenu(SessionEditActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_encode, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_encode_utf8:
                                config.setEncodeSend("UTF-8");
                                syncUI();
                                return true;
                            case R.id.item_encode_ascii:
                                config.setEncodeSend("US-ASCII");
                                syncUI();
                                return true;
                            case R.id.item_encode_iso8859:
                                config.setEncodeSend("ISO-8859-1");
                                syncUI();
                                return true;
                            case R.id.item_encode_utf16be:
                                config.setEncodeSend("UTF-16BE");
                                syncUI();
                                return true;
                            case R.id.item_encode_utf16le:
                                config.setEncodeSend("UTF-16LE");
                                syncUI();
                                return true;
                            case R.id.item_encode_utf16:
                                config.setEncodeSend("UTF-16");
                                syncUI();
                                return true;
                            case R.id.item_encode_gbk:
                                config.setEncodeSend("GBK");
                                syncUI();
                                return true;
                            case R.id.item_encode_gb2312:
                                config.setEncodeSend("GB2312");
                                syncUI();
                                return true;
                        }
                        return false;
                    }
                });
            }
        });

        // 增删编码只需修改此两处
        tvEncodeRecv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 弹出菜单
                PopupMenu popupMenu = new PopupMenu(SessionEditActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_encode, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_encode_utf8:
                                config.setEncodeRecv("UTF-8");
                                syncUI();
                                return true;
                            case R.id.item_encode_ascii:
                                config.setEncodeRecv("US-ASCII");
                                syncUI();
                                return true;
                            case R.id.item_encode_iso8859:
                                config.setEncodeRecv("ISO-8859-1");
                                syncUI();
                                return true;
                            case R.id.item_encode_utf16be:
                                config.setEncodeRecv("UTF-16BE");
                                syncUI();
                                return true;
                            case R.id.item_encode_utf16le:
                                config.setEncodeRecv("UTF-16LE");
                                syncUI();
                                return true;
                            case R.id.item_encode_utf16:
                                config.setEncodeRecv("UTF-16");
                                syncUI();
                                return true;
                            case R.id.item_encode_gbk:
                                config.setEncodeRecv("GBK");
                                syncUI();
                                return true;
                            case R.id.item_encode_gb2312:
                                config.setEncodeRecv("GB2312");
                                syncUI();
                                return true;
                        }
                        return false;
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_add_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_delete:
                configPassing = null;
                break;
            case R.id.item_check:
                configPassing = config;
                break;
            default:
        }
        setResult(0, new Intent().putExtra("config", configPassing));
        finish();
        return true;
    }

    // 根据config设置UI
    private void syncUI() {
        // 服务器属性
        switch (config.getCSMode()) {
            case 0:
                radioGroupCS.check(R.id.rbtn_client);
                cardViewClient.setVisibility(View.VISIBLE);
                cardViewServer.setVisibility(View.GONE);
                break;
            case 1:
                rbtnUDP.setVisibility(View.GONE); // 禁止选择UDP服务器
                rbtnTCP.setChecked(true); // 自动选上TCP协议
                cardViewServer.setVisibility(View.VISIBLE);
                cardViewClient.setVisibility(View.GONE);
                radioGroupCS.check(R.id.rbtn_server);
                break;
        }

        switch (config.getProtocol()) {
            case 0:
                radioGroupProtocol.check(R.id.rbtn_tcp);
                break;
            case 1:
                radioGroupProtocol.check(R.id.rbtn_udp);
                break;
        }

        // 客户端属性
        editServerHost.setText(config.getServerHost());
        editServerPort.setText("" + config.getServerPort());

        if (!config.getSourceHost().equals("0.0.0.0")) {
            editSourceHost.setText(config.getSourceHost());
        } else {
            editSourceHost.setText("");
        }

        if (config.getSourcePort() != 0) {
            editSourcePort.setText("" + config.getSourcePort());
        } else {
            editSourcePort.setText("");
        }
        checkBoxAutoConnect.setChecked(config.isAutoConnect());

        // 服务端属性
        editListenHost.setText(config.getListenHost());
        editListenPort.setText("" + config.getListenPort());
        checkBoxAutoListen.setChecked(config.isAutoListen());

        // 通用属性
        if (config.getConnectTimeout() != 0){
            editConnectTimeout.setText(""+config.getConnectTimeout());
        } else {
            editConnectTimeout.setText("");
        }


        switch (config.getIOMode()) {
            case 0:
                radioGroupIO.check(R.id.rbtn_rw);
                break;
            case 1:
                radioGroupIO.check(R.id.rbtn_ro);
                break;
            case 2:
                radioGroupIO.check(R.id.rbtn_wo);
                break;
        }

        switch (config.getEOL()) {
            case 0:
                radioGroupEOL.check(R.id.rbtn_lf);
                break;
            case 1:
                radioGroupEOL.check(R.id.rbtn_crlf);
                break;
            case 2:
                radioGroupEOL.check(R.id.rbtn_cr);
                break;
            case 3:
                radioGroupEOL.check(R.id.rbtn_no_eol);
                break;
        }

        tvEncodeSend.setText(config.getEncodeSend());
        tvEncodeRecv.setText(config.getEncodeRecv());


    }

    // 按下返回键
    @Override
    public void onBackPressed() {
        setResult(0, new Intent().putExtra("config", configPassing));
        finish();
        //super.onBackPressed();
    }

    // 下面三个函数实现了点击非键盘区域隐藏键盘功能
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
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
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
