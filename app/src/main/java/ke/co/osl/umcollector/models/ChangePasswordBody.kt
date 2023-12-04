package ke.co.osl.umcollector.models

data class ChangePasswordBody(
    val Password: String,
    val NewPassword: String
)