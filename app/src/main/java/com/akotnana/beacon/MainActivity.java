package com.akotnana.beacon;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.makeramen.roundedimageview.RoundedImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayout imageContainer = (LinearLayout) findViewById(R.id.imageContainer);
        addImageToContainer(imageContainer);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "TODO: new picture upload thing", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }

    public void addImageToContainer(LinearLayout imageContainer) {
        //TODO get images from server then display
        for(int i = 0; i < 5; i++) {
            RoundedImageView riv = new RoundedImageView(this);
            riv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            riv.setCornerRadius((float) 5);
            riv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.concert_crowd, null));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 5, 10, 5);
            imageContainer.addView(riv,layoutParams);
        }
    }

}
