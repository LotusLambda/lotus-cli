object Session {

    init {

    }
    var currentSession : SessionStatus = SessionStatus.None


    fun loginUser( token: String,  email: String){
        currentSession = SessionStatus.LoggedIn(token,email)
    }

}

sealed class SessionStatus {
    object None : SessionStatus()
    data class LoggedIn(val token: String, val email: String) : SessionStatus()
}