package snailmail.client

import snailmail.core.GroupChat
import snailmail.core.PersonalChat
import snailmail.core.TextMessage
import snailmail.core.UserCredentials
import snailmail.core.api.API

class ConsoleClient(api: API) {
    private val client = Client(api)

    private val commandsDescription = mapOf<Pair<String, List<String>>, String>(
            Pair(":help", listOf(""))
                    to "display this list of commands",
            Pair(":exit", listOf(""))
                    to "exit SnailMail",
            Pair(":send", listOf("<username>", "<message>"))
                    to "send the message to the user with <username>",
            Pair(":sendToGroupChat", listOf("<chatTitle>", "<message>"))
                    to "send the message to the <chatTitle> group chat",
            Pair(":viewAvailableChats", listOf(""))
                    to "show all chats you are member of",
            Pair(":findUsername", listOf("<username>"))
                    to "find <username> and show his info",
            Pair(":getGroupChat", listOf("<chatTitle>"))
                    to "find and show history of <chatTitle> group chat",
            Pair(":getPersonalChatWith", listOf("<username>"))
                    to "find and show history of chat with <username>",
            Pair(":createGroupChat", listOf("<chatTitle> [<username>]"))
                    to "create group chat with title <chatTitle> and invite all users from [<username>]"
    )

    private fun getUserCredentials(): UserCredentials {
        print("Username: ")
        val username = readLine()!!
        print("Password: ")
        val password = readLine()!!
        return UserCredentials(username, password)
    }

    fun startSession() {
        println("Welcome to SnailMail!")
        while (true) {
            print("Do you have an account? (y/n) ")
            val answer = readLine()!!
            if (answer == "y") {
                var userCredentials = getUserCredentials()
                while (true) {
                    val authenticationResult = client.authenticate(userCredentials)
                    if (authenticationResult) {
                        break
                    } else {
                        println("Username or/and password are incorrect")
                        userCredentials = getUserCredentials()
                    }
                }
                println("Successful authentication! You can write commands!")
                break
            } else if (answer == "n") {
                var userCredentials = getUserCredentials()
                while (true) {
                    val authenticationResult = client.register(userCredentials)
                    if (authenticationResult) {
                        break
                    } else {
                        println("Registration failed, try to change username")
                        userCredentials = getUserCredentials()
                    }
                }
                println("Successful registration! You can write commands!")
                break
            } else {
                print("Incorrect format of the answer, answer again: ")
            }
        }
    }

    private fun executeCommand(cmd: String): Boolean {
        val args = cmd.split(' ')
        var isSuccess = false
        when (args[0]) {
            ":send" -> {
                if (args.size >= 3) {
                    val message = args.filterIndexed { index, _ -> index >= 2 }
                            .joinToString(separator = " ")
                    try {
                        val username = args[1]
                        client.sendMessage(username, message)
                        isSuccess = true
                    } catch (e: MessengerException) {
                        println(e.message)
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            }
            ":sendToGroupChat" -> {
                if (args.size >= 3) {
                    val message = args.filterIndexed { index, _ -> index >= 2 }
                            .joinToString(separator = " ")
                    val chatTitle = args[1]
                    try {
                        client.sendMessageToGroupChat(chatTitle, message)
                        isSuccess = true
                    } catch (e: MessengerException) {
                        println(e.message)
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            }
            ":viewAvailableChats" -> {
                try {
                    val availableChats = client.findAvailableChats()
                    for (chat in availableChats) {
                        if (chat is PersonalChat) {
                            val person1 = client.findUserById(chat.person1)
                            val person2 = client.findUserById(chat.person2)
                            if (client.self().id == person1.id) {
                                println(person2.username)
                            } else {
                                println(person1.username)
                            }
                        } else if (chat is GroupChat) {
                            println("${chat.title} (group)")
                        }
                    }
                    isSuccess = true
                } catch (e: MessengerException) {
                    println(e.message)
                } catch (e: Exception) {
                    println(e.message)
                }
            }
            ":getGroupChat" -> {
                if (args.size == 2) {
                    val chatTitle = args[1]
                    try {
                        val chatHistory = client.getGroupChatHistory(chatTitle)
                        for (message in chatHistory) {
                            when (message) {
                                is TextMessage -> println(formatTextMessage(message))
                                else -> println(message)
                            }
                        }
                        isSuccess = true
                    } catch (e: MessengerException) {
                        println(e.message)
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            }
            ":createGroupChat" -> {
                if (args.size >= 3) {
                    val chatTitle = args[1]
                    val members = args.filterIndexed { index, _ -> index >= 2 }
                    try {
                        client.createGroupChat(chatTitle, members)
                        isSuccess = true
                    } catch (e: MessengerException) {
                        println(e.message)
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            }
            ":findUsername" -> {
                if (args.size == 2) {
                    val username = args[1]
                    try {
                        val user = client.findUser(username)
                        println(user.displayName)
                        isSuccess = true
                    } catch (e: MessengerException) {
                        println(e.message)
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            }
            ":getPersonalChatWith" -> {
                if (args.size == 2) {
                    val username = args[1]
                    try {
                        val history = client.getPersonalChatHistory(username)
                        for (message in history) {
                            when (message) {
                                is TextMessage -> println(formatTextMessage(message))
                                else -> println(message)
                            }
                        }
                        isSuccess = true
                    } catch (e: MessengerException) {
                        println(e.message)
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            }
            ":help" -> {
                commandsDescription.forEach {
                    println("${it.key.first.plus(" ").plus(it.key.second.joinToString(separator = " ")).padEnd(50)} - ${it.value}")
                }
                isSuccess = true
            }
            ":exit" -> {
                return false
            }
            else -> isSuccess = false
        }
        if (!isSuccess)
            println("Incorrect command, try again...")
        return true
    }

    fun writeCommand(): Boolean {
        print("> ")
        val cmd = readLine()
        if (cmd == null || cmd.toLowerCase() == "quit")
            return false
        return executeCommand(cmd)
    }

    fun endSession() {
    }

    private fun formatTextMessage(message: TextMessage): String {
        return StringBuilder()
                .append("${message.date}| ")
                .apply {
                    if (message.sender != null) {
                        try {
                            val sender = client.findUserById(message.sender)
                            this.append("${sender.username}: ")
                        } catch (e: Exception) {
                            this.append("<deleted>: ")
                        }
                    }
                }
                .append(message.content).toString()
    }
}