package com.example.macksham.friendbuzz;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionPagerAdopter extends FragmentPagerAdapter{

    public SectionPagerAdopter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                RequestsFragment requestFragemnt = new RequestsFragment();
                return requestFragemnt;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "Requests";
            case 1:
                return "Chats";
            case 2:
                return "Contacts";
            default:
                return null;
        }
    }
}
