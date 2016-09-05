package co.mantara.memenator;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.mantara.memenator.adapter.MemeRecyclerAdapter;
import co.mantara.memenator.model.Meme;
import co.mantara.memenator.util.Constanta;

/**
 * Created by Gilang on 04/09/2016.
 * gilangmantara@gmail.com
 */
public class MainActivity extends AppCompatActivity implements MyOnClickListener {

    private List<Meme> listMeme;

    private RecyclerView list;
    private LinearLayout lnError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setLogo(R.mipmap.ic_launcher);
        }

        checkAllPermission();

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllMemes();
    }

    private void initUI() {
        // FAB ADD BUTTON for generate new MEME
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogPicker();
            }
        });

        lnError = (LinearLayout) findViewById(R.id.lnError);
        list = (RecyclerView) findViewById(R.id.list);

        loadAllMemes();
    }

    private void loadAllMemes() {
        // Check for Generated Folder
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + Constanta.FilePath.GENERATED);
        if (!file.exists()) file.mkdirs();

        // Check for Thumnails Folder
        File file2 = new File(Environment.getExternalStorageDirectory()
                + File.separator + Constanta.FilePath.THUMBNAIL);
        if (!file2.exists()) file2.mkdirs();

        if (file.isDirectory()) {
            File[] listFile = file.listFiles();

            if (listFile == null) {
                checkAllPermission();
                return;
            }

            if (listFile.length > 0) {
                lnError.setVisibility(View.GONE);
                listMeme = new ArrayList<>();
                for (File aListFile : listFile) {

                    String name = aListFile.getName();
                    Meme meme = new Meme(name, file2.getAbsolutePath()
                            + "/" + name.replace("_image", "_thumb"));
                    listMeme.add(meme);
                }
                MemeRecyclerAdapter adapter = new MemeRecyclerAdapter(MainActivity.this, listMeme);

                GridLayoutManager glm = new GridLayoutManager(MainActivity.this, 3);
                list.setAdapter(adapter);
                list.setLayoutManager(glm);
                adapter.setOnItemClickListener(MainActivity.this);
            } else {
                lnError.setVisibility(View.VISIBLE);
            }
        } else {
            lnError.setVisibility(View.VISIBLE);
        }
    }

    private void showDialogPicker() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_picker);
        dialog.setTitle("Select image from");
        Button btnGallery = (Button) dialog.findViewById(R.id.btnGallery);
        Button btnCamera = (Button) dialog.findViewById(R.id.btnCamera);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnGallery_listener(dialog);
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnCamera_listener(dialog);
            }
        });
        dialog.show();
    }

    // region LISTENER
    private void btnGallery_listener(Dialog dialog) {
        dialog.hide();
        Intent intent = new Intent(MainActivity.this, GeneratorActivity.class);
        intent.putExtra(GeneratorActivity.PARAM_PICKER, 1);
        startActivity(intent);
    }

    private void btnCamera_listener(Dialog dialog) {
        dialog.hide();
        Intent intent = new Intent(MainActivity.this, GeneratorActivity.class);
        intent.putExtra(GeneratorActivity.PARAM_PICKER, 2);
        startActivity(intent);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(MainActivity.this, MemeActivity.class);
        intent.putExtra(MemeActivity.PARAM_NAME, listMeme.get(position).getName());
        startActivity(intent);
    }
    // endregion LISTENER

    // region OPTION MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // endregion OPTION MENU

    /**
     * REQUEST PERMISSION
     **/
    // region REQUEST PERMISSION
    private static final int PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    private void checkAllPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Phone permission has not been granted.
            requestPhoneStatePermission();
        }
    }

    private void requestPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            /*Snackbar.make(mLayout, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA);
                        }
                    })
                    .show();*/
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {

            // Received permission result for camera permission.est. ");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Camera permission has been granted, preview can be displayed
            } else {

            }
        }
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {

            // Received permission result for camera permission.est. ");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Camera permission has been granted, preview can be displayed
            } else {

            }
        }
    }
    // endregion REQUEST PERMISSION
}
