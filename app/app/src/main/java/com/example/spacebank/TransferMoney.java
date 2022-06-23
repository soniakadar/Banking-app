package com.example.spacebank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TransferMoney extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference;
    private TextInputLayout inputLayoutAccountTransferMoney, inputLayoutRecipientTransferMoney, inputLayoutAmountTransferMoney;
    private TextInputEditText txtAccount, txtRecipient, txtAmount, txtDescription;
    private AccountHelper account, recipient;
    private TransactionHelper transactionAccount, transactionRecipient;
    private Intent transferMoneyIntent;
    private boolean actionDone = false;
    private Button btnDeclineTransfer, btnSendMoney;

    private void initializeWidgets() {
        btnSendMoney = findViewById(R.id.btnSendMoney);
        btnDeclineTransfer = findViewById(R.id.btnDeclineTransfer);
        txtAccount = findViewById(R.id.txtAccountTransferMoney);
        txtRecipient = findViewById(R.id.txtRecipientTransferMoney);
        txtAmount = findViewById(R.id.txtAmountTransferMoney);
        txtDescription = findViewById(R.id.txtDescriptionTransferMoney);
        inputLayoutAccountTransferMoney = findViewById(R.id.inputLayoutAccountTransferMoney);
        inputLayoutRecipientTransferMoney = findViewById(R.id.inputLayoutRecipientTransferMoney);
        inputLayoutAmountTransferMoney = findViewById(R.id.inputLayoutAmountTransferMoney);
        transferMoneyIntent = getIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_money);

        initializeWidgets();

        btnDeclineTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent transferMoney = getIntent();
                Intent homePage2 = new Intent(getApplicationContext(), HomePage2.class);
                homePage2.putExtra("cnp", transferMoney.getStringExtra("cnp"));
                startActivity(homePage2);
            }
        });

        btnSendMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputLayoutAccountTransferMoney.setError(null);
                inputLayoutAccountTransferMoney.setErrorEnabled(false);
                inputLayoutRecipientTransferMoney.setError(null);
                inputLayoutRecipientTransferMoney.setErrorEnabled(false);
                inputLayoutAmountTransferMoney.setError(null);
                inputLayoutAmountTransferMoney.setErrorEnabled(false);
                sendMoney();


            }
        });

    }

    private void sendMoney() {
        checkAccount();
    }

    private void checkAccount() {
        reference = database.getReference("accounts");
        String cnp = transferMoneyIntent.getStringExtra("cnp");
        reference.orderByChild("accountNr").equalTo(String.valueOf(txtAccount.getText())).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!actionDone) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            account = childSnapshot.getValue(AccountHelper.class);
                            if (account.getCnp().equals(cnp)) {
                                if (Float.parseFloat(txtAmount.getText().toString()) <= Float.parseFloat(account.getSum())) {
                                    checkRecipient();
                                } else {
                                    inputLayoutAmountTransferMoney.setError("Insufficient sold");
                                    inputLayoutAmountTransferMoney.requestFocus();
                                }
                            } else {
                                inputLayoutAccountTransferMoney.setError("Entered Account does not exist");
                                inputLayoutAccountTransferMoney.requestFocus();
                            }
                        }
                    } else {
                        inputLayoutAccountTransferMoney.setError("Entered Account does not exist");
                        inputLayoutAccountTransferMoney.requestFocus();
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
        String cnp = transferMoneyIntent.getStringExtra("cnp");
        reference.orderByChild("accountNr").equalTo(String.valueOf(txtRecipient.getText())).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!actionDone) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            recipient = childSnapshot.getValue(AccountHelper.class);
                            if (!recipient.getCnp().equals(cnp)) {
                                if (recipient.getCurrency().equals(account.getCurrency())) {
                                    transferMoneyAction();
                                } else {
                                    inputLayoutAmountTransferMoney.setError("Accounts have different currency! Please use Exchange for money conversion!");
                                    inputLayoutAmountTransferMoney.requestFocus();
                                }
                            } else {
                                inputLayoutRecipientTransferMoney.setError("Please use 'Add money' for transferring money between your accounts");
                                inputLayoutRecipientTransferMoney.requestFocus();
                            }
                        }
                    } else {
                        transferMoneyAction();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void transferMoneyAction(){

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("accounts");
        float amount = Float.parseFloat(txtAmount.getText().toString());
        float accountSold = Float.parseFloat(account.getSum()) - amount;
        account.setSum(String.valueOf(accountSold));
        reference.child(account.getAccountNr()).setValue(account);

        if(recipient!=null) {
            float recipientSold = Float.parseFloat(recipient.getSum()) + amount;
            recipient.setSum(String.valueOf(recipientSold));
            reference.child(recipient.getAccountNr()).setValue(recipient);
        }

        saveTransaction();
        actionDone = true;
        Toast toast = Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT);
        toast.show();
        Intent homePage2 = new Intent(getApplicationContext(), HomePage2.class);
        homePage2.putExtra("cnp", transferMoneyIntent.getStringExtra("cnp"));
        startActivity(homePage2);
    }

    private void saveTransaction() {
        reference = database.getReference("transactions");
        createTransactionAccount();
        reference.child(transactionAccount.getKey()).setValue(transactionAccount);
        if(recipient!=null) {
            createTransactionRecipient();
            reference.child(transactionRecipient.getKey()).setValue(transactionRecipient);
        }
    }

    private void createTransactionAccount() {
        SimpleDateFormat formatterKey = new SimpleDateFormat("ddMMyyyyHHmmss");
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String type = "External transfer - Sender";

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
        String type = "External transfer - Recipient";

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