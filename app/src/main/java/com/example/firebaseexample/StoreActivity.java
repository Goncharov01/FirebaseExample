package com.example.firebaseexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StoreActivity extends AppCompatActivity {


    private static final String TAG = "MessageActivity";
    private static final String REQUIRED = "Required";

    private Button btnBack;
    private Button btnSend;
    private EditText edtSentText;
    private TextView tvAuthor;
    private TextView tvTime;
    private TextView tvBody;

    private FirebaseUser user;

    private DatabaseReference mDatabase;
    private DatabaseReference mMessageReference;
    private ChildEventListener mMessageListener;

    private ArrayList<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        btnSend = (Button) findViewById(R.id.btn_send);
        btnBack = (Button) findViewById(R.id.btn_back);
        edtSentText = (EditText) findViewById(R.id.edt_sent_text);
        tvAuthor = (TextView) findViewById(R.id.tv_author);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvBody = (TextView) findViewById(R.id.tv_body);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMessageReference = FirebaseDatabase.getInstance().getReference("messages");
        user = FirebaseAuth.getInstance().getCurrentUser();

        messageList = new ArrayList<>();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitMessage();
                edtSentText.setText("");
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvAuthor.setText("");
        tvTime.setText("");
        tvBody.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);

                Log.e(TAG, "onChildAdded:" + message.body);

                Message latest = messageList.get(messageList.size() - 1);

                tvAuthor.setText(latest.author);
                tvBody.setText(latest.body);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.e(TAG, "onChildChanged:" + dataSnapshot.getKey());

                Message message = dataSnapshot.getValue(Message.class);
                Toast.makeText(StoreActivity.this, "onChildChanged: " + message.body, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.e(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                Message message = dataSnapshot.getValue(Message.class);
                Toast.makeText(StoreActivity.this, "onChildRemoved: " + message.body, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.e(TAG, "onChildMoved:" + dataSnapshot.getKey());

                Message message = dataSnapshot.getValue(Message.class);
                Toast.makeText(StoreActivity.this, "onChildMoved: " + message.body, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "postMessages:onCancelled", databaseError.toException());
                Toast.makeText(StoreActivity.this, "Failed to load Message.", Toast.LENGTH_SHORT).show();
            }
        };

        mMessageReference.addChildEventListener(childEventListener);


        mMessageListener = childEventListener;
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (mMessageListener != null) {
            mMessageReference.removeEventListener(mMessageListener);
        }

        for (Message message : messageList) {
            Log.e(TAG, "listItem: " + message.body);
        }
    }

    private void submitMessage() {
        final String body = edtSentText.getText().toString();

        if (TextUtils.isEmpty(body)) {
            edtSentText.setError(REQUIRED);
            return;
        }

        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user == null) {
                    Log.e(TAG, "onDataChange: User data is null!");
                    Toast.makeText(StoreActivity.this, "onDataChange: User data is null!", Toast.LENGTH_SHORT).show();
                    return;
                }

                writeNewMessage(body);
            }

            @Override
            public void onCancelled(DatabaseError error) {

                Log.e(TAG, "onCancelled: Failed to read user!");
            }
        });
    }

    private void writeNewMessage(String body) {

        Message message = new Message(getUsernameFromEmail(user.getEmail()), body);

        Map<String, Object> messageValues = message.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        String key = mDatabase.child("messages").push().getKey();

        childUpdates.put("/messages/" + key, messageValues);
        childUpdates.put("/user-messages/" + user.getUid() + "/" + key, messageValues);

        mDatabase.updateChildren(childUpdates);
    }

    private String getUsernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

}
