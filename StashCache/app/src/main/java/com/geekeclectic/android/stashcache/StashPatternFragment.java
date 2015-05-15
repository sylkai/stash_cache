package com.geekeclectic.android.stashcache;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

/**
 * Fragment to display information for a pattern in the stash and allow the user to edit it.
 * ChangedFragmentListener interface is required to notify the host activity that changes in this
 * fragment may impact other StashPatternFragments and their displayed information should be
 * refreshed.  Implements listeners for dialog callbacks for selecting fabric and threads to
 * associate with the pattern.  The Observer implementation allows the host activity to refresh the
 * display of the fragment in case the underlying information for the Pattern has been updated.
 */

public class StashPatternFragment extends Fragment implements DatePickerDialogFragment.DatePickerDialogListener, PickOneDialogFragment.OnDialogPickOneListener, SelectFabricDialogFragment.SelectFabricDialogListener, Observer, SelectThreadQuantityDialogFragment.SelectThreadQuantityDialogListener, SelectEmbellishmentQuantityDialogFragment.SelectEmbellishmentQuantityDialogListener {

    public static final String EXTRA_PATTERN_ID = "com.geekeclectic.android.stashcache.pattern_id";
    public static final String EXTRA_TAB_ID = "com.geekeclectic.android.stashcache.calling_stash_id";
    public static final String TAG = "StashPatternFragment";

    private static final int REQUEST_PICK_NEW_FABRIC = 0;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_OK = 0;

    private static final String DIALOG_FABRIC = "fabric";
    private static final String DIALOG_THREAD = "thread";
    private static final String DIALOG_DATE = "date";
    private static final int VIEW_ID = StashConstants.PATTERN_VIEW;

    private StashPattern mPattern;
    private StashPatternFragment mFragment;
    private StashFabric mFabric;
    private ArrayList<UUID> mCurrentFabric;
    private ArrayList<UUID> mThreadList;
    private ArrayList<UUID> mEmbellishmentList;
    private ArrayList<UUID> mFinishesList;
    private UUID mPatternId;
    private EditText mTitleField;
    private EditText mSourceField;
    private EditText mWidthField;
    private EditText mHeightField;
    private CheckBox mIsKitted;
    private CheckBox mInProgress;
    private ImageView mViewPhoto;
    private ImageView mEditPhoto;
    private ImageView mEditFabric;
    private ImageView mEditThread;
    private ImageView mEditEmbellishment;
    private ListView mFabricDisplay;
    private TextView mFabricInfo;
    private ListView mThreadDisplayList;
    private ListView mEmbellishmentDisplayList;
    private ListView mFinishList;
    private TextView mFinishTitle;
    private StashCreateShoppingList mShoppingList;
    private View mStartDateGroup;
    private ImageView mEditStartDate;
    private TextView mStartDate;

    private ChangedFragmentListener mCallback;
    private int callingTab;

    private String mPhotoPath;

    public interface ChangedFragmentListener {
        void updateFragments();
    }

    public StashPatternFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get patternId and use it to get pattern
        mPatternId = (UUID)getArguments().getSerializable(EXTRA_PATTERN_ID);
        callingTab = getArguments().getInt(EXTRA_TAB_ID);
        mPattern = StashData.get(getActivity()).getPattern(mPatternId);

        // set reference to fragment for setting listeners
        mFragment = this;

        mFabric = mPattern.getFabric();
        mCurrentFabric = new ArrayList<UUID>();
        if (mFabric != null) {
            mCurrentFabric.add(mFabric.getId());
        }

        mThreadList = mPattern.getThreadList();
        mEmbellishmentList = mPattern.getEmbellishmentList();
        mFinishesList = mPattern.getFinishes();

        mShoppingList = new StashCreateShoppingList();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // should never need to throw an exception, but it must be handled anyway...
        try {
            mCallback = (ChangedFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ChangedFragmentListener");
        }
    }

    public static StashPatternFragment newInstance(UUID patternId, int tab) {
        // associate patternId with the fragment through arguments
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PATTERN_ID, patternId);
        args.putInt(EXTRA_TAB_ID, tab);

        StashPatternFragment fragment = new StashPatternFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                // set up navigation
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    Intent i = new Intent(getActivity(), StashOverviewActivity.class);

                    // note which category is calling the up to display appropriate fragment (pattern)
                    i.putExtra(StashOverviewActivity.EXTRA_VIEW_ID, VIEW_ID);
                    i.putExtra(StashOverviewActivity.EXTRA_FRAGMENT_ID, callingTab);

                    NavUtils.navigateUpTo(getActivity(), i);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // may be used later with the photo stuff
    @Override
    public void onStart() {
        super.onStart();
    }

    // may be used later with the photo stuff
    @Override
    public void onStop() {
        super.onStop();
        // StashPhotoUtils.cleanImageView(mViewPhoto);
    }

    @Override
    public void onPause() {
        super.onPause();
        StashData.get(getActivity()).saveStash();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFabricInfo();
    }

    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pattern, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                // turn on up navigation
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        /*mViewPhoto = (ImageView)v.findViewById(R.id.pattern_photo_detail);
        // set listener to trigger when layout is fully drawn to pass on proper values to showPhoto
        // per http://stackoverflow.com/questions/3591784/getwidth-returns-0?lq=1
        mViewPhoto.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @Deprecated
            public void onGlobalLayout() {
                // remove so it is called only once
                mViewPhoto.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                // scale the photo for display
                showPhoto();
            }
        });

        mEditPhoto = (ImageView)v.findViewById(R.id.pattern_photoButton);
        mEditPhoto.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    File photoFile = null;

                    try {
                        photoFile = StashPhotoUtils.createImageFile();
                    } catch (IOException ex) {
                        Log.e(TAG, "error creating photo file");
                    }

                    if (photoFile != null) {
                        mPhotoPath = Uri.fromFile(photoFile).getPath();

                        StashPhoto photo = new StashPhoto(mPhotoPath);
                        mPattern.setPhoto(photo);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });*/

        // editText for pattern name
        mTitleField = (EditText)v.findViewById(R.id.pattern_name);
        mTitleField.setText(mPattern.getPatternName());
        mTitleField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                // listener appears to be called on fragment creation so only make changes
                // if things have actually been changed
                if (!c.toString().equals(mPattern.getPatternName())) {
                    mPattern.setPatternName(c.toString());
                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mPattern.toString());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        // editText for the designer name
        mSourceField = (EditText)v.findViewById(R.id.designer_name);
        mSourceField.setText(mPattern.getSource());
        mSourceField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                // listener appears to be called on fragment creation so only make changes
                // if things have actually been changed+
                if (!c.toString().equals(mPattern.getSource())) {
                    mPattern.setSource(c.toString());
                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mPattern.toString());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        // checkbox to indicate whether a pattern is to be kitted or not
        mIsKitted = (CheckBox)v.findViewById(R.id.pattern_kitted);
        mIsKitted.setChecked(mPattern.isKitted());
        mIsKitted.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                mPattern.setKitted(checked);
                mShoppingList.updateShoppingList(getActivity());
            }
        });

        // checkbox to indicate whether a pattern is in progress or not
        mInProgress = (CheckBox)v.findViewById(R.id.pattern_in_progress);
        if (mFabric != null) {
            mInProgress.setChecked(mFabric.inUse());
            mInProgress.setEnabled(true);
        }
        mInProgress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                mFabric.setUse(checked);

                // if the box has been set and there is no existing start date, create one
                if (checked && mFabric.getStartDate() == null) {
                    mFabric.setStartDate(Calendar.getInstance());
                }

                updateFabricInfo();
            }
        });

        // editText for pattern width in stitches
        mWidthField = (EditText)v.findViewById(R.id.pattern_width);
        if (mPattern.getWidth() > 0) {
            mWidthField.setText(Integer.toString(mPattern.getWidth()));
        }
        mWidthField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (c.length() > 0) {
                    mPattern.setWidth(Integer.parseInt(c.toString()));
                } else {
                    // user removed width information
                    mPattern.setWidth(StashConstants.INT_ZERO);
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        // editText for pattern height in stitches
        mHeightField = (EditText)v.findViewById(R.id.pattern_height);
        if (mPattern.getHeight() > 0) {
            mHeightField.setText(Integer.toString(mPattern.getHeight()));
        }
        mHeightField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (c.length() > 0) {
                    mPattern.setHeight(Integer.parseInt(c.toString()));
                } else {
                    // user removed height information
                    mPattern.setHeight(StashConstants.INT_ZERO);
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        // button to allow selection of fabric linked to pattern
        mEditFabric = (ImageView)v.findViewById(R.id.pattern_fabric_edit);
        mEditFabric.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                PickOneDialogFragment dialog = PickOneDialogFragment.newInstance(R.array.fabric_choice_picker, 0);
                dialog.setDialogPickOneListener(mFragment);
                dialog.show(fm, DIALOG_FABRIC);
            }
        });

        mFabricInfo = (TextView)v.findViewById(R.id.pattern_fabric_display);
        mFabricDisplay = (ListView)v.findViewById(R.id.pattern_fabric_display_list);

        FabricAdapter currentFabricAdapter = new FabricAdapter(mCurrentFabric);
        mFabricDisplay.setAdapter(currentFabricAdapter);
        mFabricDisplay.setEmptyView(mFabricInfo);
        mFabricDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ViewHolder vh = (ViewHolder) view.getTag();

                // start StashFabricPagerActivity
                Intent intent = new Intent(getActivity(), StashFabricPagerActivity.class);
                intent.putExtra(StashFabricFragment.EXTRA_FABRIC_ID, vh.itemId);
                intent.putExtra(StashFabricFragment.EXTRA_TAB_ID, callingTab);
                startActivity(intent);
            }
        });

        mStartDateGroup = v.findViewById(R.id.pattern_start_group);

        mEditStartDate = (ImageView)v.findViewById(R.id.pattern_start_date_edit);
        mEditStartDate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Calendar calendar;

                if (mFabric.getStartDate() != null) {
                    calendar = mFabric.getStartDate();
                } else {
                    calendar = Calendar.getInstance();
                }

                DatePickerDialogFragment dialog = DatePickerDialogFragment.newInstance(calendar);
                dialog.setDatePickerDialogListener(mFragment);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        mStartDate = (TextView)v.findViewById(R.id.pattern_start_date);

        updateFabricInfo();

        // button to allow selection of threads used in pattern
        mEditThread = (ImageView)v.findViewById(R.id.pattern_thread_edit);
        mEditThread.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();

                SelectThreadQuantityDialogFragment dialog = SelectThreadQuantityDialogFragment.newInstance(StashData.get(getActivity()).getThreadList(), mPattern, getActivity());
                dialog.setSelectThreadQuantityDialogCallback(mFragment);
                dialog.show(fm, DIALOG_THREAD);
            }
        });

        mThreadDisplayList = (ListView)v.findViewById(R.id.pattern_thread_list);
        Collections.sort(mThreadList, new StashThreadComparator(getActivity()));
        ThreadAdapter adapter = new ThreadAdapter(mThreadList);
        mThreadDisplayList.setAdapter(adapter);
        mThreadDisplayList.setEmptyView(v.findViewById(R.id.pattern_thread_display));
        mThreadDisplayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ViewHolder vh = (ViewHolder)view.getTag();

                // start StashThreadPagerActivity
                Intent intent = new Intent(getActivity(), StashThreadPagerActivity.class);
                intent.putExtra(StashThreadFragment.EXTRA_THREAD_ID, vh.itemId);
                intent.putExtra(StashThreadFragment.EXTRA_TAB_ID, callingTab);
                startActivity(intent);
            }
        });

        // button to allow selection of threads used in pattern
        mEditEmbellishment = (ImageView)v.findViewById(R.id.pattern_embellishment_edit);
        mEditEmbellishment.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();

                // create a copy of the thread list to avoid modification errors
                SelectEmbellishmentQuantityDialogFragment dialog = SelectEmbellishmentQuantityDialogFragment.newInstance(StashData.get(getActivity()).getEmbellishmentList(), mPattern, getActivity());
                dialog.setSelectEmbellishmentQuantityDialogCallback(mFragment);
                dialog.show(fm, DIALOG_THREAD);
            }
        });

        mEmbellishmentDisplayList = (ListView)v.findViewById(R.id.pattern_embellishment_list);
        Collections.sort(mEmbellishmentList, new StashEmbellishmentComparator(getActivity()));
        EmbellishmentAdapter adapter1 = new EmbellishmentAdapter(mEmbellishmentList);
        mEmbellishmentDisplayList.setAdapter(adapter1);
        mEmbellishmentDisplayList.setEmptyView(v.findViewById(R.id.pattern_embellishment_display));
        mEmbellishmentDisplayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ViewHolder vh = (ViewHolder) view.getTag();

                // start StashThreadPagerActivity
                Intent intent = new Intent(getActivity(), StashEmbellishmentPagerActivity.class);
                intent.putExtra(StashEmbellishmentFragment.EXTRA_EMBELLISHMENT_ID, vh.itemId);
                intent.putExtra(StashEmbellishmentFragment.EXTRA_TAB_ID, callingTab);
                startActivity(intent);
            }
        });

        mFinishTitle = (TextView)v.findViewById(R.id.pattern_finish_list_title);
        mFinishList = (ListView) v.findViewById(R.id.pattern_finish_list);
        Collections.sort(mFinishesList, new StashFabricComparator(getActivity()));
        FabricAdapter adapter2 = new FabricAdapter(mFinishesList);
        mFinishList.setAdapter(adapter2);
        mFinishList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ViewHolder vh = (ViewHolder) view.getTag();

                // start StashFabricPagerActivity
                Intent intent = new Intent(getActivity(), StashFabricPagerActivity.class);
                intent.putExtra(StashFabricFragment.EXTRA_FABRIC_ID, vh.itemId);
                intent.putExtra(StashFabricFragment.EXTRA_TAB_ID, callingTab);
                startActivity(intent);
            }
        });

        // only shows the finishes list if there are actual finishes
        if (!mFinishesList.isEmpty()) {
            mFinishTitle.setVisibility(View.VISIBLE);
            mFinishList.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnChildren(mFinishList);
        }

        setListViewHeightBasedOnChildren(mFabricDisplay);
        setListViewHeightBasedOnChildren(mThreadDisplayList);
        setListViewHeightBasedOnChildren(mEmbellishmentDisplayList);

        return v;
    }

    // called when the user has made a selection in the fabric choice dialog box
    public void onSelectedOption(int selectedIndex) {
        if (selectedIndex == StashConstants.USE_EXISTING_FABRIC) {
            // user chose to use existing fabric
            Log.d(TAG, "User chose to use existing fabric");

            // get list of all fabric in the stash
            ArrayList<UUID> fabricStash = StashData.get(getActivity()).getStashFabricList();
            ArrayList<UUID> possibleFabrics = new ArrayList<UUID>();
            int previousFabric;

            // if fabric can fit the pattern, add to list for the adapter
            for (UUID fabricId : fabricStash) {
                StashFabric fabric = StashData.get(getActivity()).getFabric(fabricId);
                if (fabric.willFit(mPattern.getWidth(), mPattern.getHeight()) && !fabric.inUse()) {
                    possibleFabrics.add(fabricId);
                }
            }

            // note the position on the list of previously selected fabric if it exists
            if (mFabric != null) {
                previousFabric = possibleFabrics.indexOf(mFabric.getId());
            } else {
                previousFabric = -1;
            }

            // create dialog for selecting the fabric from existing fabrics
            FragmentManager fm = getActivity().getSupportFragmentManager();
            SelectFabricDialogFragment dialog = SelectFabricDialogFragment.newInstance(possibleFabrics, previousFabric);
            dialog.setSelectFabricDialogListener(mFragment);
            dialog.show(fm, DIALOG_FABRIC);
        } else if (selectedIndex == StashConstants.REMOVE_FABRIC) {
            // user chose to remove fabric

            mPattern.setFabric(null);
            mFabric.setUsedFor(null);
            mFabric.setUse(false);
            mFabric = null;
            mInProgress.setEnabled(false);

            updateFabricInfo();
        } else {
            // user chose to create new fabric
            Log.d(TAG, "User chose to create new fabric");

            if (mFabric != null) {
                // if current fabric exists, remove link to pattern
                mFabric.setUsedFor(null);
            }

            // create new fabric
            mFabric = new StashFabric(getActivity());
            StashData.get(getActivity()).addFabric(mFabric);

            // set links between fabric and pattern
            mFabric.setUsedFor(mPattern);
            mPattern.setFabric(mFabric);

            // start activity for user to enter information about fabric
            Intent i = new Intent(getActivity(), StashFabricPagerActivity.class);
            i.putExtra(StashFabricFragment.EXTRA_FABRIC_ID, mFabric.getId());
            startActivityForResult(i, REQUEST_PICK_NEW_FABRIC);
        }
    }

    // called when the user has picked a fabric from the list of fabrics that can fit the pattern
    public void onSelectedFabric(UUID fabricId) {
        if (mFabric == null || mFabric.getId() != fabricId) {
            // if user kept the same fabric, do nothing

            if (mFabric != null) {
                // if current fabric exists, remove link to pattern
                mFabric.setUsedFor(null);
                mCurrentFabric.clear();
            }

            // get the new fabric
            mFabric = StashData.get(getActivity()).getFabric(fabricId);

            if (mFabric.usedFor() != null) {
                // if the new fabric had a pattern associated with it, remove pattern link to fabric
                mFabric.usedFor().setFabric(null);
            }

            // set links between fabric and pattern
            mFabric.setUsedFor(mPattern);
            mPattern.setFabric(mFabric);
            mCurrentFabric.add(mFabric.getId());

            // update all displayed info
            updateFabricInfo();
            mCallback.updateFragments();
            mInProgress.setEnabled(true);
        }
    }

    // called once the user has updated the thread quantities to notify the pattern fragment to
    // refresh the display list
    public void onThreadQuantitiesUpdate() {
        Collections.sort(mThreadList, new StashThreadComparator(getActivity()));
        ((ThreadAdapter)mThreadDisplayList.getAdapter()).notifyDataSetChanged();
    }

    // called once the user has updated the embellishment quantities to notify the pattern fragment
    // to refresh the display list
    public void onEmbellishmentQuantitiesUpdate() {
        Collections.sort(mEmbellishmentList, new StashEmbellishmentComparator(getActivity()));
        ((EmbellishmentAdapter) mEmbellishmentDisplayList.getAdapter()).notifyDataSetChanged();
    }

    // called when the datepicker has been used to update the date displayed
    public void onDateSet(Calendar calendar) {
        mFabric.setStartDate(calendar);
        updateFabricInfo();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_PICK_NEW_FABRIC) {
            updateFabricInfo();
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            // showPhoto();
        }
    }

    // called when triggered by the observer to let the fragment know to refresh itself
    @Override
    public void update(Observable observable, Object data) {
        // refresh the fabric from the pattern data
        mFabric = mPattern.getFabric();
        if (mFabric == null) {
            mCurrentFabric.clear();
        } else if (mCurrentFabric.isEmpty()) {
            mCurrentFabric.add(mFabric.getId());
        } else if (mFabric.getId() != mCurrentFabric.get(0)) {
            mCurrentFabric.clear();
            mCurrentFabric.add(mFabric.getId());
        }

        // update the display
        updateFabricInfo();

        // update other display bits
        mIsKitted.setChecked(mPattern.isKitted());

        // update the finish display
        FabricAdapter adapter = (FabricAdapter)mFinishList.getAdapter();
        adapter.notifyDataSetChanged();
        if (!mFinishesList.isEmpty()) {
            mFinishTitle.setVisibility(View.VISIBLE);
            mFinishList.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnChildren(mFinishList);
        } else {
            mFinishTitle.setVisibility(View.INVISIBLE);
            mFinishList.setVisibility(View.INVISIBLE);
            setListViewHeightBasedOnChildren(mFinishList);
        }
    }

    // called any time the user needs to update the fabric info
    private void updateFabricInfo() {
        FabricAdapter adapter = (FabricAdapter)mFabricDisplay.getAdapter();
        adapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(mFabricDisplay);

        if (mFabric == null) {
            // calculate the size of fabric needed based on the defaults and update the empty listview
            // to show it
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            double edgeBuffer;

            try {
                edgeBuffer = Double.parseDouble(sharedPrefs.getString(StashPreferencesActivity.KEY_BORDER_SETTING, StashConstants.DEFAULT_BORDER));
            } catch (NumberFormatException e) {
                edgeBuffer = Double.parseDouble(StashConstants.DEFAULT_BORDER);
            }

            int defaultCount = Integer.parseInt(sharedPrefs.getString(StashPreferencesActivity.KEY_COUNT_SETTING, StashConstants.DEFAULT_COUNT));
            int overCount;

            if (sharedPrefs.getBoolean(StashPreferencesActivity.KEY_OVER_SETTING, true)) {
                overCount = StashConstants.OVER_TWO;
            } else {
                overCount = StashConstants.OVER_ONE;
            }

            double fabricWidth = mPattern.getWidth() / ((double) defaultCount / overCount) + StashConstants.TWO_BORDERS * edgeBuffer;
            double fabricHeight = mPattern.getHeight() / ((double) defaultCount / overCount) + StashConstants.TWO_BORDERS * edgeBuffer;

            mFabricInfo.setText(String.format(getString(R.string.pattern_no_fabric), defaultCount, overCount, edgeBuffer, fabricWidth, fabricHeight));
            mInProgress.setChecked(false);

            mStartDateGroup.setVisibility(View.GONE);
        } else {
            // determine which bits are needed to be displayed (depending on whether the mFabric is in use)
            if (mFabric.inUse()) {
                if (mFabric.getStartDate() != null) {
                    mStartDateGroup.setVisibility(View.VISIBLE);
                    Calendar date = mFabric.getStartDate();

                    String dateText = date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " " + date.get(Calendar.DAY_OF_MONTH) + ", " + date.get(Calendar.YEAR);
                    mStartDate.setText(dateText);
                } else {
                    mStartDate.setText(R.string.no_date_set);
                }
            } else {
                mStartDateGroup.setVisibility(View.GONE);
            }
        }
    }

/*    private void showPhoto() {
        StashPhoto photo = mPattern.getPhoto();

        if (photo != null) {
            String path = photo.getFilename();
            StashPhotoTask task = new StashPhotoTask(getActivity(), mViewPhoto, path);
            task.execute(mViewPhoto.getId());
        }
    }*/

    private void setListViewHeightBasedOnChildren(ListView listView) {
        // method modified from an answer here: http://stackoverflow.com/questions/3495890/how-can-i-put-a-listview-into-a-scrollview-without-it-collapsing/3495908#3495908
        // to set the height based on a maximum number of items on the listView
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        if (listAdapter.getCount() > StashConstants.DISPLAY_LIST_ITEMS) {
            for (int i = 0; i < StashConstants.DISPLAY_LIST_ITEMS; i++) {
                View listItem = listAdapter.getView(i, null, listView);
                if (listItem instanceof ViewGroup)
                    listItem.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
        } else {
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                if (listItem instanceof ViewGroup)
                    listItem.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private class ThreadAdapter extends ArrayAdapter<UUID> {

        public ThreadAdapter(ArrayList<UUID> threads) {
            super(getActivity(), StashConstants.NO_RESOURCE, threads);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_pattern_thread, null);

                ViewHolder vh = new ViewHolder();
                vh.info = (TextView)convertView.findViewById(R.id.pattern_thread_item_description);
                vh.quantity = (TextView)convertView.findViewById(R.id.pattern_thread_quantity);
                vh.type = (TextView)convertView.findViewById(R.id.pattern_thread_item_type);
                convertView.setTag(vh);
            }

            ViewHolder vh = (ViewHolder)convertView.getTag();

            // configure the view for this thread
            StashThread thread = StashData.get(getActivity()).getThread(getItem(position));

            vh.info.setText(thread.getSource() + " " + thread.getCode());
            vh.type.setText(thread.getType());
            vh.quantity.setText(Integer.toString(mPattern.getQuantity(thread)));
            vh.itemId = thread.getId();

            return convertView;
        }

    }

    private class EmbellishmentAdapter extends ArrayAdapter<UUID> {

        public EmbellishmentAdapter(ArrayList<UUID> embellishments) {
            super(getActivity(), StashConstants.NO_RESOURCE, embellishments);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_pattern_embellishment, null);

                ViewHolder vh = new ViewHolder();
                vh.info = (TextView)convertView.findViewById(R.id.pattern_embellishment_item_description);
                vh.quantity = (TextView)convertView.findViewById(R.id.pattern_embellishment_quantity);
                convertView.setTag(vh);
            }

            ViewHolder vh = (ViewHolder)convertView.getTag();

            // configure the view for this thread
            StashEmbellishment embellishment = StashData.get(getActivity()).getEmbellishment(getItem(position));

            vh.info.setText(embellishment.toString());
            vh.quantity.setText(Integer.toString(mPattern.getQuantity(embellishment)));
            vh.itemId = embellishment.getId();

            return convertView;
        }

    }

    private class FabricAdapter extends ArrayAdapter<UUID> {
        public FabricAdapter(ArrayList<UUID> fabrics) {
            super(getActivity(), StashConstants.NO_RESOURCE, fabrics);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_pattern_fabric, null);

                ViewHolder vh = new ViewHolder();
                vh.info = (TextView)convertView.findViewById(R.id.pattern_fabric_list_item_infoTextView);
                vh.type = (TextView)convertView.findViewById(R.id.pattern_fabric_list_item_sizeTextView);
                convertView.setTag(vh);
            }

            ViewHolder vh = (ViewHolder)convertView.getTag();

            // configure the view for this fabric
            StashFabric fabric = StashData.get(getActivity()).getFabric(getItem(position));

            vh.info.setText(fabric.getInfo());
            vh.type.setText(fabric.getSize());
            vh.itemId = fabric.getId();

            return convertView;
        }
    }

    private static class ViewHolder {
        public TextView info;
        public TextView quantity;
        public TextView type;
        public UUID itemId;
    }

}
