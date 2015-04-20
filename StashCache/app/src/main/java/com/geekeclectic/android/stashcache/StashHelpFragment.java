package com.geekeclectic.android.stashcache;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by sylk on 4/20/2015.
 */
public class StashHelpFragment extends Fragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                // turn on up navigation
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        TextView linkText = (TextView) v.findViewById(R.id.help_intro);
        Linkify.addLinks(linkText, Linkify.ALL);
        linkText.setMovementMethod(LinkMovementMethod.getInstance());
        linkText.setText(Html.fromHtml(getString(R.string.help_intro)));

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                // set up navigation
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    Intent i = new Intent(getActivity(), StashOverviewActivity.class);

                    // note which category is calling the up to display appropriate fragment (pattern)
                    i.putExtra(StashOverviewActivity.EXTRA_VIEW_ID, StashConstants.PATTERN_VIEW);
                    i.putExtra(StashOverviewActivity.EXTRA_FRAGMENT_ID, StashConstants.STASH_TAB);

                    NavUtils.navigateUpTo(getActivity(), i);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
