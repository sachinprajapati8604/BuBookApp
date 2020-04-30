package com.example.bubookapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {
private WebView mywebview;
private String weburl="http://bubook.epizy.com/?i=1";
ProgressBar progressBar;
ProgressDialog progressDialog;
RelativeLayout relativeLayout;
Button btn_nonet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //fullscreen display code
        Window window=getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        mywebview=(WebView)findViewById(R.id.my_wv);
        progressBar=(ProgressBar)findViewById(R.id.my_pb);
        btn_nonet=(Button)findViewById(R.id.btn_nonet);
        relativeLayout=(RelativeLayout)findViewById(R.id.relativelayout);

        if(savedInstanceState !=null){
            mywebview.restoreState(savedInstanceState);
        }
        else {
            mywebview.getSettings().setJavaScriptEnabled(true);
            mywebview.getSettings().setLoadWithOverviewMode(true);
            mywebview.getSettings().setUseWideViewPort(true);
            mywebview.getSettings().setDomStorageEnabled(true);
            mywebview.getSettings().setLoadsImagesAutomatically(true);

            checkConnection();
        }



        //code for downloading files in app
        mywebview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String s, final String s1, final String s2, final String s3, long l) {
                Dexter.withActivity(MainActivity.this)
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s));
                                request.setMimeType(s3);
                                String cookies = CookieManager.getInstance().getCookie(s);
                                request.addRequestHeader("cookie",cookies);
                                request.addRequestHeader("User-Agent",s1);
                                request.setDescription("Downloading File.....");
                                request.setTitle(URLUtil.guessFileName(s,s2,s3));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                                s,s2,s3));
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);
                                Toast.makeText(MainActivity.this, "Downloading File..", Toast.LENGTH_SHORT).show();

                            }
                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                            }
                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading Please Wait...");

        WebSettings webSettings=mywebview.getSettings();
        webSettings.setJavaScriptEnabled(true);




        mywebview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });  //it will prevent not to go browser

        mywebview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                //code for set title
                setTitle("Loading...");
                progressDialog.show();
                if(newProgress==100){
                    progressBar.setVisibility(View.GONE);
                    setTitle(view.getTitle());  //it will take title of website
                    progressDialog.dismiss();
                }

                super.onProgressChanged(view, newProgress);
            }
        });

        btn_nonet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection();
            }
        });

    }

    //this piece of code prevent app to exit..it will go previous back by pressing back button
    @Override
    public void onBackPressed() {
        if (mywebview.canGoBack()) {

            mywebview.goBack();
        }else {
            AlertDialog.Builder builder =new AlertDialog.Builder(this);
            builder.setMessage("Are you sure want to exit ?")
                    .setNegativeButton("No",null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                          finishAffinity();
                        }
                    }).show();
        }

    }
    public  void checkConnection(){
        ConnectivityManager connectivityManager=(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()) {
            mywebview.loadUrl(weburl);
            mywebview.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);

        }
        else if (mobile.isConnected()) {
            mywebview.loadUrl(weburl);
            mywebview.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
        }
        else{
            mywebview.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
        }

    }
//code for back previou and reload button in menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.nav_previous:
                onBackPressed();
                break;
            case R.id.nav_next:
                if (mywebview.canGoForward()) {

                    mywebview.goForward();
                }
                break;
            case R.id.nav_reload:
                checkConnection();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    //saved instance mehtod saves page wile rotatng the mobile
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mywebview.saveState(outState);
    }
}
