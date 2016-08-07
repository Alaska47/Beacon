package com.akotnana.beacon;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.makeramen.roundedimageview.RoundedImageView;
import com.wefika.flowlayout.FlowLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.akotnana.beacon.fileprovider";

    private LinearLayout imageContainerMain;
    private LinearLayout imageContainerDialog;
    private Button captureButton;
    private RoundedImageView rCaptureImage;
    private FlowLayout flowLayout;
    private ImageButton addButton;
    private MaterialDialog materialDialog;
    private Button tempButton;
    private EditText nextInterest;
    private String interestName;
    private LinearLayout tagsLayout;

    private List<Beacon> persons;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageContainerMain = (LinearLayout) findViewById(R.id.imageContainer);
        loadImages(imageContainerMain);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayCreateDialog(view);
            }
        });

        rv = (RecyclerView)findViewById(R.id.recycler_view);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "first");

        sequence.setConfig(config);

        sequence.addSequenceItem(imageContainerMain.getChildAt(0),
                "Images posted near your location", "GOT IT");

        sequence.addSequenceItem(rv,
                "Beacons that match your specified interests", "GOT IT");

        sequence.addSequenceItem(fab,
                "Add beacons at your current location", "GOT IT");

        sequence.start();

        Button but = (Button) findViewById(R.id.map_activity);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MapActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.pull_in_down, R.anim.push_out_up);
            }
        });

    }

    private void initializeData(){
        persons = new ArrayList<>();
        persons.add(new Beacon("Club", "#gay\t#gay\t#gay", R.drawable.red_pin));
        persons.add(new Beacon("Swimming", "#gay\t#gay\t#gay", R.drawable.red_pin));
        persons.add(new Beacon("Shwetark", "#gay\t#gay\t#gay", R.drawable.red_pin));
    }

    private void initializeAdapter(){
        RVAdapter adapter = new RVAdapter(persons);
        rv.setAdapter(adapter);
    }

    public void displayCreateDialog(View anchorView) {
        materialDialog = new MaterialDialog.Builder(this)
                .title("Create Beacon")
                .customView(R.layout.fragment_share, false)
                .positiveText("CREATE")
                .negativeText("CANCEL")
                .build();

        materialDialog.show();

        View view = materialDialog.getCustomView();
        captureButton = (Button) view.findViewById(R.id.photoCapture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeImageFromCamera(view);
            }
        });
        rCaptureImage = (RoundedImageView) view.findViewById(R.id.image_from_camera);
        addButton = (ImageButton) view.findViewById(R.id.add_button);
        flowLayout = (FlowLayout) view.findViewById(R.id.interest_layout);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addInterestToLayout();
            }
        });
        tagsLayout = (LinearLayout) view.findViewById(R.id.tags_layout);
    }

    public void addInterestToLayout() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Add Tag")
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
                        button.setLayoutParams(layoutParams);
                        view.removeView(button);
                        button.setText(interestName);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title("Delete Tag")
                                        .content("Are you sure you want to delete that tag?")
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
                        flowLayout.addView(button, 0);
                        float y = button.getY();
                        y += 5;
                        button.setY(y);
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

    public void takeImageFromCamera(View view) {
        File path = new File(getFilesDir(), "beacon_images/");
        if (!path.exists()) path.mkdirs();
        File image = new File(path, "image.jpg");
        Uri imageUri = FileProvider.getUriForFile(this, CAPTURE_IMAGE_FILE_PROVIDER, image);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    public void loadImages(LinearLayout imageContainer) {
        //TODO get images from server then display
        for(int i = 0; i < 5; i++) {
            RoundedImageView riv = new RoundedImageView(this);
            riv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            riv.setCornerRadius((float) 20);
            riv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.concert_crowd, null));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 5, 10, 5);
            imageContainer.addView(riv,layoutParams);
        }
    }

    public void addImageToContainer(LinearLayout imageContainer, Drawable item) {
        //TODO get images from server then display
        imageContainer.removeAllViews();
        RoundedImageView riv = new RoundedImageView(this);
        riv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        riv.setCornerRadius((float) 20);
        riv.setImageDrawable(item);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 5, 10, 5);
        imageContainer.addView(riv,layoutParams);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MainActivity", "called");
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                File path = new File(getFilesDir(), "beacon_images/");
                if (!path.exists()) path.mkdirs();
                File imageFile = new File(path, "image.jpg");
                // use imageFile to open your image
                Log.d("MainActivity", "" + imageFile.exists());
                Bitmap bitmap = decodeSampledBitmapFromFile(imageFile.getAbsolutePath(), 1000, 700);
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                rCaptureImage.setImageDrawable(d);
                captureButton.setText("Retake photo");
                tagsLayout.setVisibility(View.VISIBLE);
                rCaptureImage.setVisibility(View.VISIBLE);
            }
        }
    }

    public Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight)
        {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth)
        {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }
}
