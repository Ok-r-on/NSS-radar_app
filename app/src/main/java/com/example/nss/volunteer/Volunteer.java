package com.example.nss.volunteer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nss.AboutDev;
import com.example.nss.MainActivity;
import com.example.nss.R;
import com.example.nss.senior.Senior;
import com.example.nss.volunteer.home.VolHome;
import com.example.nss.volunteer.notice.VolNotice;
import com.example.nss.volunteer.sched.VolSchedule;
import com.google.android.material.bottomnavigation.BottomNavigationView;

//TODO get username that is passed from login and put it in toolbar
public class Volunteer extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_FIRST_TIME_LOGIN = "FirstTimeLogin";
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);


        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.apply();

        String UserName = sharedPref.getString("UserName","");
        Toolbar toolbar=findViewById(R.id.toolbarforvol);
        setSupportActionBar(toolbar);
        toolbar.setTitle(UserName);

        ViewPager viewPager = findViewById(R.id.view_pagerVol);
        VolunteerVPAdapter adapter = new VolunteerVPAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new VolHome());
        adapter.addFragment(new VolSchedule());
        adapter.addFragment(new VolNotice());
        viewPager.setAdapter(adapter);

        bottomNavigationView = findViewById(R.id.bottomNavBarVol);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Unused
            }
            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);

            }
            @Override
            public void onPageScrollStateChanged(int state) {
                // Unused
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navVolHome) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (item.getItemId() == R.id.navVolSched) {
                viewPager.setCurrentItem(1);
                return true;
            }  else if(item.getItemId() == R.id.navVolNotice){
                viewPager.setCurrentItem(2);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_menu_for_vol,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id == R.id.logoutforall){
            logout();

        } else if(id == R.id.abouttheapp){
            startActivity(new Intent(Volunteer.this, AboutDev.class));
        } else if (id == R.id.toSenior) {
            toSr();
        }
        return true;
    }

    private void toSr() {
        Dialog dialog = new Dialog(Volunteer.this);
        dialog.setContentView(R.layout.dialog_to_sr);
        EditText srlogpass=dialog.findViewById(R.id.srlogpass);
        Button srcodelogbtn=dialog.findViewById(R.id.srcodelogbtn);

        srcodelogbtn.setOnClickListener(v -> {
            if("N-".equals(srlogpass.getText().toString().trim())){
                startActivity(new Intent(Volunteer.this, Senior.class));
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void logout() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("UserName","");
        editor.putBoolean(KEY_FIRST_TIME_LOGIN, true);
        editor.apply();

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Volunteer.this, MainActivity.class));
        finish();
    }

}