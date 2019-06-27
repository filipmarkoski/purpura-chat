package com.purpura.googlemaps2018.ui;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.UserClient;
import com.purpura.googlemaps2018.models.User;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.firebase.auth.FirebaseAuth.getInstance;

public class ProfileEditActivity extends AppCompatActivity implements
        View.OnClickListener,
        IProfile {

    private static final String TAG = "ProfileEditActivity";


    //widgets
    private CircleImageView mAvatarImage;

    //vars
    private ImageListFragment mImageListFragment;

    EditText username;
    EditText fName;
    EditText lName;
    EditText email;
    EditText age;
    EditText bio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mAvatarImage = findViewById(R.id.image_choose_avatar);

        //This is used to select a new avatar
        findViewById(R.id.image_choose_avatar).setOnClickListener(this);
        findViewById(R.id.text_choose_avatar).setOnClickListener(this);

        //SAVE AND CANCEL BUTTONS LISTENERS
        findViewById(R.id.saveProfile_button).setOnClickListener(this);
        findViewById(R.id.cancelProfile_button).setOnClickListener(this);

        //fields
        username=(EditText)findViewById(R.id.userName_editText);
        fName=(EditText)findViewById(R.id.firstName_editText);
        lName=(EditText)findViewById(R.id.lastName_editText);
        email=(EditText)findViewById(R.id.email_editText);
        age=(EditText)findViewById(R.id.age_editText);
        bio=(EditText)findViewById(R.id.bio_editText);

        retrieveProfileImage();
        initialiseFields();
    }

    private void retrieveProfileImage(){
        RequestOptions requestOptions = new RequestOptions()
                .error(R.drawable.cwm_logo)
                .placeholder(R.drawable.cwm_logo);

        int avatar = 0;
        try{
            avatar = Integer.parseInt(((UserClient)getApplicationContext()).getUser().getAvatar());
        }catch (NumberFormatException e){
            Log.e(TAG, "retrieveProfileImage: no avatar image. Setting default. " + e.getMessage() );
        }

        Glide.with(ProfileEditActivity.this)
                .setDefaultRequestOptions(requestOptions)
                .load(avatar)
                .into(mAvatarImage);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_choose_avatar:
            case R.id.text_choose_avatar:
                mImageListFragment = new ImageListFragment();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up)
                        .replace(R.id.fragment_container, mImageListFragment, getString(R.string.fragment_image_list))
                        .commit();
                break;
            case R.id.cancelProfile_button:
                finish();
                break;
            case R.id.saveProfile_button:
                okClicked();
                finish();
                break;
        }

    }

    private void initialiseFields(){
        User user=getCurrentUser();

        if(user != null){
            //username
            username.setText(user.getUsername());

            //firstName
            if(user.getFirstName() == null){
                fName.setText("");
            }
            else{
                fName.setText(user.getFirstName());
            }

            //lastName
            if(user.getLastName() == null){
                lName.setText("");
            }
            else{
                lName.setText(user.getLastName());
            }

            //email
            email.setText(user.getEmail());

            //age
            if(user.getAge()>0 && user.getAge()<130){
                age.setText(user.getAge().toString());
            }
            else{
                age.setText("");
            }

            //bio
            if(user.getBiography()==null){
                bio.setText("");
            }
            else{
                bio.setText(user.getBiography());
            }
        }
    }
    private void okClicked(){
        User user=getCurrentUser();
        String usnm=username.getText().toString();
        String fname=fName.getText().toString();
        String lname=lName.getText().toString();
        String ageet=age.getText().toString();
        String bioet=bio.getText().toString();

        if(usnm==null || usnm.length()==0){

        }
        else{
            user.setUsername(usnm);
        }

        if(fname!=null){
            user.setFirstName(fname);
        }
        if(fname.length()==0){
            user.setFirstName("");
        }

        if(lname!=null){
            user.setLastName(lname);
        }
        if(lname.length()==0){
            user.setLastName("");
        }

        if(age == null || age.length() == 0){
            //user.setAge(-5);
        }else{
            int i=-4;
            try {
                i=Integer.parseInt(ageet);
                if(i>0 && i<130)
                    user.setAge(i);

            } catch (NumberFormatException e) {

            }
        }

        if(bioet.length()==0 || bioet==null){
            user.setBiography(null);
        }
        else{
            user.setBiography(bioet);
        }
        ((UserClient) (getApplicationContext())).setUser(user);
        saveUser(user);

    }
    private User getCurrentUser() {
        return ((UserClient) (getApplicationContext())).getUser();
    }

    private void saveUser(User user) {
        if (user != null) {
            DocumentReference userRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_users))
                    .document(getInstance().getUid());

            userRef.set(user)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "saveCurrentUser: user saved in db");

                            }
                        }
                    });
        }
    }
    @Override
    public void onImageSelected(int resource) {

        // remove the image selector fragment
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up)
                .remove(mImageListFragment)
                .commit();

        // display the image
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.cwm_logo)
                .error(R.drawable.cwm_logo);

        Glide.with(this)
                .setDefaultRequestOptions(requestOptions)
                .load(resource)
                .into(mAvatarImage);

        // update the client and database
        User user = ((UserClient)getApplicationContext()).getUser();
        user.setAvatar(String.valueOf(resource));

        FirebaseFirestore.getInstance()
                .collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid())
                .set(user);
    }
}
