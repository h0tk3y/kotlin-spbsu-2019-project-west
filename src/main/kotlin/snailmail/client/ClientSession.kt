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
        return ClientAPI.searchByUsername(username)
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
                    val user = findUser(args[1])
                    if (user == null) {
                        println("This user doesn't exist")
                        isSuccess = false
                    } else {
                        ClientAPI.sendTextMessage(message,
                                ClientAPI.getPersonalChatWith(user))
                    }
                } else isSuccess = false
            }
            "/viewAvailableChats" -> {
                val availableChats = ClientAPI.getAvailableChats().getChats()
                for (chat in availableChats) {
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
                        println("This user doesn't exist")
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
                        println("This user doesn't exist")
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


