package pro.nandor.appthatchecklanguages

import android.app.Application
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class MainApplication:Application() {
    companion object{
        lateinit var realm: Realm
    }

    override fun onCreate() {
        super.onCreate()
        realm = Realm.open(
            RealmConfiguration.create(
                schema = setOf(Lexeme::class)
            )
        )
    }
}