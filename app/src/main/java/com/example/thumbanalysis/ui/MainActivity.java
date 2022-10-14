package com.example.thumbanalysis.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.androidnetworking.AndroidNetworking;
import com.example.thumbanalysis.utils.CommonObjects;
import com.example.thumbanalysis.models.ListModel;
import com.example.thumbanalysis.R;
import com.example.thumbanalysis.models.ResponseObject;
import com.example.thumbanalysis.network_services.RetrofitApi;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.integratedbiometrics.ibscanmatcher.IBMatcher;
import com.integratedbiometrics.ibscanmatcher.IBMatcherException;


import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity  {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    static InputMethod sourceChoice;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    Button btnSample, btnThumb, btnAnalyze;
    TextView tvTemplatePath, tvZipPath;
    File zipFile, templateFile;
    ProgressDialog progressDialog;
    PickiT pickiT;


    ActivityResultLauncher<String> resultLauncherTemplateFile = registerForActivityResult(new ActivityResultContracts.GetContent()
            , new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    try {

                        verifyStoragePermissions(MainActivity.this);
                        Log.e("TAG", "onActivityResult: " + getNameFromURI(uri));
//                        String path = getPath(MainActivity.this, uri);
//                        path = path.replace("/document/raw:", "");
//                        DocumentFile documentFile = DocumentFile.fromSingleUri(MainActivity.this, uri);
//                        File file = new File(path);
//                        String fileExt = MimeTypeMap.getFileExtensionFromUrl(file.toString());
//                        System.out.println(fileExt);
//                        System.out.println(uri.getPath());
//                        System.out.println(file.getPath());
                        String path = getNameFromURI( uri);
                        if (path.contains(".ibsm_template")) {

                        Log.e("REAL PATH", "onActivityResult: "+  path );
//                        File file = new File(path);
//                        String fileExt = MimeTypeMap.getFileExtensionFromUrl(file.toString());
//                        System.out.println(fileExt);
//                        System.out.println(uri.getPath());
//                        System.out.println(file.getPath());
//                        templateFile = file;
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            File mydir = MainActivity.this.getDir("", Context.MODE_PRIVATE);
                            File file1 = new File(mydir.getAbsolutePath() + "temp.ibsm_template");
                            file1.createNewFile();
                            copyInputStreamToFile(inputStream, file1);
                            templateFile = file1;
                            btnThumb.setVisibility(View.GONE);
                            tvTemplatePath.setText(templateFile.getAbsolutePath());
                            tvTemplatePath.setVisibility(View.VISIBLE);
                        } else {
                            new AlertDialog.Builder(MainActivity.this).setTitle("ERROR").setMessage("Please select a .ibsm_template file").setIcon(R.drawable.ic_baseline_error_24).show();
                        }



                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (zipFile != null && templateFile != null) {
                            btnAnalyze.setEnabled(true);
                        }
                    }
                }
            });
    ActivityResultLauncher<String> resultLauncherZipFIle = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    try {
//                        String path = getPath(MainActivity.this, uri);
//                        File file = new File(path);
//                        String fileExt = MimeTypeMap.getFileExtensionFromUrl(file.toString());
                        Log.e("ZIP", "onActivityResult: " + getNameFromURI(uri) );

                        if (getNameFromURI(uri).contains(".zip")) {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            File mydir = MainActivity.this.getDir("", Context.MODE_PRIVATE);
                            File file1 = new File(mydir.getAbsolutePath() + "temp.zip");
                            file1.createNewFile();
                            copyInputStreamToFile(inputStream, file1);

                            zipFile = file1;
                            btnSample.setVisibility(View.GONE);
                            tvZipPath.setText(zipFile.getAbsolutePath());
                            tvZipPath.setVisibility(View.VISIBLE);
                            System.out.println(zipFile.getName());
                        } else {
                            new AlertDialog.Builder(MainActivity.this).setTitle("ERROR").setMessage("Please select a .zip file").setIcon(R.drawable.ic_baseline_error_24).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (zipFile != null && templateFile != null) {
                            btnAnalyze.setEnabled(true);
                        }
                    }
                }
            });

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        // append = false
        try {
            BufferedOutputStream outputStream =new BufferedOutputStream( new FileOutputStream(file, false));
            int read;
            byte[] bytes = new byte[65536];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getPath(Context context, Uri uri) {
        String result = null;

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    void setSourceChoice(InputMethod method) {
        sourceChoice = method;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.resetBtn) {
            templateFile = null;
            zipFile = null;
            tvTemplatePath.setText("");
            tvTemplatePath.setVisibility(View.GONE);

            tvZipPath.setText("");
            tvZipPath.setVisibility(View.GONE);

            btnThumb.setVisibility(View.VISIBLE);
            btnSample.setVisibility(View.VISIBLE);
            btnAnalyze.setEnabled(false);
            deleteFiles();

        }
        return true;
    }

    private void deleteFiles() {
        File mydir = MainActivity.this.getDir("templates", Context.MODE_PRIVATE);
        List<File> files = new ArrayList<>();

        File[] listFiles = mydir.listFiles();
        if (listFiles != null)
            for (File f : listFiles) {
                if (f.isFile()) {
                    files.add(f);
                } else if (f.isDirectory()) {
                    Collections.addAll(files, f.listFiles());
                    files.add(f);
                }
            }
        if (files.size() > 0)
            for (File f : files) {
                f.delete();
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSample = findViewById(R.id.btnSample);
        btnThumb = findViewById(R.id.btnThumb);
        btnAnalyze = findViewById(R.id.btnAnalyze);

        tvZipPath = findViewById(R.id.tvZipPath);
        tvTemplatePath = findViewById(R.id.tvTemplatePath);
        verifyStoragePermissions(MainActivity.this);
        AndroidNetworking.initialize(MainActivity.this);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait");

        btnAnalyze.setOnClickListener(v -> {
            try {
                Log.e("TAG", "onClick: " + "Starting Analysis");
                ZipFile file = new ZipFile(zipFile.getAbsolutePath());
                Log.e("ABS PATH", "onClick: " + zipFile.getAbsolutePath());
                File mydir = MainActivity.this.getDir("templates", Context.MODE_PRIVATE);
                Log.e("Dir", "onClick: " + mydir.getAbsolutePath());
                file.extractAll(mydir.getAbsolutePath());

                List<File> files = new ArrayList<>();
                zipFile.delete();

                File[] listFiles = mydir.listFiles();
                if (listFiles != null)
                    for (File f : listFiles) {
                        if (f.getName().contains("zip")) {
                            continue;
                        } else if (f.isFile()) {
                            files.add(f);
                        } else if (f.isDirectory()) {
                            for (File ff : f.listFiles()) {
                                files.add(ff);
                            }
                        }
                    }

                System.out.println(files);

                CommonObjects.LIST_OF_SCORES.clear();
                for (File tempFile : files) {
                    System.out.println(templateFile.getPath());
                    IBMatcher matcher = IBMatcher.getInstance();
                    IBMatcher.Template t2 = matcher.loadTemplate(templateFile.getPath());
                    IBMatcher.Template t1 = matcher.loadTemplate(tempFile.getPath());
                    System.out.println(templateFile.getPath());
                    int score = matcher.matchTemplates(t1, t2);
                    Log.i("TAG", "onClick: " + score);
                    System.out.println(tempFile.getName());
                    CommonObjects.LIST_OF_SCORES.add(new ListModel(tempFile.getName().split("\\.")[0], String.valueOf(score)));

                }
                deleteFiles();
                zipFile.delete();
                templateFile.delete();
            } catch (IBMatcherException | ZipException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(MainActivity.this, ListingActivity.class);
            startActivity(intent);
        });


        btnSample.setOnClickListener(v -> {
//
            String[] items = {"LOCAL", "REMOTE"};
            int checkedItem = -1;
            new AlertDialog.Builder(MainActivity.this).setTitle("SOURCE").setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            setSourceChoice(InputMethod.LOCAL);
                            break;
                        case 1:
                            setSourceChoice(InputMethod.SERVER);
                            break;
                    }
                }
            }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (sourceChoice == InputMethod.LOCAL) {
                        resultLauncherZipFIle.launch("application/zip");
                    } else if (sourceChoice == InputMethod.SERVER) {

                        Dialog customDialog = new Dialog(MainActivity.this);
                        customDialog.setContentView(R.layout.custom_dialog);
                        Window window = customDialog.getWindow();
                        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
                        EditText edtSemis = customDialog.findViewById(R.id.edtSemis);
                        Button doneBtn = customDialog.findViewById(R.id.btnDone);

                        doneBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                /*
https://ssdms.seld.gos.pk/api/method/frappe.integrations.meta_api.get_url?semis_code=126362&Authorization=token ddd76cd90e50609:a766b57d4f38d24
*/
                                String baseUrl = "https://ssdms.seld.gos.pk/api/method/frappe.integrations.meta_api.get_url?semis_code=";
                                String authToken = "token ddd76cd90e50609:a766b57d4f38d24";
                                String semisCode;

                                if (!edtSemis.getText().equals("")) {
                                    semisCode = edtSemis.getText().toString();
                                    String finalUrl = baseUrl + semisCode + authToken;
                                    System.out.println(finalUrl);
                                    File mydir = MainActivity.this.getDir("templates", Context.MODE_PRIVATE);
                                    customDialog.dismiss();
                                    progressDialog.show();

                                    RetrofitApi.getClient().getFilePath(semisCode, authToken).enqueue(new Callback<ResponseObject>() {
                                        @Override
                                        public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                                            System.out.println(response.body().getMessage().getName());
                                            RetrofitApi.getClient().downLoadFile(response.body().getMessage().getName()).enqueue(new Callback<ResponseBody>() {
                                                @Override
                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                    if (response.isSuccessful()) {
                                                        Log.e("RES", "onResponse: " + "INSIDE RESPONE");
                                                        File file = new File(mydir.getAbsolutePath() + "/" + semisCode + ".zip");
                                                        try {
                                                            file.createNewFile();
                                                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                                                            fileOutputStream.write(response.body().bytes());
                                                            fileOutputStream.close();
                                                            btnSample.setVisibility(View.GONE);
                                                            tvZipPath.setText(file.getAbsolutePath());
                                                            tvZipPath.setVisibility(View.VISIBLE);
                                                            System.out.println(file.getName());
                                                            progressDialog.dismiss();
                                                            zipFile = file;
//                                                        progressDialog.dismiss();
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                            progressDialog.dismiss();
                                                        } finally {
                                                            if (zipFile != null && templateFile != null) {
                                                                btnAnalyze.setEnabled(true);
                                                                progressDialog.dismiss();
                                                            }
                                                        }

                                                    }
                                                    Log.e("OUTSIDE", "onResponse: " + "INSIDE RESPONSE");

                                                }

                                                @Override
                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                    t.printStackTrace();
                                                    Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                                                    progressDialog.dismiss();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseObject> call, Throwable t) {
                                            t.printStackTrace();
                                            progressDialog.dismiss();
                                            Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });

                                } else {
                                    edtSemis.setError("Please enter a valid url");
                                }
                            }
                        });

                        customDialog.show();
                    }
                }
            }).create().show();

        });

        btnThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultLauncherTemplateFile.launch(
                        "application/octet-stream"
                );
            }
        });
    }




    enum InputMethod {
        LOCAL, SERVER
    }
    @SuppressLint("Range")
    public String getNameFromURI(Uri uri) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Cursor c = getContentResolver().query(uri, null, null, null, null);
            c.moveToFirst();
            return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }else{
          return   getPath(MainActivity.this, uri);
        }
    }
}
