package com.mh.evgeniy.photogallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by evgeniy on 25.07.2016.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity{

    protected abstract Fragment createFragment();

    protected int getLayoutResId(){
        return R.layout.activity_fragment;
    }


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        FragmentManager fm=getSupportFragmentManager();

        Fragment fragment=fm.findFragmentById(R.id.fragmentContainer);

        if(fragment==null){
            Log.d("sasa","fragment==null");
            fragment=createFragment();
            fm.beginTransaction().add(R.id.fragmentContainer,fragment).commit();

        }else Log.d("sasa","fragment!=null");

    }

}
