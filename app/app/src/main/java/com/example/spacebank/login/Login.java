package com.example.spacebank.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.spacebank.AESCrypt;
import com.example.spacebank.HomePage2;
import com.example.spacebank.R;
import com.example.spacebank.UserHelper;
import com.example.spacebank.signup.SignUpName;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    Button btnForgotPassword, btnLogIn, btnSignUp;
    TextInputLayout inputLayoutEmail, inputLayoutPassword;
    TextInputEditText txtEmail, txtPassword;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference;

    private void initializeWidgets() {
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail1);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword1);

        txtEmail = findViewById(R.id.txtEmail1);
        txtPassword = findViewById(R.id.txtPassword1);

        btnLogIn = findViewById(R.id.btnLogIn);
        btnSignUp = findViewById(R.id.btnSignUp);
      /*  btnForgotPassword = findViewById(R.id.btnForgotPassword);*/
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeWidgets();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), SignUpName.class);
                startActivity(myIntent);
            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateEmail() | !validatePassword()){
                    return;
                }
                else
                    isUser();
            }
        });
        /*    btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), ForgotPassword.class);
                startActivity(myIntent);
            }
        });


     */

    }

    private Boolean validateEmail() {
        String val = txtEmail.getText().toString();
        if (val.isEmpty()) {
            inputLayoutEmail.setError("Email is mandatory");
            return false;
        } else {
            inputLayoutEmail.setError(null);
            inputLayoutEmail.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validatePassword() {
        String val = txtPassword.getText().toString();
        if (val.isEmpty()) {
            inputLayoutPassword.setError("Password is mandatory");
            return false;
        } else {
            inputLayoutPassword.setError(null);
            inputLayoutPassword.setErrorEnabled(false);
            return true;
        }
    }

    /*public void loginUser(View view){

        if(!validateEmail() | !validatePassword()){
            return;
        }
        else
            isUser();
    }*/


    public void isUser(){
        String userEnteredEmail = txtEmail.getText().toString().trim();
        String userEnteredPassword = txtPassword.getText().toString().trim();
        reference = database.getReference("tbusers");
       /* reference.orderByChild("email").equalTo(userEnteredEmail).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    UserHelper user = snapshot.getValue(UserHelper.class);
         });
         }*/


        reference.orderByChild("email").equalTo(userEnteredEmail).addValueEventListener(new ValueEventListener()
         {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        UserHelper user = childSnapshot.getValue(UserHelper.class);
                        if (!user.getCnp().equals(null)) {
                            inputLayoutEmail.setError(null);
                            inputLayoutEmail.setErrorEnabled(false);
                            String passwordEncrypt = null;
                            String passwordFromDB = user.getPassword();
                            try {
                                passwordEncrypt = AESCrypt.encrypt(userEnteredPassword);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (passwordFromDB.equals(passwordEncrypt)) {

                                inputLayoutPassword.setError(null);
                                inputLayoutPassword.setErrorEnabled(false);

                                Intent intent = new Intent(getApplicationContext(), HomePage2.class);
                                intent.putExtra("cnp", user.getCnp());
                                intent.putExtra("email", user.getEmail());
                                intent.putExtra("firstName", user.getFirstName());
                                intent.putExtra("lastName", user.getLastName());
                                intent.putExtra("phone", user.getPhone());
                                intent.putExtra("password", user.getPassword());
                                startActivity(intent);
                            } else {
                                inputLayoutPassword.setError("Wrong password");
                                inputLayoutPassword.requestFocus();
                            }
                        } else {
                            inputLayoutEmail.setError("There is no account associated to this email");
                            inputLayoutEmail.requestFocus();
                        }
                    }
                }else{
                    inputLayoutEmail.setError("There is no account associated to this email");
                    inputLayoutEmail.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}