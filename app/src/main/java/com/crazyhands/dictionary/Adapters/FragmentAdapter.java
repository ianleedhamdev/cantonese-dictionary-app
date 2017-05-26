package com.crazyhands.dictionary.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.crazyhands.dictionary.Fragments.AllListFragment;
import com.crazyhands.dictionary.Fragments.BasicWordsFragment;
import com.crazyhands.dictionary.Fragments.NumbersFragment;


public class FragmentAdapter extends FragmentPagerAdapter {
    private String tabTitles[] = new String[]{"basic", "all", "numbers"};

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new BasicWordsFragment();
        } else if (position == 1) {
            return new AllListFragment();
        } else if (position == 2) {
            return new NumbersFragment();
        } else return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return 3;
    }
}