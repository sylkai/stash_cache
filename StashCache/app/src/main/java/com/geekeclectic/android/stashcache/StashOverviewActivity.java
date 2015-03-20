package com.geekeclectic.android.stashcache;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
public class StashOverviewActivity extends FragmentActivity implements UpdateFragment.OnTabSwipeListener, UpdateListFragment.UpdateListFragmentsListener {

    private static final int REQUEST_CHOOSE_STASH = 1;
    private static final int REQUEST_PREFERENCES_UPDATE = 2;
    public static final String TAG = "stash overview";
    public static final String EXTRA_FRAGMENT_ID = "com.geekeclectic.android.stashcache.active_fragment_id";
    public static final String EXTRA_VIEW_ID = "com.geekeclectic.android.stashcache.active_view_id";

    private int currentTab;
    private int currentView;
    private Menu menuRef;

    protected UpdateFragment createFragment(int currentTab) {
        if (currentTab == 0) {
            return new StashOverviewPagerFragment();
        } else if (currentTab == 1) {
            return new MasterOverviewPagerFragment();
        } else {
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
            // get the tab / view pair from the intent
            currentView = getIntent().getIntExtra(EXTRA_VIEW_ID, 0);
            currentTab = getIntent().getIntExtra(EXTRA_FRAGMENT_ID, 0);
        } else {
            // default to stash / patterns view
            currentTab = 0;
            currentView = 0;
        }

        // if there's no fragment stored, create a new one
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

                // get (or create) the appropriate fragment
                if (selection.equals("Master List")) {
                    if (fragmentManager.findFragmentByTag(selection) == null) {
                        fragment = new MasterOverviewPagerFragment();
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    } else {
                        fragment = (UpdateFragment) fragmentManager.findFragmentByTag(selection);
                        ft.replace(R.id.fragmentContainer, fragment, selection);
                    }
                } else if (selection.equals("Shopping List")) {
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

        StashCreateShoppingList shoppingList = new StashCreateShoppingList();
        shoppingList.updateShoppingList(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stash_menu, menu);
        menuRef = menu;
        setVisibleMenuItems();
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
        final UpdateFragment fragment = (UpdateFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

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
                    startActivity(Intent.createChooser(saveIntent, getString(R.string.send_file_to)));
                } catch (IOException e) {
                    //
                }
                return super.onOptionsItemSelected(item);
            case R.id.menu_item_delete_stash:
                // display a dialog box to confirm that the user absolutely wants to delete the stash
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.delete_stash_warning);
                builder.setMessage(R.string.delete_stash_additional);
                builder.setIcon(R.drawable.ic_dialog_alert_holo_light);

                builder.setPositiveButton(R.string.delete_stash_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // it was confirmed, so delete the stash and refresh fragments
                        StashData.get(getApplicationContext()).deleteStash();
                        fragment.stashChanged();
                    }
                });

                builder.setNegativeButton(R.string.delete_stash_back, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //
                    }
                });

                builder.show();

                return super.onOptionsItemSelected(item);
            case R.id.menu_item_preferences:
                Intent intent = new Intent(this, StashPreferencesActivity.class);
                startActivityForResult(intent, REQUEST_PREFERENCES_UPDATE);

                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onTabSwipe(int selectedView) {
        currentView = selectedView;
        if (menuRef != null) {
            setVisibleMenuItems();
        }
    }

    public void onListFragmentUpdate() {
        UpdateFragment fragment = (UpdateFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        fragment.stashChanged();
    }

    private void adjustViewsIfNeeded(int changeTabTo) {
        // current view is shopping list, so need to adjust to no fabric view by adding 1
        if (currentTab == 2) {
            if (currentView > 0) {
                currentView = currentView + 1;
            }
        // switching to shopping list, so adjust to no fabric view by subtracting 1
        } else if (changeTabTo == 2) {
            if (currentView > 0) {
                currentView = currentView - 1;
            }
        }
    }

    private void setVisibleMenuItems() {
        if (menuRef.hasVisibleItems()) {
            menuRef.setGroupVisible(R.id.menu_add_edit, false);
        }

        if (currentView == 0) {
            menuRef.findItem(R.id.menu_item_new_pattern).setVisible(true);
        } else if (currentTab < 2 && currentView == 1) {
            menuRef.findItem(R.id.menu_item_new_fabric).setVisible(true);
        } else if ((currentTab < 2 && currentView == 2) || (currentTab == 2 && currentView == 1)) {
            menuRef.findItem(R.id.menu_item_edit_thread_stash).setVisible(true);
            menuRef.findItem(R.id.menu_item_new_thread).setVisible(true);
        } else {
            menuRef.findItem(R.id.menu_item_edit_embellishment_stash).setVisible(true);
            menuRef.findItem(R.id.menu_item_new_embellishment).setVisible(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        UpdateFragment fragment = (UpdateFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (requestCode == REQUEST_CHOOSE_STASH) {
            if (resultCode != RESULT_OK) {
                return;
            }

            try {
                // in order to handle Google Drive's may-or-may-not-have-downloaded issue, using getContentResolver()
                // per http://stackoverflow.com/questions/27771003/intent-action-get-content-with-google-drive
                AsyncStashImport importStash = new AsyncStashImport(getContentResolver().openInputStream(data.getData()), fragment);
                importStash.execute();
            } catch (FileNotFoundException e) {
                //
            }
        } else if (requestCode == REQUEST_PREFERENCES_UPDATE) {
            StashCreateShoppingList shoppingList = new StashCreateShoppingList();
            shoppingList.updateShoppingList(this);

            fragment.stashChanged();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // import the stash as a async task with a dialog that blocks user activity on the UI thread
    // during the import
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
