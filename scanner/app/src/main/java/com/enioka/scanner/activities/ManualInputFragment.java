package com.enioka.scanner.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;

import com.enioka.scanner.R;
import com.enioka.scanner.api.Scanner;
import com.enioka.scanner.data.Barcode;
import com.enioka.scanner.data.BarcodeType;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper fragment designed to take soft keyboard input (in case camera or laser are broken).
 * Hooked on the Scanner.ScannerDataCallback interface which the main activity should already implement.
 */
public class ManualInputFragment extends DialogFragment {

    private Scanner.ScannerDataCallback cb;
    private Boolean closeOnValidation;
    private int inviteTextId;
    protected List<ManualInputItem> items = new ArrayList<>();
    protected int threshold = 5;
    protected DialogInterface di;

    public void setAutocompletion(List<String> autocompletion, int threshold) {
        this.setAutocompletion(autocompletion, null, threshold);
    }

    public void setAutocompletion(List<String> autocompletion, List<Boolean> autocompletionDoneItems, int threshold) {
        boolean useDoneItems = autocompletionDoneItems != null && autocompletionDoneItems.size() == autocompletion.size();

        // Build array of items
        for (int i = 0; i < autocompletion.size(); i++) {
            if (useDoneItems) {
                items.add(new ManualInputItem(autocompletion.get(i), autocompletionDoneItems.get(i)));
            } else {
                items.add(new ManualInputItem(autocompletion.get(i), false));
            }
        }
        this.threshold = threshold;
    }

    public static ManualInputFragment newInstance() {
        return newInstance(true, R.string.fragment_scan_manual_invite);
    }

    public static ManualInputFragment newInstance(Boolean closeOnValidation) {
        return newInstance(closeOnValidation, R.id.scanner_manual_invite);
    }

    public static ManualInputFragment newInstance(Boolean closeOnValidation, int inviteTextId) {
        ManualInputFragment res = new ManualInputFragment();
        Bundle b = new Bundle(1);
        b.putBoolean("CLOSE_ON_VALIDATION", closeOnValidation);
        b.putInt("INVITE_TEXT_ID", inviteTextId);
        res.setArguments(b);
        return res;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            // The host should be an activity implementing a barcode listener.
            cb = (Scanner.ScannerDataCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Scanner.ScannerDataCallback");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        closeOnValidation = getArguments().getBoolean("CLOSE_ON_VALIDATION", true);
        inviteTextId = getArguments().getInt("INVITE_TEXT_ID", R.string.fragment_scan_manual_invite);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_scan_manual_input, null);
        builder.setView(view);


        final Dialog res = builder.create();

        ArrayAdapter<ManualInputItem> adapter = new ManualInputAdapter(this.getContext(), R.id.textViewItem, items);

        AutoCompleteTextView textView = (AutoCompleteTextView) view.findViewById(R.id.scanner_manual_input);
        textView.setThreshold(this.threshold);
        textView.setAdapter(adapter);
        Log.v("ManualInput", "Set " + items.size() + " autocompletion elements");

        res.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        view.findViewById(R.id.scanner_manual_bt_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = ((TextView) view.findViewById(R.id.scanner_manual_input)).getText().toString();
                if (txt.isEmpty()) {
                    return;
                }

                List<Barcode> res = new ArrayList<Barcode>(1);
                Barcode b = new Barcode(txt, BarcodeType.UNKNOWN);
                res.add(b);
                cb.onData(null, res);

                // Do not always dismiss - let the host do it in some cases (it may want to validate the data).
                if (closeOnValidation) {
                    dismiss();
                }

            }
        });

        ((AutoCompleteTextView) view.findViewById(R.id.scanner_manual_input)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String txt = ((TextView) view.findViewById(R.id.scanner_manual_input)).getText().toString();
                    if (txt.isEmpty()) {
                        return false;
                    }

                    List<Barcode> res = new ArrayList<Barcode>(1);
                    Barcode b = new Barcode(txt, BarcodeType.UNKNOWN);
                    res.add(b);
                    cb.onData(null, res);

                    if (closeOnValidation) {
                        dismiss();
                    }

                    return true;
                }
                return false;
            }
        });

        ((TextView) view.findViewById(R.id.scanner_manual_invite)).setText(getResources().getString(inviteTextId));

        return res;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (di != null) {
            di.dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (di != null) {
            di.cancel();
        }
    }

    public void setDialogInterface(DialogInterface di) {
        this.di = di;
    }


    /**
     *
     */
    public class ManualInputItem {
        private String text;
        private boolean done;

        public ManualInputItem(String text, boolean done) {
            this.text = text;
            this.done = done;
        }

        public boolean isDone() {
            return this.done;
        }

        public String getText() {
            return this.text;
        }
    }

    /**
     * Custom array adapter for AutoCompleteTextView
     * Allows to declare some items as "done" : green text and tick icon on the left
     */
    public class ManualInputAdapter extends ArrayAdapter<ManualInputItem> {
        private LayoutInflater layoutInflater;
        List<ManualInputItem> items;

        private Filter filter = new Filter() {
            @Override
            public String convertResultToString(Object resultValue) {
                return ((ManualInputItem) resultValue).getText();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint != null) {
                    ArrayList<ManualInputItem> suggestions = new ArrayList<>();
                    for (ManualInputItem item : items) {
                        if (item.getText().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(item);
                        }
                    }

                    results.values = suggestions;
                    results.count = suggestions.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                if (results != null && results.count > 0) {
                    // we have filtered results
                    addAll((ArrayList<ManualInputItem>) results.values);
                }
                notifyDataSetChanged();
            }
        };

        ManualInputAdapter(Context context, int textViewResourceId, List<ManualInputItem> items) {
            super(context, textViewResourceId, items);
            // copy all the customers into a master list
            this.items = new ArrayList<ManualInputItem>(items.size());
            this.items.addAll(items);
            layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                view = layoutInflater.inflate(R.layout.dropdown_item, null);
            }

            ManualInputItem item = getItem(position);

            TextView textView = (TextView) view.findViewById(R.id.textViewItem);
            textView.setText(item.getText());

            if(item.isDone()) {
                textView.setTextColor(getResources().getColor(R.color.doneItemColor));
                view.findViewById(R.id.doneImageView).setVisibility(View.VISIBLE);
            } else {
                textView.setTextColor(getResources().getColor(R.color.defaultItemColor));
                view.findViewById(R.id.doneImageView).setVisibility(View.INVISIBLE);
            }

            return view;
        }

        @Override
        public Filter getFilter() {
            return filter;
        }
    }

}
