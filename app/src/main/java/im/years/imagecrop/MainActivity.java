package im.years.imagecrop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.File;

import im.years.imagepicker.ImagePickerManager;

public class MainActivity extends AppCompatActivity {

    private ImagePickerManager mImagePickerManager;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        imageView = (ImageView)findViewById(R.id.imageView);

        mImagePickerManager = new ImagePickerManager(this);
    }


    public void PickImage(View v) {


        mImagePickerManager.pickImage(true ,new ImagePickerManager.ImagePickerListener() {
            @Override
            public void onImageChosen(ChosenImage image) {

                final Uri source = Uri.parse(new File(image
                        .getThumbnailSmallPath()).toString());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageURI(source);
                    }
                });



                Log.e("dddddd", image.getOriginalPath());
            }

            @Override
            public void onError(String reason) {
                Log.e("dddddd", reason);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mImagePickerManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
