package com.example.fyp1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.fyp1.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class mainmenu2 extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new BlankFragment());

        binding..setOnIntemSelectedListener(item->{
            switch (item.getItemId()){
                case R.id.home:
                    replaceFragment(new BlankFragment());
                    break;
                case R.id.profile:
                    //replaceFragment(new BlankFragment());
                    break;
                case R.id.exit:
                    //replaceFragment(new BlankFragment());
                    break;
            }

            return true;
        });*/
    }
    /*private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout);
        fragmentTransaction.commit();
    }*/
}