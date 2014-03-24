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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity  {



    List<String> images = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Добавляем фотографии
        images.add("https://scontent-b-vie.xx.fbcdn.net/hphotos-frc3/t1.0-9/1507629_466591540113829_1051378391_n.jpg");
        images.add("https://fbcdn-sphotos-f-a.akamaihd.net/hphotos-ak-ash4/t1.0-9/10154181_712747295438215_1228462748_n.jpg");
        images.add("https://fbcdn-sphotos-b-a.akamaihd.net/hphotos-ak-ash4/t1.0-9/1965078_712747302104881_558766371_n.jpg");
        images.add("https://scontent-b-vie.xx.fbcdn.net/hphotos-prn2/t1.0-9/1506552_712747308771547_1579743111_n.jpg");
        images.add("https://fbcdn-sphotos-d-a.akamaihd.net/hphotos-ak-frc1/t1.0-9/10151238_712747298771548_1594536303_n.jpg");
        images.add("https://fbcdn-sphotos-d-a.akamaihd.net/hphotos-ak-prn2/t1.0-9/10150606_712747962104815_602512917_n.jpg");
        images.add("https://scontent-a-vie.xx.fbcdn.net/hphotos-prn1/t1.0-9/625536_684131198271951_1934286775_n.jpg");
        images.add("https://scontent-b-vie.xx.fbcdn.net/hphotos-frc3/t1.0-9/10151803_712748152104796_1258909793_n.jpg");
        images.add("https://fbcdn-sphotos-e-a.akamaihd.net/hphotos-ak-prn1/t1.0-9/1979626_712748495438095_1068899742_n.jpg");
        images.add("https://fbcdn-sphotos-d-a.akamaihd.net/hphotos-ak-prn2/t1.0-9/10001484_711868838859394_603810095_n.jpg");
        images.add("https://fbcdn-sphotos-c-a.akamaihd.net/hphotos-ak-prn1/t1.0-9/1009996_711869162192695_1482305841_n.jpg");
        images.add("https://scontent-a-vie.xx.fbcdn.net/hphotos-prn2/t1.0-9/970817_712749385438006_1018680715_n.jpg");
        images.add("https://scontent-a-vie.xx.fbcdn.net/hphotos-ash3/t1.0-9/10154042_712749905437954_558808856_n.jpg");
        images.add("https://scontent-a-vie.xx.fbcdn.net/hphotos-prn2/t1.0-9/970972_711869305526014_1497877792_n.jpg");
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
                //Когда загрузились- запускаем очередь обработки изображений
                case LoaderCallbackInterface.SUCCESS:
                    proceedImageQueue();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    public void proceedImageQueue() {
        //Пока очередь обработки не дошла до конца- обрабатываем новые изображения
        if( images.size() > 0 ) {
            new DownloadImageTask().execute( images.get(0) );
            images.remove(0);
        }
    }

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
            }
            proceedImageQueue();
        }
    }

    public Bitmap peopleDetect ( String path ) {
        Bitmap bitmap = null;
        float execTime;
        try {
            // Закачиваем фотографию
            URL url = new URL( path );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(input, null, opts);
            long time = System.currentTimeMillis();
            // Создаем матрицу изображения для OpenCV и помещаем в нее нашу фотографию
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            // Переконвертируем матрицу с RGB на градацию серого
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 4);
            HOGDescriptor hog = new HOGDescriptor();
            //Получаем стандартный определитель людей и устанавливаем его нашему дескриптору
            MatOfFloat descriptors = HOGDescriptor.getDefaultPeopleDetector();
            hog.setSVMDetector(descriptors);
            // Определяем переменные, в которые будут помещены результаты поиска ( locations - прямоуголные области, weights - вес (можно сказать релевантность) соответствующей локации)
            MatOfRect locations = new MatOfRect();
            MatOfDouble weights = new MatOfDouble();
            // Собственно говоря, сам анализ фотографий. Результаты запишутся в locations и weights
            hog.detectMultiScale(mat, locations, weights);
            execTime = ( (float)( System.currentTimeMillis() - time ) ) / 1000f;
            //Переменные для выделения областей на фотографии
            Point rectPoint1 = new Point();
            Point rectPoint2 = new Point();
            Scalar fontColor = new Scalar(0, 0, 0);
            Point fontPoint = new Point();
            // Если есть результат - добавляем на фотографию области и вес каждой из них
            if (locations.rows() > 0) {
                List<Rect> rectangles = locations.toList();
                int i = 0;
                List<Double> weightList = weights.toList();
                for (Rect rect : rectangles) {
                    float weigh = weightList.get(i++).floatValue();

                    rectPoint1.x = rect.x;
                    rectPoint1.y = rect.y;
                    fontPoint.x  = rect.x;
                    fontPoint.y  = rect.y - 4;
                    rectPoint2.x = rect.x + rect.width;
                    rectPoint2.y = rect.y + rect.height;
                    final Scalar rectColor = new Scalar( 0  , 0 , 0  );
                    // Добавляем на изображения найденную информацию
                    Core.rectangle(mat, rectPoint1, rectPoint2, rectColor, 2);
                    Core.putText(mat,
                            String.format("%1.2f", weigh),
                            fontPoint, Core.FONT_HERSHEY_PLAIN, 1.5, fontColor,
                            2, Core.LINE_AA, false);

                }
            }
            fontPoint.x = 15;
            fontPoint.y = bitmap.getHeight() - 20;
            // Добавляем дополнительную отладочную информацию
            Core.putText(mat,
                    "Processing time:" + execTime + " width:" + bitmap.getWidth() + " height:" + bitmap.getHeight() ,
                    fontPoint, Core.FONT_HERSHEY_PLAIN, 1.5, fontColor,
                    2, Core.LINE_AA, false);
            Utils.matToBitmap( mat , bitmap );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


}
