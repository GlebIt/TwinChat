package glebit.com.twinchat;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Gleb on 31.08.2015.
 */
public class TwinChatApplication extends Application
{
    @Override
    public void onCreate()
    {
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "Jz9cCctK3KCxPw2gOwnDM6PfMAVFqvGfuznaFx6W", "bSuZYwxkz5eeTfCR7QiHTqsKSBNiDsLrc1bBuBOb");

        super.onCreate();
    }
}
