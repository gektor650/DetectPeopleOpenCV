package com.example.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Вызываем асинхронный загрузчик библиотеки
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    String[] urls = {
                            "https://scontent-b-vie.xx.fbcdn.net/hphotos-frc3/t1.0-9/1507629_466591540113829_1051378391_n.jpg",
                            "https://scontent-a-vie.xx.fbcdn.net/hphotos-prn1/t1.0-9/625536_684131198271951_1934286775_n.jpg",
                            "https://fbcdn-sphotos-d-a.akamaihd.net/hphotos-ak-prn2/t1.0-9/10001484_711868838859394_603810095_n.jpg",
                            "https://fbcdn-sphotos-c-a.akamaihd.net/hphotos-ak-prn1/t1.0-9/1009996_711869162192695_1482305841_n.jpg",
                            "https://scontent-a-vie.xx.fbcdn.net/hphotos-prn2/t1.0-9/970972_711869305526014_1497877792_n.jpg",
                            "https://scontent-b-vie.xx.fbcdn.net/hphotos-prn1/t1.0-9/s720x720/945187_544223272290619_225103547_n.jpg",
                            "https://scontent-a-vie.xx.fbcdn.net/hphotos-ash2/t1.0-9/s720x720/1005862_564941006885512_1452410858_n.jpg",
                            "http://cs418927.vk.me/v418927118/56ab/WuIj599QBWI.jpg",
                            "http://cs418927.vk.me/v418927118/57b9/QhCMOoCM4Ws.jpg",

                    };
                    for( String url : urls ) {
                        new DownloadImageTask().execute( url );
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    int count = 1;
    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            return peopleDetect( params[0] );
        }
        @Override
        protected void onPostExecute(Bitmap bitmap ) {
            super.onPostExecute(bitmap);
            if( bitmap != null ) {
                LinearLayout layout = (LinearLayout) findViewById( R.id.linear );
                ImageView image = new ImageView( getApplicationContext() );
                image.setImageBitmap( bitmap );
                layout.addView( image );
                FileOutputStream out;
                try {
                    out = new FileOutputStream( count++ + "image.jpg" );
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Bitmap peopleDetect ( String path ) {
        Bitmap bitmap = null;
        try {
            URL url = new URL( path );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(input, null, opts);
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 4);
            HOGDescriptor hog = new HOGDescriptor();
            MatOfFloat descriptors = HOGDescriptor.getDefaultPeopleDetector();
            hog.setSVMDetector(descriptors);
            MatOfRect locations = new MatOfRect();
            MatOfDouble weights = new MatOfDouble();
            hog.detectMultiScale(mat, locations, weights);
            final Point rectPoint1 = new Point();
            final Point rectPoint2 = new Point();
            Random random = new Random();
            if (locations.rows() > 0) {
                List<Rect> rectangles = locations.toList();
                for (Rect rect : rectangles) {
                    rectPoint1.x = rect.x;
                    rectPoint1.y = rect.y;
                    rectPoint2.x = rect.x + rect.width;
                    rectPoint2.y = rect.y + rect.height;
                    final Scalar rectColor = new Scalar(random.nextInt( 255 ) , random.nextInt( 255 ) , random.nextInt( 255 ) );
                    Core.rectangle(mat, rectPoint1, rectPoint2, rectColor, 2);
                }
            }
            Utils.matToBitmap( mat , bitmap );
        } catch (IOException e) {
            e.printStackTrace();
        }
         return bitmap;
    }


}
