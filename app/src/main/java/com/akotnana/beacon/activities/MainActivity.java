package com.akotnana.beacon.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akotnana.beacon.R;
import com.akotnana.beacon.utils.Beacon;
import com.akotnana.beacon.utils.RVAdapter;
import com.makeramen.roundedimageview.RoundedImageView;
import com.wefika.flowlayout.FlowLayout;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        getSupportActionBar().setTitle("Beacon");
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
        persons.add(new Beacon("IncubateDC", "#hack   #hackathon   #entrepreneurship   #android", R.drawable.red_pin));
        persons.add(new Beacon("Bubble RUN Washington D.C!", "#run   #fitness", R.drawable.red_pin));
        persons.add(new Beacon("2016 Summer Spirit Festival", "#festival   #fair", R.drawable.red_pin));
        persons.add(new Beacon("Timbers vs DC United Soccer Game", "#soccer   #tickets   #dcunited", R.drawable.red_pin));
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
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        File path = new File(getFilesDir(), "beacon_images/");
                        if (!path.exists()) path.mkdirs();
                        File image = new File(path, "image.jpg");
                        try {
                            new ImageSender().execute(image.getAbsolutePath()).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                })
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Bitmap bitmapSizeByScale( Bitmap bitmapIn, float scall_zero_to_one_f) {

        Bitmap bitmapOut = Bitmap.createScaledBitmap(bitmapIn,
                Math.round(bitmapIn.getWidth() * scall_zero_to_one_f),
                Math.round(bitmapIn.getHeight() * scall_zero_to_one_f), false);

        return bitmapOut;
    }

    class ImageRetriever extends AsyncTask< Void, Void, Drawable[] > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Drawable[] doInBackground(Void...params) {
            Socket socket = null;
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            Drawable[] dd = null;
            try {
                socket = new Socket("alaskapi4713.ddns.net", 1337);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                oos.writeObject(1); //case1
                int numImages = (int) ois.readObject();
                dd = new Drawable[numImages];
                for(int i = 0; i < numImages; i++) {
                    byte[] bb = (byte[]) ois.readObject();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bb , 0, bb .length);
                    Drawable d = new BitmapDrawable(getResources(), bitmap);
                    dd[i] = d;
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if(ois != null)
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if(oos != null)
                    try {
                        oos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return dd;
        }



        protected void onPostExecute(Boolean result) {

        }
    }

    class ImageSender extends AsyncTask< String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String...params) {
            Socket socket = null;
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            String message = "";
            try {
                socket = new Socket("172.29.24.149", 25565);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                String fin = "5 "+ "34.48713" +" "+ "72.219310" +" "+"gay" + " " + "yash" + " " + "homo";
                oos.writeObject(fin);
                Bitmap myBitmap = BitmapFactory.decodeFile(params[0]);
                myBitmap = bitmapSizeByScale(myBitmap, 0.1f);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Log.d("ImageSender", "" + byteArray.length);
                oos.writeObject(byteArray);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(ois != null)
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if(oos != null)
                    try {
                        oos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return "";

        }

        protected void onPostExecute(boolean result) {

        }
    }


}
