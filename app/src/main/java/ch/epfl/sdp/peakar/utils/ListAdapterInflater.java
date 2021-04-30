package ch.epfl.sdp.peakar.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Class to inflate an layout. Used to create item layouts in list adapters.
 */
public class ListAdapterInflater {

    /**
     * Create a layout
     * @param layout layout to create
     * @param context the context
     * @param parent layouts parent
     * @return an new created layout
     */
    public static View createLayout(int layout, Context context, ViewGroup parent){
        LayoutInflater vi;
        vi = LayoutInflater.from(context);
        return vi.inflate(layout, parent, false);
    }
}
