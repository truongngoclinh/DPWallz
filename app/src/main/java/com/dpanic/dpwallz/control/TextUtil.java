package com.dpanic.dpwallz.control;

import com.dpanic.dpwallz.util.Constants;

/**
 * Created by dpanic on 12/10/2016.
 * Project: DPWallz
 */

public class TextUtil {

    public static String getSearchLink(String query) {
        String submitQuery = query.trim().replace(" ", "%20");
        submitQuery = submitQuery.replace("#", "%23");
        return Constants.SEARCH_PREFIX_LINK + submitQuery + "/";
    }

    public static String getColorFromSearchText(String searchText) {
        return searchText.substring(searchText.indexOf("#"));
    }
}
