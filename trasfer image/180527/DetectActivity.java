package com.example.hayeon.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetectActivity extends AppCompatActivity {

    ImageView pic_btn;
    ImageView send_btn;

    //  TCP연결 관련
    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 9999;
    private final String ip = "172.20.10.2";
    Handler handler;
    private ImageView picture;
    static JSONObject jObj = null;
    Bitmap bitmap;

    public static final int REQUEST_IMAGE_CAPTURE = 1001;
    File file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            clientSocket = new Socket(ip, port); //소켓만들기
            //       socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socketOut = new PrintWriter(clientSocket.getOutputStream(), true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        pic_btn = (ImageView) findViewById(R.id.btn);
        send_btn = (ImageView) findViewById(R.id.sendbtn);
        picture = (ImageView) findViewById(R.id.picture);

        checkDangerousPermissions();
    }

    private void checkDangerousPermissions() {

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    final static String JPEG_FILE_PREFIX = "IMG_";
    final static String JPEG_FILE_SUFFIX = ".jpg";
    String mCurrentPhotoPath;

    private File createFile() throws IOException {

        //   String imageFileName = "test.jpg";
        File storageDir = Environment.getExternalStorageDirectory();

        //this.getactivity().getExternalFilesDir(null).getAbsolutePath();

        //return curFile;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + JPEG_FILE_SUFFIX;
      /*  File image = File.createTempFile(
                imageFileName,            // prefix
                JPEG_FILE_SUFFIX,        // suffix
                storageDir                // directory
        );*/

        File curFile = new File(storageDir, imageFileName);

        mCurrentPhotoPath = curFile.getAbsolutePath();

        return curFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inSampleSize = 2;//값에 따라 1/N 줄어듦

            if (file != null) {

                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                picture.setImageBitmap(bitmap);
                Toast.makeText(this, "촬영 성공! ", Toast.LENGTH_LONG).show();
                galleryAddPic();

            } else {
                Toast.makeText(getApplicationContext(), "File is null.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void galleryAddPic() {

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void SendToServer(View v) {
        //클라이언트로 보낼 출력스트림을 얻는다


        socketOut.println("Send");

        DataOutputStream os = null;

        try {
            os = new DataOutputStream(clientSocket.getOutputStream());

            //이미지를 비트맵으로 불려온다
            byte[] data = bitmapToByteArray();

            //실제 데이터를 보낸다
            os.write(data, 0, data.length);
            os.flush();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(getBaseContext(), GetFromServer.class);
        startActivity(intent);
    }

    public byte[] bitmapToByteArray() {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public void picClicked(View v) {

        try {
            file = createFile();

        } catch (IOException ex) {
            ex.printStackTrace();
        }


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

        if (intent.resolveActivity(getPackageManager()) != null) {
            //btn.setVisibility(View.GONE);
          //  pic_btn.setText("다시 찍기");
//            pic_btn.setImageResource(R.drawable.again);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

}
