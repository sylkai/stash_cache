package com.geekeclectic.android.stashcache;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.io.IOException;

/**
 * Created by sylk on 2/19/2015.
 */
public class StashOverviewActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new StashOverviewPagerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.drop_down_list, android.R.layout.simple_spinner_dropdown_item);

        ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
            // get the strings provided for the ArrayAdapter
            String[] strings = getResources().getStringArray(R.array.drop_down_list);

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                String selection = strings[position];
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment;
                FragmentTransaction ft = fragmentManager.beginTransaction();

                if (selection.equals("Master List")) {
                    if (fragmentManager.findFragmentByTag(selection) == null) {
                        fragment = new MasterOverviewPagerFragment();
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    } else {
                        fragment = fragmentManager.findFragmentByTag(selection);
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    }
                } else if (selection.equals("Shopping List")) {
                    StashCreateShoppingList createList = new StashCreateShoppingList();
                    createList.updateShoppingList(StashData.get(getParent()));

                    fragment = new ShoppingOverviewPagerFragment();
                    ft.replace(R.id.fragmentContainer, fragment, selection);
                } else {
                    if (fragmentManager.findFragmentByTag(selection) == null) {
                        fragment = new StashOverviewPagerFragment();
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    } else {
                        fragment = fragmentManager.findFragmentByTag(selection);
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    }
                }

                ft.commit();

                return true;
            }
        };

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
        actionBar.setSelectedNavigationItem(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stash_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UpdateFragment fragment = (UpdateFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        // handling item selection
        switch (item.getItemId()) {
            case R.id.menu_item_import_stash:
                 StashImporter importer = new StashImporter();
                try {
                    importer.importStash(getApplicationContext());
                } catch (IOException e) {
                    //
                }
                StashData.get(getApplicationContext()).saveStash();
                fragment.stashChanged();
                return super.onOptionsItemSelected(item);
            case R.id.menu_item_delete_stash:
                StashData.get(getApplicationContext()).deleteStash();
                fragment.stashChanged();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
