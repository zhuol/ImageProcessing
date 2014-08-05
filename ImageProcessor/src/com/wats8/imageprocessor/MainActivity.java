
package com.wats8.imageprocessor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import me.kiip.sdk.Kiip;
import me.kiip.sdk.Poptart;

public class MainActivity extends BaseActivity {

    // Static final constants
    private static final int SELECT_PICTURE = 1;
    private static final int DEFAULT_ASPECT_RATIO_VALUES = 10;
    private static final int ROTATE_NINETY_DEGREES = 90;
    private static final String ASPECT_RATIO_X = "ASPECT_RATIO_X";
    private static final String ASPECT_RATIO_Y = "ASPECT_RATIO_Y";
    private static final int ON_TOUCH = 1;

    // Instance variables
    private int mAspectRatioX = DEFAULT_ASPECT_RATIO_VALUES;
    private int mAspectRatioY = DEFAULT_ASPECT_RATIO_VALUES;

    Bitmap croppedImage;

    // Saves the state upon rotating the screen/restarting the activity
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(ASPECT_RATIO_X, mAspectRatioX);
        bundle.putInt(ASPECT_RATIO_Y, mAspectRatioY);
    }

    // Restores the state upon rotating the screen/restarting the activity
    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        mAspectRatioX = bundle.getInt(ASPECT_RATIO_X);
        mAspectRatioY = bundle.getInt(ASPECT_RATIO_Y);
    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // Sets fonts for all
        Typeface mFont = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
        ViewGroup root = (ViewGroup) findViewById(R.id.mylayout);
        setFont(root, mFont);

        // Initialize components of the app
        final CropImageView cropImageView = (CropImageView) findViewById(R.id.CropImageView);
        cropImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });

        final SeekBar aspectRatioXSeek = (SeekBar) findViewById(R.id.aspectRatioXSeek);
        final SeekBar aspectRatioYSeek = (SeekBar) findViewById(R.id.aspectRatioYSeek);
        final ToggleButton fixedAspectRatioToggle = (ToggleButton) findViewById(R.id.fixedAspectRatioToggle);
        final LinearLayout fixedAspectRatioLayout = (LinearLayout) this.findViewById(R.id.fixedAspectRatioLayout);
        Spinner showGuidelinesSpin = (Spinner) findViewById(R.id.showGuidelinesSpin);

        // Disable fixedAspectRatioLayout by default
        fixedAspectRatioLayout.setVisibility(LinearLayout.GONE);

        // Sets sliders to be disabled until fixedAspectRatio is set
        aspectRatioXSeek.setEnabled(false);
        aspectRatioYSeek.setEnabled(false);

        // Set initial spinner value
        showGuidelinesSpin.setSelection(ON_TOUCH);

        //Sets the rotate button
        final Button rotateButton = (Button) findViewById(R.id.Button_rotate);
        rotateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(ROTATE_NINETY_DEGREES);
            }
        });

        // Sets fixedAspectRatio
        fixedAspectRatioToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cropImageView.setFixedAspectRatio(isChecked);
                if (isChecked) {
                    fixedAspectRatioLayout.setVisibility(LinearLayout.VISIBLE);
                    aspectRatioXSeek.setEnabled(true);
                    aspectRatioYSeek.setEnabled(true);
                } else {
                    fixedAspectRatioLayout.setVisibility(LinearLayout.GONE);
                    aspectRatioXSeek.setEnabled(false);
                    aspectRatioYSeek.setEnabled(false);
                }
            }
        });

        // Sets initial aspect ratio to 10/10, for demonstration purposes
        cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);

        // Sets aspectRatioX
        final TextView aspectRatioX = (TextView) findViewById(R.id.aspectRatioX);

        aspectRatioXSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar aspectRatioXSeek, int progress, boolean fromUser) {
                try {
                    mAspectRatioX = progress;
                    cropImageView.setAspectRatio(progress, mAspectRatioY);
                    aspectRatioX.setText(" " + progress);
                } catch (IllegalArgumentException e) {
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Sets aspectRatioY
        final TextView aspectRatioY = (TextView) findViewById(R.id.aspectRatioY);

        aspectRatioYSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar aspectRatioYSeek, int progress, boolean fromUser) {
                try {
                    mAspectRatioY = progress;
                    cropImageView.setAspectRatio(mAspectRatioX, progress);
                    aspectRatioY.setText(" " + progress);
                } catch (IllegalArgumentException e) {
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        // Sets up the Spinner
        showGuidelinesSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cropImageView.setGuidelines(i);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        // Crop image
        final Button cropButton = (Button) findViewById(R.id.Button_crop);
        cropButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                croppedImage = cropImageView.getCroppedImage();
                ImageView croppedImageView = (ImageView) findViewById(R.id.croppedImageView);

                croppedImageView.setVisibility(View.VISIBLE);
                croppedImageView.setImageBitmap(croppedImage);
            }
        });

        // Hide cropped image
        final Button hideCropButton = (Button) findViewById(R.id.Button_HideShowCopper);
        hideCropButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                croppedImage = cropImageView.getCroppedImage();
                ImageView croppedImageView = (ImageView) findViewById(R.id.croppedImageView);

                if (croppedImageView.getVisibility() == View.GONE) {
                    croppedImageView.setVisibility(View.VISIBLE);
                } else {
                    croppedImageView.setVisibility(View.GONE);
                }
            }
        });

        // Save Image into Gallery
        final Button saveButton = (Button) findViewById(R.id.Button_Save);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                croppedImage = cropImageView.getCroppedImage();
                savePhoto(croppedImage);

                //saveIntoGallery(croppedImage);
            }
        });
    }

    /*
     * Sets the font on all TextViews in the ViewGroup. Searches recursively for
     * all inner ViewGroups as well. Just add a check for any other views you
     * want to set as well (EditText, etc.)
     */
    public void setFont(ViewGroup group, Typeface font) {
        int count = group.getChildCount();
        View v;
        for (int i = 0; i < count; i++) {
            v = group.getChildAt(i);
            if (v instanceof TextView || v instanceof EditText || v instanceof Button) {
                ((TextView) v).setTypeface(font);
            } else if (v instanceof ViewGroup)
                setFont((ViewGroup) v, font);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Used to save image
    @TargetApi(Build.VERSION_CODES.FROYO)
    public void savePhoto(Bitmap bmp) {
        /*File imageFileFolder = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Pictures"); //new File(Environment.getExternalStorageDirectory(), "ImageProcessor");*/
        File imageFileFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                getApplicationContext().getPackageName() + "/Pictures");
        if (!imageFileFolder.exists()) {
            imageFileFolder.mkdirs();
        }
        //File imageFileFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        FileOutputStream out = null;
        Calendar c = Calendar.getInstance();
        String date = fromInt(c.get(Calendar.MONTH))
                + fromInt(c.get(Calendar.DAY_OF_MONTH))
                + fromInt(c.get(Calendar.YEAR))
                + fromInt(c.get(Calendar.HOUR_OF_DAY))
                + fromInt(c.get(Calendar.MINUTE))
                + fromInt(c.get(Calendar.SECOND));
        File imageFileName = new File(imageFileFolder, date.toString() + ".jpg");
        try {
            out = new FileOutputStream(imageFileName);
            //out = getApplicationContext().openFileOutput(date.toString(), Context.MODE_PRIVATE);

            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update image into gallery by using public directory and absolute path
        saveIntoGallery(imageFileName.getAbsolutePath());

        callKiipDamn();
    }

    private String fromInt(int val) {
        return String.valueOf(val);
    }

    private void saveIntoGallery(String mCurrentPhotoPath) {
        //Save into Gallery
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void callKiipDamn(){
        Kiip.getInstance().saveMoment("SB_Moment", new Kiip.Callback() {
            @Override
            public void onFinished(Kiip kiip, Poptart reward) {
                onPoptart(reward);
            }

            @Override
            public void onFailed(Kiip kiip, Exception exception) {
                // handle failure
            }
        });

        Kiip.OnContentListener onContentListener = new Kiip.OnContentListener() {
            @Override
            public void onContent(Kiip kiip, String momentId, int quantity, String transactionId,
                                  String signature) {
                // Handle receiving virtual reward. Increment users wallet with quantity etc.
            }
        };
        Kiip.getInstance().setOnContentListener(onContentListener);
    }
}
