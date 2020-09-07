package Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import Share.ShareActivity;
import Utils.FirebaseMethods;
import Utils.UniversalImageLoader;
import de.hdodenhof.circleimageview.CircleImageView;
import dialogs.ConfirmPasswordDialog;
import models.User;
import models.UserAccountSettings;
import models.UserSettings;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener {


    private static final String TAG = "EditProfileFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;
    //EditProfile fragments widgets
    private EditText mDisplayName,mUsername,mWebsite,mDescription,mEmail,mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;

    //vars
    private UserSettings mUserSettings;

    @Override
    public void OnConfirmPassword(String password) {
        Log.d(TAG, "OnConfirmPassword: Got the password: " + password);
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        // Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if(task.isSuccessful()){
                                        try{
                                            if(task.getResult().getProviders().size() == 1){
                                                Log.d(TAG, "onComplete: That email is already taken: ");
                                                Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                            }else{
                                                Log.d(TAG, "onComplete: That email is availabel");
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User email address updated.");
                                                                    Toast.makeText(getActivity(), "Voila Email Updated!!", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });

                                            }
                                        }catch(NullPointerException e){
                                            Log.e(TAG, "onComplete: NULLPONTEREXCEPTION: " + e.getMessage());
                                        }
                                    }
                                }
                            });
                        }
                        else{
                            Log.d(TAG, "onComplete: Reauthentication failed");
                        }
                    }

                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_editprofile,container,false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName=(EditText) view.findViewById(R.id.display_name);
        mUsername=(EditText) view.findViewById(R.id.username);
        mWebsite=(EditText) view.findViewById(R.id.website);
        mDescription=(EditText) view.findViewById(R.id.description);
        mEmail=(EditText) view.findViewById(R.id.email);
        mPhoneNumber=(EditText) view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto=(TextView) view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods=new FirebaseMethods(getActivity());

       // setProfileImage();
        setupFirebaseAuth();
        ImageView backArrow=(ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        ImageView checkmark = (ImageView) view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Attempting to save changes");
                saveProfileSettings();
            }
        });
        return view;
    }

    private void saveProfileSettings(){
        final String displayName=mDisplayName.getText().toString();
        final String username=mUsername.getText().toString();
        final String website=mWebsite.getText().toString();
        final String description=mDescription.getText().toString();
        final String email=mEmail.getText().toString();
        final long phoneNumber=Long.parseLong(mPhoneNumber.getText().toString());


                //case 1:if user made a change to username
                if(!mUserSettings.getUser().getUsername().equals(username)){
                    checkIfUsernameExists(username);
                }

                //case 2:if user made change to their email
                if(!mUserSettings.getUser().getEmail().equals(email)){
                    //Step 1.) Reauthenticate
                    //          Confirm the password and email
                    ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
                    dialog.show(getFragmentManager(),getString(R.string.confirm_password_dialog));
                    dialog.setTargetFragment(EditProfileFragment.this,1);
                    //Step 2.) Check is the email is alreasy regosterd or not
                    //           fetchProvidersEmail(String email)
                    //Step 3.) Change the email
                    //           Submit the new email to database and authenticate it

                }
                if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
                    //update displayname
                    mFirebaseMethods.updateUserAccountSettings(displayName,null,null,0);
                }
                if(!mUserSettings.getSettings().getWebsite().equals(website)){
                    //update website
                    mFirebaseMethods.updateUserAccountSettings(null,website,null,0);
                }
                if(!mUserSettings.getSettings().getDescription().equals(description)){
                    //update description
                    mFirebaseMethods.updateUserAccountSettings(null,null,description,0);
                }
                if(!mUserSettings.getSettings().getProfile_photo().equals(phoneNumber)){
                    //update phone number
                    mFirebaseMethods.updateUserAccountSettings(null,null,null,phoneNumber);
                }
    }

    /**
     * Checks if @param username already exists in database
     * @param username
     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query=reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    //add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Voila Saved Username!!", Toast.LENGTH_SHORT).show();
                }
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "onDataChange: checkIfUsernameExists: " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "That username already taken.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: setting widgets");
        mUserSettings=userSettings;
        // User user=userSettings.getUser();
        UserAccountSettings settings=userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));
        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Changing profile photo");
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });
    }

    /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        userID=mAuth.getCurrentUser().getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //retrieve user information from database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
                //retrieve images for the user in question
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
