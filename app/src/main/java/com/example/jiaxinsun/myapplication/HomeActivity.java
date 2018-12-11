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

public class HomeActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private FloatingActionButton fab_btn;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private EditText mBudget;

    private String ItemName;
    private int Quantity;
    private int Priority;
    private double Price;
    private String postKey;

    private Button GoShopping;


    private ArrayList<Item> ItemArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shopping List");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        String uId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);
        mDatabase.keepSynced(true);



        fab_btn = findViewById(R.id.fab);
        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog();
            }
        });


        recyclerView = findViewById(R.id.recycler_home);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mBudget = findViewById(R.id.budget_home);


        ItemArray = new ArrayList<Item>();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Item item = snap.getValue(Item.class);
                    ItemArray.add(item);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });



        GoShopping=findViewById(R.id.btn_shopping);
        GoShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String budgetString = mBudget.getText().toString().trim();
                if(ItemArray.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Empty Shopping List", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(budgetString)){
                    mBudget.setError("Required Field..");
                    return;

                }else {
                    Toast.makeText(getApplicationContext(), "processing...", Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(HomeActivity.this, ResultActivity.class);
                    myIntent.putExtra("budget", budgetString);
                    HomeActivity.this.startActivity(myIntent);
                    return;
                }

            }
        });





    }


    private void customDialog() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View myView = inflater.inflate(R.layout.input_data, null);
        final AlertDialog dialog = myDialog.create();
        dialog.setView(myView);


        final EditText item = myView.findViewById(R.id.edt_itemName);
        final EditText quantity = myView.findViewById(R.id.edt_quantity);
        final EditText priority = myView.findViewById(R.id.edt_priority);
        final EditText price = myView.findViewById(R.id.edt_price);

        Button btnSave = myView.findViewById(R.id.btn_save);


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mItemName = item.getText().toString().trim();
                String mQuantity = quantity.getText().toString().trim();
                String mPriority = priority.getText().toString().trim();
                String mPrice = price.getText().toString().trim();


                if (TextUtils.isEmpty(mItemName)) {
                    item.setError("Required Field..");
                    return;
                }

                if (TextUtils.isEmpty(mQuantity)) {
                    quantity.setError("Required Field..");
                    return;
                }

                if (TextUtils.isEmpty(mPriority)) {
                    priority.setError("Required Field..");
                    return;
                }

                if (TextUtils.isEmpty(mPrice)) {
                    price.setError("Required Field..");
                    return;
                }


                if(findItem(mItemName)>=0){
                    item.setError("item already exist..");
                    return;
                }


                int quantityInt = Integer.parseInt(mQuantity);
                int priorityInt = Integer.parseInt(mPriority);
                double priceDouble = Double.parseDouble(mPrice);

                String id = mDatabase.push().getKey();
                Item item = new Item(mItemName, quantityInt, id, priceDouble, priorityInt,0);
                mDatabase.child(id).setValue(item);
                Toast.makeText(getApplicationContext(), "Item Add", Toast.LENGTH_SHORT).show();


                dialog.dismiss();
            }
        });


        dialog.show();

    }






    @Override
    protected void onStart() {
        super.onStart();
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

                        update();
                    }
                });

            }
        };
        recyclerView.setAdapter(adapter);

    }


    public static class myViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public myViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setItem(String itemName) {
            TextView mItemName = mView.findViewById(R.id.itemName);
            mItemName.setText(itemName);
        }

        public void setQuantity(int quantity) {
            TextView mQuantity = mView.findViewById(R.id.quantity);
            String quantityInput = String.valueOf(quantity);
            mQuantity.setText(quantityInput);
        }

        public void setPriority (int priority) {
            TextView mPriority = mView.findViewById(R.id.priority);
            String priorityInput = String.valueOf(priority);
            mPriority.setText(priorityInput);
        }


        public void setPrice(double price){
            TextView mPrice = mView.findViewById(R.id.price);
            String priceInput = String.valueOf(price);

            mPrice.setText(priceInput);


            //mPrice.setText(String.format("%.2f", priceInput));
        }

    }



    public void update() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View mView = inflater.inflate(R.layout.input_update, null);
        final AlertDialog dialog = myDialog.create();
        dialog.setView(mView);


        final TextView ItemName_update = mView.findViewById(R.id.itemName_update);
        final EditText edt_Quantity = mView.findViewById(R.id.edt_quantity_update);
        final EditText edt_Priority = mView.findViewById(R.id.edt_priority_update);
        final EditText edt_Price = mView.findViewById(R.id.edt_price_update);


        ItemName_update.setText(ItemName);
        //ItemName_update.setSelection(ItemName.length());

        edt_Quantity.setText(String.valueOf(Quantity));
        edt_Quantity.setSelection(String.valueOf(Quantity).length());

        edt_Priority.setText(String.valueOf(Priority));
        edt_Priority.setSelection(String.valueOf(Priority).length());


        edt_Price.setText(String.valueOf(Price));
        edt_Price.setSelection(String.valueOf(Price).length());

        Button btn_Update = mView.findViewById(R.id.btn_update);
        Button btn_Delete = mView.findViewById(R.id.btn_delete);

        btn_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Quantity = edt_Quantity.getText().toString().trim();
                String Priority = edt_Priority.getText().toString().trim();
                String Price = edt_Price.getText().toString().trim();



                if (TextUtils.isEmpty(Quantity)) {
                    edt_Quantity.setError("Required Field..");
                    return;
                }

                if (TextUtils.isEmpty(Priority)) {
                    edt_Priority.setError("Required Field..");
                    return;
                }

                if (TextUtils.isEmpty(Price)) {
                    edt_Price.setError("Required Field..");
                    return;
                }


                int QuantityInt = Integer.parseInt(Quantity);
                int PriorityInt = Integer.parseInt(Priority);
                Double PriceDouble = Double.parseDouble(Price);

                Item item = new Item(ItemName, QuantityInt, postKey, PriceDouble,PriorityInt,0);

                mDatabase.child(postKey).setValue(item);

                dialog.dismiss();

            }
        });

        btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(postKey).removeValue();

                dialog.dismiss();

            }
        });

        dialog.show();

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
                String budgetString = mBudget.getText().toString().trim();
                Intent myIntent = new Intent(HomeActivity.this, ResultActivity.class);
                myIntent.putExtra("budget", budgetString);
                HomeActivity.this.startActivity(myIntent);
                break;

            case R.id.clearAll:
                Toast.makeText(getApplicationContext(), "Data Remove", Toast.LENGTH_SHORT).show();
                DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                root.setValue(null);
                startActivity(new Intent(this, HomeActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }









}

