package glebit.com.twinchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class SendPhotoActivity extends ActionBarActivity
{
    protected Button mSendButton;
    protected Button mCancelButton;
    protected EditText mPhotoDescriptionField;
    protected ImageView mPhotoImageView;
    protected Uri mMediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_photo);

        mSendButton=(Button)findViewById(R.id.btnSend);
        mCancelButton=(Button)findViewById(R.id.btnCancel);
        mCancelButton.setOnClickListener(cancelButtonOnClickListener);
        mPhotoDescriptionField=(EditText)findViewById(R.id.PhotoDescriptionField);
        mPhotoImageView=(ImageView)findViewById(R.id.sendPhotoImg);
        mMediaUri=getIntent().getData();

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(mMediaUri);
        sendBroadcast(mediaScanIntent);
        mPhotoImageView.setImageURI(mMediaUri);
    }

    private View.OnClickListener cancelButtonOnClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            finish();
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
