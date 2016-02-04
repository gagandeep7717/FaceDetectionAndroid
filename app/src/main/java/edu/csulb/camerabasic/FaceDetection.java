package edu.csulb.camerabasic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.Image;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FaceDetection extends AppCompatActivity {
    private Button btnRetakePicture,btnAddMask;
    Bitmap imgFace = null;

    private static final int MAX_FACES = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);
        btnAddMask = (Button) findViewById(R.id.btnAddMask);
        Intent intent = getIntent();
        //String filePath = intent.getStringExtra(AndroidCamera.EXTRA_MESSAGE);
        //String imageType = intent.getStringExtra(AndroidCamera.EXTRA_TYPE);
        Bundle myBundle = intent.getExtras();
        String imageType = myBundle.getString("imageType");
        String filePath = myBundle.getString("filePath");
        Log.d("FACE DETECTION INTENT", "|"+imageType+"|");
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        File imageFile = new File(filePath);
        String Back = "back";
        String Front = "front";
        if (imageType.equals(Front) ) {
            if(imageFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                Bitmap rotated = Bitmap.createBitmap(myBitmap,0,0,myBitmap.getWidth(),myBitmap.getHeight(),matrix,true);
                imgFace = rotated.copy(rotated.getConfig(),true);
                imageView.setImageBitmap(rotated);


            }
        } else {
            if(imageFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotated = Bitmap.createBitmap(myBitmap,0,0,myBitmap.getWidth(),myBitmap.getHeight(),matrix,true);
                imgFace = rotated.copy(rotated.getConfig(),true);
                imageView.setImageBitmap(rotated);

            }
        }


        btnRetakePicture = (Button) findViewById(R.id.btnTakePicture);

        btnRetakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startCamIntent = new Intent(getApplicationContext(), AndroidCamera.class);
                startActivity(startCamIntent);
            }
        });

        btnAddMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectFaces();
            }
        });

    }

    private void detectFaces(){
        if(imgFace != null){
            int width = imgFace.getWidth();
            int height = imgFace.getHeight();

            FaceDetector detector = new FaceDetector(width, height,FaceDetection.MAX_FACES);
            FaceDetector.Face[] faces = new FaceDetector.Face[FaceDetection.MAX_FACES];

            Bitmap bitmap565 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Paint ditherPaint = new Paint();
            Paint drawPaint = new Paint();
            Paint drawCircle = new Paint();

            ditherPaint.setDither(true);
            drawPaint.setColor(Color.RED);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(2);

            drawCircle.setColor(Color.GREEN);
            drawCircle.setStyle(Paint.Style.STROKE);
            drawCircle.setStrokeWidth(5);

            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap565);
            canvas.drawBitmap(imgFace, 0, 0, ditherPaint);

            int facesFound = detector.findFaces(bitmap565, faces);
            PointF midPoint = new PointF();
            float eyeDistance = 0.0f;
            float confidence = 0.0f;

            Log.i("FaceDetector", "Number of faces found: " + facesFound);

            if(facesFound > 0)
            {
                for(int index=0; index<facesFound; ++index){
                    faces[index].getMidPoint(midPoint);
                    eyeDistance = faces[index].eyesDistance();
                    confidence = faces[index].confidence();

                    Log.i("FaceDetector",
                            "Confidence: " + confidence +
                                    ", Eye distance: " + eyeDistance +
                                    ", Mid Point: (" + midPoint.x + ", " + midPoint.y + ")");

                    canvas.drawRect((int) midPoint.x - eyeDistance,
                            (int) midPoint.y - eyeDistance,
                            (int) midPoint.x + eyeDistance,
                            (int) midPoint.y + eyeDistance, drawPaint);

                    canvas.drawCircle((int) midPoint.x - (eyeDistance / 2), (int) midPoint.y, 30.0f, drawCircle);
                    canvas.drawCircle((int)midPoint.x + (eyeDistance/2),(int)midPoint.y , 30.0f, drawCircle);
                    drawTriangle((int) midPoint.x, (int) midPoint.y, canvas);
                }
            }

            String filepath = Environment.getExternalStorageDirectory() + "/facedetect" + System.currentTimeMillis() + ".jpg";

            try {
                FileOutputStream fos = new FileOutputStream(filepath);

                bitmap565.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ImageView imageView = (ImageView)findViewById(R.id.imageView);

            imageView.setImageBitmap(bitmap565);
        }
    }

    public void drawTriangle(int mpX, int mpY, Canvas canvas) {
        Paint drawTriangle = new Paint();

        drawTriangle.setColor(Color.RED);
        drawTriangle.setStyle(Paint.Style.FILL_AND_STROKE);
        drawTriangle.setStrokeWidth(5);
        drawTriangle.setAntiAlias(true);

        canvas.drawCircle(mpX, mpY+40 , 35.0f, drawTriangle);

        /*
        Point a = new Point(mpX,mpY);
        Point b = new Point(mpX-5,mpY+10);
        Point c = new Point(mpX+5,mpY+10);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.lineTo(b.x,b.y);
        path.lineTo(c.x,c.y);
        path.lineTo(a.x,a.y);
        path.close();

        canvas.drawPath(path, drawTriangle);
        */

    }
}
