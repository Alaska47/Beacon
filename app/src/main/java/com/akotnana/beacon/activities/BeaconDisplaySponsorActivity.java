package com.akotnana.beacon.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akotnana.beacon.R;
import com.akotnana.beacon.fragments.OverlapFragment;
import com.akotnana.beacon.utils.Beacon;
import com.akotnana.beacon.utils.Data;
import com.akotnana.beacon.utils.OnSwipeTouchListener;
import com.akotnana.beacon.utils.RVAdapter;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.List;

import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;

public class BeaconDisplaySponsorActivity extends AppCompatActivity {

    private FlowLayout flowLayout1;
    private ImageView heartButton;

    private List<Beacon> address;
    private RecyclerView rv;

    private Bitmap icon;
    private Drawable dd;

    private TextView sponsorDesc;

    public static Drawable[] covers = new Drawable[0];
    public static int[] likes = {1, 0, 5, 7, 10, 11, 6, 1};
    public static int currentPos = 0;
    public static String[] tags = {"data~werid~test", "data2~werid2~test2", "data3~werid3~test3", "data4~werid4~test4", "data5~werid5~test5", "data6~werid6~test6", "data7~werid7~test7", "data8~werid8~test8"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_display_sponsor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("McDonald's");

        rv = (RecyclerView)findViewById(R.id.recycler_view1);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();

        /*
        ProgressDialog pd = new ProgressDialog(BeaconDisplaySponsorActivity.this);
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.show();
           */
        sponsorDesc = (TextView) findViewById(R.id.sponsor_desc);
        sponsorDesc.setText("McDonald's is the world's largest chain of hamburger fast food restaurants, serving around 68 million customers daily in 119 countries across 36,538 outlets.");

        heartButton = (ImageView) findViewById(R.id.heart_view);
        icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.heart);
        dd = ResourcesCompat.getDrawable(getResources(), R.drawable.heart, null);
        dd = createMarkerIcon(dd, "" + likes[0], icon.getWidth(), icon.getHeight());
        heartButton.setImageDrawable(dd);

        PagerContainer pagerContainer = (PagerContainer) findViewById(R.id.pager_container);
        pagerContainer.setOverlapEnabled(true);

        final ViewPager viewPager = pagerContainer.getViewPager();
        final MyFragmentPagerAdapter pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount());
        viewPager.setAdapter(pagerAdapter);

        heartButton.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            public void onSwipeLeft() {
                //
            }

            public void onSwipeRight() {
                //
            }

            public void openTheThing() {
                Log.d("GAY", "HOMO");
                Snackbar.make(findViewById(R.id.gay_faf2), "Photo liked!", Snackbar.LENGTH_SHORT)
                        .show();
                likes[currentPos] += 1;
                dd = ResourcesCompat.getDrawable(getResources(), R.drawable.heart, null);
                dd = createMarkerIcon(dd, "" + likes[currentPos], icon.getWidth(), icon.getHeight());
                heartButton.setImageDrawable(dd);
            }

            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        pagerContainer.setLongClickable(true);
        pagerContainer.setClickable(true);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                dd = ResourcesCompat.getDrawable(getResources(), R.drawable.heart, null);
                dd = createMarkerIcon(dd, "" + likes[position], icon.getWidth(), icon.getHeight());
                heartButton.setImageDrawable(dd);
                currentPos = position;
                flowLayout1.removeAllViews();
                addTagsFromString(tags[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        new CoverFlow.Builder().with(viewPager)
                .scale(0.3f)
                .pagerMargin(getResources().getDimensionPixelSize(R.dimen.overlap_pager_margin))
                .spaceSize(0f)
                .build();


        //Manually setting the first View to be elevated
        viewPager.post(new Runnable() {
            @Override public void run() {
                Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                ViewCompat.setElevation(fragment.getView(), 8.0f);
            }
        });


        flowLayout1 = (FlowLayout) findViewById(R.id.interest_layout_inside);
        addTagsFromString("burger~fries~fast food~ronald~mcdonald");
    }

    private void initializeData(){
        address = new ArrayList<>();
        address.add(new Beacon("Address", "Fashion Centre at Pentagon City, 1100 S Hayes St, Arlington, VA 22202", R.drawable.red_pin));
    }

    private void initializeAdapter(){
        RVAdapter adapter = new RVAdapter(address);
        rv.setAdapter(adapter);
    }

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

    private void addTagsFromString(String dat) {
        String[] tags = dat.split("~");
        for(String interestName : tags) {
            LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.interest_button, null);
            final Button button = (Button) view.findViewById(R.id.temp_button);
            FlowLayout.LayoutParams layoutParams = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10,10,10,10);
            button.setPadding(43,43,43,43);
            button.setLayoutParams(layoutParams);
            view.removeView(button);
            button.setText(interestName);
            flowLayout1.addView(button, 0);
        }
    }

    private Drawable createMarkerIcon(Drawable backgroundImage, String text,
                                      int width, int height) {

        Bitmap canvasBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // Create a canvas, that will draw on to canvasBitmap.
        Canvas imageCanvas = new Canvas(canvasBitmap);

        // Set up the paint for use with our Canvas
        Paint imagePaint = new Paint();
        imagePaint.setColor(Color.WHITE);
        imagePaint.setTextAlign(Paint.Align.CENTER);
        imagePaint.setTextSize(50f);

        // Draw the image to our canvas
        backgroundImage.draw(imageCanvas);

        // Draw the text on top of our image
        imageCanvas.drawText(text, width / 2, height / 2 + 5, imagePaint);

        // Combine background and text to a LayerDrawable
        LayerDrawable layerDrawable = new LayerDrawable(
                new Drawable[]{backgroundImage, new BitmapDrawable(canvasBitmap)});
        return layerDrawable;
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public int currentPos;

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override public Fragment getItem(int position) {
            currentPos = position;
            return OverlapFragment.newInstance(Data.covers[position]);
        }

        public int getCurrentPos() {
            return currentPos;
        }

        @Override public int getCount() {
            return Data.covers.length;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_up, R.anim.push_out_down);
    }

    class BeaconRetriever extends AsyncTask< String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String...params) {
            String message = "";
            return message;
        }

        protected void onPostExecute(Boolean result) {

        }
    }
}
