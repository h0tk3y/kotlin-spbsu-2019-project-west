package core.models

import core.models.AppUser

class Message(val author: AppUser, val text: String) {
    fun send() {

    }
}