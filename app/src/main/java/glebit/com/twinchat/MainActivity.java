package glebit.com.twinchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity
{

    protected static final String TAG=MainActivity.class.getSimpleName();

    public static final int TAKE_PHOTO_REQUEST=0;
    public static final int PICK_PHOTO_REQUEST=2;

    protected ListView mMessageListView;
    protected Button mSendButton;
    protected EditText mMessageField;
    protected ImageView mSendImageView;
    protected List<ParseObject> mMessages;
    protected ParseUser mCurrentUser;
    protected ParseUser mUserFriend;
    protected ArrayAdapter<ParseObject> mMessageAdapter;
    protected DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected Uri mMediaUri;

    protected DialogInterface.OnClickListener mDialogListener=new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case 0:
                    // TODO: Take picture
                    Intent takePhotoIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri=getOutputMediaFileUri();
                    if(mMediaUri==null)
                    {
                        Toast.makeText(MainActivity.this, R.string.error_external_storage,
                                Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;
                case 1:
                    Intent choosePhotoIntent=new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                    break;
            }
        }
    };

    private Uri getOutputMediaFileUri()
    {
        if(isExternalStorageAvailable())
        {
            String appName=MainActivity.this.getString(R.string.app_name);
            File mediaStorageDir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    appName);

            if(!mediaStorageDir.exists())
            {
                if(!mediaStorageDir.mkdir())
                {
                    Log.e(TAG, "Faild to create directory");
                    return null;
                }
            }

            File mediaFile;
            Date now=new Date();
            String timestamp=new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

            String path=mediaStorageDir.getPath()+File.separator;

            mediaFile=new File(path+"IMG_"+timestamp+".jpg");

            Log.d(TAG, "File:"+Uri.fromFile(mediaFile));

            return Uri.fromFile(mediaFile);
        }
        else
        {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSendImageView=(ImageView)findViewById(R.id.sendImageView);
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerList=(ListView)findViewById(R.id.left_drawer);

        mDrawerToggle =new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawerOpen, R.string.drawerClose)
        {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                new String[]{getString(R.string.menu_friends_label), getString(R.string.menu_logout_label)}));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                switch (position)
                {
                    case 0:
                        navigateToFriends();
                        break;
                    case 1:
                        navigateToLogin();
                        break;
                }
            }
        });

        mCurrentUser=ParseUser.getCurrentUser();

        if(mCurrentUser==null)
        {
            navigateToLogin();
        }

        mMessageListView=(ListView)findViewById(R.id.lstMessages);
        mSendButton=(Button)findViewById(R.id.btnSend);
        mSendButton.setOnClickListener(sendButtonOnClickListener);

        mMessageField=(EditText)findViewById(R.id.messageField);

        ParseRelation<ParseUser> usrRel= ParseUser.getCurrentUser().getRelation(ParseConstants.KEY_FRIEND_RELATION);

        // жутковатый костыль
        try
        {
            List<ParseUser> friends=usrRel.getQuery().find();
            if(friends.size()==0)
            {
                navigateToFriends();
            }
            else
                mUserFriend=friends.get(0);
        }
        catch (ParseException e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    private boolean isExternalStorageAvailable()
    {
        String state= Environment.getExternalStorageState();

        if(state.equals(Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        ParseQuery<ParseObject> recipeQuery=new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        recipeQuery.whereEqualTo(ParseConstants.KEY_RECIPIENT_ID, mCurrentUser.getObjectId());

        ParseQuery<ParseObject> senderQuery=new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        senderQuery.whereEqualTo(ParseConstants.KEY_SENDER_ID, mCurrentUser.getObjectId());

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(recipeQuery);
        queries.add(senderQuery);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.addAscendingOrder(ParseConstants.KEY_CREATED_AT);

        mainQuery.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> messages, ParseException e)
            {
                if(e==null)
                {
                    mMessages = messages;

                    mMessageAdapter=new MessageAdapter(MainActivity.this, mMessages);
                    mMessageListView.setAdapter(mMessageAdapter);
                }
                else
                {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if(mUserFriend!=null)
        {
            ParseFile userAvatar=mUserFriend.getParseFile(ParseConstants.KEY_USER_AVATAR);

            userAvatar.getDataInBackground(new GetDataCallback()
            {
                @Override
                public void done(byte[] bytes, ParseException e)
                {
                    Drawable dUsrAvatar = new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));

                    menu.getItem(1).setIcon(dUsrAvatar);
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_user:
                // TODO: Go to user activity
                break;
            case R.id.action_attach_Photo:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choises, mDialogListener);
                AlertDialog dialog=builder.create();
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener sendButtonOnClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(mUserFriend!=null)
            {
                String messageText=mMessageField.getText().toString();

                final ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
                message.put(ParseConstants.KEY_SENDER_ID, mCurrentUser.getObjectId());
                message.put(ParseConstants.KEY_SENDER_NAME, mCurrentUser.getUsername());
                message.put(ParseConstants.KEY_RECIPIENT_ID, mUserFriend.getObjectId());
                message.put(ParseConstants.KEY_MESSAGE_TEXT, messageText);

                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e==null) {
                            Toast.makeText(MainActivity.this, "Message sent", Toast.LENGTH_LONG);
                            mMessages.add(message);
                            mMessageAdapter.notifyDataSetChanged();
                            mMessageField.setText("");
                        }
                    }
                });
            }
        }
    };

    private ParseObject getMessage(String messageText)
    {
        ParseObject message=new ParseObject(ParseConstants.CLASS_MESSAGES);

        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_USERNAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_ID, mUserFriend);
        message.put(ParseConstants.KEY_MESSAGE_TEXT, messageText);

        return message;
    }

    private void navigateToLogin()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK)
        {
            if(requestCode==PICK_PHOTO_REQUEST)
            {
                if(data==null)
                {
                    Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
                }
                else
                {
                    mMediaUri=data.getData();
                    Intent sendPhotoIntent=new Intent(this, SendPhotoActivity.class);
                    sendPhotoIntent.setData(mMediaUri);
                    startActivity(sendPhotoIntent);
//                    mMediaUri=data.getData();
//                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                    mediaScanIntent.setData(mMediaUri);
//                    sendBroadcast(mediaScanIntent);
//                    mSendImageView.setImageURI(mMediaUri);
                }

            }

        }
    }

    private void navigateToFriends()
    {
        Intent intent = new Intent(this, FriendsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
