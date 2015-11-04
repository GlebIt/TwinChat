package glebit.com.twinchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class FriendsActivity extends Activity
{
    private static final String TAG=FriendsActivity.class.getSimpleName();

    private List<ParseUser> mUsers;
    protected ListView mUserList;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mUserList=(ListView)findViewById(R.id.friendsList);
        mUserList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mUserList.setOnItemClickListener(onItemClickListener);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mCurrentUser=ParseUser.getCurrentUser();
        mFriendsRelation=mCurrentUser.getRelation(ParseConstants.KEY_FRIEND_RELATION);

        final ParseQuery<ParseUser> query=ParseUser.getQuery();
        query.orderByDescending(ParseConstants.KEY_USERNAME);
        query.setLimit(1000);

        query.findInBackground(new FindCallback<ParseUser>()
        {
            @Override
            public void done(List<ParseUser> users, ParseException e)
            {
                if(e==null)
                {
                    mUsers=users;
                    mUsers.remove(mCurrentUser);
                    String[] userNames=new String[mUsers.size()];

                    int i=0;

                    for(ParseUser user:mUsers)
                    {
                        if(user!=mCurrentUser)
                        {
                            userNames[i] = user.getUsername();
                            i++;
                        }
                    }

                    ArrayAdapter<String> adapter=new ArrayAdapter<String>(FriendsActivity.this,
                                         android.R.layout.simple_list_item_checked, userNames);

                    mUserList.setAdapter(adapter);

                    addFriendsCheckmarks();
                }
            }
        });

    }

    protected AdapterView.OnItemClickListener onItemClickListener =new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            int friendsCount=mUserList.getCheckedItemCount();

            if(mUserList.isItemChecked(position))
            {
                if(friendsCount>=1)
                {
                    mUserList.setItemChecked(position, false);
                    AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                    builder.setMessage(getString(R.string.message_friends_count_overflow))
                            .setTitle(getString(R.string.alert_dialog_title))
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog=builder.create();
                    dialog.show();
                }
                mFriendsRelation.add(mUsers.get(position));
            }
            else
            {
                mFriendsRelation.remove(mUsers.get(position));
            }

            mCurrentUser.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if(e!=null)
                    {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
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
        switch (id)
        {
            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;
            case R.id.action_main:
                Intent intent=new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addFriendsCheckmarks()
    {
        ParseRelation<ParseUser> relation=mCurrentUser.getRelation(ParseConstants.KEY_FRIEND_RELATION);
        relation.getQuery().findInBackground(new FindCallback<ParseUser>()
        {
            @Override
            public void done(List<ParseUser> friends, ParseException e)
            {
                if (e == null)
                {
                    for (int i = 0; i < mUsers.size(); i++)
                    {
                        ParseUser user = mUsers.get(i);

                        for (ParseUser friend : friends)
                        {
                            if(friend!=mCurrentUser)
                            {
                                if (friend.getObjectId().equals(user.getObjectId()))
                                {
                                    mUserList.setItemChecked(i, true);
                                }
                            }
                        }
                    }
                } else
                {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void navigateToLogin()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
