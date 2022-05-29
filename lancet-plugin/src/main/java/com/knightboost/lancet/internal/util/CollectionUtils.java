package com.knightboost.lancet.internal.util;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Chris Nokleberg
 * @version $Id: CollectionUtils.java,v 1.7 2004/06/24 21:15:21 herbyderby Exp $
 */
public class CollectionUtils {
    private CollectionUtils() { }


    public static void reverse(Map source, Map target) {
        for (Iterator it = source.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            target.put(source.get(key), key);
        }
    }

}
