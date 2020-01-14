package com.onurcankeskin.looneytune;

import android.animation.Animator;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.BindingAdapter;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageTask;
import com.onurcankeskin.looneytune.DesignUtils.AlertDialog;
import com.onurcankeskin.looneytune.Interface.INoteClickToPlayEvent;
import com.onurcankeskin.looneytune.Interface.INotePlayerFinished;
import com.onurcankeskin.looneytune.Utils.SharedPreferencesHelper;
import com.onurcankeskin.looneytune.databinding.FragmentTunerBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class TunerFragment extends Fragment {

    private MainActivity activity;
    private Tuner tuner;
    private FragmentTunerBinding binding;

    private TunerResult result;

    private SoundPlayer soundPlayer;
    private boolean startSfxPlayed = false;
    INoteClickToPlayEvent noteClickToPlayEvent;

    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageTask mUpload;
    private String uid;

    private View v;
    private CardView cv_logout;
    private TextView navName, navMail;
    private RelativeLayout rl_account;
    private CircleImageView mCircleImage;

    public TunerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();

    }

    public void initTuner() {
        String tunerModeString =
                SharedPreferencesHelper.getSharedPreferenceString(getContext(), "selectedTunerMode", getString(R.string.tuner_mode_default));
        TunerMode tunerMode = TunerMode.valueOf(tunerModeString, getContext());
        tuner = new Tuner(getContext(), tunerMode);
        tuner.setSoundPlayer(soundPlayer);
        binding.setTuner(tuner);
        binding.setCurrentNotePlaying(soundPlayer.currentNote);
    }

    private void initFakeTuner() {
        tuner = new Tuner(getContext(), TunerMode.getChromaticMode(getContext()));
        tuner.isFake = true;
        binding.setTuner(tuner);
        TunerResult fakeTunerResult =
                new TunerResult(0.00, tuner.tunerMode.getNotesObjects(), new TunerOptions(getContext()));
        binding.setResult(fakeTunerResult);
        binding.setCurrentNotePlaying(soundPlayer.currentNote);
    }

    private void noteFound(final TunerResult result) {
        if (getActivity() != null && !tuner.isMuted()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.setResult(result);
                }
            });
        }
    }

    public void startAttempt() {
        if (Tuner.checkIfMicrophoneIsAvailable(getContext())) {
            // Checking if the input is already occupied by another app.
            setupUI();
            start();

            // On App Start SFX
            if (!startSfxPlayed) {
                soundPlayer.playSfxOnStart();
                startSfxPlayed = true;
            }
        } else if ((tuner == null || tuner.isFake) || (!tuner.isRecording)) {
            // Checking if we've already created a placeholder tuner
            // OR if a tuner already exists but isn't recording
            initFakeTuner();
            showBusyMicrophoneDialog();
        }
    }

    public void start() {
        // Visual only
        if (tuner != null && !tuner.isFake && tuner.isRecording) {
            return;
        }
        initTuner();
        binding.tunerLine.setPercentage(50.0);
        tuner.start();
        tuner.setOnNoteFoundListener(new Tuner.OnNoteFoundListener() {
            @Override
            public void onEvent(TunerResult note) {
                noteFound(note);
            }
        });
        tuner.sendNullResult();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabaseRef = FirebaseDatabase.getInstance().getReference("user_profile").child(uid);
            rl_account.setVisibility(View.VISIBLE);
            cv_logout.setVisibility(View.VISIBLE);

            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String mail = dataSnapshot.child("email").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();

                    navName.setText(name);
                    navMail.setText(mail);
                    if (!image.equals("default")) {
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.mipmap.icon)
                                .into(mCircleImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(image).placeholder(R.mipmap.icon).into(mCircleImage);
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void stop() {
        if (tuner.isRecording) {
            soundPlayer.playNote("");
            tuner.stop();
            tuner.destroy();
        }
    }

    private void setupUI() {
        binding.navigationBlockPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording()) {
                    stop();
                }
                activity.openModes();
            }
        });
        noteClickToPlayEvent = new INoteClickToPlayEvent() {
            @Override
            public void onEvent(String note) {
                tuner.sendNullResult();
                soundPlayer.playNote(note);
                tuner.setMuted(soundPlayer.isPlayingNote());
            }
        };
        binding.notesCollectionBlock.setNoteClickToPlayEvent(noteClickToPlayEvent);
        soundPlayer.setOnNotePlayerFinished(new INotePlayerFinished() {
            @Override
            public void onEvent() {
                binding.tunerPlayingBlock.stopAnimation();
                if (soundPlayer.isPlaying()) {
                    binding.tunerPlayingBlock.startAnimation();
                }
            }
        });
        binding.tunerPlayingBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPlayer.playNote("");
                tuner.setMuted(false);
            }
        });
    }

    private void showBusyMicrophoneDialog() {
        final AlertDialog dialog = new AlertDialog();

        dialog.show(getFragmentManager(), "dialog_busy_microphone");

        dialog.setDialogCreatedListener(new AlertDialog.OnDialogCreatedListener() {
            @Override
            public void onCreate() {
                dialog.getDialogTitle().setText(getString(R.string.busy_microphone_dialog_title));
                dialog.getDialogContent().setText(getString(R.string.busy_microphone_dialog_text));

                dialog.getPositiveButton().setText(getString(R.string.busy_microphone_dialog_button_text));
                dialog.getPositiveButton().setButtonVariant(getString(R.string.button_variant_white));
                dialog.getPositiveButton().updateStyle();

                dialog.getNegativeButton().setVisibility(View.GONE);
            }
        });

        dialog.setDialogResultListener(new AlertDialog.OnDialogResultListener() {
            @Override
            public void onPositiveResult() {
                startAttempt();
            }

            @Override
            public void onNegativeResult() {
                startAttempt();
            }
        });
    }

    public boolean isRecording() {
        return tuner.isRecording;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTunerBinding.inflate(inflater, container, false);


        //Log.e("TSM Test 1", String.valueOf(binding.frequencyHertz.getText()));

        //binding.tsm.setText(binding.frequencyHertz.getText());

        //binding.tsm.setText(String.valueOf(result.getFrequencyLabel()));

        //Log.e("TSM Test 2", String.valueOf(result.getFrequencyLabel()));

        cv_logout = binding.navViewTuner.findViewById(R.id.nav_logout);
        rl_account = binding.navViewTuner.findViewById(R.id.rl_if_account_signIn);
        navName = binding.navViewTuner.findViewById(R.id.nav_name);
        navMail = binding.navViewTuner.findViewById(R.id.nav_mail);
        mCircleImage = binding.navViewTuner.findViewById(R.id.nav_circle_image);

        binding.drawermenuTuner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawerTuner.openDrawer(Gravity.LEFT);
            }
        });

        binding.navViewTuner.findViewById(R.id.nav_account_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AccountSettings_Activity.class);
                startActivity(intent);
            }
        });

        final Window window = getActivity().getWindow();
        binding.constScreen.setOnLongClickListener(new View.OnLongClickListener() {
            boolean screen;

            @Override
            public boolean onLongClick(View v) {
                screen = !screen;
                if (screen) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    Toast.makeText(getActivity(), "Screen ON", Toast.LENGTH_SHORT).show();
                    binding.imgScreenOnOff.setVisibility(View.VISIBLE);
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    Toast.makeText(getActivity(), "Screen OFF", Toast.LENGTH_SHORT).show();
                    binding.imgScreenOnOff.setVisibility(View.GONE);
                }
                return true;
            }
        });

        binding.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View popupView = layoutInflater.inflate(R.layout.popup_menu, null);

                View settingsItem = popupView.findViewById(R.id.menu_item_settings);
                View aboutItem = popupView.findViewById(R.id.menu_item_help);
                View privacyItem = popupView.findViewById(R.id.menu_item_privacy);

                // Do your customised stuff

                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setOutsideTouchable(true);
                settingsItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).openSettings();
                        popupWindow.dismiss();
                    }
                });
                aboutItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                privacyItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                popupWindow.showAsDropDown(binding.menuButton, 1000, -1000);
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            this.activity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        soundPlayer = new SoundPlayer(getContext());
        startAttempt();
    }

    @Override
    public void onPause() {
        super.onPause();
        stop();
    }


    @BindingAdapter({"app:isTunerResultVisible"})
    public static void setIsTunerResultVisible(final View v, boolean isActive) {
        if (v.getId() == R.id.tunerListeningBlock) {
            TunerModeListeningBlock view = (TunerModeListeningBlock) v;
            if (isActive && !view.isActive) {
                view.setVisibility(View.VISIBLE);
                view.isActive = true;
                view.clearAnimation();
                view.animate().alpha(1f).setDuration(400);
                view.startAnimation();
            } else if (!isActive && view.isActive) {
                view.stopAnimation();
                view.isActive = false;
                view.clearAnimation();
                view.animate().alpha(0f).setDuration(0);
                view.setVisibility(View.INVISIBLE);
            }
        }
        if (v.getId() == R.id.tunerPlayingBlock) {
            TunerModePlayingBlock view = (TunerModePlayingBlock) v;
            if (isActive && !view.isActive) {
                view.setVisibility(View.VISIBLE);
                view.isActive = true;
                view.clearAnimation();
                view.animate().alpha(1f).setDuration(400);
//                view.startAnimation();
            } else if (!isActive && view.isActive) {
//                view.stopAnimation();
                view.isActive = false;
                view.clearAnimation();
                view.animate().alpha(0f).setDuration(0);
                view.setVisibility(View.GONE);
            }
        }
        if (v.getId() == R.id.tvNote || v.getId() == R.id.tvOctave || v.getId() == R.id.tvNoteSharp) {
            final View view = v;
            if (isActive) {
                view.setVisibility(View.VISIBLE);
                view.setAlpha(1f);
                view.animate().setListener(null);
            } else {
                view.animate().alpha(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        view.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
        }
    }

    @BindingAdapter({"android:src"})
    public static void setImageViewResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }
}
