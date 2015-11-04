package glebit.com.twinchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Gleb on 03.09.2015.
 */
public class MessageAdapter extends ArrayAdapter
{
    protected Context mContext;
    protected List<ParseObject> mMessages;

    public MessageAdapter(Context context, List<ParseObject> messages)
    {
        super(context, R.layout.message_item, messages);
        mContext=context;
        mMessages=messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if(convertView==null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ViewHolder();
            holder.messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
            holder.userNameLabel=(TextView)convertView.findViewById(R.id.userNameLabel);
            holder.contentLayout=(LinearLayout)convertView.findViewById(R.id.contentLayout);
            convertView.setTag(holder);
        }
        else
        {
            holder=(ViewHolder)convertView.getTag();
        }

        ParseObject message=mMessages.get(position);

        holder.userNameLabel.setText(message.getString(ParseConstants.KEY_SENDER_NAME));
        String msgText=message.getString(ParseConstants.KEY_MESSAGE_TEXT);
        holder.messageTextView.setText(msgText);

        String msgUsrId=message.getString(ParseConstants.KEY_SENDER_ID);

        String msgCurUsrId=ParseUser.getCurrentUser().getObjectId();
        if(msgUsrId.equals(msgCurUsrId))
        {
            holder.contentLayout.setBackgroundResource(R.drawable.out_message_bg);

            RelativeLayout.LayoutParams contentLayoutParams=new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            contentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            contentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

            holder.contentLayout.setLayoutParams(contentLayoutParams);

            RelativeLayout.LayoutParams avatarLayoutParams=new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            avatarLayoutParams.addRule(RelativeLayout.LEFT_OF, R.id.contentLayout );
        }
        else
        {
            holder.contentLayout.setBackgroundResource(R.drawable.in_message_bg);

            RelativeLayout.LayoutParams contentLayoutParams=new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            contentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            contentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            holder.contentLayout.setLayoutParams(contentLayoutParams);

            RelativeLayout.LayoutParams avatarLayoutParams=new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            avatarLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.contentLayout );
        }


        return convertView;
    }

    public class ViewHolder
    {
        TextView messageTextView;
        TextView userNameLabel;
        LinearLayout contentLayout;
        ImageView avatarImageView;
    }

    public void refill(List<ParseObject> messages)
    {
        mMessages.clear();
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }
}
