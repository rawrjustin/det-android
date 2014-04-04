package com.jab.det;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.jab.det.DTObjects.DTFriend;

public class FriendBreakdownActivity extends Activity {

    public static final String FRIEND_FB_ID = "friend_fb_id";
    public DTFriend friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_breakdown);

        Intent intent = getIntent();
        String friendFbId = intent.getStringExtra(FRIEND_FB_ID);
        for (DTFriend friend : DetApplication.friends) {
            if (friend.getFriend().getFacebookId().equals(friendFbId)) {
                this.friend = friend;
                break;
            }
        }

        if (this.friend == null) {
            Log.wtf(DetApplication.TAG, "Friend not found");
        }
    }
}
