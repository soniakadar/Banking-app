package com.example.spacebank.signup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.spacebank.AESCrypt;
import com.example.spacebank.R;
import com.example.spacebank.UserHelper;
import com.example.spacebank.login.Login;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SignUpName extends AppCompatActivity {
    private static final String ALGORITHM = "AES";
    private static final String KEY = "1Hbfh667adfDEJ78";

    Button btnSignup2, btnLogin2;
    TextInputLayout inputLayoutFirstName, inputLayoutLastName, inputLayoutEmail, inputLayoutPhone, inputLayoutCnp, inputLayoutPassword, inputLayoutConfirmPassword;
    TextInputEditText txtFirstName, txtLastName, txtEmail, txtPhone, txtCnp, txtPassword, txtConfirmPassword;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_name);

        initializeWidgets();

        btnSignup2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (!validateFirstName() | !validateLastName() | !validatePhone() | !validateCnp() | !validateEmail() | !validatePassword())
                    return;

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("tbusers");

                // get values
                String firstName = txtFirstName.getText().toString();
                String lastName = txtLastName.getText().toString();
                String phone = txtPhone.getText().toString();
                String cnp = txtCnp.getText().toString();
                String email = txtEmail.getText().toString();
                String password = txtPassword.getText().toString();
                String passwordEncrypted = null;
                try {
                    passwordEncrypted = AESCrypt.encrypt(txtPassword.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                UserHelper userHelper = new UserHelper(firstName, lastName, phone, cnp, email, passwordEncrypted);
                reference.child(cnp).setValue(userHelper);


                Intent myIntent = new Intent(view.getContext(), Login.class);
                startActivity(myIntent);

            }
        });

    }

    private Boolean validateFirstName() {
        String val = txtFirstName.getText().toString();

        if (val.isEmpty()) {
            inputLayoutFirstName.setError("Field cannot be empty");
            return false;
        } else {
            inputLayoutFirstName.setError(null);
            inputLayoutFirstName.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateLastName() {
        String val = txtLastName.getText().toString();

        if (val.isEmpty()) {
            inputLayoutLastName.setError("Field cannot be empty");
            return false;
        } else {
            inputLayoutLastName.setError(null);
            inputLayoutLastName.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validatePhone() {
        String val = txtPhone.getText().toString();

        if (val.isEmpty()) {
            inputLayoutPhone.setError("Field cannot be empty");
            return false;
        } else {
            inputLayoutPhone.setError(null);
            inputLayoutPhone.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateCnp() {
        String val = txtCnp.getText().toString();

        if (val.isEmpty()) {
            inputLayoutCnp.setError("Field cannot be empty");
            return false;
        } else if (val.length() != 13) {
            inputLayoutCnp.setError("CNP need to have 13 characters");
            return false;
        } else {
            inputLayoutCnp.setError(null);
            inputLayoutCnp.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateEmail() {
        String val = txtEmail.getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";

        if (val.isEmpty()) {
            inputLayoutEmail.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(emailPattern)) {
            inputLayoutEmail.setError("Invalid email address");
            return false;
        } else {
            inputLayoutEmail.setError(null);
            inputLayoutEmail.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validatePassword() {
        String val = txtPassword.getText().toString();
        String passwordValidation = "^" +
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +          //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                // "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[±§!@#$%^&+=])" +    //at least 1 special character
                "(?=.\\S+$)" +          //no white space
                ".{4,}" +                //at least 4 characters
                "$";

        if (val.isEmpty()) {
            inputLayoutPassword.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(passwordValidation)) {
            inputLayoutPassword.setError("Password is to weak");
            return false;
        } else {
            inputLayoutPassword.setError(null);
            inputLayoutPassword.setErrorEnabled(false);
            return true;
        }

    }


  /*  public void registerUser(View view){

        if(!validateFirstName() | !validateLastName() | !validatePhone() | !validateCnp() | !validateEmail() | !validatePassword())
            return;

        // get values
        String firstName = txtFirstName.getText().toString();
        String lastName = txtLastName.getText().toString();
        String phone = txtPhone.getText().toString();
        String cnp  = txtCnp.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        UserHelper userHelper = new UserHelper(firstName, lastName,phone, cnp, email, password);
        reference.child(email).setValue(userHelper);

    }*/

    private void initializeWidgets() {
        inputLayoutFirstName = findViewById(R.id.inputLayoutFirstName);
        inputLayoutLastName = findViewById(R.id.inputLayoutLastName);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        inputLayoutPhone = findViewById(R.id.inputLayoutPhone);
        inputLayoutCnp = findViewById(R.id.inputLayoutCnp);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
    //    inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword);

        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtCnp = findViewById(R.id.txtCnp);
        txtPassword = findViewById(R.id.txtPassword);
    //    txtConfirmPassword = findViewById(R.id.txtConfirmPassword);

        btnSignup2 = findViewById(R.id.btnSignup2);
        btnLogin2 = findViewById(R.id.btnLogin2);
    }

}