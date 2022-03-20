package com.example.proyectomensajeriaprogra;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton SendMessageButton, SendFilesButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;
    private FirebaseAuth mAuth;
    private DataSnapshot dataSnapshot;
    private DatabaseReference UserRef, GroupNameRef, GroupMessageKeyRef;
    private String CurrentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private String checker = "", myurl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;
    private String messageKey;
    private DatabaseReference RootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        CurrentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, CurrentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(CurrentGroupName);

        InitializeFields();

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoToDatabase();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence option[] = new CharSequence[]
                        {
                                "Images",
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Seleccionar foto");

                builder.setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "seleccionar imagen"), 438);

                        }

                    }
                });
                builder.show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()) {
            String chatdate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();


            displayTextMessage.append(chatName + " :\n" + chatMessage + "\n " + chatTime + "    " + chatdate + "\n\n\n");

            //mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }
    }

    private void GetUserInfo() {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(CurrentGroupName);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessage = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
        loadingBar = new ProgressDialog(this);


    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            loadingBar.setTitle("imagen enviada");
            loadingBar.setMessage("Espera un momento, por favor!!");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            fileUri = data.getData();
/*
            if (!checker.equals("image")) {

            } else*/
            ////////////////////////////////////////777
            if (checker.equals("image")) {
                fileUri = data.getData();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image FileGroup");


                final String MessageKey = GroupNameRef.push().getKey();
                StorageReference filePath = storageReference.child(MessageKey + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }

                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri dowloadUrl = task.getResult();
                            myurl = dowloadUrl.toString();
                            Calendar calForDara = Calendar.getInstance();
                            SimpleDateFormat CurrentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                            currentDate = CurrentDateFormat.format(calForDara.getTime());
                    //        String retrieveProfileImage = snapshot.child("image").getValue().toString();

                      //      Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                            Calendar calForTime = Calendar.getInstance();
                            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                            currentTime = currentTimeFormat.format(calForTime.getTime());

                            HashMap<String, Object> groupMessageKey = new HashMap<>();
                            GroupNameRef.updateChildren(groupMessageKey);

                            GroupMessageKeyRef = GroupNameRef.child(MessageKey);

                            HashMap<String, Object> messageInfoMap = new HashMap<>();
                            messageInfoMap.put("name", fileUri.getLastPathSegment());
                            messageInfoMap.put("message", myurl);
                            messageInfoMap.put("date", currentDate);
                            messageInfoMap.put("time", currentTime);

                            GroupMessageKeyRef.updateChildren(messageInfoMap);

                            finish();
                        }
                    }
                });

            } else {
                loadingBar.dismiss();
                Toast.makeText(this, "Imagen No seleccionada, Error!!", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String MessageKey = GroupNameRef.push().getKey();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "please write message first", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calForDara = Calendar.getInstance();
            SimpleDateFormat CurrentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = CurrentDateFormat.format(calForDara.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(MessageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);

            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }
}