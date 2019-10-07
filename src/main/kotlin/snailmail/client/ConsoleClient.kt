package snailmail.client

import snailmail.core.UserCredentials
import snailmail.server.Server

class ConsoleClient(server: Server) {
    private val client = Client(server)

    private fun getUserCredentials() : UserCredentials {
        print("Username: ")
        val username = readLine()!!
        print("Password: ")
        val password = readLine()!!
        return UserCredentials(username, password)
    }

    fun startSession(){
        println("Welcome to SnailMail!")
        while(true) {
            print("Do you have an account? (y/n)")
            val answer = readLine()!!
            var userCredentials = getUserCredentials();
            if (answer == "y") {
                while (true) {
                    val authenticationResult = client.authenticate(userCredentials)
                    if (authenticationResult) {
                        break
                    } else {
                        println("Username or/and password are incorrect")
                        userCredentials = getUserCredentials()
                    }
                }
                break
            } else if (answer == "n") {
                while(true) {
                    val authenticationResult = client.register(userCredentials)
                    if (authenticationResult) {
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
        var isSuccess = false;
        when(args[0]) {
            "/send" -> {
                if (args.size >= 3) {
                    val message = args.filterIndexed { index, _ -> index >= 2 }
                            .joinToString(separator = " ")
                    try {
                        client.sendMessage(args[1], message)
                        isSuccess = true
                    } catch (e: MessengerException) {
                        println(e.message)
                    }
                }
            }
            "/viewAvailableChats" -> {
                try {
                    val availableChats = client.findAvailableChats()
                    for (chat in availableChats) {
                        println(chat)
                    }
                    isSuccess = true
                } catch (e: MessengerException) {
                    print(e.message)
                }
            }
            "/getChatMessages" -> {
                isSuccess = true
                //TO-DO
            }
            "/findUsername" -> {
                if (args.size == 2) {
                    try {
                        val user = client.findUser(args[1])
                        println(user)
                        isSuccess = true
                    } catch (e: MessengerException) {
                        println(e.message)
                    }
                }
            }
            "/getPersonalChat" -> {
                if (args.size == 2) {
                    try {
                        val history = client.getPersonalChatHistory(args[1])
                        for (message in history) {
                            println(message)
                        }
                        isSuccess = true
                    } catch (e: UserNotFoundException) {
                        println(e.message)
                    }
                }
            }
            else -> isSuccess = false
        }
        if (!isSuccess)
            println("Incorrect command, try again...")
    }

    fun endSession() {
    }

}