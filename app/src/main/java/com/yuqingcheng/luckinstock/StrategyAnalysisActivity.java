package com.yuqingcheng.luckinstock;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.parse.ParseUser;
import com.yuqingcheng.luckinstock.model.trader.MyStockAnalyzer;
import com.yuqingcheng.luckinstock.model.trader.StockAnalyzer;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class StrategyAnalysisActivity extends AppCompatActivity {

    final static int EDIT_SIMULATION_PLOT = 0;
    final static int DELETE_CONFIRMATION = 1;
    final static int ADD_SIMULATION_PLOT = 2;

    final static String SIMULATION_PREFIX = "Simulation";

    final static String AUTO_REBALANCED_STRATEGY = "Auto-Rebalanced Strategy";

    Set<Integer> colors;

    StockAnalyzer analyzer;

    List<String> listViewSimulations;

    Map<String, String> listViewSimulationInfoMap;

    ListView listView;

    Map<String, Map<String, Integer>> baskets;

    Map<String, Integer> basketDates;

    Map<String, String> simulationJSONMap;

    Map<String, List<Integer>> seriesXMap; // local stock date data of each plot.

    Map<String, List<Double>> seriesYMap; // local stock price data of each plot.

    Map<String, Integer> curveUpdateMap; // detect change in same curve.

    XYPlot plot;

    Map<String, LineAndPointFormatter> formatterMap;

    Map<String, Integer> colorMap;

    Set<Integer> dates;

    Map<Integer, Integer> dateValueMap;

    Map<Integer, Integer> dateParseMap;

    ArrayAdapter<String> listViewAdapter;

    int simulationIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strategy_analysis);

        analyzer = new MyStockAnalyzer();

        listViewSimulations = new LinkedList<>();

        listViewSimulationInfoMap = new HashMap<>();

        curveUpdateMap = new HashMap<>();

        plot = (XYPlot) findViewById(R.id.strategyPlot);

        seriesXMap = new HashMap<>();

        seriesYMap = new HashMap<>();

        formatterMap = new HashMap<>();

        colorMap = new HashMap<>();

        dates = new TreeSet<>();

        dateValueMap = new HashMap<>();

        dateParseMap = new HashMap<>();

        simulationIndex = 0;

        simulationJSONMap = new HashMap<>();

        colors = new HashSet<Integer>(Arrays.asList(new Integer[]{ Color.rgb(252, 40, 252),
                Color.rgb(103, 113, 245), Color.rgb(149, 216, 245), Color.rgb(239, 223, 80),
                Color.rgb(195, 194, 189), Color.rgb(255, 163, 4), Color.rgb(235, 70, 70), Color.rgb(88, 239, 93)}));

        listView = (ListView) findViewById(R.id.strategyListView);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new StrategyAnalysisActivity.XLabelFormat());

        ParseUser user = ParseUser.getCurrentUser();

        baskets = new HashMap<>();

        basketDates = new HashMap<>();

        SynchonizeBasketTask synchonizeBasketTask = new SynchonizeBasketTask();

        try {

            if(synchonizeBasketTask.execute(user.getString("baskets"), user.getString("basketDates")).get()){
                listViewAdapter = new StrategyAnalysisActivity.ListViewAdapter(this, listViewSimulations);
                listView.setAdapter(listViewAdapter);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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
            name.setText(listViewSimulationInfoMap.get(symbols.get(position)));

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), EditStrategyActivity.class);

                    try {

                        JSONObject jsonObject = new JSONObject(simulationJSONMap.get(symbols.get(position)));

                        intent.putExtra("simulationName", symbols.get(position));

                        Iterator<String> ite = jsonObject.keys();

                        while(ite.hasNext()) {
                            String key = ite.next();
                            intent.putExtra(key, jsonObject.getString(key));
                        }

                        startActivityForResult(intent, EDIT_SIMULATION_PLOT);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment dialog = new StrategyAnalysisActivity.DeleteConfirmationDialog();
                    Bundle args = new Bundle();
                    args.putString(DeleteConfirmationDialog.SIMULATION_SYMBOL, symbols.get(position));

                    dialog.setArguments(args);
                    dialog.setTargetFragment(dialog, DELETE_CONFIRMATION); //FIXME
                    dialog.show(getFragmentManager(), "tag");
                }
            });

            return rowView;

        }
    }

    public static class DeleteConfirmationDialog extends DialogFragment {

        public static final String SIMULATION_SYMBOL = "DeleteConfirmationDialog.Symbol";
        String symbol;

        StrategyAnalysisActivity strategyAnalysisActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try{
                strategyAnalysisActivity = (StrategyAnalysisActivity) activity;
            }catch (ClassCastException e){
                e.printStackTrace();
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            symbol = args.getString(SIMULATION_SYMBOL);
            String message = "Are you sure to delete the plot for " + symbol + "?";

            return new AlertDialog.Builder(getActivity())
                    .setTitle("Confirm To Delete")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.putExtra("simulationName", symbol);
                            strategyAnalysisActivity.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
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

    public void addStrategySimulation(View view) {

        if(colors.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Too many plot in graph, please delete some plot to continue.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        Intent intent = new Intent(this, EditStrategyActivity.class);

        startActivityForResult(intent, ADD_SIMULATION_PLOT);

    }

    /**
     * A general checking method to detect any change in plot data,
     * if change detected, update plots.
     */
    private void refreshPlot() {
        Map<String, Integer> newCurveUpdateMap = analyzer.getCurveUpdateMap();
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

        if (requestCode == EDIT_SIMULATION_PLOT) {

            if(resultCode == Activity.RESULT_OK){

                String simulationJSON = data.getStringExtra("simulationJSON");

                String simulationName = data.getStringExtra("simulationName");

                simulationJSONMap.put(simulationName, simulationJSON);

                try {

                    JSONObject jsonObject = new JSONObject(simulationJSON);

                    StringBuffer simulationInfo = new StringBuffer();

                    if(jsonObject.getString("basketName") != null) simulationInfo.append(jsonObject.getString("basketName"));

                    if(jsonObject.getString("strategyName") != null) simulationInfo.append("-"+jsonObject.getString("strategyName"));

                    if(jsonObject.getString("invest")!= null) simulationInfo.append("-"+jsonObject.getString("invest"));

                    listViewSimulationInfoMap.put(simulationName, simulationInfo.toString());

                    listViewAdapter.notifyDataSetChanged();

                    executeStrategy(simulationName, simulationJSON);

                    refreshPlot();

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result

            }
        } else if(requestCode == ADD_SIMULATION_PLOT) {
            if(resultCode == Activity.RESULT_OK){

                if (colors.isEmpty()) {
                    Toast toast = Toast.makeText(this, "Please delete some plots before adding new plot.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                String simulationName = SIMULATION_PREFIX+(simulationIndex++);

                String simulationJSON = data.getStringExtra("simulationJSON");

                simulationJSONMap.put(simulationName, simulationJSON);

                try {

                    JSONObject jsonObject = new JSONObject(simulationJSON);

                    StringBuffer simulationInfo = new StringBuffer();

                    if(jsonObject.getString("basketName") != null) simulationInfo.append(jsonObject.getString("basketName"));

                    if(jsonObject.getString("strategyName") != null) simulationInfo.append("-"+jsonObject.getString("strategyName"));

                    if(jsonObject.getString("invest")!= null) simulationInfo.append("-"+jsonObject.getString("invest"));

                    listViewSimulations.add(simulationName);
                    listViewSimulationInfoMap.put(simulationName, simulationInfo.toString());
                    int color = colors.iterator().next();
                    LineAndPointFormatter formatter = new LineAndPointFormatter(Color.TRANSPARENT, color, Color.TRANSPARENT, null);
                    colors.remove(color);
                    colorMap.put(simulationName, color);
                    formatterMap.put(simulationName, formatter);
                    listViewAdapter.notifyDataSetChanged();

                    executeStrategy(simulationName, simulationJSON);

                    refreshPlot();

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result

            }

        } else if(requestCode == DELETE_CONFIRMATION) {
            if(resultCode == Activity.RESULT_OK) {
                String simulationName = data.getStringExtra("simulationName");
                this.analyzer.removeDisplayedItem(simulationName);
                this.listViewSimulations.remove(simulationName);
                this.listViewSimulationInfoMap.remove(simulationName);
                colors.add(colorMap.get(simulationName));
                colorMap.remove(simulationName);
                formatterMap.remove(simulationName);
                this.listViewAdapter.notifyDataSetChanged();
                refreshPlot();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result

            }
        }
    }//onActivityResult

    private void executeStrategy(String simulationName, String simulationJSON) {
            try {
                JSONObject jsonObject = new JSONObject(simulationJSON);
                if(jsonObject.getString("strategyName").equals(AUTO_REBALANCED_STRATEGY)) {
                    AddAutoBalancedStrategySimulationTask task = new AddAutoBalancedStrategySimulationTask();
                    if (task.execute(simulationName,
                            jsonObject.getString("basketName"),
                            jsonObject.getString("invest"),
                            jsonObject.getString("period"),
                            jsonObject.getString("endDate")).get())
                        Toast.makeText(getApplicationContext(), "simulation done.", Toast.LENGTH_SHORT);
                    else
                        Toast.makeText(getApplicationContext(), "simulation failed.", Toast.LENGTH_SHORT);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
    }

    private class SynchonizeBasketTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strs) {
            try {
                JSONObject basketJSON = new JSONObject(strs[0]);
                JSONObject basketDateJSON = new JSONObject(strs[1]);
                Iterator<String> ite = basketJSON.keys();
                while (ite.hasNext()) {
                    String key = ite.next();
                    try {
                        JSONObject valJSON = basketJSON.getJSONObject(key);
                        Map<String, Integer> valMap = new HashMap<>();
                        Iterator<String> valIte = valJSON.keys();

                        while (valIte.hasNext()) {
                            String valKey = valIte.next();
                            valMap.put(valKey, valJSON.getInt(valKey));
                        }
                        baskets.put(key, valMap);
                    } catch (org.json.JSONException e) {
                        continue;
                    }
                }
                ite = basketDateJSON.keys();
                while (ite.hasNext()) {
                    String key = ite.next();
                    basketDates.put(key, basketDateJSON.getInt(key));
                }

                for(String basketName : baskets.keySet()) {
                    analyzer.addNewBasket(basketName, basketDates.get(basketName));
                    analyzer.addStockOrSharesToBasket(basketName, baskets.get(basketName));
                }
                return true;
            }catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Async task to add items to analyzer, related with retrieving data from the web.
     */

    private class AddAutoBalancedStrategySimulationTask extends AsyncTask<String, Void, Boolean> {

        String[] strs;

        @Override
        protected Boolean doInBackground(String... strs) {

            this.strs = strs;

            try{
                String simulationName = strs[0];
                String basketName = strs[1];
                double invest = Double.valueOf(strs[2]);
                int period = Integer.valueOf(strs[3]);
                String endDate = strs[4];

                analyzer.generateAutoRebalanceStrategy(simulationName, basketName, invest, period, endDate);

            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

    }
}
