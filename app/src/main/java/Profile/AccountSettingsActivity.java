package Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.instagramclone.R;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import Utils.BottomNavigationViewHelper;
import Utils.FirebaseMethods;
import Utils.SectionStagePagerAdapter;

public class AccountSettingsActivity extends AppCompatActivity {
    private static final String TAG = "AccountSettingsActivity";
    private static final int ACTIVITY_NUM=4;
    private Context mContext;
    public SectionStagePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        mContext= AccountSettingsActivity.this;
        mViewPager=(ViewPager) findViewById(R.id.viewpager_container);
        mRelativeLayout=(RelativeLayout) findViewById(R.id.relLayout1);
        setupSettingsList();
        setupBottomNavigationView();
        setupFragments();
        getIncomingIntent();
        //Setup the backarrow navigation to ProfileActivity
        ImageView backArrow=(ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Log.d(TAG, "onClick: Clicked backarrow");
                finish();
            }
        });
    }

    private void getIncomingIntent(){
        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.selected_image)) || intent.hasExtra(getString(R.string.selected_bitmap))) {
            Log.d(TAG, "getIncomingIntent: New Incoming imgURl");
            if (intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment))) {
                if (intent.hasExtra(getString(R.string.selected_image))) {
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0, intent.getStringExtra(getString(R.string.selected_image)), null);
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0, null, (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap)));
                }
            }
        }
        if(intent.hasExtra(getString(R.string.calling_activity))){
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }
    }

    private void setupFragments(){
        pagerAdapter=new SectionStagePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(),getString(R.string.edit_profile_fragment));
        pagerAdapter.addFragment(new SignOutFragment(),getString(R.string.sign_out_fragment));

    }
    public void setViewPager(int fragmentNumber){
            mRelativeLayout.setVisibility(View.GONE);
            mViewPager.setAdapter(pagerAdapter);
            mViewPager.setCurrentItem(fragmentNumber);
    }
    private void setupSettingsList(){
        ListView listView=(ListView) findViewById(R.id.lvAccountSettings);
        ArrayList<String> options=new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.sign_out_fragment));

        ArrayAdapter adapter = new ArrayAdapter(mContext,android.R.layout.simple_list_item_1,options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setViewPager(position);
            }
        });
    }

    private void setupBottomNavigationView(){
        //Log.d(TAG, "setupBottomNavigationView: settingupBottomNavigation");
        BottomNavigationViewEx bottomNavigationViewEx=(BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(AccountSettingsActivity.this,this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem =menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


}
