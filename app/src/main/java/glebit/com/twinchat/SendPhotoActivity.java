package glebit.com.twinchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.List;


public class SendPhotoActivity extends ActionBarActivity
{
    protected static final String TAG=MainActivity.class.getSimpleName();

    protected Button mSendButton;
    protected Button mCancelButton;
    protected EditText mPhotoDescriptionField;
    protected ImageView mPhotoImageView;
    protected Uri mMediaUri;
    protected ParseUser mUserFriend;
    protected ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_photo);

        mSendButton=(Button)findViewById(R.id.btnSendPhoto);
        mSendButton.setOnClickListener(sendButtonOnClickListener);
        mCancelButton=(Button)findViewById(R.id.btnCancel);
        mCancelButton.setOnClickListener(cancelButtonOnClickListener);
        mPhotoDescriptionField=(EditText)findViewById(R.id.PhotoDescriptionField);
        mPhotoImageView=(ImageView)findViewById(R.id.sendPhotoImg);
        mMediaUri=getIntent().getData();

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(mMediaUri);
        sendBroadcast(mediaScanIntent);
        Picasso.with(this).load(mMediaUri).into(mPhotoImageView);
//        mPhotoImageView.setImageURI(mMediaUri);

        mCurrentUser=ParseUser.getCurrentUser();

        ParseRelation<ParseUser> usrRel= ParseUser.getCurrentUser().getRelation(ParseConstants.KEY_FRIEND_RELATION);

        // жутковатый костыль
        try
        {
            List<ParseUser> friends=usrRel.getQuery().find();
            mUserFriend=friends.get(0);
        }
        catch (ParseException e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    private View.OnClickListener cancelButtonOnClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            finish();
        }
    };

    private View.OnClickListener sendButtonOnClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(mUserFriend!=null)
            {
                String messageText=mPhotoDescriptionField.getText().toString();

                final ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
                message.put(ParseConstants.KEY_SENDER_ID, mCurrentUser.getObjectId());
                message.put(ParseConstants.KEY_SENDER_NAME, mCurrentUser.getUsername());
                message.put(ParseConstants.KEY_RECIPIENT_ID, mUserFriend.getObjectId());
                message.put(ParseConstants.KEY_MESSAGE_TEXT, messageText);

                byte[] fileBytes = FileHelper.getByteArrayFromFile(SendPhotoActivity.this, mMediaUri);

                fileBytes = FileHelper.reduceImageForUpload(fileBytes);

                 String fileName = FileHelper.getFileName(this, mMediaUri, "image");
                 ParseFile file = new ParseFile(fileName, fileBytes);

                 message.put(ParseConstants.KEY_FILE, file);

//                if (fileBytes == null)
//                {
//                    return null;
//                } else
//                {
//                    if (mFileType.equals(ParseConstants.TYPE_IMAGE))
//                    {
//                        fileBytes = FileHelper.reduceImageForUpload(fileBytes);
//                    }
//
//                    String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
//                    ParseFile file = new ParseFile(fileName, fileBytes);
//
//                    message.put(ParseConstants.KEY_FILE, file);
//
//                    //return message;
//                }

                message.saveInBackground(new SaveCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        if(e==null)
                        {
                            finish();
                        }
                    }
                });
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_send_photo, menu);
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
}
