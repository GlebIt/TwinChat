package glebit.com.twinchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;


public class LoginActivity extends Activity
{
    protected EditText mUsernameField;
    protected EditText mPasswordField;
    protected Button mLoginButton;
    protected TextView mSignUpText;
    protected int mFriendsCount;

    protected final static String TAG=LoginActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsernameField=(EditText)findViewById(R.id.usernameField);
        mPasswordField=(EditText)findViewById(R.id.passwordField);
        mLoginButton =(Button)findViewById(R.id.loginButton);
        mSignUpText=(TextView)findViewById(R.id.signUpText);

        mLoginButton.setOnClickListener(loginInOnclickListener);

        mSignUpText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent=new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
        switch(id)
        {
            case R.id.action_main:
                Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private View.OnClickListener loginInOnclickListener =new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String userName=mUsernameField.getText().toString();
            String password=mPasswordField.getText().toString();

            userName=userName.trim();
            password=password.trim();

            if(userName.isEmpty()||password.isEmpty())
            {
                showLoginError(getString(R.string.login_error_message));
            }
            else
            {
                setProgressBarIndeterminateVisibility(true);

                ParseUser.logInInBackground(userName, password, new LogInCallback()
                {
                    @Override
                    public void done(ParseUser user, ParseException e)
                    {
                        setProgressBarIndeterminateVisibility(false);

                        if(e==null)
                        {
                            ParseRelation<ParseUser> usrRel=user.getRelation(ParseConstants.KEY_FRIEND_RELATION);

                            try
                            {
                                List<ParseUser> friends=usrRel.getQuery().find();
                                mFriendsCount=friends.size();
                            }
                            catch (ParseException e1)
                            {
                                Log.e(TAG, e.getMessage());
                            }

                            Class<?> cl;
                            if (mFriendsCount>0)
                                cl=MainActivity.class;
                            else
                                cl=FriendsActivity.class;

                            Intent intent=new Intent(LoginActivity.this, cl);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);
                        }
                        else
                        {
                            showLoginError(e.getMessage());
                        }
                    }
                });
            }
        }

        private void showLoginError(String message)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage(message)
                    .setTitle(getString(R.string.login_error_title))
                    .setPositiveButton(android.R.string.ok, null);

            AlertDialog dialog=builder.create();
            dialog.show();
        }
    };
}
