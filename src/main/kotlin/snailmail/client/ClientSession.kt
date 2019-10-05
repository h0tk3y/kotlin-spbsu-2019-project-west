package snailmail.client

import snailmail.core.User
import snailmail.core.UserCredentials
import snailmail.core.api.API

class ClientSession(val ClientAPI : API) {
    private fun getUserCredentials() : UserCredentials {
        print("Username: ")
        val username = readLine()!!
        print("Password: ")
        val password = readLine()!!
        return UserCredentials(username, password)
    }

    private fun findUser(username : String) : User? {
        val user = ClientAPI.searchByUsername(username)
        if (user == null) {
            println("This user doesn't exist")
        }
        return user
    }

    fun startSession(){
        while(true) {
            print("Welcome to SnailMail! Do you have an account? (y/n)")
            val answer = readLine()!!
            var userCredentials = getUserCredentials();
            if (answer == "y") {
                var isSuccess = false
                do {
                    userCredentials = getUserCredentials()
                    isSuccess = ClientAPI.authenticate(userCredentials).successful
                } while (!isSuccess)
                break;
            } else if (answer == "n") {
                ClientAPI.register(userCredentials).successful
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
                if (args.size == 3) {
                    val user = findUser(args[1])
                    if (user == null) {
                        isSuccess = false
                    } else {
                        ClientAPI.sendTextMessage(args[2],
                                ClientAPI.getPersonalChatWith(user))
                    }
                } else isSuccess = false
            }
            "/viewAwailableChats" -> {
                val awailableChats = ClientAPI.getAvailableChats().getChats()
                for (chat in awailableChats) {
                    println(chat)
                }
            }
            "/getChatMessages" -> {
                //TO-DO
            }
            "/findUsername" -> {
                if (args.size == 2) {
                    val user = findUser(args[1])
                    if (user == null) {
                        isSuccess = false
                    } else {
                        println(user)
                    }
                } else isSuccess = false
            }
            "/getPersonalChat" -> {
                if (args.size == 2) {
                    val user = findUser(args[1])
                    if (user == null) {
                        isSuccess = false
                    } else {
                        val chat = ClientAPI.getPersonalChatWith(user)
                        val history = ClientAPI.getChatMessages(chat).getMessages()
                        for (message in history) {
                            println(message)
                        }
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


