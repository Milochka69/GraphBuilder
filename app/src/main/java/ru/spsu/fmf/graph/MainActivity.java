package ru.spsu.fmf.graph;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.spsu.fmf.graph.util.Utilities;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;

    private String jsonPoints;

    private Type pointsType = new TypeToken<List<DataPoint>>(){}.getType();

    @BindView(R.id.graph)
    GraphView mGraphView;
    @BindView(R.id.message)
    TextView mMessage;
    @BindView(R.id.load)
    FloatingActionButton mLoadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        restoreState(savedInstanceState);

        new MaterialShowcaseView.Builder(this)
                .setTarget(mLoadButton)
                .setDismissText(R.string.action_understood)
                .setContentText(R.string.showcase_begin_work_with)
                .setDelay(Utilities.SHOWCASE_DELAY)
                .singleUse(Utilities.SHOWCASE_LOAD_ID)
                .show();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("message_visibility", mMessage.getVisibility());
        outState.putInt("graph_visibility", mGraphView.getVisibility());
        outState.putString("graph_points", jsonPoints);
    }

    private void restoreState(Bundle savedInstanceState) {
        try {
            //noinspection ResourceType
            mMessage.setVisibility(savedInstanceState.getInt("message_visibility"));
            //noinspection ResourceType
            mGraphView.setVisibility(savedInstanceState.getInt("graph_visibility"));

            loadSeries(savedInstanceState.getString("graph_points"));

        } catch (Exception ignore){}
    }

    @OnClick(R.id.load)
    void performFileSearch() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == READ_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            if (resultData != null) {

                Uri uri = resultData.getData();

                try {
                    loadSeries(readTextFromUri(uri));

                    mGraphView.setVisibility(View.VISIBLE);
                    mMessage.setVisibility(View.INVISIBLE);
                }
                catch (Exception ex) {
                    jsonPoints = null;

                    new AlertDialog.Builder(this)
                            .setMessage(ex.getMessage())
                            .setCancelable(false)
                            .setNegativeButton(R.string.action_ok, null)
                            .create()
                            .show();
                }
            }
        }
    }

    @NonNull
    private String readTextFromUri(Uri uri) throws IOException, NullPointerException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) stringBuilder.append(line);
        reader.close();
        inputStream.close();
        return stringBuilder.toString();
    }

    private void loadSeries(String pointsText) {
        jsonPoints = pointsText;
        List<DataPoint> points = new Gson().fromJson(jsonPoints, pointsType);

        DataPoint[] dp = new DataPoint[points.size()];
        dp = points.toArray(dp);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>( dp );
        mGraphView.addSeries(series);
    }
}