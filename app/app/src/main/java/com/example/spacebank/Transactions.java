package com.example.spacebank;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Transactions extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference;
    private List<String> accountListTransactions = new ArrayList<>();
    private List<TransactionHelper> transactionList = new ArrayList<>();
    private TransactionAdapter mAdapter;
    private ArrayAdapter<String> mAdapterAccountTransactions;
    private Button btnBackToHomePage2;
    private Spinner spinnerTransactions;

    //private ListView transactionList;

    private void initializeWidgets() {
        btnBackToHomePage2 = findViewById(R.id.btnBackToHomePage2);
        spinnerTransactions = findViewById(R.id.spinnerTransactions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        initializeWidgets();
        setAccountsSpinner();

        mAdapter = new TransactionAdapter(transactionList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        recyclerViewTransactions.setLayoutManager(mLayoutManager);
        recyclerViewTransactions.setItemAnimator(new DefaultItemAnimator());
        recyclerViewTransactions.setAdapter(mAdapter);


        btnBackToHomePage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent transactionInt = getIntent();
                Intent homePage2 = new Intent(getApplicationContext(), HomePage2.class);
                homePage2.putExtra("cnp", transactionInt.getStringExtra("cnp"));
                startActivity(homePage2);
            }
        });

        spinnerTransactions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!Objects.equals(position, 0)) {
                String accountNr = String.valueOf(spinnerTransactions.getSelectedItem()).split(" ")[0];
                showTransactions(accountNr);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }


    //transactions list
    private void showTransactions(String accountNr) {
        transactionList.clear();
        getTransactionsFromDB(accountNr);

    }

    private void getTransactionsFromDB(String accountNr) {
        reference = database.getReference("transactions");

        reference.orderByChild("account").equalTo(accountNr).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                TransactionHelper transaction = snapshot.getValue(TransactionHelper.class);
                if(!transaction.getKey().contains("r")) {
                    transactionList.add(transaction);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {           }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }
        });

        reference.orderByChild("recipient").equalTo(accountNr).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                TransactionHelper transaction = snapshot.getValue(TransactionHelper.class);
                if(transaction.getKey().contains("r") ) {
                    transactionList.add(transaction);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {           }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }
        });
    }

    //spinner
    private void setAccountsSpinner() {
        mAdapterAccountTransactions = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1,
                accountListTransactions);
        accountListTransactions.clear();
        mAdapterAccountTransactions.clear();
        prepareAccountsForTransactions();
        spinnerTransactions.setAdapter(mAdapterAccountTransactions);
    }

    private void prepareAccountsForTransactions() {
        reference = database.getReference("accounts");

        Intent intent = getIntent();
        String cnp = intent.getStringExtra("cnp");

        accountListTransactions.add(" ");
        reference.orderByChild("cnp").equalTo(cnp).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                AccountHelper account = snapshot.getValue(AccountHelper.class);
                accountListTransactions.add(account.getAccountNr() + " " + account.getCurrency());
                mAdapterAccountTransactions.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}