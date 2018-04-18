package group8.tcss450.uw.edu.chatclient;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Launcher activity.
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class SignInActivity extends AppCompatActivity implements
        LoginFragment.OnFragmentInteractionListener, RegistrationFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //if(saved) autoSignIn()

        if (findViewById(R.id.fragmentContainer) != null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new LoginFragment())
                    .commit();
        }
    }

    /** Called when login info is saved */
    public void autoSignIn(View view) {
//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);

    }

    @Override
    public void onLoginFragmentInteraction(String name, char[] password) {
        TextView userView = findViewById(R.id.usernameText);
        TextView passView = findViewById(R.id.passwordText);
        boolean good = true;

        if(name == null || password == null){
            userView.setError("All fields must be filled");
            good = false;
        }else{
            if(name.length() < 4){
                userView.setError("Username must be more than 3 chars in length");
                good = false;
            }
            if(password.length < 6){
                passView.setError("Password must be more than 5 chars in length");
                good = false;
            }
        }

        if(good){
            loadDisplayFragment(name, password);
        }

    }

    @Override
    public void onRegisterFragmentInteraction() {
        RegistrationFragment registerFragment = new RegistrationFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, registerFragment)
                .addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onFragmentInteraction(String name, char[] password, char[] passcopy) {
        TextView userView = findViewById(R.id.regUsernameText);
        TextView passView = findViewById(R.id.regPassText);
        boolean good = true;

        if(name == null || password == null || passcopy == null){
            userView.setError("All fields must be filled");
            good = false;
        }else{
            if(name.length() < 3){
                userView.setError("Username must be more than 3 chars in length");
                good = false;
            }
            if(password.length < 6 || passcopy.length < 6){
                passView.setError("Password must be more than 5 chars in length");
                good = false;
            }
            if(password.length == passcopy.length){
                for(int i = 0; i < password.length; i++){
                    if(password[i] != passcopy[i]){
                        passView.setError("Passwords must match");
                        good = false;
                        break;
                    }
                }
            }
        }

        if(good){
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            loadDisplayFragment(name, password);
        }
    }

    public void loadDisplayFragment(String name, char[] password) {

    }
}