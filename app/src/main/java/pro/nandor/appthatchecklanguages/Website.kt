package pro.nandor.appthatchecklanguages

data class Website(
    val displayName: String,
    val language: String,
    val urlWithOptionalPlaceholder:String,
    val shouldRefreshOnWordChange: Boolean,
    val baseURL: String,
)
