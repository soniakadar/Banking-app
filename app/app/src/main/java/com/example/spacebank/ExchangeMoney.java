package com.example.spacebank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class ExchangeMoney extends AppCompatActivity {


    private Button btnExchange, btnDeclineExchange;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference;
    private TextInputLayout inputLayoutAccountExchange, inputLayoutRecipientExchange, inputLayoutAmountExchange;
    private TextInputEditText txtAccount, txtRecipient, txtAmount, txtDescription;
    private AccountHelper account, recipient;
    private TransactionHelper transactionAccount, transactionRecipient;
    private Intent exchangeIntent;
    private boolean actionDone = false;

    private void initializeWidgets() {
        btnExchange= findViewById(R.id.btnExchange);
        btnDeclineExchange = findViewById(R.id.btnDeclineExchange);
        txtAccount = findViewById(R.id.txtAccountExchange);
        txtRecipient = findViewById(R.id.txtRecipientExchange);
        txtAmount = findViewById(R.id.txtAmountExchange);
        txtDescription = findViewById(R.id.txtDescriptionExchange);
        inputLayoutAccountExchange = findViewById(R.id.inputLayoutAccountExchange);
        inputLayoutRecipientExchange = findViewById(R.id.inputLayoutRecipientExchange);
        inputLayoutAmountExchange = findViewById(R.id.inputLayoutAmountExchange);
        exchangeIntent = getIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_money);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        initializeWidgets();

        btnDeclineExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent exchangeMoney = getIntent();
                Intent homePage2 = new Intent(getApplicationContext(), HomePage2.class);
                homePage2.putExtra("cnp", exchangeMoney.getStringExtra("cnp"));
                startActivity(homePage2);
            }
        });

        btnExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputLayoutAccountExchange.setError(null);
                inputLayoutAccountExchange.setErrorEnabled(false);
                inputLayoutAmountExchange.setError(null);
                inputLayoutAmountExchange.setErrorEnabled(false);
                inputLayoutRecipientExchange.setError(null);
                inputLayoutRecipientExchange.setErrorEnabled(false);
                exchangeMoney();
            }
        });

    }

    private void exchangeMoney() {
        checkAccount();
    }

    private void checkAccount() {
        reference = database.getReference("accounts");
        String cnp = exchangeIntent.getStringExtra("cnp");
        reference.orderByChild("accountNr").equalTo(String.valueOf(txtAccount.getText())).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!actionDone) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            account = childSnapshot.getValue(AccountHelper.class);
                            if (account.getCnp().equals(cnp)) {
                                if (Integer.parseInt(txtAmount.getText().toString()) <= Integer.parseInt(account.getSum())) {
                                    checkRecipient();
                                } else {
                                    inputLayoutAmountExchange.setError("Insufficient sold");
                                    inputLayoutAmountExchange.requestFocus();
                                }
                            } else {
                                inputLayoutAccountExchange.setError("Entered Account does not exist");
                                inputLayoutAccountExchange.requestFocus();
                            }
                        }
                    } else {
                        inputLayoutAccountExchange.setError("Entered Account does not exist");
                        inputLayoutAccountExchange.requestFocus();
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
        String cnp = exchangeIntent.getStringExtra("cnp");
        reference.orderByChild("accountNr").equalTo(String.valueOf(txtRecipient.getText())).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!actionDone) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            recipient = childSnapshot.getValue(AccountHelper.class);
                            if (recipient.getCnp().equals(cnp)) {
                                if (!recipient.getCurrency().equals(account.getCurrency())) {
                                    exchangeAction();
                                } else {
                                    inputLayoutAmountExchange.setError("Accounts have same currency! Please use 'Add money' for transfer!");
                                    inputLayoutAmountExchange.requestFocus();
                                }
                            } else {
                                inputLayoutRecipientExchange.setError("Entered Recipient does not exist");
                                inputLayoutRecipientExchange.requestFocus();
                            }
                        }
                    } else {
                        inputLayoutRecipientExchange.setError("Entered Recipient does not exist");
                        inputLayoutRecipientExchange.requestFocus();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void exchangeAction(){
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("accounts");
        String baseCurrency = account.getCurrency();
        String convertedToCurrency = recipient.getCurrency();
        int amount = Integer.parseInt(txtAmount.getText().toString());
        int accountSold = Integer.parseInt(account.getSum()) - amount;
        account.setSum(String.valueOf(accountSold));

        int convertedAmount = callAPI(baseCurrency,convertedToCurrency,amount);
        recipient.setSum(String.valueOf(convertedAmount));

        reference.child(account.getAccountNr()).setValue(account);
        reference.child(recipient.getAccountNr()).setValue(recipient);
        saveTransaction(convertedAmount);
        actionDone = true;
        Toast toast = Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT);
        toast.show();
        Intent homePage2 = new Intent(getApplicationContext(), HomePage2.class);
        homePage2.putExtra("cnp", exchangeIntent.getStringExtra("cnp"));
        startActivity(homePage2);
    }

    private void saveTransaction(int convertedAmount) {
        createTransactionAccount();
        createTransactionRecipient(convertedAmount);

        reference = database.getReference("transactions");
        reference.child(transactionAccount.getKey()).setValue(transactionAccount);
        reference.child(transactionRecipient.getKey()).setValue(transactionRecipient);
    }

    private int callAPI(String baseCurrency, String convertedToCurrency, int amount) {
        int convertedAmount = 0;
        String access_key = "cS9Qs9Fhhbz9YUnYsqSw";
        String API = "https://fcsapi.com/api-v3/forex/latest?symbol=" + baseCurrency +"/" + convertedToCurrency + "&access_key="+ access_key;
        try {
            URL url = new URL(API);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            if(connection!=null) {
                return getAPIresponse(connection, amount);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertedAmount;
    }

    private int getAPIresponse(HttpsURLConnection connection, int amount) throws IOException {
        int convertedAmount = 0;

        if (connection.getResponseCode() == 200 )
        {
              BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
              StringBuilder  sb = new StringBuilder();
              String output;
              while ((output = br.readLine()) != null) {
                  sb.append(output);
                  return calculate(amount,output);
              }

        }  else {
                //error
        }
        return 0;
    }

    private int calculate(int amount, String output) {
        String rateString = output.split("c")[4].replace('"',' ')
                .replace(':', ' ').replace(',', ' ');
     float rate =  Float.parseFloat(rateString);
     float convertedAmount = amount * rate;
     return (int) convertedAmount;
    }
//

    private void createTransactionAccount() {
        SimpleDateFormat formatterKey = new SimpleDateFormat("ddMMyyyyHHmmss");
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String type = "Exchange - Sender";

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

    private void createTransactionRecipient(int convertedAmount)  {
        SimpleDateFormat formatterKey = new SimpleDateFormat("ddMMyyyyHHmmss");
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String type = "Exchange - Recipient";

        transactionRecipient = new TransactionHelper();
        transactionRecipient.setAccount(account.getAccountNr());
        transactionRecipient.setRecipient(recipient.getAccountNr());
        transactionRecipient.setAmount("+" + String.valueOf(convertedAmount) + " " + recipient.getCurrency());
        transactionRecipient.setDescription(txtDescription.getText().toString());
        transactionRecipient.setKey(formatterKey.format(date).toString()+"r");
        transactionRecipient.setType(type);
        transactionRecipient.setDate(formatterDate.format(date).toString());
        transactionRecipient.setTime(formatterTime.format(date).toString());
    }

}