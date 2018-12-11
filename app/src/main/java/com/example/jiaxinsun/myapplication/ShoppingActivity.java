package com.example.jiaxinsun.myapplication;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
//import android.widget.Toolbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShoppingActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private FloatingActionButton fab_btn;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private ListView mListView;
    private EditText mBudget;
    private TextView BudgetResult;

    private String ItemName;
    private int Quantity;
    private int QuantityBought;
    private int Priority;
    private double Price;
    private double TotalPrice;
    private String postKey;



    private ArrayList<Item> ItemArray;
    private ArrayList<Item> ItemBoughtArray;
    private ArrayList<Item> ItemNotBoughtArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String value = intent.getStringExtra("budget");



        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shopping List");


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        String uId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);
        mDatabase.keepSynced(true);


        mListView = findViewById(R.id.listView_result);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);



        BudgetResult = findViewById(R.id.budget_result);


        ItemArray = new ArrayList<Item>();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double total = 0;
                String budgetString = mBudget.getText().toString().trim();
                double budgetDouble = Double.parseDouble(budgetString);
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Item item = snap.getValue(Item.class);

                    ItemArray.add(item);


                    //total += item.getPrice()*item.getQuantityBought();


                    //double moneyLeft = budgetDouble - total;

                    //String moneyLeftString = String.valueOf(moneyLeft);

                    BudgetResult.setText(budgetString);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });

        ArrayAdapter<Item> arrayAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, ItemArray);
        mListView.setAdapter(arrayAdapter);
    }









    private int findItem(String itemName) {

        for (int i = 0; i < ItemArray.size(); i++) {
            if (ItemArray.get(i).getItemName() != null) {
                if (ItemArray.get(i).getItemName().toLowerCase().equals(itemName.toLowerCase())) {
                    return i;
                }
            }
        }

        return -1;
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

            case R.id.history:
                // startActivity(new Intent(this, ShoppingActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
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



/*
    public void GoShopping(){
        ItemArray = selectionSort();



        FirebaseRecyclerAdapter<Item, myViewHolder> adapter = new FirebaseRecyclerAdapter<Item, myViewHolder>
                (Item.class, R.layout.item, myViewHolder.class, mDatabase) {
            @Override
            protected void populateViewHolder(myViewHolder viewHolder, final Item mItem, final int position) {
                viewHolder.setItem(mItem.getItemName());
                viewHolder.setQuantity(mItem.getQuantity());
                viewHolder.setPriority(mItem.getPriority());
                viewHolder.setPrice(mItem.getPrice());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postKey = getRef(position).getKey();
                        ItemName = mItem.getItemName();
                        Quantity = mItem.getQuantity();
                        Priority = mItem.getPriority();
                        Price = mItem.getPrice();

                    }
                });

            }
        };
        recyclerView.setAdapter(adapter);

    }
*/







}

