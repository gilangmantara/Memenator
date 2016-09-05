package co.mantara.memenator;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.OutputStream;

import co.mantara.memenator.util.Constanta;

/**
 * Created by Gilang on 04/09/2016.
 * gilangmantara@gmail.com
 */
public class MemeActivity extends AppCompatActivity {

    public static final String PARAM_NAME = "param_name";
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Meme");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initUI();
    }

    private void initUI() {
        SubsamplingScaleImageView ivMeme = (SubsamplingScaleImageView) findViewById(R.id.ivMeme);

        fileName = getIntent().getExtras().getString(PARAM_NAME);

        if (!"".equals(fileName) && fileName != null) {
            final File fileLocation = new File(Environment.getExternalStorageDirectory()
                    + File.separator + Constanta.FilePath.GENERATED, fileName);
            ivMeme.setImage(ImageSource.uri(fileLocation.getAbsolutePath()));
        } else {
            finish();
        }
    }

    // region OPTION MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_meme, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_delete:
                deleteMeme();
                return true;
            case R.id.action_share:
                shareMeme();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // endregion OPTION MENU

    private void deleteMeme() {
        final File fileMeme = new File(Environment.getExternalStorageDirectory()
                + File.separator + Constanta.FilePath.GENERATED, fileName);
        boolean memeDeleted = fileMeme.delete();
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + Constanta.FilePath.THUMBNAIL,
                fileName.replace("_image", "_thumb"));
        boolean thumbDeleted = file.delete();
        if (memeDeleted && thumbDeleted) {
            finish();
        }
    }

    private void shareMeme() {
        final File fileMeme = new File(Environment.getExternalStorageDirectory()
                + File.separator + Constanta.FilePath.GENERATED, fileName);
        Bitmap meme = BitmapFactory.decodeFile(fileMeme.getPath());
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "title");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);

        OutputStream outstream;
        try {
            outstream = getContentResolver().openOutputStream(uri);
            meme.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
            outstream.close();
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share Image"));
    }
}
