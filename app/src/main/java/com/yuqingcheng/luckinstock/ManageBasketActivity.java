package com.yuqingcheng.luckinstock;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ManageBasketActivity extends AppCompatActivity {

    final int EDIT_BASKET_NAME_DATE = 0;
    final int DELETE_BASKET_CONFIRMATION = 1;
    final int EDIT_STOCK_SHARES = 2;
    final int DELETE_STOCK_IN_BASKET = 3;

    ImageButton addBasket;
    ListView basketsListView;
    ListView stockShareListView;
    Map<String, Map<String, Integer>> baskets;
    Map<String, Integer> basketDates;
    View hovered;
    String hoveredBasket;
    List<String> basketNames;
    BasketListViewAdapter basketListViewAdapter;
    List<String> stockShareList;
    StockShareListViewAdapter stockShareListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_basket);

        addBasket = (ImageButton) findViewById(R.id.addBasket);
        basketsListView = (ListView) findViewById(R.id.basketList);
        stockShareListView = (ListView) findViewById(R.id.stockShareList);
        baskets = new HashMap<>();
        basketDates = new HashMap<>();
        hoveredBasket = "";

        try{
            Intent intent = getIntent();
            JSONObject basketJSON = new JSONObject(intent.getStringExtra("baskets"));
            JSONObject basketDateJSON = new JSONObject(intent.getStringExtra("basketDates"));
            Iterator<String> ite = basketJSON.keys();
            while(ite.hasNext()) {
                String key = ite.next();
                baskets.put(key, (Map<String, Integer>)basketJSON.get(key));
            }
            ite = basketDateJSON.keys();
            while(ite.hasNext()){
                String key = ite.next();
                basketDates.put(key, basketDateJSON.getInt(key));
            }
        }catch(Exception e) {

        }

        basketNames = new ArrayList(baskets.keySet());

        basketListViewAdapter = new BasketListViewAdapter(this, basketNames);

        basketsListView.setAdapter(basketListViewAdapter);

        stockShareList = new ArrayList<>();

        stockShareListViewAdapter = new StockShareListViewAdapter(this, stockShareList);

        stockShareListView.setAdapter(stockShareListViewAdapter);

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

            Map<String, Integer> stocks = baskets.get(names.get(position));
            final int date = basketDates.get(names.get(position));

            TextView basketName = (TextView) rowView.findViewById(R.id.basketName);

            TextView basketInfo = (TextView) rowView.findViewById(R.id.basketInfo);

            ImageButton delete = (ImageButton) rowView.findViewById(R.id.delete);

            ImageButton edit = (ImageButton) rowView.findViewById(R.id.edit);

            basketName.setText(names.get(position));

            StringBuffer info = new StringBuffer();

            try {

                info.append(new SimpleDateFormat("yy-MMM-dd")
                        .format(new SimpleDateFormat("yyyyMMdd")
                                .parse("" + date)).toString());
            }catch (Exception e) {
                e.printStackTrace();
            }

            for(Map.Entry<String, Integer> entry : stocks.entrySet()) {
                info.append(" "+entry.getKey()+":");
                info.append(""+entry.getValue()+", ");
            }

            if(info.charAt(info.length()-1) == ' ') info.deleteCharAt(info.length()-1);
            if(info.charAt(info.length()-1) == ',') info.deleteCharAt(info.length()-1);

            if(info.length() > 50) {
                basketInfo.setText(info.substring(0, 50)+"..");
            }else{
                basketInfo.setText(info.toString());
            }


            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hoveredBasket = names.get(position);

                    setHoverListItem(view);

                    stockShareList.clear();

                    for(Map.Entry<String, Integer> entry : baskets.get(names.get(position)).entrySet()) {
                        stockShareList.add(entry.getKey()+","+entry.getValue());
                    }

                    stockShareListViewAdapter.notifyDataSetChanged();

                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, EditBasketActivity.class);
                    intent.putExtra("basketName", names.get(position));
                    intent.putExtra("date", date);
                    Map<String, List<String>> map = new HashMap<String, List<String>>();
                    map.put("names", names);
                    intent.putExtra("basketNamesJSON", new JSONObject(map).toString());

                    startActivityForResult(intent, EDIT_BASKET_NAME_DATE);

                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment dialog = new ManageBasketDialog();
                    Bundle args = new Bundle();
                    args.putString(ManageBasketDialog.BASKET_NAME, names.get(position));

                    dialog.setArguments(args);
                    dialog.setTargetFragment(dialog, DELETE_BASKET_CONFIRMATION);
                    dialog.show(getFragmentManager(), "tag");
                }
            });

            return rowView;

        }
    }

    /**
     * Adapter to handle the data in list view.
     */
    private class StockShareListViewAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final List<String> stocks;
        private final List<String> shares;

        public StockShareListViewAdapter(Context context, List<String> data) {
            super(context, -1, data);
            this.context = context;
            this.stocks = new ArrayList<>();
            this.shares = new ArrayList<>();
            for (String each : data) {
                String[] strs = each.split(",");
                this.stocks.add(strs[0]);
                this.shares.add(strs[1]);
            }
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.stock_in_basket_list_view, parent, false);

            TextView stock = (TextView) findViewById(R.id.textView10);
            TextView info = (TextView) findViewById(R.id.stockInfo);
            stock.setText(stocks.get(position));
            info.setText("shares: "+shares.get(position));

            ImageButton edit = (ImageButton) findViewById(R.id.edit);
            ImageButton delete = (ImageButton) findViewById(R.id.delete);

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, EditStockInBasketActivity.class);
                    intent.putExtra("basketName", hoveredBasket);
                    intent.putExtra("stockSymbol", stocks.get(position));
                    intent.putExtra("numShares", shares.get(position));

                    startActivityForResult(intent, EDIT_STOCK_SHARES);

                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment dialog = new ManageBasketDialog();
                    Bundle args = new Bundle();
                    args.putString(ManageBasketDialog.STOCK_SYMBOL, stocks.get(position));
                    args.putString(ManageBasketDialog.BASKET_NAME, hoveredBasket);

                    dialog.setArguments(args);
                    dialog.setTargetFragment(dialog, DELETE_STOCK_IN_BASKET);
                    dialog.show(getFragmentManager(), "tag");

                }
            });

            return rowView;
        }
    }

    public static class ManageBasketDialog extends DialogFragment {

        public static final String STOCK_SYMBOL = "ManageBasketDialog.stockSymbol";
        public static final String BASKET_NAME = "ManageBasketDialog.basketName";

        ManageBasketActivity activity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try{
                this.activity = (ManageBasketActivity) activity;
            }catch (ClassCastException e){
                e.printStackTrace();
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

            if(getTargetRequestCode() == activity.DELETE_BASKET_CONFIRMATION) {

                final String basketName = args.getString(BASKET_NAME);

                return dialog.setTitle("Confirm to delete basket")
                        .setMessage("Are you sure to delete this basket?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.putExtra("basket", basketName);
                                activity.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
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

            }else if(getTargetRequestCode() == activity.DELETE_STOCK_IN_BASKET){
                final String basketName = args.getString(BASKET_NAME);
                final String stockSymbol = args.getString(STOCK_SYMBOL);

                return dialog.setTitle("Confirm to delete stock in basket")
                        .setMessage("Are you sure to delete this stock in basket?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.putExtra("basket", basketName);
                                intent.putExtra("stockSymbol", stockSymbol);
                                activity.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
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

            return null;
        }
    }

    public void addBasket(View view) {
        Intent intent = new Intent(this, EditBasketActivity.class);
        intent.putExtra("basketName", "");
        startActivityForResult(intent, EDIT_BASKET_NAME_DATE);
    }

    public void addStockToBasket(View view) {
        Intent intent = new Intent(this, EditStockInBasketActivity.class);
        intent.putExtra("basketName", hoveredBasket);
        intent.putExtra("stockSymbol", "");
        intent.putExtra("numShares", "");
        startActivityForResult(intent, EDIT_STOCK_SHARES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == EDIT_BASKET_NAME_DATE) {
            if(resultCode == RESULT_OK) {
                try{
                    String name = data.getStringExtra("basketName");
                    int date = data.getIntExtra("date", -1);
                    String formerName = data.getStringExtra("formerBasketName");
                    Log.i("name/date/formerName", name+"/"+date+"/"+formerName);

                    if(formerName.length() > 0) {
                        Map<String, Integer> stockMap = baskets.get(formerName);
                        baskets.remove(formerName);
                        basketDates.remove(formerName);
                        baskets.put(name, stockMap);
                        basketDates.put(name, date);
                    }else{
                        baskets.put(name, new HashMap<String, Integer>());
                        basketDates.put(name, date);
                    }
                    updateBasketDataToServerAndListView();
                }catch(Exception e) {

                }
            }
        }else if(requestCode == DELETE_BASKET_CONFIRMATION){
            if(resultCode == RESULT_OK) {
                try{
                    String name = data.getStringExtra("basket");
                    baskets.remove(name);
                    basketDates.remove(name);
                    updateBasketDataToServerAndListView();
                }catch(Exception e) {

                }
            }
        }else if(requestCode == DELETE_STOCK_IN_BASKET) {
            if(resultCode == RESULT_OK) {
                try{
                    String basketName = data.getStringExtra("basket");
                    String stockSymbol = data.getStringExtra("stockSymbol");
                    baskets.get(basketName).remove(stockSymbol);
                    updateBasketDataToServerAndListView();
                }catch (Exception e) {

                }
            }
        }else if(requestCode == EDIT_STOCK_SHARES) {
            if(resultCode == RESULT_OK) {
                try{
                    String basketName = data.getStringExtra("basket");
                    String stockSymbol = data.getStringExtra("stockSymbol");
                    int numShares = data.getIntExtra("numShares", -1);
                    baskets.get(basketName).put(stockSymbol, numShares);
                    updateBasketDataToServerAndListView();
                }catch (Exception e) {

                }
            }

        }
    }

    private void updateBasketDataToServerAndListView() {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("baskets", new JSONObject(baskets).toString());
        user.put("basketDates", new JSONObject(basketDates).toString());

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    //FIXME
                    basketListViewAdapter.notifyDataSetChanged();
                    stockShareListViewAdapter.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(), "Basket information saved.",Toast.LENGTH_SHORT);

                }else{
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
                }
            }
        });

    }

    protected void setHoverListItem(View view) {
        if (hovered != null) {
            hovered.setHovered(false);
        }
        hovered = view;
        hovered.setHovered(true);
    }

}
