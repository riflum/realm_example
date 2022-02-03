package com.example.realmdatabaseexample

import org.bson.types.ObjectId
import android.os.Bundle
import android.util.Log
import androidx.core.app.ComponentActivity
import com.example.realmdatabaseexample.model.Task
import com.example.realmdatabaseexample.model.TaskStatus
import com.example.realmdatabaseexample.viewmodel.BackgroundQuickStart
import com.example.realmdatabaseexample.viewmodel.addChangeListenerToRealm
//import com.mongodb.realm.examples.YOUR_APP_ID
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import io.realm.kotlin.where
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

class MainActivity : ComponentActivity() {
    lateinit var uiThreadRealm: Realm
    lateinit var app: App
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this) // context, usually an Activity or Application
        val appID : String = "application-android-bdmpg";
        app = App(AppConfiguration.Builder(appID)
            .build())
        val credentials: Credentials = Credentials.anonymous()
        app.loginAsync(credentials) {
            if (it.isSuccess) {
                Log.v("QUICKSTART", "Successfully authenticated anonymously.")
                val user: User? = app.currentUser()
                val partitionValue: String = "My Project"
                val config = SyncConfiguration.Builder(user, partitionValue)
                    .build()
                uiThreadRealm = Realm.getInstance(config)
                addChangeListenerToRealm(uiThreadRealm)
                val task : FutureTask<String> = FutureTask(BackgroundQuickStart(app.currentUser()!!), "test")
                val executorService: ExecutorService = Executors.newFixedThreadPool(2)
                executorService.execute(task)
            } else {
                Log.e("QUICKSTART", "Failed to log in. Error: ${it.error}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // the ui thread realm uses asynchronous transactions, so we can only safely close the realm
        // when the activity ends and we can safely assume that those transactions have completed
        uiThreadRealm.close()
        app.currentUser()?.logOutAsync() {
            if (it.isSuccess) {
                Log.v("QUICKSTART", "Successfully logged out.")
            } else {
                Log.e("QUICKSTART", "Failed to log out, error: ${it.error}")
            }
        }
    }

}


