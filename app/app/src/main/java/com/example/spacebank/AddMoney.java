package com.example.spacebank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class AddMoney extends AppCompatActivity {


    private Button btnAddMoney, btnDeclineMoney;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference;
    private TextInputLayout inputLayoutAccountAddMoney, inputLayoutRecipientAddMoney, inputLayoutAmountAddMoney;
    private TextInputEditText txtAccount, txtRecipient, txtAmount, txtDescription;
    private AccountHelper account, recipient;
    private TransactionHelper transactionAccount, transactionRecipient;
    private Intent addMoneyIntent;
    private boolean actionDone = false;

    private void initializeWidgets() {
        txtAccount = findViewById(R.id.accountAddMoney);
        txtRecipient = findViewById(R.id.recipientAddMoney);
        txtAmount = findViewById(R.id.amountAddMoney);
        txtDescription = findViewById(R.id.descriptionAddMoney);
        inputLayoutAccountAddMoney = findViewById(R.id.inputLayoutAccountAddMoney);
        inputLayoutRecipientAddMoney = findViewById(R.id.inputLayoutRecipientAddMoney);
        inputLayoutAmountAddMoney = findViewById(R.id.inputLayoutAmountAddMoney);
        btnAddMoney = findViewById(R.id.btnAddMoney);
        btnDeclineMoney = findViewById(R.id.btnDeclineMoney);
        addMoneyIntent = getIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money);
        initializeWidgets();

        btnDeclineMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addMoney = getIntent();
                Intent homePage2 = new Intent(getApplicationContext(), HomePage2.class);
                homePage2.putExtra("cnp", addMoney.getStringExtra("cnp"));
                startActivity(homePage2);
            }
        });

        btnAddMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputLayoutAmountAddMoney.setError(null);
                inputLayoutAmountAddMoney.setErrorEnabled(false);
                inputLayoutAccountAddMoney.setError(null);
                inputLayoutAccountAddMoney.setErrorEnabled(false);
                inputLayoutRecipientAddMoney.setError(null);
                inputLayoutRecipientAddMoney.setErrorEnabled(false);
                addMoney();
            }
        });

    }

    private void addMoney() {
        checkAccount();
    }

    private void checkAccount() {
        reference = database.getReference("accounts");
        String cnp = addMoneyIntent.getStringExtra("cnp");
        reference.orderByChild("accountNr").equalTo(String.valueOf(txtAccount.getText())).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!actionDone) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            account = childSnapshot.getValue(AccountHelper.class);
                            if (account.getCnp().equals(cnp)) {
                                if(account.getBlocked().equals("")) {
                                    if (Integer.parseInt(txtAmount.getText().toString()) <= Integer.parseInt(account.getSum())) {
                                        checkRecipient();
                                    } else {
                                        inputLayoutAmountAddMoney.setError("Insufficient sold");
                                        inputLayoutAmountAddMoney.requestFocus();
                                    }
                                }else{
                                    inputLayoutAccountAddMoney.setError("Entered Account is blocked");
                                    inputLayoutAccountAddMoney.requestFocus();
                                }
                            } else {
                                inputLayoutAccountAddMoney.setError("Entered Account does not exist");
                                inputLayoutAccountAddMoney.requestFocus();
                            }
                        }
                    } else {
                        inputLayoutAccountAddMoney.setError("Entered Account does not exist");
                        inputLayoutAccountAddMoney.requestFocus();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkRecipient(){
        reference = database.getReference("accounts");
        String cnp = addMoneyIntent.getStringExtra("cnp");
        reference.orderByChild("accountNr").equalTo(String.valueOf(txtRecipient.getText())).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!actionDone) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            recipient = childSnapshot.getValue(AccountHelper.class);
                            if (recipient.getCnp().equals(cnp)) {
                                if (recipient.getCurrency().equals(account.getCurrency())) {
                                    addMoneyAction();
                                } else {
                                    inputLayoutAmountAddMoney.setError("Accounts have different currency! Please use Exchange for money conversion!");
                                    inputLayoutAmountAddMoney.requestFocus();
                                }
                            } else {
                                inputLayoutRecipientAddMoney.setError("Entered Recipient does not exist");
                                inputLayoutRecipientAddMoney.requestFocus();
                            }
                        }
                    } else {
                        inputLayoutRecipientAddMoney.setError("Entered Recipient does not exist");
                        inputLayoutRecipientAddMoney.requestFocus();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addMoneyAction(){


        int amount = Integer.parseInt(txtAmount.getText().toString());
        int accountSold = Integer.parseInt(account.getSum()) - amount;
        int recipientSold =  Integer.parseInt(recipient.getSum()) + amount;

        saveTransaction();

        account.setSum(String.valueOf(accountSold));
        recipient.setSum(String.valueOf(recipientSold));

        reference = database.getReference("accounts");
        reference.child(account.getAccountNr()).setValue(account);
        reference.child(recipient.getAccountNr()).setValue(recipient);

        actionDone = true;
        Toast toast = Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT);
        toast.show();
        Intent homePage2 = new Intent(getApplicationContext(), HomePage2.class);
        homePage2.putExtra("cnp", addMoneyIntent.getStringExtra("cnp"));
        startActivity(homePage2);
    }

    private void saveTransaction() {
        createTransactionAccount();
        createTransactionRecipient();

        reference = database.getReference("transactions");
        reference.child(transactionAccount.getKey()).setValue(transactionAccount);
        reference.child(transactionRecipient.getKey()).setValue(transactionRecipient);

   }

    private void createTransactionAccount() {
        SimpleDateFormat formatterKey = new SimpleDateFormat("ddMMyyyyHHmmss");
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String type = "Internal transfer - Sender";

        transactionAccount = new TransactionHelper();
        transactionAccount.setAccount(account.getAccountNr());
        transactionAccount.setRecipient(recipient.getAccountNr());
        transactionAccount.setAmount("-" + txtAmount.getText().toString() + " " + account.getCurrency());
        transactionAccount.setDescription(txtDescription.getText().toString());
        transactionAccount.setKey(formatterKey.format(date).toString());
        transactionAccount.setType(type);
        transactionAccount.setDate(formatterDate.format(date).toString());
        transactionAccount.setTime(formatterTime.format(date).toString());
    }

    private void createTransactionRecipient()  {
        SimpleDateFormat formatterKey = new SimpleDateFormat("ddMMyyyyHHmmss");
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String type = "Internal transfer - Recipient";

        transactionRecipient = new TransactionHelper();
        transactionRecipient.setAccount(account.getAccountNr());
        transactionRecipient.setRecipient(recipient.getAccountNr());
        transactionRecipient.setAmount("+" + txtAmount.getText().toString() + " " + recipient.getCurrency());
        transactionRecipient.setDescription(txtDescription.getText().toString());
        transactionRecipient.setKey(formatterKey.format(date).toString()+"r");
        transactionRecipient.setType(type);
        transactionRecipient.setDate(formatterDate.format(date).toString());
        transactionRecipient.setTime(formatterTime.format(date).toString());
    }
}