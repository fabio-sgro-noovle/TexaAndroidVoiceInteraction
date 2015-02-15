package it.noovle.android.texaandroidvoiceinteraction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by fabiosgro on 15/02/15.
 */

public class RisultatiAdapter extends ArrayAdapter<Risultato> {
    public RisultatiAdapter(Context context, ArrayList<Risultato> risultati) {
        super(context, 0, risultati);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Risultato r = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.risultato_layout, parent, false);
        }
        // Lookup view for data population
        TextView numTV = (TextView) convertView.findViewById(R.id.numResult);
        TextView titleTV = (TextView) convertView.findViewById(R.id.titleResult);
        // Populate the data into the template view using the data object
        numTV.setText(r.getNum());
        titleTV.setText(r.getTitolo());
        // Return the completed view to render on screen
        return convertView;
    }
}
