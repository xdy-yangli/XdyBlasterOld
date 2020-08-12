package com.example.xdyblaster.Adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.xdyblaster.fragment.FragmentData;

import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    //    @StringRes
    private final Context mContext;
    private List<FragmentData> fragments;
    private int count;


    public SectionsPagerAdapter(Context context, FragmentManager fm, List<FragmentData> fragments, int count) {
        super(fm, count);
        mContext = context;
        this.fragments = fragments;
        this.count = count;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return fragments.get(position);
//        return PlaceholderFragment.newInstance(position + 1);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return fragments.size();
        //return count;
    }
}