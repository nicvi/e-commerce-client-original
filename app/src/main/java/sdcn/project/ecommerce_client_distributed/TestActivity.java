package sdcn.project.ecommerce_client_distributed;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import sdcn.project.ecommerce_client_distributed.R;

public class TestActivity extends AppCompatActivity {

    Button b1;
    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarbHandler = new Handler();
    private long fileSize = 0;
    private TextView textView_Contador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        b1=(Button)findViewById(R.id.button);
        textView_Contador  = findViewById(R.id.textView_Contador);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar = new ProgressDialog(v.getContext());
                progressBar.setCancelable(false);
                progressBar.setMessage("Realizando compra...");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setProgress(0);
                progressBar.setMax(100);
                progressBar.show();
                //progressBarStatus = 0;

                fileSize = 0;
                new Thread(new Runnable() {
                    public void run() {
                        while (progressBarStatus < 50) {
                            // call the method that will be waited to finish it
                            progressBarStatus = downloadFile();

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            progressBarbHandler.post(new Runnable() {
                                public void run() {
                                    progressBar.setProgress(progressBarStatus);
                                }
                            });
                        }

                        if (progressBarStatus >= 50) {
                            /*try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                             */
                            progressBar.dismiss();
                        }
                    }
                }).start();

            }
        });
        System.out.println("progressBarStatus: "+progressBarStatus);
        textView_Contador.setText(String.valueOf(progressBarStatus));
    }

    public int downloadFile() {
        while (fileSize <= 10) {
            fileSize++;

            if (fileSize == 1) {
                return 10;
            }else if (fileSize == 2) {
                return 20;
            }else if (fileSize == 3) {
                return 30;
            }else if (fileSize == 4) {
                return 40;
            }else if (fileSize == 5) {
                System.out.println("fileSize: "+fileSize);
                return 50;
            }else if (fileSize == 7) {
                return 70;
            }else if (fileSize == 8) {
                System.out.println("fileSize: "+fileSize);
                return 80;
            }
        }
        System.out.println("fileSize: "+fileSize);
        return 100;
    }
}