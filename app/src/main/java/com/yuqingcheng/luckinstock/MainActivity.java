package com.yuqingcheng.luckinstock;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    final static int UPDATE_STOCK_TO_DISPLAY = 0;
    final static int DELETE_CONFIRMATION = 1;
    final static float MA_50_COLOR_RATIO = 0.75f;
    final static float MA_200_COLOR_RATIO = 0.6f;
    final static String MA_50_CHECKED = "50_CHECKED";
    final static  String MA_200_CHECKED = "200_CHECKED";
    final static String MA_50_SERIES_POSTFIX ="-MA-50";
    final static String MA_200_SERIES_POSTFIX = "-MA-200";

    Set<Integer> colors;

    StockAnalyzer analyzer;

    List<String> listViewSymbols;

    Map<String, String> listViewNameMap;

    Map<String, String> fromDateMap;

    Map<String, String> toDateMap;

    ListView listView;

    Map<String, List<Integer>> seriesXMap; // local stock date data of each plot.

    Map<String, List<Double>> seriesYMap; // local stock price data of each plot.

    Map<String, Integer> curveUpdateMap; // detect change in same curve.

    Map<String, List<Integer>> maXMap;

    Map<String, List<Double>> maYMap;

    Map<String, Integer> maUpdateMap; // detect change in same ma curve.

    XYPlot plot;

    Map<String, LineAndPointFormatter> formatterMap;

    Map<String, Integer> colorMap;

    Set<Integer> dates;

    Map<Integer, Integer> dateValueMap;

    Map<Integer, Integer> dateParseMap;

    ArrayAdapter<String> listViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        analyzer = new MyStockAnalyzer();

        listViewSymbols = new LinkedList<>();

        listViewNameMap = new HashMap<>();

        curveUpdateMap = new HashMap<>();

        fromDateMap = new HashMap<>();

        toDateMap = new HashMap<>();

        plot = (XYPlot) findViewById(R.id.plot);

        seriesXMap = new HashMap<>();

        seriesYMap = new HashMap<>();

        maXMap = new HashMap<>();

        maYMap = new HashMap<>();

        maUpdateMap = new HashMap<>();

        formatterMap = new HashMap<>();

        colorMap = new HashMap<>();

        dates = new TreeSet<>();

        dateValueMap = new HashMap<>();

        dateParseMap = new HashMap<>();

        colors = new HashSet<Integer>(Arrays.asList(new Integer[]{ Color.rgb(252, 40, 252),
                Color.rgb(103, 113, 245), Color.rgb(149, 216, 245), Color.rgb(239, 223, 80),
                Color.rgb(195, 194, 189), Color.rgb(255, 163, 4), Color.rgb(235, 70, 70), Color.rgb(88, 239, 93)}));

        listView = (ListView) findViewById(R.id.listView);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new XLabelFormat());

        listViewAdapter = new ListViewAdapter(this, listViewSymbols);

        listView.setAdapter(listViewAdapter);
    }


    /**
     * A formmater to format the layout of x label.
     */
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

    /**
     * Adapter to handle the data in list view.
     */
    private class ListViewAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final List<String> symbols;

        public ListViewAdapter(Context context, List<String> symbols) {
            super(context, -1, symbols);
            this.context = context;
            this.symbols = symbols;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_item, parent, false);

            final TextView symbol = (TextView) rowView.findViewById(R.id.symbol);

            ImageView color = (ImageView) rowView.findViewById(R.id.color);

            TextView name = (TextView) rowView.findViewById(R.id.name);

            ImageButton edit = (ImageButton) rowView.findViewById(R.id.edit);
            ImageButton delete = (ImageButton) rowView.findViewById(R.id.delete);

            symbol.setText(symbols.get(position));

            int plotColor = colorMap.get(symbols.get(position));
            color.setImageDrawable(new ColorDrawable(plotColor));
            name.setText(listViewNameMap.get(symbols.get(position)));

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), AddStockActivity.class);
                    intent.putExtra("symbol", symbols.get(position));
                    intent.putExtra("fromDate", fromDateMap.get(symbols.get(position)));
                    intent.putExtra("toDate", toDateMap.get(symbols.get(position)));
                    String movingAverage = "";
                    if(maXMap.containsKey(symbols.get(position)+MA_50_SERIES_POSTFIX)){
                        movingAverage += MA_50_CHECKED;
                    }
                    if(maXMap.containsKey(symbols.get(position)+MA_200_SERIES_POSTFIX)){
                        movingAverage += MA_200_CHECKED;
                    }
                    intent.putExtra("movingAverage", movingAverage);

                    startActivityForResult(intent, UPDATE_STOCK_TO_DISPLAY);

                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment dialog = new DeleteConfirmationDialog();
                    Bundle args = new Bundle();
                    args.putString(DeleteConfirmationDialog.STOCK_SYMBOL, symbols.get(position));

                    dialog.setArguments(args);
                    dialog.setTargetFragment(dialog, DELETE_CONFIRMATION); //FIXME
                    dialog.show(getFragmentManager(), "tag");
                }
            });

            return rowView;

        }
    }

    public static class DeleteConfirmationDialog extends DialogFragment {

        public static final String STOCK_SYMBOL = "DeleteConfirmationDialog.Symbol";
        String symbol;

        MainActivity mainActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try{
                mainActivity = (MainActivity) activity;
            }catch (ClassCastException e){
                e.printStackTrace();
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            symbol = args.getString(STOCK_SYMBOL);
            String message = "Are you sure to delete the plot for " + symbol
                    + "? Its moving average plot will be deleted as well.";

            return new AlertDialog.Builder(getActivity())
                    .setTitle("Confirm To Delete")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.putExtra("symbol", symbol);
                            mainActivity.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                        }
                    })
                    .create();
        }

    }

    /**
     * call back method when clicking "add stock" button, direct to AddStockActivity.
     *
     * @param view
     */

    public void addStock(View view) {

        if(colors.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Too many plot in graph, please delete some plot to continue.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        Intent intent = new Intent(this, AddStockActivity.class);

        startActivityForResult(intent, UPDATE_STOCK_TO_DISPLAY);

    }

    /**
     * A general checking method to detect any change in plot data,
     * if change detected, update plots.
     */
    private void refreshPlot() {
        Map<String, Integer> newCurveUpdateMap = analyzer.getCurveUpdateMap();
        Map<String, Integer> newMaUpdateMap = analyzer.getMaUpdateMap();
        Map<String, List<Integer>> newXMap = analyzer.getXMap();
        Map<String, List<Double>> newYMap = analyzer.getYMap();
        boolean changed = false;

        Set<String> currNames = new HashSet(curveUpdateMap.keySet());

        //check removed plot
        for(String name : currNames) {
            if(!newCurveUpdateMap.containsKey(name)) {
                changed = true;
                curveUpdateMap.remove(name);

                seriesXMap.remove(name);
                seriesYMap.remove(name);
            }
        }

        //check added plot
        for(String name : newCurveUpdateMap.keySet()) {
            if(!curveUpdateMap.containsKey(name)
                    || curveUpdateMap.get(name) != newCurveUpdateMap.get(name)) {
                changed = true;
                curveUpdateMap.put(name, newCurveUpdateMap.get(name));
                seriesXMap.put(name, newXMap.get(name));
                seriesYMap.put(name, newYMap.get(name));
            }
        }

        //check removed ma plots.
        Set<String> currMaNames = new HashSet(maUpdateMap.keySet());
        for(String name : currMaNames) {
            if(!newMaUpdateMap.containsKey(name)) {
                changed = true;
                maUpdateMap.remove(name);
                maXMap.remove(name);
                maYMap.remove(name);
            }
        }

        //check added moving average, note that newCurveUpdateMap doesn't have ma information,
        // but newXMap has.
        for(String name : newMaUpdateMap.keySet()) {
            if(!maUpdateMap.containsKey(name)
                    || maUpdateMap.get(name) != newMaUpdateMap.get(name)) {
                changed = true;
                maUpdateMap.put(name, newMaUpdateMap.get(name));
                maXMap.put(name, newXMap.get(name));
                maYMap.put(name, newYMap.get(name));
            }
        }

        if(changed) updatePlot();

    }

    /**
     * actually update plots in view according to current local data.
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

        for(String maName : this.maUpdateMap.keySet()) {
            List<Integer> tempList = new ArrayList<>();
            for(Integer date : this.maXMap.get(maName)) {
                tempList.add(this.dateValueMap.get(date));
            }
            String[] strs = maName.split("-");
            int plotColor = colorMap.get(strs[0]);
            float ratio = strs[2].equals("50") ? MA_50_COLOR_RATIO : MA_200_COLOR_RATIO;
            int color = Color.rgb(Math.round(Color.red(plotColor) * ratio),
                    Math.round(Color.green(plotColor) * ratio),
                    Math.round(Color.blue(plotColor) * ratio));
            LineAndPointFormatter formatter = new LineAndPointFormatter(Color.TRANSPARENT, color, Color.TRANSPARENT, null);
            plot.addSeries(new SimpleXYSeries(tempList, this.maYMap.get(maName), maName), formatter);
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

        if (requestCode == UPDATE_STOCK_TO_DISPLAY) {

            if(resultCode == Activity.RESULT_OK){

                String symbol = data.getStringExtra("symbol");
                String fromDate = data.getStringExtra("fromDate");
                String toDate = data.getStringExtra("toDate");
                String name = data.getStringExtra("name");
                String movingAverage = data.getStringExtra("movingAverage");

                fromDateMap.put(symbol, fromDate);

                toDateMap.put(symbol, toDate);

                boolean listViewUpdated = true;

                for(String each : this.listViewSymbols) {
                    if(each.equals(symbol)) {
                        listViewUpdated = false;
                        break;
                    }
                }

                if(listViewUpdated){
                    if(colors.isEmpty()){
                        Toast toast = Toast.makeText(this, "Please delete some plots before adding new plot.", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }
                    listViewSymbols.add(symbol);
                    listViewNameMap.put(symbol, name);
                    int color = colors.iterator().next();
                    LineAndPointFormatter formatter = new LineAndPointFormatter(Color.TRANSPARENT, color, Color.TRANSPARENT, null);

                    colors.remove(color);
                    colorMap.put(symbol, color);
                    formatterMap.put(symbol, formatter);
                    listViewAdapter.notifyDataSetChanged();
                }

                AddDisplayedItemsTask addDisplayedItemsTask = new AddDisplayedItemsTask();

                try{
                    if(addDisplayedItemsTask.execute(symbol, fromDate, toDate).get()) {

                        AddMovingAverageTask addMovingAverageTask1 = new AddMovingAverageTask();
                        AddMovingAverageTask addMovingAverageTask2 = new AddMovingAverageTask();

                        if(movingAverage.equals(MA_50_CHECKED+MA_200_CHECKED)) {

                            if(addMovingAverageTask1.execute(symbol, "50").get()) {
                                if(addMovingAverageTask2.execute(symbol, "200").get()) refreshPlot();
                            }// two tasks run one after another to avoid concurrency of editing maps in analyzer.

                        } else if(movingAverage.equals(MA_50_CHECKED)){
                            this.analyzer.removeMovingAverage(symbol, 200);
                            if(addMovingAverageTask1.execute(symbol, "50").get()) refreshPlot();
                        }else if(movingAverage.equals(MA_200_CHECKED)){
                            this.analyzer.removeMovingAverage(symbol, 50);
                            if(addMovingAverageTask1.execute(symbol, "200").get()) refreshPlot();
                        }else{
                            this.analyzer.removeMovingAverage(symbol, 50);
                            this.analyzer.removeMovingAverage(symbol, 200);
                            refreshPlot();
                        }


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
        } else if(requestCode == DELETE_CONFIRMATION) {
            if(resultCode == Activity.RESULT_OK) {
                String symbol = data.getStringExtra("symbol");
                this.analyzer.removeDisplayedItem(symbol);
                this.analyzer.removeMovingAverage(symbol, 50);
                this.analyzer.removeMovingAverage(symbol, 200);
                this.listViewSymbols.remove(symbol);
                this.listViewNameMap.remove(symbol);
                this.fromDateMap.remove(symbol);
                this.toDateMap.remove(symbol);
                colors.add(colorMap.get(symbol));
                colorMap.remove(symbol);
                formatterMap.remove(symbol);
                this.listViewAdapter.notifyDataSetChanged();
                refreshPlot();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result

            }
        }
    }//onActivityResult

    /**
     * Async task to add items to analyzer, related with retrieving data from the web.
     */

    private class AddDisplayedItemsTask extends AsyncTask<String, Void, Boolean> {

        String[] strs;

        @Override
        protected Boolean doInBackground(String... strs) {

            this.strs = strs;

            try{

                analyzer.addStockOrBasketHistoricalDataToDisplayedItems(strs[0], strs[1], strs[2]);

            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

    }

    private class AddMovingAverageTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strs) {
            String symbol = strs[0];
            int days= Integer.valueOf(strs[1]);
            try{

                analyzer.addMovingAverageForExistingDisplayedItems(symbol, days);

            }catch(Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
