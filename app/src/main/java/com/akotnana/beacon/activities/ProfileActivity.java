package com.akotnana.beacon.activities;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akotnana.beacon.R;
import com.wefika.flowlayout.FlowLayout;

public class ProfileActivity extends AppCompatActivity {

    private FlowLayout flowLayout;
    private ImageButton addButton;
    private Button tempButton;
    private EditText nextInterest;
    private String interestName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle("Profile");

        flowLayout = (FlowLayout) findViewById(R.id.interest_layout);
        addButton = (ImageButton) findViewById(R.id.add_button);
        tempButton = (Button) findViewById(R.id.temp_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addInterestToLayout();
            }
        });
    }

    public void addInterestToLayout() {
        MaterialDialog dialog = new MaterialDialog.Builder(ProfileActivity.this)
                .title("Add Interest")
                .customView(R.layout.search_simple, false)
                .positiveText("Add")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        interestName = nextInterest.getText().toString();
                        LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.interest_button, null);
                        final Button button = (Button) view.findViewById(R.id.temp_button);
                        FlowLayout.LayoutParams layoutParams = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(10,10,10,10);
                        button.setPadding(43,43,43,43);
                        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        button.setLayoutParams(layoutParams);
                        view.removeView(button);
                        button.setText(interestName);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new MaterialDialog.Builder(ProfileActivity.this)
                                        .title("Delete Interest")
                                        .content("Are you sure you want to delete that interest?")
                                        .positiveText("Delete")
                                        .negativeText("Cancel")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                flowLayout.removeView(button);
                                            }
                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .show();
                            }
                        });

                        /*
                        final Button button = new Button(getContext());
                        button.setText(interestName);
                        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        int s = dpToPx(5);
                        params.setMargins(s, s, s, s);
                        button.setGravity(Gravity.CENTER);
                        button.setLayoutParams(params);
                        button.setClickable(true);
                        button.setBackgroundResource(R.drawable.rounded_button);
                        button.setIncludeFontPadding(false);
                        */

                        flowLayout.addView(button, 0);
                        button.setY(button.getY() + 5);

                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .build();
        View inflated = dialog.getCustomView();
        nextInterest = (EditText) inflated.findViewById(R.id.next_interest);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class UserUpdater extends AsyncTask< String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String...params) {
            return "";
        }

        protected void onPostExecute(Boolean result) {

        }
    }
}
