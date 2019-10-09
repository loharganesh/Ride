package app.ride.View;



import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import app.ride.Fragments.DriveHistory;
import app.ride.Fragments.RideHistory;


public class HistoryPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public HistoryPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        RideHistory rideHistory = new RideHistory();
        DriveHistory driveHistory = new DriveHistory();
        switch (position) {
            case 0:
                return rideHistory;
            case 1:
                return driveHistory;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
