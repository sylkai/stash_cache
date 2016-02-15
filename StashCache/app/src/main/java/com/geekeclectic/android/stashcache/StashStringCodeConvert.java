package com.geekeclectic.android.stashcache;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sylk on 7/21/2015.
 */
public class StashStringCodeConvert {

    public boolean leadsWithDigit(String code) {
        if (code.length() > 0) {
            // called to determine if a string starts with a digit
            // from http://stackoverflow.com/questions/1223052/how-do-i-find-out-if-first-character-of-a-string-is-a-number
            char c = code.charAt(0);
            return (c >= '0' && c <= '9');
        } else {
            return false;
        }
    }

    public Integer numericCode(String code) {
        // only called if the string leads with a digit, so j should never be null on return
        // adapted from http://stackoverflow.com/questions/3552756/best-way-to-get-integer-part-of-the-string-600sp
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(code);
        Integer j = null;
        if (m.find()) {
            j = Integer.valueOf(m.group(1));
        }

        return j;
    }

}
