package pro.nandor.appthatchecklanguages


import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Lexeme: RealmObject{
    @PrimaryKey var id: ObjectId = ObjectId()
    var language: String = ""
    var foreignWord: String = ""
    var englishWord: String = ""
    var foreignContext: String = ""
    var exportTimeStamp: Int = 0
}
