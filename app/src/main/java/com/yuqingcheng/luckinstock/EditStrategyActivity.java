package com.yuqingcheng.luckinstock;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.yuqingcheng.luckinstock.util.DateParser;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class EditStrategyActivity extends AppCompatActivity {

    final int TEXT_LENGTH_LIMIT = 30;

    final int hoveredColor = Color.argb(50, 15, 15, 15);
    final int unhoveredColor = Color.argb(0, 255, 255, 255);
    final String REGREX = "#####";

    Button submitStrategy;
    ListView basketsListView;
    ListView strategyListView;
    EditText investEditText;
    EditText periodEditText;
    EditText endDateEditText;
    Map<String, Map<String, Integer>> baskets;
    Map<String, Integer> basketDates;
    View hoveredBasket;
    View hoveredStrategy;
    String hoveredBasketName;
    String hoveredStrategyName;
    String simulationName;
    List<String> basketNames;
    EditStrategyActivity.BasketListViewAdapter basketListViewAdapter;
    List<String> strategyList;
    EditStrategyActivity.StrategyListViewAdapter strategyListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_strategy);

        submitStrategy = (Button) findViewById(R.id.addStrategyButton);
        basketsListView = (ListView) findViewById(R.id.basketListInStrategyPage);
        strategyListView = (ListView) findViewById(R.id.strategyList);
        investEditText = (EditText) findViewById(R.id.investEditText);
        periodEditText = (EditText) findViewById(R.id.periodEditText);
        endDateEditText = (EditText) findViewById(R.id.strategyEndDateEditText);
        baskets = new HashMap<>();
        basketDates = new HashMap<>();
        hoveredBasketName = "";
        hoveredStrategyName = "";
        basketNames = new ArrayList<>();
        simulationName = "";

        try {
            Intent intent = getIntent();
            hoveredBasketName = intent.getStringExtra("basketName");
            hoveredStrategyName = intent.getStringExtra("strategyName");
            investEditText.setText(intent.getStringExtra("invest"));
            periodEditText.setText(intent.getStringExtra("period"));
            endDateEditText.setText(intent.getStringExtra("endDate"));
            simulationName = intent.getStringExtra("simulationName");

        }catch (Exception e) {
            e.printStackTrace();
        }

        ParseUser user = ParseUser.getCurrentUser();

        try {
            JSONObject basketJSON = new JSONObject(user.getString("baskets"));
            JSONObject basketDateJSON = new JSONObject(user.getString("basketDates"));
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        basketListViewAdapter = new EditStrategyActivity.BasketListViewAdapter(this, basketNames);

        basketsListView.setAdapter(basketListViewAdapter);

        String uid = ParseUser.getCurrentUser().getString("userId");

        strategyList = new ArrayList<>();

        try {
            strategyList = new ArrayList(new GetUserStrategyTask().execute(uid).get());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
        }

        strategyListViewAdapter = new StrategyListViewAdapter(this, strategyList);

        strategyListView.setAdapter(strategyListViewAdapter);

        basketListViewAdapter.notifyDataSetChanged();

        strategyListViewAdapter.notifyDataSetChanged();
    }

    private class GetUserStrategyTask extends AsyncTask<String, Void, Vector<String>> {

        @Override
        protected Vector<String> doInBackground(String... strings) {
            String uid = strings[0];

            ParseQuery<ParseObject> query = ParseQuery.getQuery("StrategyPermit");

            query.whereEqualTo("userId", uid);

            final Vector<String> strategyNameList = new Vector<>(); // using vector to ensure thread-safety

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        for (ParseObject each : objects) {
                            ParseQuery<ParseObject> subQuery = ParseQuery.getQuery("Strategy");
                            subQuery.whereEqualTo("strategyId", each.getString("strategyId"));
                            subQuery.setLimit(1);
                            subQuery.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    if (e == null)
                                        strategyNameList.add(objects.get(0).getString("name")+REGREX
                                                +objects.get(0).getString("strategyDescription"));
                                    else e.printStackTrace();
                                }
                            });
                        }
                    } else {
                        e.printStackTrace();
                    }
                }
            });

            return strategyNameList;
        }
    }

    /**
     * Adapter to handle the data in list view.
     */
    private class BasketListViewAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final List<String> names;

        public BasketListViewAdapter(Context context, List<String> names) {
            super(context, -1, names);
            this.context = context;
            this.names = names;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.basket_list_view, parent, false);

            TextView basketName = (TextView) rowView.findViewById(R.id.basketName);

            TextView basketInfo = (TextView) rowView.findViewById(R.id.basketInfo);

            ImageButton delete = (ImageButton) rowView.findViewById(R.id.deleteBasket);

            ImageButton edit = (ImageButton) rowView.findViewById(R.id.editBasket);

            Map<String, Integer> stocks = baskets.get(names.get(position));
            final int date = basketDates.get(names.get(position));

            if (names.get(position).length() > TEXT_LENGTH_LIMIT) {
                basketName.setText(names.get(position).substring(0, TEXT_LENGTH_LIMIT) + "..");
            } else {
                basketName.setText(names.get(position));
            }
            StringBuffer info = new StringBuffer();

            try {
                info.append(new SimpleDateFormat("yy-MMM-dd")
                        .format(new SimpleDateFormat("yyyyMMdd")
                                .parse("" + date)).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (Map.Entry<String, Integer> entry : stocks.entrySet()) {
                info.append(" " + entry.getKey() + ":");
                info.append("" + entry.getValue() + ", ");
            }

            if (info.charAt(info.length() - 1) == ' ') info.deleteCharAt(info.length() - 1);
            if (info.charAt(info.length() - 1) == ',') info.deleteCharAt(info.length() - 1);

            if (info.length() > TEXT_LENGTH_LIMIT) {
                basketInfo.setText(info.substring(0, TEXT_LENGTH_LIMIT) + "..");
            } else {
                basketInfo.setText(info.toString());
            }


            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hoveredBasketName = names.get(position);
                    setHoveredBasketListItem(view);
                }
            });

            edit.setVisibility(View.INVISIBLE);

            delete.setVisibility(View.INVISIBLE);

            if(hoveredBasketName.equals(names.get(position))) {
                setHoveredBasketListItem(rowView);
            }

            return rowView;

        }
    }

    /**
     * Adapter to handle the data in list view.
     */
    private class StrategyListViewAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final List<String> names;

        public StrategyListViewAdapter(Context context, List<String> names) {
            super(context, -1, names);
            this.context = context;
            this.names = names;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.strategy_in_list_view, parent, false);

            TextView strategyName = (TextView) rowView.findViewById(R.id.strategyName);

            TextView strategyInfo = (TextView) rowView.findViewById(R.id.strategyInfo);

            ImageButton strategyInfoButton = (ImageButton) rowView.findViewById(R.id.strategyInfoButton);

            String[] strs = names.get(position).split(REGREX);

            final String strategyNameStr = strs[0];
            final String strategyInfoStr = strs[1];

            if (strategyNameStr.length() > TEXT_LENGTH_LIMIT) {
                strategyName.setText(strategyNameStr.substring(0, TEXT_LENGTH_LIMIT) + "..");
            } else {
                strategyName.setText(strategyNameStr);
            }

            if (strategyInfoStr.length() > TEXT_LENGTH_LIMIT) {
                strategyInfo.setText(strategyInfoStr.substring(0, TEXT_LENGTH_LIMIT) + "..");
            } else {
                strategyInfo.setText(strategyInfoStr);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hoveredStrategyName = strategyNameStr;
                    setHoveredStrategyListItem(view);
                }
            });

            strategyInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getApplicationContext())
                            .setTitle(strategyNameStr)
                            .setMessage(strategyInfoStr)
                            .setPositiveButton(android.R.string.ok, null)
                            .create();
                }
            });

            if(hoveredStrategyName.equals(strategyNameStr)) {
                setHoveredStrategyListItem(rowView);
            }

            return rowView;

        }
    }

    public void submitStrategy(View view) {
        try {
            double invest = Double.valueOf(investEditText.getText().toString());
            try{
                int period = Integer.valueOf(periodEditText.getText().toString());
                try{
                    String date = endDateEditText.getText().toString();
                    int dateAsInteger = DateParser.parseDateToInteger(date);
                    int currentDate = Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                    if(dateAsInteger > currentDate) {
                        Toast.makeText(getApplicationContext(), "Please enter a past date", Toast.LENGTH_SHORT);
                        return;
                    }
                    if(hoveredBasketName.length() > 0 && hoveredStrategyName.length() > 0) {
                        Intent intent = new Intent();
                        Map<String, String> map = new HashMap<>();
                        map.put("invest", ""+invest);
                        map.put("period", ""+period);
                        map.put("endDate", ""+dateAsInteger);
                        map.put("basketName", hoveredBasketName);
                        map.put("strategyName", hoveredStrategyName);
                        try {
                            intent.putExtra("simulationJSON", new JSONObject(map).toString());
                            if(simulationName.length() > 0) intent.putExtra("simulationName", simulationName);
                            setResult(RESULT_OK, intent);
                            finish();
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),
                                "Please select a basket and a strategy.", Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "Please make sure input date is in right format.", Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Please make sure input period is in correct format.", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Please make sure input invest is in correct format.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void setHoveredBasketListItem(View view) {
        if (hoveredBasket != null) {
            hoveredBasket.setBackgroundColor(unhoveredColor);
            hoveredBasket.setHovered(false);
        }
        hoveredBasket = view;
        hoveredBasket.setHovered(true);
        hoveredBasket.setBackgroundColor(hoveredColor);
    }

    protected void setHoveredStrategyListItem(View view) {
        if (hoveredStrategy != null) {
            hoveredStrategy.setBackgroundColor(unhoveredColor);
            hoveredStrategy.setHovered(false);
        }
        hoveredStrategy = view;
        hoveredStrategy.setHovered(true);
    }
}
