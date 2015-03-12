package com.geekeclectic.android.stashcache;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Activity to host fragments (stash/master list/shopping list) each hosting a viewpager which
 * displays lists for the various classes of items.  Nested fragments code based in part on project
 * found here (https://github.com/marcoRS/nested-fragments) for guidance on how the layout
 * notes in the docs apply.  More information here:
 * http://stackoverflow.com/questions/13379194/how-to-add-a-fragment-inside-a-viewpager-using-nested-fragment-android-4-2?rq=1
 * Thanks to StackOverflow for solving the issue of the dropdown menu theme-ing:
 * http://stackoverflow.com/questions/15948026/cant-change-the-text-color-with-android-action-bar-drop-down-navigation
 */
public class StashOverviewActivity extends FragmentActivity implements UpdateFragment.OnTabSwipeListener {

    private static final int REQUEST_CHOOSE_STASH = 1;
    public static final String EXTRA_FRAGMENT_ID = "com.geekeclectic.android.stashcache.active_fragment_id";
    public static final String EXTRA_VIEW_ID = "com.geekeclectic.android.stashcache.active_view_id";
    private int currentTab;
    private int currentView;

    protected UpdateFragment createFragment(int currentTab) {
        if (currentTab == 0) {
            return new StashOverviewPagerFragment();
        } else if (currentTab == 1) {
            return new MasterOverviewPagerFragment();
        } else {
            StashCreateShoppingList createList = new StashCreateShoppingList();
            createList.updateShoppingList(StashData.get(getParent()));

            return new ShoppingOverviewPagerFragment();
        }
    }

    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();
        UpdateFragment fragment = (UpdateFragment) fm.findFragmentById(R.id.fragmentContainer);

        String[] strings = getResources().getStringArray(R.array.drop_down_list);

        if (getIntent().getExtras() != null) {
            currentView = getIntent().getIntExtra(EXTRA_VIEW_ID, 0);
            currentTab = getIntent().getIntExtra(EXTRA_FRAGMENT_ID, 0);
        } else {
            currentTab = 0;
            currentView = 0;
        }

        if (fragment == null) {
            fragment = createFragment(currentTab);
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment, strings[currentTab])
                    .commit();
        }

        // create spinner for the drop down menu
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getActionBar().getThemedContext(), R.array.drop_down_list, android.R.layout.simple_spinner_dropdown_item);

        ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
            // get the strings provided for the ArrayAdapter
            String[] strings = getResources().getStringArray(R.array.drop_down_list);

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                String selection = strings[position];
                FragmentManager fragmentManager = getSupportFragmentManager();
                UpdateFragment fragment;
                FragmentTransaction ft = fragmentManager.beginTransaction();

                if (selection.equals("Master List")) {
                    if (fragmentManager.findFragmentByTag(selection) == null) {
                        fragment = new MasterOverviewPagerFragment();
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    } else {
                        fragment = (UpdateFragment) fragmentManager.findFragmentByTag(selection);
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    }
                } else if (selection.equals("Shopping List")) {
                    StashCreateShoppingList createList = new StashCreateShoppingList();
                    createList.updateShoppingList(StashData.get(getParent()));

                    if (fragmentManager.findFragmentByTag(selection) == null) {
                        fragment = new ShoppingOverviewPagerFragment();
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    } else {
                        fragment = (UpdateFragment) fragmentManager.findFragmentByTag(selection);
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    }
                } else {
                    if (fragmentManager.findFragmentByTag(selection) == null) {
                        fragment = new StashOverviewPagerFragment();
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    } else {
                        fragment = (UpdateFragment) fragmentManager.findFragmentByTag(selection);
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    }
                }

                adjustViewsIfNeeded(position);
                currentTab = position;
                fragment.setCurrentView(currentView);
                ft.commit();

                return true;
            }
        };

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
        actionBar.setSelectedNavigationItem(currentTab);
        fragment.setCurrentView(currentView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stash_menu, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        UpdateFragment fragment = (UpdateFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        fragment.setCurrentView(currentView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UpdateFragment fragment = (UpdateFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        // handling item selection
        switch (item.getItemId()) {
            case R.id.menu_item_import_stash:
                Intent chooseIntent = new Intent();
                chooseIntent.addCategory(Intent.CATEGORY_OPENABLE);
                chooseIntent.setType("text/plain");
                chooseIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(chooseIntent, "select file"), REQUEST_CHOOSE_STASH);

                return super.onOptionsItemSelected(item);
            case R.id.menu_item_export_stash:
                StashExporter exporter = new StashExporter();

                try {
                    File file = exporter.exportStash(getApplicationContext());

                    Intent saveIntent = new Intent(Intent.ACTION_SEND);

                    // better filter of apps by using "application/octet-stream" instead of "text/plain"
                    // this isn't appropriate to send to a messenger-type app, for example and this was an
                    // easy way to filter while still providing access to Dropbox/Google Drive/email
                    saveIntent.setType("application/octet-stream");
                    saveIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    startActivity(Intent.createChooser(saveIntent, "title"));
                } catch (IOException e) {
                    //
                }
                return super.onOptionsItemSelected(item);
            case R.id.menu_item_delete_stash:
                StashData.get(getApplicationContext()).deleteStash();
                fragment.stashChanged();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onTabSwipe(int selectedView) {
        currentView = selectedView;
    }

    private void adjustViewsIfNeeded(int changeTabTo) {
        if (currentTab == 2) {
            if (currentView > 0) {
                currentView = currentView + 1;
            }
        } else if (changeTabTo == 2) {
            if (currentView > 0) {
                currentView = currentView - 1;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        UpdateFragment fragment = (UpdateFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (requestCode == REQUEST_CHOOSE_STASH) {

            try {
                // in order to handle Google Drive's may-or-may-not-have-downloaded issue, using getContentResolver()
                // per http://stackoverflow.com/questions/27771003/intent-action-get-content-with-google-drive
                AsyncStashImport importStash = new AsyncStashImport(getContentResolver().openInputStream(data.getData()), fragment);
                importStash.execute();
            } catch (FileNotFoundException e) {
                //
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class AsyncStashImport extends AsyncTask<Void, Void, Void> {
        private InputStream forImporter;
        private UpdateFragment fragment;
        TransparentProgressDialog dialog;
        int resultId;

        public AsyncStashImport(InputStream in, UpdateFragment updateFragment) {
            super();
            forImporter = in;
            fragment = updateFragment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = TransparentProgressDialog.show(StashOverviewActivity.this);
            dialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                // in order to handle Google Drive's may-or-may-not-have-downloaded issue, using getContentResolver()
                // per http://stackoverflow.com/questions/27771003/intent-action-get-content-with-google-drive
                StashImporter importer = new StashImporter(forImporter);
                resultId = importer.importStash(getApplicationContext());
            } catch (IOException e) {
                //
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (dialog != null) {
                dialog.dismiss();
            }

            String toastText;

            if (resultId == 0) {
                toastText = getString(R.string.successful_import);
            } else if (resultId == 1) {
                toastText = getString(R.string.incorrect_file_format);
            } else {
                toastText = getString(R.string.incorrect_number_format);
            }

            Toast.makeText(StashOverviewActivity.this, toastText, Toast.LENGTH_SHORT).show();

            fragment.stashChanged();
            StashData.get(getApplicationContext()).saveStash();
        }
    }

}
