package com.example.securechat.ActivityUi;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.securechat.R;
import com.example.securechat.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView img_profile;
    TextView txt_username, txt_email;
    DatabaseReference reference;
    FirebaseUser fuser;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private FirebaseAuth.AuthStateListener authStateListener;
    private StorageTask uploadTask;
    Button btn_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.status));


        img_profile = findViewById(R.id.profile_frag_image);
        txt_username = findViewById(R.id.profile_username);
        btn_logout = findViewById(R.id.btn_logout);
        txt_email = findViewById(R.id.profile_email);
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("User").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    Log.i("Profile", "ImageURL: " + user.getImageURl());
                    if (user.getImageURl().equals("default")) {
                        img_profile.setImageResource(R.drawable.ic_user_profile);
                    } else {
                        Picasso.get()
                                .load(user.getImageURl())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.ic_user_profile)
                                .into(img_profile, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {

                                        Picasso.get().load(user.getImageURl()).placeholder(R.drawable.ic_user_profile).into(img_profile);
                                    }
                                });
                    }
                    txt_username.setText(user.getUsername());
                    txt_email.setText(user.getEmail());
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FirebaseAuth.getInstance().signOut();
                //startActivity(new Intent(getActivity(), StartActivity.class));
                alertsignout();

            }
        });

    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = ProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        Log.i("ProfileFrg", "getFileExtension: " + mimeTypeMap);
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(Uri imageUri) {
        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Uploading");
        pd.show();
        //Log.i("Profile", "uploadImage: "+imageUri);
        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = (Uri) task.getResult();
                        String mUri = downloadUri.toString();
                        reference = FirebaseDatabase.getInstance().getReference("User").child(fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURl", mUri);
                        reference.updateChildren(map);
                        pd.dismiss();
                        ;
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });

        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);


            /*if(uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(this, "Upload in progress",Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }*/
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                uploadImage(imageUri);
            }
        } else if (uploadTask != null && uploadTask.isInProgress()) {
            Toast.makeText(this, "Upload in progress", Toast.LENGTH_SHORT).show();
        } else {
        }
    }

    public void alertsignout() {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog2.setTitle("Confirm SignOut");

        // Setting Dialog Message
        alertDialog2.setMessage("Are you sure you want to Signout?");


        // Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(ProfileActivity.this, StartActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });
        alertDialog2.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Denied", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog2.show();
    }

}
