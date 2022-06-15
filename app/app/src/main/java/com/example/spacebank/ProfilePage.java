package com.example.spacebank;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spacebank.login.Login;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfilePage extends AppCompatActivity {

    TextInputLayout inputLayoutFullName, inputLayoutEmail, inputLayoutPassword, inputLayoutPhone;
    TextInputEditText txtFullName, txtBday, txtPhone, txtEmail, txtPassword;
    TextView txtFull_name;
    Button btnBackToHomePage, btnUpdateProfile;
    DatabaseReference reference;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String cnp;
    UserHelper userDB;
    Intent intent;


    @SuppressLint("WrongViewCast")
    private void initializeWidgets() {
        reference = FirebaseDatabase.getInstance().getReference("tbusers");

        inputLayoutFullName = findViewById(R.id.inputLayoutFullName);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmailProfile);
        inputLayoutPassword = findViewById(R.id.inputLayoutPasswordProfile);
        inputLayoutPhone = findViewById(R.id.inputLayoutPhoneProfile);
        txtFullName = findViewById(R.id.txtFullName);
        txtBday = findViewById(R.id.txtBday);
        txtPhone = findViewById(R.id.txtPhoneNo);
        txtEmail = findViewById(R.id.txtEmail2);
        txtPassword = findViewById(R.id.txtPassword2);
        txtFull_name = findViewById(R.id.full_name);
        btnBackToHomePage = findViewById(R.id.btnBackToHomePage);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("tbusers");
        intent = getIntent();
        cnp = intent.getStringExtra("cnp");
        setContentView(R.layout.activity_profile_page);

        initializeWidgets();
        getUserData();

        btnBackToHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profilePage = getIntent();
                Intent homePage2 = new Intent(getApplicationContext(), HomePage2.class);
                homePage2.putExtra("cnp", profilePage.getStringExtra("cnp"));
                startActivity(homePage2);
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });


    }

    private void updateProfile() {
        if ( checkDataChanged() ) {
            //update DB
             updateProfileDB();
            //display message
            Toast.makeText(getApplicationContext(), "Data has been updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Data not updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileDB() {
        UserHelper userForUpdate = new UserHelper();
        userForUpdate.setCnp(cnp);
        userForUpdate.setEmail(txtEmail.getText().toString());
        userForUpdate.setPassword(txtPassword.getText().toString());
        userForUpdate.setPhone(txtPhone.getText().toString());
        userForUpdate.setFirstName(txtFullName.getText().toString().split(" ")[0]);
        userForUpdate.setLastName(txtFullName.getText().toString().split(" ")[1]);

        reference.child(cnp).setValue(userForUpdate);
    }

    private void getUserData() {
        reference.orderByChild("cnp").equalTo(cnp).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                userDB = snapshot.getValue(UserHelper.class);

                int gender = Integer.parseInt(cnp.substring(0, 1));
                String birthD = cnp.substring(5, 7) + "." + cnp.substring(3, 5) + ".";
                if (gender == 1 || gender == 2) {
                    birthD = birthD + "19";
                } else {
                    birthD = birthD + "20";
                }
                birthD = birthD + cnp.substring(1, 3);
                txtFull_name.setText(userDB.getFirstName() + " " + userDB.getLastName());
                txtFullName.setText(userDB.getFirstName() + " " + userDB.getLastName());
                txtBday.setText(birthD);
                txtPhone.setText(userDB.getPhone());
                txtEmail.setText(userDB.getEmail());
                txtPassword.setText(userDB.getPassword());
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

    private boolean checkDataChanged() {
      if(validatePassword() && validateEmail() && validatePhone() && validateFullName()) {
         if(phoneChanged() || emailChanged() || passwordChanged() || nameChanged())
          return true;
         else
           return false;
      }else
          return false;
    }

    private boolean validatePhone() {
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

    private boolean validateEmail() {
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

    private boolean validatePassword() {
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

    private boolean validateFullName() {
        String val = txtFullName.getText().toString();

        if (val.isEmpty()) {
            inputLayoutFullName.setError("Field cannot be empty");
            return false;
        } else {
            inputLayoutFullName.setError(null);
            inputLayoutFullName.setErrorEnabled(false);
            return true;
        }
    }

    private boolean phoneChanged(){
        if(txtPhone.getText().toString().equals(userDB.getPhone()))
            return false;
        else
            return true;
    }

    private boolean emailChanged(){
        if(txtEmail.getText().toString().equals(userDB.getEmail()))
            return false;
        else
            return true;
    }

    private boolean passwordChanged(){
        if(txtPassword.getText().toString().equals(userDB.getPassword()))
            return false;
        else
            return true;
    }

    private boolean nameChanged(){
        if(txtFullName.getText().toString().equals(userDB.getFirstName() + " " + userDB.getLastName()))
            return false;
        else
            return true;
    }

}