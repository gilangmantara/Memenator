package co.mantara.memenator;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.jakewharton.rxbinding.widget.RxTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import co.mantara.memenator.util.Constanta;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func2;

/**
 * Created by Gilang on 04/09/2016.
 * gilangmantara@gmail.com
 */
public class GeneratorActivity extends AppCompatActivity {

    public static final String PARAM_PICKER = "param_picker";
    private static final int IMAGE_GALLERY = 1;
    private static final int IMAGE_CAMERA = 2;

    private ImageView imageView;
    private EditText etTop, etBottom;
    private SeekBar sbTop, sbBot;
    private FloatingActionButton btnSave;

    private Bitmap imageOriginal = null;
    private Uri imageCameraUri;
    private String fileExt = "jpg"; // 1=JPEG, 2=PNG, 3=WEBP
    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;

    private Subscription subs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Generate Meme");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initUI();

        // Read Picker PARAMETER
        int param = getIntent().getExtras().getInt(PARAM_PICKER);
        if (param == 1) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Chose Image"), IMAGE_GALLERY);
        } else if (param == 2) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String fileName = String.valueOf(System.currentTimeMillis());
            File photo = new File(Environment.getExternalStorageDirectory() + File.separator
                    + Constanta.FilePath.CAMERA, fileName + ".jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photo));
            imageCameraUri = Uri.fromFile(photo);
            startActivityForResult(intent, IMAGE_CAMERA);
        }
    }

    @Override
    protected void onDestroy() {
        imageOriginal = null;
        subs.unsubscribe();
        super.onDestroy();
    }

    private void initUI() {
        imageView = (ImageView) findViewById(R.id.imageView);
        etTop = (EditText) findViewById(R.id.etTop);
        etBottom = (EditText) findViewById(R.id.etBottom);
        sbTop = (SeekBar) findViewById(R.id.sbTop);
        sbBot = (SeekBar) findViewById(R.id.sbBot);
        RadioGroup rgType = (RadioGroup) findViewById(R.id.rgType);
        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.rbJpeg:
                        fileExt = "jpg";
                        compressFormat = Bitmap.CompressFormat.JPEG;
                        break;
                    case R.id.rbPng:
                        fileExt = "png";
                        compressFormat = Bitmap.CompressFormat.PNG;
                        break;
                    case R.id.rbWebp:
                        fileExt = "webp";
                        compressFormat = Bitmap.CompressFormat.WEBP;
                        break;
                }
            }
        });

        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                imageView.setImageBitmap(generateMeme(imageOriginal));
            }
        };

        sbTop.setOnSeekBarChangeListener(onSeekBarChangeListener);
        sbBot.setOnSeekBarChangeListener(onSeekBarChangeListener);

        btnSave = (FloatingActionButton) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageOriginal != null) {
                    String fileName = String.valueOf(System.currentTimeMillis());
                    String path = Constanta.FilePath.GENERATED + fileName + "_image." + fileExt;
                    Bitmap memeFull = generateMeme(imageOriginal);

                    saveBitmapToFile(memeFull, path, 100, compressFormat);

                    // Generate thumbnail from generated image
                    Bitmap thumbnail = generateThumbnail(memeFull);
                    String pathThumb = Constanta.FilePath.THUMBNAIL + fileName + "_thumb." + fileExt;
                    saveBitmapToFile(thumbnail, pathThumb, 100, compressFormat);

                    finish();
                }
            }
        });
        btnSave.setEnabled(false);

        setupFields();
    }

    // Setup auto generate by InputText
    private void setupFields() {
        Observable<CharSequence> obsEtTop = RxTextView.textChanges(etTop);
        Observable<CharSequence> obsEtBot = RxTextView.textChanges(etBottom);
        subs = Observable.combineLatest(obsEtTop, obsEtBot,
                new Func2<CharSequence, CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence charSequence, CharSequence charSequence2) {
                        return charSequence.length() > 0 || charSequence2.length() > 0;
                    }
                }).subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    btnSave.setEnabled(true);
                    imageView.setImageBitmap(generateMeme(imageOriginal));
                } else {
                    btnSave.setEnabled(false);
                    imageView.setImageBitmap(imageOriginal);
                }
            }
        });
    }

    /**
     * Receive the result from the startActivity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case IMAGE_GALLERY:
                    imageFromGallery(data);
                    break;
                case IMAGE_CAMERA:
                    imageFromCamera();
                    break;
                default:
                    finish();
                    break;
            }
        }
    }

    /**
     * Image result from Camera
     */
    private void imageFromCamera() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageCameraUri.getPath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 500, 500);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        updateImageView(BitmapFactory.decodeFile(imageCameraUri.getPath(), options));
    }


    /**
     * Image result from Gallery
     */
    private void imageFromGallery(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 500, 500);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        updateImageView(BitmapFactory.decodeFile(filePath, options));
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Update the imageView with new bitmap
     */
    private void updateImageView(Bitmap image) {
        //Start Meme GENERATOR
        imageOriginal = image;
        imageView.setImageBitmap(imageOriginal);
    }

    /**
     * Generate meme
     */
    private Bitmap generateMeme(Bitmap image) {
        int MIN_SIZE = 10;
        int topTextSize = sbTop.getProgress() + MIN_SIZE;
        int bottomTextSize = sbBot.getProgress() + MIN_SIZE;
        Bitmap imageNew = image.copy(image.getConfig(), true);
        Canvas canvas = new Canvas(imageNew);

        // Font Family (TypeFace) - Default meme = Impact
        Typeface impactTF = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/impact.ttf");

        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setARGB(255, 0, 0, 0);
        strokePaint.setTextAlign(Paint.Align.CENTER);

        strokePaint.setTypeface(impactTF);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(1);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setARGB(255, 255, 255, 255);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(impactTF);
        textPaint.setStyle(Paint.Style.FILL);

        strokePaint.setTextSize(topTextSize);
        canvas.drawText(etTop.getText().toString().toUpperCase(), imageNew.getWidth() / 2,
                (int) (1.5 * topTextSize), strokePaint);
        textPaint.setTextSize(topTextSize);
        canvas.drawText(etTop.getText().toString().toUpperCase(), imageNew.getWidth() / 2,
                (int) (1.5 * topTextSize), textPaint);

        strokePaint.setTextSize(bottomTextSize);
        canvas.drawText(etBottom.getText().toString().toUpperCase(), imageNew.getWidth() / 2,
                imageNew.getHeight() - (int) (1.5 * bottomTextSize), strokePaint);
        textPaint.setTextSize(bottomTextSize);
        canvas.drawText(etBottom.getText().toString().toUpperCase(), imageNew.getWidth() / 2,
                imageNew.getHeight() - (int) (1.5 * bottomTextSize), textPaint);

        return imageNew;
    }

    private Bitmap generateThumbnail(Bitmap imageBitmap) {
        try {
            final int THUMBNAIL_SIZE = 200;
            return ThumbnailUtils.extractThumbnail(imageBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String saveBitmapToFile(Bitmap bitmap, String path, int quality,
                                          Bitmap.CompressFormat imageFormat) {
        File imageFile = new File(Environment.getExternalStorageDirectory()
                + File.separator + path);
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(imageFormat, quality, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e("BitmapToTempFile", "Error writing bitmap", e);
        }
        return imageFile.getAbsolutePath();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
