package com.demo.tts;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.sql.Array;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService exec = Executors.newCachedThreadPool();
    private ProgressDialog dialog;
    private boolean isCopy;
    private boolean isInit;
    private View copyView;
    private View initView;
    private View startView;
    private EditText editText;

    private TTSManager ttsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);


        File file = new File(getFilesDir().getAbsoluteFile()+"/01");
        isCopy = file.exists();

        copyView = findViewById(R.id.copy);
        initView = findViewById(R.id.init);
        startView = findViewById(R.id.start);
        editText = findViewById(R.id.edit);

        enable();

        ttsManager = new TTSManager();

        copyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCopy) return;
                dialog.show();
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileUtils.copyAssets(getAssets(), "01", getFilesDir().getAbsoluteFile());
                            isCopy = true;
                        }catch (Exception e){
                            isCopy = false;
                            e.printStackTrace();
                        }finally {
                            enable();
                        }
                    }
                });
            }
        });

        initView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInit) return;
                dialog.show();
                ttsManager.run(MainActivity.this, "01", exec, new TTSManager.Callback() {
                    @Override
                    public void init(boolean success) {
                        isInit = success;
                        enable();
                    }
                });
            }
        });

        startView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if (TextUtils.isEmpty(text)) return;
                ttsManager.push(Arrays.asList(new SpeechText[]{new SpeechText("", text)}));
            }
        });
    }

    private void enable(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                initView.setEnabled(isCopy);
                startView.setEnabled(isCopy && isInit);
            }
        });
    }
}