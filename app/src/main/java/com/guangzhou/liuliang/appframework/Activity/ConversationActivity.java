package com.guangzhou.liuliang.appframework.Activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.android.volley.RequestQueue;
import com.guangzhou.liuliang.appframework.R;

import java.util.Collection;
import java.util.Iterator;

import io.rong.imlib.MessageTag;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.TypingMessage.TypingStatus;
import io.rong.imlib.model.Conversation;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

public class ConversationActivity extends AppCompatActivity {

    boolean goToMain =false;

    RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Uri uri = getIntent().getData();
        final String title = uri.getQueryParameter("title");
        if(uri.getBooleanQueryParameter("isNotify",false)){
            goToMain = true;
        }

        final String mTargetId = uri.getQueryParameter("targetId");
        setTitle(title);

        //正在输入的状态提示
        RongIMClient.setTypingStatusListener(new RongIMClient.TypingStatusListener() {
            @Override
            public void onTypingStatusChanged(Conversation.ConversationType type, String targetId, Collection<TypingStatus> typingStatusSet) {
                //当输入状态的会话类型和targetID与当前会话一致时，才需要显示
                if (type.equals(Conversation.ConversationType.PRIVATE) && targetId.equals(mTargetId)) {
                    //count表示当前会话中正在输入的用户数量，目前只支持单聊，所以判断大于0就可以给予显示了
                    int count = typingStatusSet.size();
                    if (count > 0) {
                        Iterator iterator = typingStatusSet.iterator();
                        TypingStatus status = (TypingStatus) iterator.next();
                        String objectName = status.getTypingContentType();

                        MessageTag textTag = TextMessage.class.getAnnotation(MessageTag.class);
                        MessageTag voiceTag = VoiceMessage.class.getAnnotation(MessageTag.class);
                        //匹配对方正在输入的是文本消息还是语音消息
                        if (objectName.equals(textTag.value())) {
                            //显示“对方正在输入”
                           ConversationActivity.this.setTitle("对方正在输入...");
                        } else if (objectName.equals(voiceTag.value())) {
                            //显示"对方正在讲话"
                            ConversationActivity.this.setTitle("对方正在讲话");
                        }
                    } else {
                        //当前会话没有用户正在输入，标题栏仍显示原来标题
                        ConversationActivity.this.setTitle(title);
                    }
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && goToMain) { //按下的如果是BACK，同时没有重复
            //do something here
            Intent intent = new Intent(this,LoadingActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
