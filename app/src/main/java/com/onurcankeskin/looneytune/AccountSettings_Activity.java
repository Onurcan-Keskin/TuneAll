package com.onurcankeskin.looneytune;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.onurcankeskin.looneytune.DesignUtils.AlertDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import uk.co.senab.photoview.PhotoViewAttacher;

@SuppressWarnings("all")
public class AccountSettings_Activity extends AppCompatActivity {

    private DrawerLayout drawer;
    private NavigationView nv;
    private Intent intent;
    private View nav_view, account_view;

    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser icurrentUser;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private StorageTask mUpload;

    private String uid;
    private String currentlang = "en", getCurrentlang;

    private CardView cv_fbLogin, cv_emaiLogin, cv_msLogin, cv_logout,
            cv_change_name, cv_change_about, cv_see_membership, cv_signInMethod;
    private TextView navName, navMail,
            acct_mail, acct_name, acct_member, acct_about, acct_signIn;
    private ImageView acct_ImageView;
    private RelativeLayout rl_noAccount, rl_if_account;
    private LinearLayout ll_if_account;
    private LinearLayout pa;

    private CircleImageView mCircleImage;

    private ProgressDialog mProgress;
    private String email, image, thumb_image, name, about, signInMethod, membership, curr_userID;

    private static int RC_SIGN_IN = 101, GALLERY_PIC = 1;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount gAccount, gAcct;
    private SignInButton mGoogleBtn;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_NO){
            setTheme(R.style.light_theme);
        } else {
            setTheme(R.style.dark_theme);
        }

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();

        //setTheme(R.style.dark_theme);
        setContentView(R.layout.activity_account_settings);

        pa = (LinearLayout) findViewById(R.id.ll_snackuse);
        nav_view = findViewById(R.id.nav_view_inc);
        account_view = findViewById(R.id.account_if_exists_inc);
        drawer = findViewById(R.id.drawer_account);

        cv_fbLogin = nav_view.findViewById(R.id.facebook_login);
        cv_msLogin = nav_view.findViewById(R.id.microsoft_login);
        cv_emaiLogin = nav_view.findViewById(R.id.email_login);
        cv_logout = nav_view.findViewById(R.id.nav_logout);

        cv_change_name = account_view.findViewById(R.id.change_user_name_layout);
        cv_change_about = account_view.findViewById(R.id.change_about_layout);
        cv_see_membership = account_view.findViewById(R.id.change_membership_layout);
        acct_ImageView = account_view.findViewById(R.id.user_image);

        acct_name = account_view.findViewById(R.id.user_name_txt);
        acct_about = account_view.findViewById(R.id.about_txt);
        acct_member = account_view.findViewById(R.id.membership_txt);
        acct_mail = account_view.findViewById(R.id.email_txt);
        acct_signIn = account_view.findViewById(R.id.signInMethod_txt);

        rl_if_account = nav_view.findViewById(R.id.rl_if_account_signIn);
        ll_if_account = account_view.findViewById(R.id.ll_account_if_exists);

        navName = nav_view.findViewById(R.id.nav_name);
        navMail = nav_view.findViewById(R.id.nav_mail);

        mCircleImage = nav_view.findViewById(R.id.nav_circle_image);

        /* Nav View */
        findViewById(R.id.drawermenu_account_tuner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        /* Nav View - Item Navigation */
        CardView cv = nav_view.findViewById(R.id.nav_tuner);
        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(AccountSettings_Activity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        /* Language Selector */
        currentlang = getIntent().getStringExtra(currentlang);
        account_view.findViewById(R.id.lng_tr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountSettings_Activity.this.setLocale("tr");
                finish();
            }
        });

        account_view.findViewById(R.id.lng_en).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountSettings_Activity.this.setLocale("en");
                finish();
            }
        });

        /* Logout */
        cv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gSignOut();
            }
        });

        /* Switch Day-Night */
        Switch dayNight = account_view.findViewById(R.id.switch_day_night);
        if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            dayNight.setChecked(true);
        }
        dayNight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    restartApp();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    restartApp();
                }
            }
        });

        rl_noAccount = findViewById(R.id.rl_no_account);

        /* Google SignIn */
        mGoogleBtn = findViewById(R.id.google_signin);
        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GsignIn();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        /* FireBase */
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    } /* OnCreate */

    public void restartApp(){
        Intent intent = new Intent(getApplicationContext(),AccountSettings_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void gSignOut() {
        final AlertDialog dialog = new AlertDialog();
        dialog.show(getFragmentManager(), "tag");
        dialog.setDialogCreatedListener(new AlertDialog.OnDialogCreatedListener() {
            @Override
            public void onCreate() {
                dialog.getDialogContent().setVisibility(View.GONE);
                dialog.getDialogTitle().setText(getText(R.string.prompt_logout_quest));

                dialog.getPositiveButton().setText(getString(R.string.action_yes));
                dialog.getPositiveButton().setButtonVariant(getString(R.string.button_variant_danger));
                dialog.getPositiveButton().setButtonGhost(true);
                dialog.getPositiveButton().updateStyle();

                dialog.getNegativeButton().setText(getString(R.string.action_cancel));
                dialog.getNegativeButton().setButtonVariant(getString(R.string.button_variant_fade));
                dialog.getNegativeButton().setButtonGhost(true);
                dialog.getNegativeButton().updateStyle();
            }
        });

        dialog.setDialogResultListener(new AlertDialog.OnDialogResultListener() {
            @Override
            public void onPositiveResult() {

                mGoogleSignInClient.signOut();
                FirebaseAuth.getInstance().signOut();
                mAuth.signOut();
                intent = new Intent(AccountSettings_Activity.this, AccountSettings_Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            }

            @Override
            public void onNegativeResult() {

            }
        });
    }

    private void GsignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* G-Task */
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                gAccount = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(gAccount);
            } catch (ApiException e) {
                Snackbar.make(pa, getText(R.string.error_GsignIn), Snackbar.LENGTH_SHORT).show();
                Log.w("G TAG:::", "signInResult:failed code = " + e.getStatusCode());
            }
        }


        /* Image-Task */
        if(requestCode == GALLERY_PIC && resultCode == RESULT_OK
            && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(1,1).start(AccountSettings_Activity.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){

                mProgress = new ProgressDialog(AccountSettings_Activity.this);
                mProgress.setTitle(getString(R.string.prompt_uploading_image));
                mProgress.setMessage(getString(R.string.prompt_uploading_image_message));
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                Uri resultUri = result.getUri();
                File thumb_filepath = new File(resultUri.getPath());

                icurrentUser = mAuth.getCurrentUser();
                curr_userID = mAuth.getUid();
                if (icurrentUser!=null){
                    String currentUID = curr_userID;
                    mDatabaseRef = FirebaseDatabase.getInstance().getReference("user_profile").child(curr_userID);

                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    final byte[] thumb_byte = baos.toByteArray();
                    final StorageReference filepath = mStorageRef.child("user_profile").child(curr_userID).child(curr_userID +".jpg");
                    final StorageReference thumb_storagePath = mStorageRef.child("user_profile").child(currentUID).child("thumbs").child(currentUID+"_thumb.jpg");

                    if (resultUri != null){
                        filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()){
                                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String downloadURL = uri.toString();
                                            UploadTask uploadTask = thumb_storagePath.putBytes(thumb_byte);
                                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    if (task.isSuccessful()){
                                                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                final String thumb_donwloadURL = uri.toString();
                                                                mDatabaseRef.child("thumb_image").setValue(thumb_donwloadURL).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            Map<String, Object> updateMap = new HashMap<>();
                                                                            updateMap.put("image",downloadURL);
                                                                            updateMap.put("thumb_image",thumb_donwloadURL);
                                                                            mDatabaseRef.updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        mProgress.dismiss();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    } else {
                                                        Snackbar.make(pa,R.string.error_uploading, Snackbar.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    Snackbar.make(pa,R.string.error_uploading, Snackbar.LENGTH_SHORT).show();
                                    mProgress.dismiss();
                                }
                            }
                        });
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                        Exception exception = result.getError();
                        Log.e("Image Upload Error", exception.getMessage());
                    }
                }

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        mProgress = new ProgressDialog(AccountSettings_Activity.this);
        mProgress.setTitle(getText(R.string.linking_google));
        mProgress.setMessage(getText(R.string.prompt_linking_google));
        mProgress.setIcon(R.mipmap.icon);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            gAcct = GoogleSignIn.getLastSignedInAccount(AccountSettings_Activity.this);
                            FirebaseUser curr = FirebaseAuth.getInstance().getCurrentUser();
                            String gID = curr.getUid();
                            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("user_profile").child(gID);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", gAcct.getDisplayName());
                            userMap.put("email", gAcct.getEmail());
                            userMap.put("tokenID", gAcct.getIdToken());
                            userMap.put("image", String.valueOf(gAcct.getPhotoUrl()));
                            userMap.put("thumb_image", "default");
                            userMap.put("about", "Using " + getText(R.string.app_name));
                            userMap.put("membership", "Standard User");
                            userMap.put("signInMethod", "GoogleSignIn");

                            mDatabaseRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        rl_noAccount.setVisibility(View.GONE);
                                        intent = new Intent(AccountSettings_Activity.this, AccountSettings_Activity.class);
                                        startActivity(intent);
                                        mProgress.dismiss();
                                    }
                                }
                            });
                        } else {
                            Snackbar.make(pa, getText(R.string.error_auth_failed), Snackbar.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        icurrentUser = mAuth.getCurrentUser();
        curr_userID = mAuth.getUid();
        if (icurrentUser != null) {
            mDatabaseRef = FirebaseDatabase.getInstance().getReference("user_profile").child(curr_userID);
            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    about = dataSnapshot.child("about").getValue().toString();
                    email = dataSnapshot.child("email").getValue().toString();
                    image = dataSnapshot.child("image").getValue().toString();
                    membership = dataSnapshot.child("membership").getValue().toString();
                    name = dataSnapshot.child("name").getValue().toString();
                    thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                    signInMethod = dataSnapshot.child("signInMethod").getValue().toString();

                    acct_name.setText(name);
                    acct_mail.setText(email);
                    acct_about.setText(about);
                    acct_member.setText(membership);
                    acct_signIn.setText(signInMethod);

                    if (!image.equals("default")) {
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.mipmap.icon)
                                .into(acct_ImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(image).placeholder(R.mipmap.icon).into(acct_ImageView);
                                    }
                                });
                    }

                    changeNameAboutImage();

                    rl_if_account.setVisibility(View.VISIBLE);
                    cv_logout.setVisibility(View.VISIBLE);
                    account_view.setVisibility(View.VISIBLE);

                    if (signInMethod.equals("GoogleSignIn")) {
                        updateUI_Google();
                    } else if (signInMethod.equals("FacebookSignIn")) {
                        updateUI_Facebook();
                    } else if (signInMethod.equals("MicrosoftSignIn")) {
                        updateUI_Microsoft();
                    } else if (signInMethod.equals("DefaultSignIn")) {
                        updateUI_Default();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            rl_noAccount.setVisibility(View.VISIBLE);
            account_view.setVisibility(View.GONE);
            cv_logout.setVisibility(View.GONE);
        }
    }

    public void setLocale(String localeName) {
        if (!localeName.equals(currentlang)) {
            Locale mylocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration config = res.getConfiguration();
            config.locale = mylocale;
            res.updateConfiguration(config, dm);
            Intent refresh = new Intent(this, AccountSettings_Activity.class);
            refresh.putExtra(currentlang, localeName);
            startActivity(refresh);
            finish();


        } else {
            Toast.makeText(AccountSettings_Activity.this, R.string.error_lang_selected, Toast.LENGTH_SHORT).show();
        }
    }

    private void changeNameAboutImage() {

        /* Change Name */
        cv_change_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(AccountSettings_Activity.this
                        ,R.style.DialogBlurTheme);
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setLayout(width, height);
                dialog.setContentView(R.layout.dialog_edit);
                dialog.getWindow().setGravity(Gravity.CENTER);
                Button save = dialog.findViewById(R.id.edt_dialog_positive);
                Button cancel = dialog.findViewById(R.id.edt_dialog_negative);
                final RelativeLayout d_pa = dialog.findViewById(R.id.dialog_pa);
                final EditText edt_name = dialog.findViewById(R.id.edt_dialog_box);
                edt_name.setBackgroundColor(R.attr.backgroundcolor);
                edt_name.setHint(R.string.name);
                edt_name.setText(acct_name.getText());
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edt_name.length()!=0) {
                            mDatabaseRef.child("name").setValue(edt_name.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Snackbar.make(pa, R.string.prompt_save_success, Snackbar.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Snackbar.make(pa, R.string.error_save, Snackbar.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            Snackbar.make(d_pa, R.string.error_empty_field, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        /* Change About */
        cv_change_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(AccountSettings_Activity.this
                        ,R.style.DialogBlurTheme);
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setLayout(width, height);
                dialog.setContentView(R.layout.dialog_edit);
                dialog.getWindow().setGravity(Gravity.CENTER);
                Button save = dialog.findViewById(R.id.edt_dialog_positive);
                Button cancel = dialog.findViewById(R.id.edt_dialog_negative);
                final RelativeLayout d_pa = dialog.findViewById(R.id.dialog_pa);
                final EditText edt_about = dialog.findViewById(R.id.edt_dialog_box);
                edt_about.setBackgroundColor(R.attr.backgroundcolor);
                edt_about.setHint(R.string.about);
                edt_about.setText(acct_about.getText());
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edt_about.length()!=0){
                            mDatabaseRef.child("about").setValue(edt_about.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Snackbar.make(pa, R.string.prompt_save_success, Snackbar.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Snackbar.make(pa, R.string.error_save, Snackbar.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            Snackbar.make(d_pa, R.string.error_empty_field, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        /* Zoom */
        acct_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(AccountSettings_Activity.this,
                        android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
                dialog.setContentView(R.layout.dialog_zoom);
                final ImageView imageView = dialog.findViewById(R.id.zoom_dialog_imageView);
                ImageButton zoomCancel = dialog.findViewById(R.id.zoom_dialog_cancel);
                mDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Picasso.get().load(image).placeholder(R.mipmap.icon).into(imageView);
                        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                zoomCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        /* ChangeImage */
        acct_ImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AccountSettings_Activity.this);
                return true;
            }
        });
    }

    private void updateUI_Google() {
        gAcct = GoogleSignIn.getLastSignedInAccount(AccountSettings_Activity.this);
        rl_noAccount.setVisibility(View.GONE);

        navName.setText(name);
        navMail.setText(gAcct.getEmail());
        final String img = String.valueOf(gAcct.getPhotoUrl());

        if (!image.equals("")) {
            Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.mipmap.icon)
                    .into(mCircleImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(img).placeholder(R.mipmap.icon).into(mCircleImage);
                        }
                    });
        }
    }

    private void updateUI_Facebook() {
    }

    private void updateUI_Microsoft() {
    }

    private void updateUI_Default() {
    }
}
