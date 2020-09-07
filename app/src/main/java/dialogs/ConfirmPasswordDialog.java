package dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.instagramclone.R;

public class ConfirmPasswordDialog extends DialogFragment {
    private static final String TAG = "ConfirmPasswordDialog";
    public interface OnConfirmPasswordListener{
        public void OnConfirmPassword(String password);
    }
    OnConfirmPasswordListener mOnConfirmPasswordListener;
    //vars
    TextView mPassword;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password,container,false);
        mPassword=(TextView) view.findViewById(R.id.confirm_password);
        Log.d(TAG, "onCreateView: started");

        TextView confirmDialog = (TextView) view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Captured Password and confirming");
                String password=mPassword.getText().toString();
                if(!password.equals("")){
                    mOnConfirmPasswordListener.OnConfirmPassword(password);
                    getDialog().dismiss();
                }else{
                    Toast.makeText(getActivity(), "You have to enter a password idiot!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView cancelDialog = (TextView) view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Closing the dialog");
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mOnConfirmPasswordListener=(OnConfirmPasswordListener) getTargetFragment();
        }catch(ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException " + e.getMessage() );
        }
    }
}
