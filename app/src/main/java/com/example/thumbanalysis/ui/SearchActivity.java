package com.example.thumbanalysis.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thumbanalysis.R;
import com.example.thumbanalysis.adapters.EmptyAdapter;
import com.example.thumbanalysis.adapters.ScoreListAdapter;
import com.example.thumbanalysis.models.ListModel;
import com.example.thumbanalysis.models.ResponseObject;
import com.example.thumbanalysis.network_services.RetrofitApi;
import com.example.thumbanalysis.utils.CommonObjects;
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
import java.util.Objects;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    Button btnSearchDone, btnPickSSDMSFile, analyzeButton;
    EditText searchBar;
    RecyclerView rvSearch;
    ProgressDialog progressDialog;
    File seldFile, dailyMonitoringFile;
    TextView pickMessage, downloadedMessage;
    LinearLayout linearLayout;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    File tempDirectory;

    ActivityResultLauncher<String> resultLauncherZipFIle = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    try {
                        Log.e("ZIP", "onActivityResult: " + getNameFromURI(uri));
                        if (getNameFromURI(uri).contains(".zip")) {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            File file = new File(tempDirectory.getAbsolutePath() + "/" + "seld.zip");
                            file.createNewFile();
                            copyInputStreamToFile(inputStream, file);

                            seldFile = file;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateAnalyzeButtonStatus(dailyMonitoringFile, seldFile, analyzeButton);
                                    btnPickSSDMSFile.setVisibility(View.GONE);
                                    pickMessage.setVisibility(View.VISIBLE);
                                    pickMessage.setText("Picked File \n" + seldFile.getPath());
                                }
                            });
                            System.out.println(seldFile.getName());
                        } else {
                            new AlertDialog.Builder(SearchActivity.this).setTitle("ERROR").setMessage("Please select a .zip file").setIcon(R.drawable.ic_baseline_error_24).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.resetBtn) {
            if (tempDirectory.exists()){
                deleteRecursive(tempDirectory);
            }
            dailyMonitoringFile = null;
            seldFile = null;
            analyzeButton.setEnabled(false);
            linearLayout.setVisibility(View.VISIBLE);
            pickMessage.setVisibility(View.GONE);
            rvSearch.setVisibility(View.INVISIBLE);
            downloadedMessage.setVisibility(View.GONE);
            btnPickSSDMSFile.setVisibility(View.VISIBLE);
            searchBar.setText("");


        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        btnSearchDone = findViewById(R.id.btnSearchDone);
        searchBar = findViewById(R.id.search_bar);
        rvSearch = findViewById(R.id.rvSearch);
        analyzeButton = findViewById(R.id.btnStartMatching);
        btnPickSSDMSFile = findViewById(R.id.btnPickSSMS);
        downloadedMessage = findViewById(R.id.downloadedFileMessage);
        pickMessage = findViewById(R.id.pickFileMessage);
        linearLayout = findViewById(R.id.searchLayout);
        progressDialog = new ProgressDialog(SearchActivity.this);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait");
        tempDirectory = SearchActivity.this.getDir("zips", Context.MODE_PRIVATE);
        tempDirectory.mkdir();

        rvSearch.setHasFixedSize(true);
        rvSearch.setLayoutManager(new LinearLayoutManager(this));
        rvSearch.setAdapter(new EmptyAdapter());

        btnSearchDone.setOnClickListener(v -> {
            if (searchBar.getText().toString().trim().equals("")) {
                searchBar.setError("This field is required");
            } else {
                String semisCode = searchBar.getText().toString();
                String authToken = "token ddd76cd90e50609:a766b57d4f38d24";
                progressDialog.show();

                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        Response<ResponseObject> responseObject = RetrofitApi.getClient().getFilePath(semisCode, authToken).execute();
                        if (responseObject.body() != null) {
                            Response<ResponseBody> bodyResponse = RetrofitApi.getClient().downLoadFile(responseObject.body().getMessage().getName()).execute();
                            tempDirectory.mkdir();
                            File file = new File(tempDirectory.getAbsolutePath() + "/" + semisCode + ".zip");
                            file.createNewFile();
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(Objects.requireNonNull(bodyResponse.body()).bytes());
                            fileOutputStream.close();
                            dailyMonitoringFile = file;
                            Log.i("ABSOLUTE ", "run: " + dailyMonitoringFile.getAbsolutePath());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateAnalyzeButtonStatus(dailyMonitoringFile, seldFile, analyzeButton);
                                    downloadedMessage.setText("Downloaded File\n" + dailyMonitoringFile.getPath());
                                    downloadedMessage.setVisibility(View.VISIBLE);
                                    linearLayout.setVisibility(View.GONE);

                                }
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(SearchActivity.this, "Incorrect Semis", Toast.LENGTH_LONG).show());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } finally {
                        progressDialog.dismiss();
                    }
                });

            }
        });
        btnPickSSDMSFile.setOnClickListener(v -> {
            verifyStoragePermissions(SearchActivity.this);
            resultLauncherZipFIle.launch("application/zip");

        });
        analyzeButton.setOnClickListener(v -> startMatching());

    }


    void startMatching() {
        try {
            ZipFile dailyMonitoringZip = new ZipFile(dailyMonitoringFile.getAbsolutePath());
            ZipFile seldZip = new ZipFile(seldFile.getAbsolutePath());
            File dailyMonitoringDirectory = new File(tempDirectory.getAbsolutePath() + "/" + "daily_templates");
            File seldDirectory = new File(tempDirectory.getAbsolutePath() + "/" + "seld_templates");
            dailyMonitoringDirectory.mkdir();
            seldDirectory.mkdir();
            dailyMonitoringZip.extractAll(dailyMonitoringDirectory.getAbsolutePath());
            seldZip.extractAll(seldDirectory.getAbsolutePath());

            ArrayList<File> dailyMonitoringFileArrayList = new ArrayList<>();
            ArrayList<File> seldFileArrayList = new ArrayList<>();

            for (File file : Objects.requireNonNull(dailyMonitoringDirectory.listFiles())) {
                if (file.isDirectory()) {
                    for (File file1 : Objects.requireNonNull(file.listFiles())) {
                        System.out.println(file1.getName());
                        dailyMonitoringFileArrayList.add(file1);
                    }
                } else if (file.isFile()) {
                    dailyMonitoringFileArrayList.add(file);
                }
            }
            for (File file : Objects.requireNonNull(seldDirectory.listFiles())) {
                if (file.isDirectory()) {
                    for (File file1 : Objects.requireNonNull(file.listFiles())) {
                        System.out.println(file1.getName());
                        seldFileArrayList.add(file1);
                    }
                } else if (file.isFile()) {
                    seldFileArrayList.add(file);
                }
            }
CommonObjects.LIST_OF_SEARCH_SCORES.clear();
            for (File seld : seldFileArrayList) {
                ListModel temp = new ListModel(seld.getName(), "Not Found");
                for (File monitoring : dailyMonitoringFileArrayList) {
                    if (seld.getName().equals(monitoring.getName())) {
                        IBMatcher matcher = IBMatcher.getInstance();
                        IBMatcher.Template t1 = matcher.loadTemplate(monitoring.getPath());
                        IBMatcher.Template t2 = matcher.loadTemplate(seld.getPath());
                        int score = matcher.matchTemplates(t1, t2);

                        temp.setScore(String.valueOf(score));

                    }
                }
                CommonObjects.LIST_OF_SEARCH_SCORES.add(temp);
            }
            rvSearch.setVisibility(View.VISIBLE);
            rvSearch.setHasFixedSize(true);
            rvSearch.setLayoutManager(new LinearLayoutManager(this));
            rvSearch.setAdapter(new ScoreListAdapter(CommonObjects.LIST_OF_SEARCH_SCORES));
            deleteRecursive(tempDirectory);
            analyzeButton.setEnabled(false);

        } catch (ZipException | IBMatcherException e) {
            e.printStackTrace();
        }

    }

    void updateAnalyzeButtonStatus(File f1, File f2, Button button) {
        if (f1 != null && f2 != null) {
            button.setEnabled(true);
        }

    }

    private static void copyInputStreamToFile(InputStream inputStream, File file) {

        // append = false
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file, false));
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

    @SuppressLint("Range")
    public String getNameFromURI(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Cursor c = getContentResolver().query(uri, null, null, null, null);
            c.moveToFirst();
            return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        } else {
            return getPath(SearchActivity.this, uri);
        }

    }

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

    void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles()))
                deleteRecursive(child);

        fileOrDirectory.delete();

    }
}