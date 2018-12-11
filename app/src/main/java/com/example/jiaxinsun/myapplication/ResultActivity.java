package com.example.jiaxinsun.myapplication;

import android.content.Context;
import android.content.Intent;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
//import android.widget.Toolbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;


public class ResultActivity extends AppCompatActivity {


    private Toolbar toolbar;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewResult;

    private ListView mListView;

    private ArrayList<Item> ItemArray;
    private ArrayList<Item> ItemBoughtArray;
    private ArrayList<Item> ItemNotBoughtArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        final String budget = intent.getStringExtra("budget");

        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Result");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        String uId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);
        mDatabase.keepSynced(true);

        final TextView budgetResult = findViewById(R.id.budget_result);

        ItemArray = new ArrayList<Item>();


        ItemAdapter arrayAdapter = new ItemAdapter(this, ItemArray);
        mListView = findViewById(R.id.ListView_home);
        mListView.setAdapter(arrayAdapter);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double total = 0;
                int i =0;
                double budgetDouble = Double.parseDouble(budget);
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Item item = snap.getValue(Item.class);
                    ItemArray.add(item);
                    Log.e("itemArray", ItemArray.get(i).getItemName());
                    Log.e("itemArray Quantity", String.valueOf(ItemArray.get(i).getQuantity()));
                    i++;
                }



                //double budgetDouble = Double.parseDouble(budget);
                double value = buyItem(budgetDouble);
                Log.e("check1234", String.valueOf(value));
                String budgetDoubleStr = String.valueOf(value);
                budgetResult.setText(budgetDoubleStr);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });










    }


    public class ItemAdapter extends ArrayAdapter<Item> {
        public ItemAdapter(Context context, ArrayList<Item> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = getItem(position);

            Log.e("print", "viewlist");

            // Check if an existing view is being reused, otherwise inflate the view


            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_result_view, parent, false);
            }


            // Lookup view for data population
            TextView itemName = convertView.findViewById(R.id.itemName_view);
            TextView itemPrice = convertView.findViewById(R.id.itemPrice_view);
            TextView itemQuantityBought = convertView.findViewById(R.id.itemBought_view);
            TextView itemPriority = convertView.findViewById(R.id.itemPriority_view);
            TextView itemSum = convertView.findViewById(R.id.itemSum_view);

            // Populate the data into the template view using the data object



            itemName.setText(item.getItemName());
            itemPrice.setText(String.valueOf(item.getPrice()));
            itemQuantityBought.setText(String.valueOf(item.getQuantityBought()));
            itemPriority.setText(String.valueOf(item.getPriority()));
            itemSum.setText(String.valueOf(item.getQuantityBought()*item.getPrice()));

            // Return the completed view to render on screen
            return convertView;
        }
    }








    public double buyItem(double money){
        double remainingMoney = money;

        ArrayList<Integer> priority = new ArrayList<Integer>();
        ArrayList<Integer> unique = new ArrayList<Integer>();
        ArrayList<Integer> count = new ArrayList<Integer>();
        ArrayList<Integer> startIndex = new ArrayList<Integer>();


        ItemArray = selectionSort();


        Log.e("array size", String.valueOf(ItemArray.size()));



        //priority list
        for(int i = 0; i < ItemArray.size(); i++){
            priority.add(ItemArray.get(i).getPriority());

            Log.e("print priority", String.valueOf(priority.get(i)));
        }

        //unique num list
        for(int i = 0; i < priority.size(); i++){
            unique.add(priority.get(i));

            Log.e("print unique", String.valueOf(unique.get(i)));

        }

        for (int i = 0; i < priority.size()-1; i++) {
            if (priority.get(i) == priority.get(i + 1)) {
                unique.remove(priority.get(i));
            }
        }

        //count list
        for (int i = 0; i < unique.size(); i++) {

            int countNum = 0;
            for (int j = 0; j < priority.size(); j++) {
                if (priority.get(j) == unique.get(i)){
                    countNum += 1;
                }
            }
            count.add(countNum);

        }

        //starting index list
        startIndex.add(0);
        int index;
        for(int i = 1 ; i < count.size(); i++){
            index = startIndex.get(i-1) + count.get(i-1);
            startIndex.add(index);
        }


        for(int i = 0; i < unique.size(); i++){
            double moneyLeft = buyItem1(startIndex.get(i), count.get(i), remainingMoney);
            remainingMoney = moneyLeft;

        }

        Log.e("remainingMoney = ", String.valueOf(remainingMoney));

        return remainingMoney;


    }


    private double buyItem1(int startIndex, int count, double money) {

        Log.e("call","call buyItem1");

        double remainingMoney1 = money;

        double minPrice = ItemArray.get(startIndex).getPrice();

        for (int i = startIndex; i < startIndex + count; i++) {
            if (ItemArray.get(i).getPrice() < minPrice) {
                minPrice = ItemArray.get(i).getPrice();
            }
        }

        int sumQuantity = 0;
        for(int i = startIndex; i< startIndex + count; i ++){

            sumQuantity += ItemArray.get(i).getQuantity();

        }

        int dummy = 0;

        while (remainingMoney1 >= minPrice && dummy < sumQuantity ) {

            for (int i = startIndex; i < startIndex + count; i++) {


                int quantityBought = ItemArray.get(i).getQuantityBought();

                if (remainingMoney1 >= ItemArray.get(i).getPrice()) {

                    if (quantityBought < ItemArray.get(i).getQuantity()) {

                        ItemArray.get(i).setQuantityBought(quantityBought + 1);

                        remainingMoney1 -= ItemArray.get(i).getPrice();

                        dummy+=1;



                    }
                }
            }
        }
        Log.e("remainingMoney1 = ", String.valueOf(remainingMoney1));

        return remainingMoney1;
    }





    public ArrayList<Item> copy(ArrayList<Item> shopList) {

        ArrayList<Item> copyArray = new ArrayList<Item>(shopList.size());
        for(Item i : shopList){
            copyArray.add(i);
        }
        return copyArray;
    }




    public ArrayList<Item> selectionSort(){
        ArrayList<Item> arr = copy(ItemArray);

        for (int i = 0; i < arr.size(); i++) {
            int pos = i;
            for (int j = i; j < arr.size(); j++) {
                if (arr.get(j).getPriority() < arr.get(pos).getPriority())
                    pos = j;
            }
            Item min = arr.get(pos);
            arr.set(pos, arr.get(i));
            arr.set(i, min);
        }

        return arr;
    }



    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.shoppingList:
                startActivity(new Intent(this, HomeActivity.class));
                break;



        }
        return super.onOptionsItemSelected(item);
    }



       /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        final String budget = intent.getStringExtra("budget");

        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Result");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        String uId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);
        mDatabase.keepSynced(true);

        recyclerViewResult = findViewById(R.id.recycler_result);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerViewResult.setHasFixedSize(true);
        recyclerViewResult.setLayoutManager(layoutManager);


        final TextView budgetResult = findViewById(R.id.budget_result);


        ItemArray = new ArrayList<Item>();


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double total = 0;
                double budgetDouble = Double.parseDouble(budget);
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Item item = snap.getValue(Item.class);
                    ItemArray.add(item);
                    total += item.getPrice()*item.getQuantityBought();
                    double moneyLeft = budgetDouble - total;
                    String moneyLeftString = String.valueOf(moneyLeft);
                    budgetResult.setText(moneyLeftString);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }





    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Item, ResultActivity.myViewHolder> adapter = new FirebaseRecyclerAdapter<Item, ResultActivity.myViewHolder>
                (Item.class, R.layout.item_result, ResultActivity.myViewHolder.class, mDatabase) {
            @Override
            protected void populateViewHolder(ResultActivity.myViewHolder viewHolder, final Item mItem, final int position) {
                viewHolder.setItem(mItem.getItemName());
                viewHolder.setQuantityBought(mItem.getQuantityBought());
                viewHolder.setPriority(mItem.getPriority());
                viewHolder.setPrice(mItem.getPrice());
                viewHolder.setTotalPrice(mItem.getQuantityBought()*mItem.getPrice());


            }
        };
        recyclerViewResult.setAdapter(adapter);

    }



    public static class myViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public myViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setItem(String itemName) {
            TextView mItemName = mView.findViewById(R.id.itemName_result);
            mItemName.setText(itemName);
        }

        public void setQuantityBought(int quantityBought) {
            TextView mQuantityBought = mView.findViewById(R.id.quantityBought_result);
            String quantityBoughtInput = String.valueOf(quantityBought);
            mQuantityBought.setText(quantityBoughtInput);
        }

        public void setPriority (int priority) {
            TextView mPriority = mView.findViewById(R.id.priority_result);
            String priorityInput = String.valueOf(priority);
            mPriority.setText(priorityInput);
        }

        public void setPrice(double price){
            TextView mPrice = mView.findViewById(R.id.price_result);
            String priceInput = String.valueOf(price);
            mPrice.setText(priceInput);
        }

        public void setTotalPrice(double totalPrice){
            TextView mTotalPrice = mView.findViewById(R.id.totalPrice_result);
            String totalPriceInput = String.valueOf(totalPrice);
            mTotalPrice.setText(totalPriceInput);
        }

    }



    public double buyItem(double money){
        double remainingMoney = money;

        ArrayList<Integer> priority = new ArrayList<Integer>();
        ArrayList<Integer> unique = new ArrayList<Integer>();
        ArrayList<Integer> count = new ArrayList<Integer>();
        ArrayList<Integer> startIndex = new ArrayList<Integer>();

        //priority list
        for(int i = 0; i < ItemArray.size(); i++){
            priority.add(ItemArray.get(i).getPriority());
        }

        //unique num list
        for(int i = 0; i < priority.size(); i++){
            unique.add(priority.get(i));
        }

        for (int i = 0; i < priority.size()-1; i++) {
            if (priority.get(i) == priority.get(i + 1)) {
                unique.remove(priority.get(i));
            }
        }

        //count list
        for (int i = 0; i < unique.size(); i++) {

            int countNum = 0;
            for (int j = 0; j < priority.size(); j++) {
                if (priority.get(j) == unique.get(i)){
                    countNum += 1;
                }
            }
            count.add(countNum);

        }

        //starting index list
        startIndex.add(0);
        int index;
        for(int i = 1 ; i < count.size(); i++){
            index = startIndex.get(i-1) + count.get(i-1);
            startIndex.add(index);
        }


        for(int i = 0; i < unique.size(); i++){
            double moneyLeft = buyItem1(startIndex.get(i), count.get(i), remainingMoney);
            remainingMoney = moneyLeft;

        }


        return remainingMoney;

    }


    private double buyItem1(int startIndex, int count, double money) {


        double remainingMoney1 = money;

        double minPrice = ItemArray.get(startIndex).getPrice();
        for (int i = startIndex; i < startIndex + count; i++) {
            if (ItemArray.get(i).getPrice() < minPrice) {
                minPrice = ItemArray.get(i).getPrice();
            }
        }

        int sumQuantity = 0;
        for(int i = startIndex; i< startIndex + count; i ++){

            sumQuantity += ItemArray.get(i).getQuantity();

        }

        int dummy = 0;

        while (remainingMoney1 >= minPrice && dummy < sumQuantity ) {

            for (int i = startIndex; i < startIndex + count; i++) {


                int quantityBought = ItemArray.get(i).getQuantityBought();

                if (remainingMoney1 >= ItemArray.get(i).getPrice()) {

                    if (quantityBought < ItemArray.get(i).getQuantity()) {

                        ItemArray.get(i).setQuantityBought(quantityBought + 1);

                        remainingMoney1 -= ItemArray.get(i).getPrice();

                        dummy+=1;



                    }
                }
            }
        }
        return remainingMoney1;
    }




    public ArrayList<Item> copy(ArrayList<Item> shopList) {

        ArrayList<Item> copyArray = new ArrayList<Item>(shopList.size());
        for(Item i : shopList){
            copyArray.add(i);
        }
        return copyArray;
    }




    public ArrayList<Item> selectionSort(){
        ArrayList<Item> arr = copy(ItemArray);

        for (int i = 0; i < arr.size(); i++) {
            int pos = i;
            for (int j = i; j < arr.size(); j++) {
                if (arr.get(j).getPriority() < arr.get(pos).getPriority())
                    pos = j;
            }
            Item min = arr.get(pos);
            arr.set(pos, arr.get(i));
            arr.set(i, min);
        }

        return arr;
    }
*/










}

