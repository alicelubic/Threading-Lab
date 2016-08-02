package generalassembly.yuliyakaleda.solution_code_thread_safe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView mImageView;
    private Button mChooseButton;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChooseButton = (Button) findViewById(R.id.choose_button);
        mImageView = (ImageView) findViewById(R.id.image);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mProgressBar.setMax(100);

        mImageView.setImageResource(R.drawable.placeholder);
        mChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == MainActivity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            //instantiating my task, and setting it to execute on the uri from the image i chose
            ImageProcessingAsyncTask task = new ImageProcessingAsyncTask();
            task.execute(selectedImage);
        }
    }

    // brings up the photo gallery/other resources to choose a picture
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //parameter types are Uri (because that's what we're workin with), Integer to measure the progress, and Bitmap because I want the new and improved bitmap to be returned
    private class ImageProcessingAsyncTask extends AsyncTask<android.net.Uri, Integer, Bitmap > {
        @Override
        protected Bitmap doInBackground(android.net.Uri... params) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(params[0]));
                return invertImageColors(bitmap);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "Image uri is not received or recognized");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values[0]);
            mProgressBar.setProgress(values[0]); //like in the in-class demo, these values are an array of values, so this passes whatever round of the loop we're in over to the onProgressUpdate thing, and because it's only one parameter, we only need the first parameter which is 0, hence why we only put [0] in the onProgressUpdate... ja feel?

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mProgressBar.setVisibility(View.INVISIBLE);//invisible cuz im DONE so i dont NEED THAT SHIT NO MO'
            mImageView.setImageBitmap(bitmap);//set the imageview to my new and improved inverted image woohoo

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);//making this visible here because onPreExecute happens in the UI thread, and because now that we're gonna be doing the work offscreen, this is our chance to send that data to the UI
        }

        private Bitmap invertImageColors(Bitmap bitmap) {
            //You must use this mutable Bitmap in order to modify the pixels
            Bitmap mutableBitmap = bitmap.copy(bitmap.getConfig(), true);

            //Loop through each pixel, and invert the colors
            for (int i = 0; i < mutableBitmap.getWidth(); i++) {
                for (int j = 0; j < mutableBitmap.getHeight(); j++) {

                    //gets the pixel
                    int pixel = mutableBitmap.getPixel(i, j);
                    //extracts the blue/green/red values from that pixel's color
                    int pixelBlue = Color.blue(pixel);
                    int pixelRed = Color.red(pixel);
                    int pixelGreen = Color.green(pixel);

                    //setting the opposite of each color value to a nice new variable
                    int invertBlue = 255 - pixelBlue;
                    int invertRed = 255 - pixelRed;
                    int invertGreen = 255 - pixelGreen;

                    //throwing all those values together to get one  nice color int
                    int invertedColor = Color.rgb(invertRed, invertGreen, invertBlue);

                    //setting my image pixel to that new color int
                    mutableBitmap.setPixel(i,j, invertedColor);
                }
                int progressVal = Math.round((long) (100 * (i / (1.0 * mutableBitmap.getWidth()))));
                //tell it to publish the progress here with each round thru the loop, and then we set the progress bar to display this shit in onProgressUpdate
                publishProgress(progressVal);
            }
            return mutableBitmap;
        }
    }
}

