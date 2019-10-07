package snailmail.client

import snailmail.core.*
import snailmail.core.api.API

class ClientSession(val ClientAPI : API) {
    private fun getUserCredentials() : UserCredentials {
        print("Username: ")
        val username = readLine()!!
        print("Password: ")
        val password = readLine()!!
        return UserCredentials(username, password)
    }

    fun findUser(username : String) : User =
        ClientAPI.searchByUsername(username) ?: throw UserNotFoundException("This user doesn't exist")

    fun sendMessage(username: String, message: String) : TextMessage {
        val user = findUser(username)
        return ClientAPI.sendTextMessage(message,
                ClientAPI.getPersonalChatWith(user))
    }

    fun findAvailableChats() : List<Chat> {
        return ClientAPI.getAvailableChats().getChats()
    }

    fun findPersonalChat(username: String) : Chat {
        val user = findUser(username)
        return ClientAPI.getPersonalChatWith(user)
    }

    fun getPersonalChatHistory(username: String) : List<Message> {
        val chat = findPersonalChat(username)
        return ClientAPI.getChatMessages(chat).getMessages()
    }

    fun startSession(){
        while(true) {
            print("Welcome to SnailMail! Do you have an account? (y/n)")
            var isSuccess = false
            val answer = readLine()!!
            var userCredentials = getUserCredentials();
            if (answer == "y") {
                do {
                    isSuccess = ClientAPI.authenticate(userCredentials).successful
                    if (!isSuccess) {
                        println("Username or/and password are incorrect")
                        userCredentials = getUserCredentials()
                    }
                } while (!isSuccess)
                break;
            } else if (answer == "n") {
                do {
                    isSuccess = ClientAPI.register(userCredentials).successful
                    if (!isSuccess) {
                        println("Registration failed, try to change username")
                        userCredentials = getUserCredentials()
                    }
                } while (!isSuccess)
                break;
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


