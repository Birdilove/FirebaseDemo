package com.example.firebasedemo;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Home extends AppCompatActivity {
    private static final String TAG = "Collection ";
    private static final String DIRECTORY_DOWNLOADS = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));;
    private StorageReference mStorageRef;
    Uri selectedImageUrl_1;
    private static final int STORAGE_PERMISSION_CODE = 101;
    Uri filePath;
    EditText addToDatabaseEdit, collectionData;
    DatabaseReference mDatabase;
    FirebaseStorage storage;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Button saveData = findViewById(R.id.savefirestore);
        Button getData = findViewById(R.id.getfirestore);
        Button uploadFile = findViewById(R.id.selectFile);
        Button uploadDatabase = findViewById(R.id.button2);
        Button downloadFile = findViewById(R.id.downloadFile);
        Button retrieveToken = findViewById(R.id.retrieveToken);
        Button getRealData = findViewById(R.id.getRealData);
        addToDatabaseEdit = findViewById(R.id.editText);
        collectionData = findViewById(R.id.collectionEditText);
        getRealData.setOnClickListener(v -> ReadDatabase());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("messages");
        mDatabase.setValue("Hello, World!");
        retrieveToken.setOnClickListener(v -> FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }
                    String token = task.getResult().getToken();
                    String msg = getString(R.string.msg_token_fmt, token);
                    Log.d(TAG, msg);
                    Toast.makeText(Home.this, msg, Toast.LENGTH_SHORT).show();
                }));
        saveData.setOnClickListener(v -> Home.this.Collection());
        getData.setOnClickListener(v -> Home.this.ReadData());
        uploadFile.setOnClickListener(v -> selectFileToUpload());
        uploadDatabase.setOnClickListener(v -> AddDatabaseEntry());
        downloadFile.setOnClickListener(v -> {
            try {
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                CloudStorageDownloadaFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public int checkPermission(String permission, int requestCode) {
        int i = 1;
        if (ContextCompat.checkSelfPermission(
                Home.this,
                permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            Home.this,
                            new String[]{permission},
                            requestCode);
        } else if (ContextCompat.checkSelfPermission(Home.this, permission)
                == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location Permission is denied, " +
                            "the app will not load nearby locations.",
                    Toast.LENGTH_SHORT).show();
        }
        return i;
    }

    public void Collection() {
        Map<String, Object> user = new HashMap<>();
        user.put("first", collectionData.getText().toString());
        user.put("last", "Love");
        user.put("born", 1815);
        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    Toast.makeText(Home.this, "Data added in collection.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    public void ReadData() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            stringBuilder.append(document.getData());
                        }
                        Toast.makeText(Home.this, stringBuilder, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void uploadFile() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            final StorageReference ref = mStorageRef.child("images/mountains.jpg");
            UploadTask uploadTask = ref.putFile(filePath);
            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    selectedImageUrl_1 = task.getResult();
                    progressDialog.dismiss();
                    Toast.makeText(Home.this, "File uploaded.",
                            Toast.LENGTH_SHORT).show();
                    Log.i("URL ", selectedImageUrl_1.toString());
                } else {
                }
            });
        }
    }

    public void selectFileToUpload() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            uploadFile();
        }
    }

    public void CloudStorageDownloadaFile() throws IOException {
        if (selectedImageUrl_1 == null) {
            Toast.makeText(this, "No new file uploaded, downloading default file.", Toast.LENGTH_SHORT).show();
            download(Home.this, "mountains","jpg", DIRECTORY_DOWNLOADS, "https://firebasestorage.googleapis.com/v0/b/fir-demo-c7630.appspot.com/o/storage%2Femulated%2F0%2FPictures%2FInstagram%2FIMG_20200708_200239_551.jpg?alt=media&token=e394e98b-07f3-49b6-bedd-8c4465baef24");
        } else {
            download(Home.this, "cloudFirestoreFile","jpg", DIRECTORY_DOWNLOADS, selectedImageUrl_1.toString());
        }
    }

    public void ReadDatabase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> value = dataSnapshot.getChildren();
                StringBuilder stringBuilder = new StringBuilder();
                for (DataSnapshot eachValue: value) {
                    stringBuilder.append(eachValue.toString());
                }
                Toast.makeText(Home.this, stringBuilder, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void AddDatabaseEntry() {
        String text = addToDatabaseEdit.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            String id = mDatabase.push().getKey();
            mDatabase.child(id).setValue(text);
            Toast.makeText(this, "Entry added", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Please enter a valid entry.", Toast.LENGTH_LONG).show();
        }
    }

    private void download(Context context, String fileName, String fileExtension, String destinationDirectory, String url){
        //Implementing Download Manager
        DownloadManager downloadManager = (DownloadManager)context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);
        downloadManager.enqueue(request);
    }
}