package com.yuqingcheng.luckinstock;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.yuqingcheng.luckinstock.model.trader.MyStockAnalyzer;
import com.yuqingcheng.luckinstock.model.trader.StockAnalyzer;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    static int ADD_STOCK_TO_DISPLAY = 0;
    static int ADD_BASKET_TO_DISPLAY = 1;
    static int EDIT_STOCK = 2;

    Set<Integer> colors;

    StockAnalyzer analyzer;

    List<String> listViewItems;

    ListView listView;

    Map<String, List<Integer>> seriesXMap; // local stock date data of each plot.

    Map<String, List<Double>> seriesYMap; // local stock price data of each plot.

    Map<String, Integer> curveUpdateMap; // detect change in same curve.

    XYPlot plot;

    Map<String, LineAndPointFormatter> formatterMap;

    Map<String, Integer> colorMap; // FIXME: could be deleted

    Set<Integer> dates;

    Map<Integer, Integer> dateValueMap;

    Map<Integer, Integer> dateParseMap;

    int minDate;

    int maxDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        analyzer = new MyStockAnalyzer();

        listViewItems = new ArrayList<>();

        curveUpdateMap = new HashMap<>();

        plot = (XYPlot) findViewById(R.id.plot);

        seriesXMap = new HashMap<>();

        seriesYMap = new HashMap<>();

        formatterMap = new HashMap<>();

        colorMap = new HashMap<>();

        dates = new TreeSet<>();

        dateValueMap = new HashMap<>();

        dateParseMap = new HashMap<>();

        colors = new HashSet<Integer>(Arrays.asList(new Integer[]{ Color.WHITE, Color.BLUE, Color.RED, Color.YELLOW,
                Color.LTGRAY, Color.CYAN, Color.MAGENTA, Color.GREEN, Color.GRAY }));


        listView = (ListView) findViewById(R.id.listView);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new XLabelFormat());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listViewItems);

        listView.setAdapter(arrayAdapter);
    }

    private class XLabelFormat extends Format {

        DateFormat fromFormat = new SimpleDateFormat("yyyyMMdd");
        DateFormat toFormat = new SimpleDateFormat("dd-MMM-yy");

        @Override
        public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition fieldPosition) {
            int val = Math.round(Float.parseFloat(obj.toString()));

            try {
                int date = dateParseMap.get(val);
                return toFormat.format(fromFormat.parse("" + date), toAppendTo, fieldPosition);
            }catch (Exception e){
                return toAppendTo;
            }
        }

        @Override
        public Object parseObject(String s, @NonNull ParsePosition parsePosition) {
            return null;
        }
    }

    public void addStock(View view) {

        if(colors.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Too many plot in graph, please delete some plot to continue.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        Intent intent = new Intent(this, AddStockActivity.class);

        startActivityForResult(intent, ADD_STOCK_TO_DISPLAY);

    }

    /**
     * A general checking method to detect any change in plot data,
     * if change detected, update curves.
     */
    private void refreshPlot() {

        Map<String, Integer> newCurveUpdateMap = analyzer.getCurveUpdateMap();
        Map<String, List<Integer>> newXMap = analyzer.getXMap();
        Map<String, List<Double>> newYMap = analyzer.getYMap();
        boolean changed = false;

        for(String name : curveUpdateMap.keySet()) {
            if(!newCurveUpdateMap.containsKey(name)) {
                changed = true;
                curveUpdateMap.remove(name);

                seriesXMap.remove(name);
                seriesYMap.remove(name);

                formatterMap.remove(name);
                colors.add(colorMap.get(name));
                colorMap.remove(name);
            }
        }

        for(String name : newCurveUpdateMap.keySet()) {
            if(!curveUpdateMap.containsKey(name)
                    || curveUpdateMap.get(name) != newCurveUpdateMap.get(name)) {
                changed = true;

                curveUpdateMap.put(name, newCurveUpdateMap.get(name));
                seriesXMap.put(name, newXMap.get(name));
                seriesYMap.put(name, newYMap.get(name));

                int color = colors.iterator().next();
                LineAndPointFormatter formatter = new LineAndPointFormatter(Color.TRANSPARENT, color, Color.TRANSPARENT, null);

                colors.remove(color);
                colorMap.put(name, color);
                formatterMap.put(name, formatter);
            }
        }

        if(changed) updatePlot();

    }

    /**
     * actually update plots according to current local data.
     */
    public void updatePlot() {

        this.dates = new TreeSet<>();
        this.dateValueMap = new HashMap<>();
        this.dateParseMap = new HashMap<>();

        for(Map.Entry<String, List<Integer>> entry : this.seriesXMap.entrySet()) {
            for(Integer date : entry.getValue()) {
                this.dates.add(date);
            }
        }
        int curr = 0;
        for(Integer date : this.dates) {
            this.dateValueMap.put(date, curr);
            this.dateParseMap.put(curr, date);
            curr++;
        }

        plot.clear();

        for(String name : this.curveUpdateMap.keySet()) {
            List<Integer> tempList = new ArrayList<>();

            for(Integer date : this.seriesXMap.get(name)) {
                tempList.add(this.dateValueMap.get(date));
            }

            plot.addSeries(new SimpleXYSeries(tempList, this.seriesYMap.get(name), name), this.formatterMap.get(name));
        }


        plot.redraw();
    }

    /**
     * Handle results returned from child activities.
     * @param requestCode specifies type of activity returned from.
     * @param resultCode state of returning result.
     * @param data intent containing results.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ADD_STOCK_TO_DISPLAY) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("result");
                String[] strs = result.split(",");

                listViewItems.add(strs[0]);

                UpdateDisplayedItemsTask updateDisplayedItemsTask = new UpdateDisplayedItemsTask();

                try{
                    if(updateDisplayedItemsTask.execute(strs[0], strs[1], strs[2]).get()) {

                        refreshPlot();

                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    Toast toast = Toast.makeText(getApplicationContext(), "Failed to add plot.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result

            }
        }
    }//onActivityResult


    public class UpdateDisplayedItemsTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strs) {

            try{

                analyzer.addStockOrBasketHistoricalDataToDisplayedItems(strs[0], strs[1], strs[2]);

            } catch(Exception e) {
              return false;
            }

            return true;
        }
    }

}
