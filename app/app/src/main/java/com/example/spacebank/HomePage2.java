package com.example.spacebank;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.spacebank.login.Login;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomePage2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // in use
    private ImageView addAccountIcon, menuIcon, addMoney, transferMoney, exchangeMoney, blockAccount, unblockAccount, transactions;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private List<AccountHelper> accountList = new ArrayList<>();
    private List<String> accountListBlocked = new ArrayList<>();
    private List<String> accountListUnblocked = new ArrayList<>();
    private List<String> currencyList = new ArrayList<>();
    private AccountsAdapter mAdapter;
    private ArrayAdapter<String> mAdapterBlock, mAdapterUnblock, mAdapterCurrency;
    private Intent homePage2, addMoneyInt, profilePage, transferMoneyInt, exchangeMoneyInt, transactionsInt;
    private AlertDialog.Builder blockDialogBuilder, unblockDialogBuilder, deleteDialogBuilder, addAccountDialogBuilder;
    private AlertDialog blockDialog, unblockDialog, deleteDialog, addAccountDialog;
    private Spinner spinnerAccounts, spinnerAccounts2, spinnerCurrency;
    private Button btnDeclineBlock, btnBlock, btnUnblock, btnDeclineUnblock, btnDeclineDelete, btnDeleteProfile, btnAddNewAccount, btnDeclineAddNew;

    private void initializeWidgets() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menu_icon);
        addMoney = findViewById(R.id.addMoney);
        transferMoney = findViewById(R.id.transferMoney);
        exchangeMoney = findViewById(R.id.exchangeMoney);
        blockAccount = findViewById(R.id.blockAccount);
        unblockAccount = findViewById(R.id.unblockAccount);
        transactions = findViewById(R.id.transactions);
        addAccountIcon = findViewById(R.id.addAccount_icon);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page2);

        initializeWidgets();
        navigationDrawer();

        addListenerToAddAccount();
        mAdapter = new AccountsAdapter(accountList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        prepareAccountData();


        addMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homePage2 = getIntent();
                addMoneyInt = new Intent(view.getContext(), AddMoney.class);
                addMoneyInt.putExtra("cnp", homePage2.getStringExtra("cnp"));
                startActivity(addMoneyInt);
            }
        });

        transferMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homePage2 = getIntent();
                transferMoneyInt = new Intent(view.getContext(), TransferMoney.class);
                transferMoneyInt.putExtra("cnp", homePage2.getStringExtra("cnp"));
                startActivity(transferMoneyInt);
            }
        });

        exchangeMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homePage2 = getIntent();
                exchangeMoneyInt = new Intent(view.getContext(), ExchangeMoney.class);
                exchangeMoneyInt.putExtra("cnp", homePage2.getStringExtra("cnp"));
                startActivity(exchangeMoneyInt);
            }
        });

        blockAccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                createBlockDialog();
            }
        });

        unblockAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUnblockDialog();
            }
        });

        transactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homePage2 = getIntent();
                transactionsInt = new Intent(view.getContext(), Transactions.class);
                transactionsInt.putExtra("cnp", homePage2.getStringExtra("cnp"));
                startActivity(transactionsInt);
            }
        });
    }

    //menu
    private void navigationDrawer()  {

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else
                    drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_profile:
                homePage2 = getIntent();
                profilePage = new Intent(getApplicationContext(), ProfilePage.class);
                profilePage.putExtra("cnp", homePage2.getStringExtra("cnp"));
                profilePage.putExtra("email", homePage2.getStringExtra("email"));
                profilePage.putExtra("firstName", homePage2.getStringExtra("firstName"));
                profilePage.putExtra("lastName", homePage2.getStringExtra("lastName"));
                profilePage.putExtra("phone", homePage2.getStringExtra("phone"));
                profilePage.putExtra("password", homePage2.getStringExtra("password"));

                startActivity(profilePage);

                break;

            case R.id.nav_signOut:
                startActivity(new Intent(getApplicationContext(), Login.class));
                break;

             // for future usage
            case R.id.nav_deleteAccount:
                createDeleteDialog();
                break;
        }

        return true;

    }

    private void createDeleteDialog() {
        deleteDialogBuilder = new AlertDialog.Builder(this);
        final View blockPopupView = getLayoutInflater().inflate(R.layout.activity_delete_profile, null);
        btnDeleteProfile = (Button) blockPopupView.findViewById(R.id.btnDeleteProfile);
        btnDeclineDelete = (Button) blockPopupView.findViewById(R.id.btnDeclineDelete);

        deleteDialogBuilder.setView(blockPopupView);
        deleteDialog = deleteDialogBuilder.create();
        deleteDialog.show();

        btnDeleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProfile();
                deleteDialog.dismiss();
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

        btnDeclineDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
            }
        });

    }

    private void deleteProfile() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("tbusers");
        Intent intent = getIntent();
        String cnp = intent.getStringExtra("cnp");
        reference.child(cnp).removeValue();

        deleteRelatedAccounts(cnp);
        Toast toast = Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT);
        toast.show();

    }

    private void deleteRelatedAccounts (String cnp) {

        reference = database.getReference("accounts");
        reference.orderByChild("cnp").equalTo(cnp).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                AccountHelper account = snapshot.getValue(AccountHelper.class);
                reference.child(account.getAccountNr()).removeValue();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }
        });
    }

    private void addListenerToAddAccount() {
        addAccountIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddAccountDialog();
            }
        });
    }

    private void createAddAccountDialog() {

        mAdapterCurrency = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1,
                currencyList);
        mAdapterCurrency.clear();
        prepareCurrencyList();

        addAccountDialogBuilder = new AlertDialog.Builder(this);
        final View addAccountPopupView = getLayoutInflater().inflate(R.layout.activity_add_new_account, null);
        btnAddNewAccount = (Button) addAccountPopupView.findViewById(R.id.btnAddNewAccount);
        btnDeclineAddNew = (Button) addAccountPopupView.findViewById(R.id.btnDeclineAddNew);

        spinnerCurrency = (Spinner) addAccountPopupView.findViewById(R.id.spinnerCurrency);
        spinnerCurrency.setAdapter(mAdapterCurrency);

        addAccountDialogBuilder.setView(addAccountPopupView);
        addAccountDialog = addAccountDialogBuilder.create();
        addAccountDialog.show();

        btnAddNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currency = (String) spinnerCurrency.getSelectedItem();
                addNewAccount(currency);
                addAccountDialog.dismiss();
            }
        });

        btnDeclineAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAccountDialog.dismiss();
            }
        });
    }

    private void prepareCurrencyList() {
        currencyList.add("EUR");
        currencyList.add("USD");
        currencyList.add("CAD");
        currencyList.add("GBP");
        currencyList.add("RON");
        currencyList.add("HUF");
        currencyList.add("JPY");
        currencyList.add("AUD");
        currencyList.add("CHF");
        currencyList.add("MDL");
        currencyList.add("NOK");
        currencyList.add("RUB");
        currencyList.add("TRY");
    }

    private void addNewAccount(String currency) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("accounts");
        Intent intent = getIntent();
        // get values
        String cnp = intent.getStringExtra("cnp");
        String sum = "0";
        String blocked = "";
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
        Date date = new Date();
        String accountNr = formatter.format(date).toString();

        AccountHelper accountModel = new AccountHelper(accountNr, cnp, currency, sum, blocked);
        reference.child(accountNr).setValue(accountModel);
    }


    //prepare data account for usage
    private void prepareAccountData() {
        reference = database.getReference("accounts");

        Intent intent = getIntent();
        String cnp = intent.getStringExtra("cnp");
        accountList.clear();
        reference.orderByChild("cnp").equalTo(cnp).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                AccountHelper account = snapshot.getValue(AccountHelper.class);
                accountList.add(account);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }
        });
        //      AccountModel account = new AccountModel("1234567", "1234.564", "eur");


    }

//block account
    private void createBlockDialog() {

        mAdapterBlock = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1,
                accountListBlocked);
        mAdapterBlock.clear();
        prepareAccountForBlock();
        blockDialogBuilder = new AlertDialog.Builder(this);
        final View blockPopupView = getLayoutInflater().inflate(R.layout.activity_block_card, null);
        spinnerAccounts = (Spinner) blockPopupView.findViewById(R.id.spinnerAccounts);
        spinnerAccounts.setAdapter(mAdapterBlock);
        btnBlock = (Button) blockPopupView.findViewById(R.id.btnBlock);
        btnDeclineBlock = (Button) blockPopupView.findViewById(R.id.btnDeclineBlock);


        blockDialogBuilder.setView(blockPopupView);
        blockDialog = blockDialogBuilder.create();
        blockDialog.show();

        btnBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String accountNr = (String) spinnerAccounts.getSelectedItem();
                blockAccount(accountNr);

                Toast toast = Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT);
                toast.show();
                blockDialog.dismiss();
            }
        });

        btnDeclineBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockDialog.dismiss();
            }
        });
    }

    private void blockAccount(String accountNr) {
        reference = database.getReference("accounts");

        Intent intent = getIntent();
        String cnp = intent.getStringExtra("cnp");

        reference.orderByChild("accountNr").equalTo(accountNr).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                AccountHelper account = snapshot.getValue(AccountHelper.class);
                account.setBlocked("X");
                reference.child(accountNr).setValue(account);
                prepareAccountData();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }
        });
    }

    private void prepareAccountForBlock() {
        reference = database.getReference("accounts");

        Intent intent = getIntent();
        String cnp = intent.getStringExtra("cnp");

        reference.orderByChild("cnp").equalTo(cnp).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                AccountHelper account = snapshot.getValue(AccountHelper.class);
                if (account.getBlocked().equals("")) {
                    accountListBlocked.add(account.getAccountNr());
                    mAdapterBlock.notifyDataSetChanged();
                }
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

//unblock account
    private void createUnblockDialog() {
        mAdapterUnblock = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1,
                accountListUnblocked);
        mAdapterUnblock.clear();
        prepareAccountForUnblock();
        unblockDialogBuilder = new AlertDialog.Builder(this);
        final View blockPopupView = getLayoutInflater().inflate(R.layout.activity_unblocked_card, null);
        spinnerAccounts2 = (Spinner) blockPopupView.findViewById(R.id.spinnerAccounts2);
        spinnerAccounts2.setAdapter(mAdapterUnblock);
        btnUnblock = (Button) blockPopupView.findViewById(R.id.btnUnblock);
        btnDeclineUnblock = (Button) blockPopupView.findViewById(R.id.btnDeclineUnblock);


        unblockDialogBuilder.setView(blockPopupView);
        unblockDialog = unblockDialogBuilder.create();
        unblockDialog.show();

        btnUnblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String accountNr = (String) spinnerAccounts2.getSelectedItem();
                unblockAccount(accountNr);
                Toast toast = Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT);
                toast.show();

                mAdapter.notifyDataSetChanged();

                unblockDialog.dismiss();
            }
        });

        btnDeclineUnblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unblockDialog.dismiss();

            }
        });
    }

    private void unblockAccount(String accountNr) {
        reference = database.getReference("accounts");

        Intent intent = getIntent();
        String cnp = intent.getStringExtra("cnp");

        reference.orderByChild("accountNr").equalTo(accountNr).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                AccountHelper account = snapshot.getValue(AccountHelper.class);
                account.setBlocked("");
                reference.child(accountNr).setValue(account);
                prepareAccountData();
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

    private void prepareAccountForUnblock() {
        reference = database.getReference("accounts");

        Intent intent = getIntent();
        String cnp = intent.getStringExtra("cnp");

        reference.orderByChild("cnp").equalTo(cnp).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                AccountHelper account = snapshot.getValue(AccountHelper.class);
                if (account.getBlocked().equals("X")) {
                    accountListUnblocked.add(account.getAccountNr());
                    mAdapterUnblock.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }
        });


    }
}