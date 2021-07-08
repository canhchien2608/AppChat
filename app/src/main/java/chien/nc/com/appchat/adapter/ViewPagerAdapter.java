package chien.nc.com.appchat.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import chien.nc.com.appchat.activity.Fragment_Account;
import chien.nc.com.appchat.activity.Fragment_Friends;
import chien.nc.com.appchat.activity.Fragment_Home;
import chien.nc.com.appchat.activity.Fragment_Mess;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new Fragment_Home();
            case 1:
                return new Fragment_Friends();
            case 2:
                return new Fragment_Mess();
            case 3:
                return new Fragment_Account();
            default:
                return new Fragment_Home();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
