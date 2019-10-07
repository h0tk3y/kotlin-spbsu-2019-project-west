package snailmail.client

import snailmail.core.*
import snailmail.core.api.API
import snailmail.core.api.*

class ClientSession(val ClientAPI : API) {
    private var token : AuthToken? = null

    private fun getUserCredentials() : UserCredentials {
        print("Username: ")
        val username = readLine()!!
        print("Password: ")
        val password = readLine()!!
        return UserCredentials(username, password)
    }
    
    fun findUser(username : String) : User {
        val t = token
        if (t == null)
            throw NullTokenException("Token doesn't exist")
        else
            return ClientAPI.searchByUsername(t, username) ?: throw UserNotFoundException("This user doesn't exist")
    }
    fun sendMessage(username: String, message: String) : TextMessage {
        val user = findUser(username)
        val t = token
        if (t == null)
            throw NullTokenException("Token doesn't exist")
        else return ClientAPI.sendTextMessage(t, message,
                ClientAPI.getPersonalChatWith(t, user.id).id)
    }

    fun findAvailableChats() : List<Chat> {
        return ClientAPI.getAvailableChats().getChats()
    }

    fun findPersonalChat(username: String) : Chat {
        val user = findUser(username)
        val t = token
        if (t == null)
            throw NullTokenException("Token doesn't exist")
        else
            return ClientAPI.getPersonalChatWith(t, user.id)
    }

    fun getPersonalChatHistory(username: String) : List<Message> {
        val chat = findPersonalChat(username)
        val t = token
        if (t == null)
            throw NullTokenException("Token doesn't exist")
        else
            return ClientAPI.getChatMessages(t, chat.id).getMessages()
    }

    fun startSession(){
        println("Welcome to SnailMail!")
        while(true) {
            print("Do you have an account? (y/n)")
            val answer = readLine()!!
            var userCredentials = getUserCredentials();
            if (answer == "y") {
                while (true) {
                    val authenticationResult = ClientAPI.authenticate(userCredentials)
                    if (authenticationResult is AuthSuccessful) {
                        token = authenticationResult.token
                        break
                    } else {
                        println("Username or/and password are incorrect")
                        userCredentials = getUserCredentials()
                    }
                }
                break
            } else if (answer == "n") {
                while(true) {
                    val authenticationResult = ClientAPI.register(userCredentials)
                    if (authenticationResult is AuthSuccessful) {
                        token = authenticationResult.token
                        break
                    } else {
                        println("Registration failed, try to change username")
                        userCredentials = getUserCredentials()
                    }
                }
                break
            } else {
                print("Incorrect format of the answer, answer again: ")
            }
        }
    }

    fun doCommand(cmd : String) {
        val args = cmd.split(' ')
        var isSuccess = true;
        when(args[0]) {
            "/send" -> {
                if (args.size >= 3) {
                    val message = args.filterIndexed { index, _ -> index >= 2 }
                            .joinToString(separator = " ")
                    try {
                        sendMessage(args[1], message)
                    }
                    catch (e : UserNotFoundException) {
                        println(e.message)
                        isSuccess = false
                    }
                } else isSuccess = false
            }
            "/viewAvailableChats" -> {
                val availableChats = findAvailableChats()
                for (chat in availableChats) {
                    println(chat)
                }
            }
            "/getChatMessages" -> {
                //TO-DO
            }
            "/findUsername" -> {
                if (args.size == 2) {
                    try {
                        val user = findUser(args[1])
                        println(user)
                    }
                    catch (e : UserNotFoundException) {
                        println(e.message)
                        isSuccess = false
                    }
                } else isSuccess = false
            }
            "/getPersonalChat" -> {
                if (args.size == 2) {
                    try {
                        val history = getPersonalChatHistory(args[1])
                        for (message in history) {
                            println(message)
                        }
                    }
                    catch (e : UserNotFoundException) {
                        println(e.message)
                        isSuccess = false
                    }
                } else isSuccess = false
            }
            else -> isSuccess = false
        }
        if (!isSuccess)
            println("Incorrect command, try again...")
    }

    fun endSession() {
    }
}


