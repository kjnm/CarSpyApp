package org.pytorch.CarSpy;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;
import org.pytorch.MemoryFormat;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

  public float[] getValue(float[] params) {
    double sum = 0;

    for (int i=0; i<params.length; i++) {
      params[i] = (float) Math.exp(params[i]);
      sum += params[i];
    }

    if (Double.isNaN(sum) || sum < 0) {
      for (int i=0; i<params.length; i++) {
        params[i] = (float) (1.0 / params.length);
      }
    } else {
      for (int i=0; i<params.length; i++) {
        params[i] = (float) (params[i] / sum);
      }
    }

    return params;
  }

  ImageView imageView;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    Bitmap bitmap = null;
    Module module = null;
    try {
      // creating bitmap from packaged into app android asset 'image.jpg',
      // app/src/main/assets/image.jpg

      bitmap = BitmapFactory.decodeStream(getAssets().open("zdjeciaapki.png"));
      // loading serialized torchscript module from packaged into app android asset model.pt,
      // app/src/model/assets/model.pt
      module = LiteModuleLoader.load(assetFilePath(this, "moblie_model.ptl"));
    } catch (IOException e) {
      Log.e("PytorchHelloWorld", "Error reading assets", e);
      finish();
    }

    // showing image on UI
    imageView = findViewById(R.id.image);
    imageView.setImageBitmap(bitmap);

    TextView textView = findViewById(R.id.text);
    textView.setVisibility(View.INVISIBLE);


    final int RESULT_LOAD_IMAGE = 123;
    imageView.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        //Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);

        openCamera();
      }

    });

  }
  Uri image_uri;
  private static final int RESULT_LOAD_IMAGE = 123;
  public static final int IMAGE_CAPTURE_CODE = 654;
  private static final int CAMERA_REQUEST = 1888;
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);


    if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
      image_uri = data.getData();
      //imageView.setImageURI(image_uri);
      Bitmap bitmap = uriToBitmap(image_uri);
    }

    if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK){
      //imageView.setImageURI(image_uri);
      Log.i("MY_Result", "onActivityResult: " + image_uri);
      Bitmap bitmap = uriToBitmap(image_uri);
      imageView.setImageBitmap(bitmap);



      float aspectRatio = bitmap.getWidth() /
              (float) bitmap.getHeight();
      int width = 400;
      int height = Math.round(width / aspectRatio);

      bitmap = Bitmap.createScaledBitmap(
              bitmap, width, height, false);


      //imageView.setImageBitmap(bitmap);




      Module module = null;
      try {
        // creating bitmap from packaged into app android asset 'image.jpg',
        // app/src/main/assets/image.jpg

        // loading serialized torchscript module from packaged into app android asset model.pt,
        // app/src/model/assets/model.pt
        module = LiteModuleLoader.load(assetFilePath(this, "moblie_model.ptl"));
      } catch (IOException e) {
        Log.e("PytorchHelloWorld", "Error reading assets", e);
        finish();
      }

      // showing image on UI
      imageView = findViewById(R.id.image);
      imageView.setImageBitmap(bitmap);

      float[] n_m = new float[]{0.0f, 0.0f, 0.0f};
      float[] n_s = new float[]{1.0f, 1.0f, 1.0f};

      // preparing input tensor
      final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,n_m, n_s, MemoryFormat.CHANNELS_LAST);
      long start = System.currentTimeMillis();
      // running the model
      final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
      long finish = System.currentTimeMillis();
      long timeElapsed = finish - start;

      // getting tensor content as java array of floats
      float[] scores = outputTensor.getDataAsFloatArray();
      getValue(scores);
      // searching for the index with maximum score
      float maxScore = -Float.MAX_VALUE;
      int maxScoreIdx = -1;
      for (int i = 0; i < scores.length; i++) {
        if (scores[i] > maxScore) {
          maxScore = scores[i];
          maxScoreIdx = i;
        }
      }

      float maxScore2 = -Float.MAX_VALUE;
      int maxScoreIdx2 = -1;
      for (int i = 0; i < scores.length; i++) {
        if (scores[i] > maxScore2 && i!=maxScoreIdx) {
          maxScore2 = scores[i];
          maxScoreIdx2 = i;
        }
      }


      float maxScore3 = -Float.MAX_VALUE;
      int maxScoreIdx3 = -1;
      for (int i = 0; i < scores.length; i++) {
        if (scores[i] > maxScore3 && i!=maxScoreIdx&& i!=maxScoreIdx2) {
          maxScore3 = scores[i];
          maxScoreIdx3 = i;
        }
      }


      final int RESULT_LOAD_IMAGE = 123;
      imageView.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          //Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
          //startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);

          openCamera();
        }
      });

      String className = CarClasses.CAR_CLASSES[maxScoreIdx];

      DecimalFormat df = new DecimalFormat("0.000");
      // showing className on UI
      TextView textView = findViewById(R.id.text);

      textView.setText(className+ " score="+  String.valueOf(df.format(maxScore))+"\n"+
              CarClasses.CAR_CLASSES[maxScoreIdx2]+ " score="+  String.valueOf(df.format(maxScore2))+"\n"+
              CarClasses.CAR_CLASSES[maxScoreIdx3]+ " score="+  String.valueOf(df.format(maxScore3))+"\n"+ " time of interference=" + String.valueOf(timeElapsed) + "(ms)");

      textView.setVisibility(View.VISIBLE);
    }

  }
  private void openCamera() {
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.TITLE, "New Picture");
    values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
    image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
    startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
  }

  private Bitmap
  uriToBitmap(Uri selectedFileUri) {
    try {
      ParcelFileDescriptor parcelFileDescriptor =
              getContentResolver().openFileDescriptor(selectedFileUri, "r");
      FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
      Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

      parcelFileDescriptor.close();
      return image;
    } catch (IOException e) {
      Log.i("MYLOG", "url");
      e.printStackTrace();
    }
    return  null;
  }

  /**
   * Copies specified asset to the file in /files app directory and returns this file absolute path.
   *
   * @return absolute file path
   */
  public static String assetFilePath(Context context, String assetName) throws IOException {
    File file = new File(context.getFilesDir(), assetName);
    if (file.exists() && file.length() > 0) {
      return file.getAbsolutePath();
    }

    try (InputStream is = context.getAssets().open(assetName)) {
      try (OutputStream os = new FileOutputStream(file)) {
        byte[] buffer = new byte[4 * 1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
          os.write(buffer, 0, read);
        }
        os.flush();
      }
      return file.getAbsolutePath();
    }
  }
}
