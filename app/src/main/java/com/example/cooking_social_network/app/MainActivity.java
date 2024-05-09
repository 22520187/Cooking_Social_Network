package com.example.cooking_social_network.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.cooking_social_network.R;
import com.example.cooking_social_network.app.Fragment.HomeFragment;
import com.example.cooking_social_network.app.Fragment.NotificationFragment;
import com.example.cooking_social_network.app.Fragment.ProfileFragment;
import com.example.cooking_social_network.app.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.nav_home) {
                    selectorFragment = new HomeFragment();
                } else if (itemId == R.id.nav_search) {
                    selectorFragment = new SearchFragment();
                } else if (itemId == R.id.nav_add) {
                    selectorFragment = null;
                    startActivity(new Intent(MainActivity.this,PostActivity.class));
                } else if (itemId == R.id.nav_heart) {
                    selectorFragment = new NotificationFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectorFragment = new ProfileFragment();
                }
                if(selectorFragment != null ) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectorFragment).commit();
                }
                return true;
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }
}