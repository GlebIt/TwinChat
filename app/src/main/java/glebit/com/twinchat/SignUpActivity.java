package glebit.com.twinchat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class SignUpActivity extends ActionBarActivity
{
    protected EditText mUserNameField;
    protected EditText mPasswordField;
    protected EditText mEmailField;
    protected Button mSignUpButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mUserNameField=(EditText)findViewById(R.id.usernameField);
        mPasswordField=(EditText)findViewById(R.id.passwordField);
        mEmailField=(EditText)findViewById(R.id.emailField);
        mSignUpButton=(Button)findViewById(R.id.signUpButton);

        mSignUpButton.setOnClickListener(signUpOnClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener signUpOnClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String userName=mUserNameField.getText().toString();
            String password=mPasswordField.getText().toString();
            String eMail=mEmailField.getText().toString();

            userName=userName.trim();
            password=password.trim();
            eMail=eMail.trim();

            if(userName.isEmpty()||password.isEmpty()||eMail.isEmpty())
            {
                AlertDialog.Builder builder=new AlertDialog.Builder(SignUpActivity.this);
                builder.setMessage(getString(R.string.signup_error_message))
                        .setTitle(getString(R.string.signup_error_title))
                        .setPositiveButton(android.R.string.ok, null);

                AlertDialog dialog=builder.create();
                dialog.show();
            }
            else
            {
                setProgressBarIndeterminateVisibility(true);

                ParseUser newUser = new ParseUser();
                newUser.setEmail(eMail);
                newUser.setUsername(userName);
                newUser.setPassword(password);

                newUser.signUpInBackground(new SignUpCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        setProgressBarIndeterminateVisibility(false);

                        if(e==null)
                        {
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else
                        {
                            AlertDialog.Builder builder=new AlertDialog.Builder(SignUpActivity.this);
                            builder.setMessage(e.getMessage())
                                    .setTitle(getString(R.string.signup_error_title))
                                    .setPositiveButton(android.R.string.ok, null);

                            AlertDialog dialog=builder.create();
                            dialog.show();
                        }
                    }
                });
            }
        }
    };
}
