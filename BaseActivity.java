package com.google.firebase.quickstart.database;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.quickstart.database.models.User;


public class BaseActivity extends AppCompatActivity {
    public static User mUser;
    //pass between main and newpost activity
    public static String[] mPath;

    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public enum PostType {
        COMMERCIAL(1),
        SOCIAL(2),
        NEWS(3),
        INQUIRY(4);
        private int value;
        PostType(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public enum PostRange {
        STREET(0),
        DISTRICT(1),
        CITY(2),
        AREA(3);
        private int value;
        PostRange(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
}

