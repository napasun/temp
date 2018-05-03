package com.estgames.estgames_framework.user;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.estgames.estgames_framework.R;

/**
 * Created by mp on 2018. 4. 3..
 */

public class UserLinkDialog extends Dialog {
    private Dialog self;
    private Button closeBt;
    private Button confirmBt;
    private Button cancelBt;

    public Runnable confirmCallBack = new Runnable() {
        @Override
        public void run() {

        }
    };
    public Runnable cancelCallBack = new Runnable() {
        @Override
        public void run() {

        }
    };
    public Runnable closeCallBack = new Runnable() {
        @Override
        public void run() {

        }
    };

    public UserLinkDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        setContentView(R.layout.userlink);

        confirmBt = (Button) findViewById(R.id.userLinkConfirm);
        cancelBt = (Button) findViewById(R.id.userLinkCancel);
        closeBt = (Button) findViewById(R.id.userLinkCloseBt);

        confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.dismiss();
                confirmCallBack.run();
            }
        });

        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.dismiss();
                cancelCallBack.run();
            }
        });

        closeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.dismiss();
                closeCallBack.run();
            }
        });
    }
}
