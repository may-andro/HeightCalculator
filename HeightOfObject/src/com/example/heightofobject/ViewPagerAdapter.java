package com.example.heightofobject;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter 
{
	 private final List<Fragment> mFragmentList = new ArrayList<Fragment>();
     
     public ViewPagerAdapter(FragmentManager manager)
     {
         super(manager);
     }

     @Override
     public Fragment getItem(int position)
     {
         return mFragmentList.get(position);
     }

     @Override
     public int getCount() 
     {
         return mFragmentList.size();
     }

     public void addFrag(Fragment fragment)
     {
         mFragmentList.add(fragment);
     }

   
}