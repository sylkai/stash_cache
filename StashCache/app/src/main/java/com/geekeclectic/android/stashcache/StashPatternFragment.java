package com.geekeclectic.android.stashcache;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

public class StashPatternFragment extends Fragment implements PickOneDialogFragment.OnDialogPickOneListener, SelectFabricDialogFragment.SelectFabricDialogListener, SelectThreadDialogFragment.SelectThreadDialogListener, Observer {

    public static final String EXTRA_PATTERN_ID = "com.geekeclectic.android.stashcache.pattern_id";
    public static final String TAG = "StashPatternFragment";
    private static final int REQUEST_PICK_NEW_FABRIC = 0;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_OK = 0;

    private static final String DIALOG_FABRIC = "fabric";
    private static final String DIALOG_THREAD = "thread";
    private static final int CATEGORY_ID = 0;

    private StashPattern mPattern;
    private StashPatternFragment mFragment;
    private StashFabric mFabric;
    private ArrayList<UUID> mThreadList;
    private UUID mPatternId;
    private EditText mTitleField;
    private EditText mSourceField;
    private EditText mWidthField;
    private EditText mHeightField;
    private CheckBox mIsKitted;
    private ImageView mViewPhoto;
    private ImageView mEditPhoto;
    private ImageView mEditFabric;
    private ImageView mEditThread;
    private TextView mFabricInfo;
    private TextView mThreadInfo;
    private ChangedFragmentListener mCallback;

    private String mPhotoPath;

    public interface ChangedFragmentListener {
        public void updateFragments();
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
        mPattern = StashData.get(getActivity()).getPattern(mPatternId);

        // set reference to fragment for setting listeners
        mFragment = this;

        mFabric = mPattern.getFabric();
        mThreadList = mPattern.getThreadList();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (ChangedFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ChangedFragmentListener");
        }
    }

    public static StashPatternFragment newInstance(UUID patternId) {
        // associate patternId with the fragment through arguments
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PATTERN_ID, patternId);

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
                    Intent i = new Intent(getActivity(), StashOverviewPagerActivity.class);

                    // note which category is calling the up to display appropriate fragment (pattern)
                    i.putExtra(StashOverviewPagerActivity.EXTRA_FRAGMENT_ID, CATEGORY_ID);
                    NavUtils.navigateUpTo(getActivity(), i);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

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
                mPattern.setPatternName(c.toString());
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
                mPattern.setSource(c.toString());
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
        mIsKitted.setChecked(mPattern.getKitted());
        mIsKitted.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                mPattern.setKitted(checked);
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
                    mPattern.setWidth(0);
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
                    mPattern.setHeight(0);
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
        updateFabricInfo();

        // button to allow selection of threads used in pattern
        mEditThread = (ImageView)v.findViewById(R.id.pattern_thread_edit);
        mEditThread.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();

                // create a copy of the thread list to avoid modification errors
                SelectThreadDialogFragment dialog = SelectThreadDialogFragment.newInstance(StashData.get(getActivity()).getThreadList(), new ArrayList<UUID>(mThreadList));
                dialog.setSelectThreadDialogListener(mFragment);
                dialog.show(fm, DIALOG_THREAD);
            }
        });

        mThreadInfo = (TextView)v.findViewById(R.id.pattern_thread_display);
        updateThreadInfo();

        return v;
    }

    public void onSelectedOption(int selectedIndex) {
        if (selectedIndex == 0) {
            // user chose to use existing fabric
            Log.d(TAG, "User chose to use existing fabric");

            // get list of all fabric
            ArrayList<UUID> fabricStash = StashData.get(getActivity()).getFabricList();
            ArrayList<UUID> possibleFabrics = new ArrayList<UUID>();
            int previousFabric;

            // if fabric can fit the pattern, add to list for the adapter
            for (UUID fabricId : fabricStash) {
                StashFabric fabric = StashData.get(getActivity()).getFabric(fabricId);
                if (fabric.willFit(mPattern.getWidth(), mPattern.getHeight())) {
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
        } else if (selectedIndex == 2) {
            // user chose to remove fabric

            mPattern.setFabric(null);
            mFabric.setUsedFor(null);
            mFabric = null;

            updateFabricInfo();
        } else {
            // user chose to create new fabric
            Log.d(TAG, "User chose to create new fabric");

            if (mFabric != null) {
                // if current fabric exists, remove link to pattern
                mFabric.setUsedFor(null);
            }

            // create new fabric
            mFabric = new StashFabric();
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

    public void onSelectedFabric(UUID fabricId) {
        if (mFabric == null || mFabric.getId() != fabricId) {
            // if user kept the same fabric, do nothing

            if (mFabric != null) {
                // if current fabric exists, remove link to pattern
                mFabric.setUsedFor(null);
            }

            mFabric = StashData.get(getActivity()).getFabric(fabricId);

            if (mFabric.usedFor() != null) {
                // if the new fabric had a pattern associated with it, remove pattern link to fabric
                mFabric.usedFor().setFabric(null);
            }

            // set links between fabric and pattern
            mFabric.setUsedFor(mPattern);
            mPattern.setFabric(mFabric);

            updateFabricInfo();
            mCallback.updateFragments();
        }
    }

    public void onThreadsSelected(ArrayList<UUID> selectedThreads) {
        ArrayList<UUID> removeThreads = new ArrayList<UUID>();

        for (UUID threadId : mThreadList) {
            if (selectedThreads.contains(threadId)) {
                // was and is still on the list, no changes needed
                selectedThreads.remove(threadId);
            } else {
                // is no longer on the list, note that it needs to be removed & info updated
                removeThreads.add(threadId);
            }
        }

        // selectedThreads now contains just new additions, removeThreads just threads that need
        // removal from the list

        for (UUID threadId : removeThreads) {
            // remove association to pattern
            StashThread thread = StashData.get(getActivity()).getThread(threadId);
            thread.removePattern(mPattern);

            // remove from list
            mPattern.removeThread(thread);
        }

        for (UUID threadId : selectedThreads) {
            // add association to pattern
            StashThread thread = StashData.get(getActivity()).getThread(threadId);
            thread.usedInPattern(mPattern);

            // add to list
            mPattern.addThread(thread);
        }

        updateThreadInfo();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_PICK_NEW_FABRIC) {
            updateFabricInfo();
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            showPhoto();
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        // refresh the fabric from the pattern data
        mFabric = mPattern.getFabric();

        // update the display
        updateFabricInfo();
    }

    private void updateFabricInfo() {
        if (mFabric != null) {
            mFabricInfo.setText(mFabric.getInfo() + "\n");
            mFabricInfo.append(mFabric.getSize());
        } else {
            mFabricInfo.setText(R.string.pattern_no_fabric);
        }
    }

    private void updateThreadInfo() {
        if (mThreadList.size() > 0) {
            mThreadInfo.setText("");

            for (UUID threadId : mThreadList) {
                StashThread thread = StashData.get(getActivity()).getThread(threadId);
                mThreadInfo.append(thread.toString() + "\n");
            }
        } else {
            mThreadInfo.setText(R.string.pattern_no_thread);
        }
    }

    private void showPhoto() {
        StashPhoto photo = mPattern.getPhoto();

        if (photo != null) {
            String path = photo.getFilename();
            StashPhotoTask task = new StashPhotoTask(getActivity(), mViewPhoto, path);
            task.execute(mViewPhoto.getId());
        }
    }

}
