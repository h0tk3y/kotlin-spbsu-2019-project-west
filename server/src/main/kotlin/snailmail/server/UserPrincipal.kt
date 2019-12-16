package snailmail.server

import io.ktor.auth.Principal
import java.util.*

data class UserPrincipal(val token: String, val id: UUID) : Principal