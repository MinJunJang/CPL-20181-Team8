package suyoung.project;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class MainActivity extends Activity {

    Button btn;
    EditText Edit;
    TextView sendmsg;

    //  TCP연결 관련
    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 9999;
    private final String ip = "20.20.3.195";
    private MyThread myThread;
    Handler handler;
    private ImageView image;


    static JSONObject jObj = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            clientSocket = new Socket(ip, port); //소켓만들기
     //       socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socketOut = new PrintWriter(clientSocket.getOutputStream(), true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        myThread = new MyThread();
        handler = new Handler();


        btn = (Button) findViewById(R.id.btn);
        Edit = (EditText) findViewById(R.id.input);
        image = (ImageView) findViewById(R.id.image);

        myThread.start();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("send message");
                socketOut.println(Edit.getText());
            }
        });
    }

    public class MyThread extends Thread {
        @Override
        public void run() {

            System.out.println("start thread");
            BufferedInputStream bis = null;

            try {
                bis = new BufferedInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] imagebuffer = null;
            int size = 0;

            byte[] buffer = new byte[10240];

                int read;

                try {
                    while((read = bis.read(buffer)) != -1 ) { //파일을 읽어오기 시작함

                        if (imagebuffer == null) {
                            //이미지버퍼 배열에 저장한다
                            imagebuffer = new byte[read];
                            System.arraycopy(buffer, 0, imagebuffer, 0, read);

                        } else {

                            //이미지버퍼 배열에 계속 이어서 저장한다
                            byte[] preimagebuffer = imagebuffer.clone();
                            imagebuffer = new byte[read + preimagebuffer.length];
                            System.arraycopy(preimagebuffer, 0, imagebuffer, 0, preimagebuffer.length);
                            System.arraycopy(buffer, 0, imagebuffer, imagebuffer.length - read, read);
                        }
                    }
                        if(read  == -1 ) {

                            Bundle bundle = new Bundle();
                            bundle.putByteArray("Data", imagebuffer);

                            Message msg = mResultHandler.obtainMessage();
                            msg.setData(bundle);
                            mResultHandler.sendMessage(msg);

                        }


                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
    }

    //byte배열을 숫자로 바꾼다
    private int getInt(byte[] data) {
        int s1 = data[0] & 0xFF;
        int s2 = data[1] & 0xFF;
        int s3 = data[2] & 0xFF;
        int s4 = data[3] & 0xFF;

        return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
    }

    //이미지뷰에 비트맵을 넣는다
    public Handler mResultHandler = new Handler() {
        public void handleMessage(Message msg) {
            byte[] data = msg.getData().getByteArray("Data");
            ((ImageView) findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        }
    };

}

