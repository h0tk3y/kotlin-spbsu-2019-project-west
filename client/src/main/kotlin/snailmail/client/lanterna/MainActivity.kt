package snailmail.client.lanterna

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder
import snailmail.core.*
import java.util.*
import java.util.regex.Pattern

class MainActivity(parent: LanternaClient) : Activity(parent) {
    private val gui = parent.gui
    private val client = parent.client

    private val window = BasicWindow()

    private var currentChat: Chat? = null

    private val container = object : Panel() {
        val statusLine = object : Panel() {
            val labelUsername = Label("<username>")
        }.apply {
            layoutManager = LinearLayout(Direction.HORIZONTAL)
            addComponent(labelUsername)
        }
        val chatsPanel = object : Panel() {
        }.apply {
            layoutManager = LinearLayout(Direction.VERTICAL)
            // preferredSize = TerminalSize((parent.terminal.terminalSize.columns * (1.0 / 5.0)).toInt(), parent.terminal.terminalSize.rows - 10)
        }
        val currentChatPanel = object : Panel() {
            val chatStatusLine = object : Panel() {
                val chatTitleLabel = Label("<chat title>")
            }.apply {
                layoutManager = LinearLayout(Direction.HORIZONTAL)
                addComponent(chatTitleLabel.withBorder(Borders.singleLine()))
            }
            val chatView = object : Panel() {}.apply {
                layoutManager = LinearLayout(Direction.VERTICAL)
            }
            val chatControls = object : Panel() {
                val chatMessageTextBox = TextBox(TerminalSize(50, 5), "Type message here...")
                val buttonSendMessage = Button("Send").apply {
                    addListener {
                        val chat = currentChat ?: return@addListener
                        val text = chatMessageTextBox.text
                        when (chat) {
                            is PersonalChat -> client.sendMessage(client.findUserById(chat.notMe(client.self().id)).username, text)
                            is GroupChat -> client.sendMessageToGroupChat(chat.title, text)
                        }
                        chatMessageTextBox.text = ""

                        updateChatList()
                        showChat(chat)
                    }
                }
            }.apply {
                layoutManager = LinearLayout(Direction.HORIZONTAL)
                addComponent(chatMessageTextBox)
                addComponent(buttonSendMessage)

                // preferredSize = TerminalSize((parent.terminal.terminalSize.columns * (3.5 / 5.0)).toInt(), parent.terminal.terminalSize.rows - 10)
            }
        }.apply {
            layoutManager = LinearLayout(Direction.VERTICAL)
            addComponent(chatStatusLine)
            addComponent(chatView)
            addComponent(chatControls)
        }
        val actionsPanel = object : Panel() {
            val buttonChatWith = Button("Chat With").apply {
                addListener {
                    val username = TextInputDialogBuilder()
                            .setTitle("Start chatting with")
                            .setDescription("Type in the username of a person you want to chat with:")
                            .setValidationPattern(Pattern.compile("[0-9a-zA-Z]{1,16}"), "Malformed username")
                            .build()
                            .showDialog(gui)
                    try {
                        val user = client.findUser(username)
                        client.getPersonalChatHistory(user.username)

                        val chats = client.findAvailableChats()
                        for (chat in chats) {
                            if (chat is PersonalChat && chat.hasMember(user.id)) {
                                currentChat = chat
                            }
                        }

                        updateChatList()
                        currentChat?.let { showChat(it) }
                    } catch (e: Exception) {
                        MessageDialogBuilder()
                                .setTitle("Error!")
                                .setText("Such user does not exist!")
                                .build()
                                .showDialog(gui)
                    }
                }
            }
            val buttonExit = Button("Exit")
        }.apply {
            layoutManager = LinearLayout(Direction.HORIZONTAL)
            addComponent(buttonChatWith)
            addComponent(buttonExit)
        }
    }.apply {
        layoutManager = LinearLayout(Direction.VERTICAL)

        addComponent(statusLine)

        val mainLayerPanel = Panel()
        mainLayerPanel.layoutManager = LinearLayout(Direction.HORIZONTAL)
        mainLayerPanel.addComponent(chatsPanel.withBorder(Borders.singleLine("Chats")))
        mainLayerPanel.addComponent(currentChatPanel.withBorder(Borders.singleLine("Current Chat")))

        addComponent(mainLayerPanel)

        addComponent(actionsPanel)
    }

    private fun showChat(chat: Chat) {
        val chatTitleLabel = container.currentChatPanel.chatStatusLine.chatTitleLabel
        when (chat) {
            is PersonalChat ->
                chatTitleLabel.text = client.findUserById(chat.notMe(client.self().id)).username
            is GroupChat ->
                chatTitleLabel.text = chat.title
        }
        val messages = when (chat) {
            is PersonalChat ->
                client.getPersonalChatHistory(client.findUserById(chat.notMe(client.self().id)).username)
            is GroupChat ->
                client.getGroupChatHistory(chat.title)
            else -> throw IllegalArgumentException()
        }

        fun makeMessage(message: Message): Component {
            val label = Label("")
            when (message) {
                is TextMessage -> {
                    val sender = message.sender.let {
                        client.findUserById(message.sender)
                    }
                    label.text = sender.displayName + ": " + message.content
                }
                else -> {
                    label.text = "<unsupported>"
                }
            }
            return label
        }

        val chatView = container.currentChatPanel.chatView
        chatView.removeAllComponents()
        for (message in messages) {
            chatView.addComponent(makeMessage(message))
        }

        currentChat = chat
    }

    private fun updateChatList() {
        val chatsView = container.chatsPanel
        chatsView.removeAllComponents()

        fun makeMessage(chat: Chat): Component {
            val panel = Panel().apply {
                layoutManager = LinearLayout(Direction.HORIZONTAL)
            }
            when (chat) {
                is PersonalChat -> {
                    val personId = chat.notMe(client.self().id)
                    val user = client.findUserById(personId)
                    panel.addComponent(Label(user.username))
                }
                is GroupChat -> {
                    panel.addComponent(Label(chat.title))
                }
            }
            panel.addComponent(Button("Show").apply {
                addListener {
                    showChat(chat)
                    container.currentChatPanel.chatControls.chatMessageTextBox.takeFocus()
                }

                layoutData = LinearLayout.createLayoutData(LinearLayout.Alignment.End)
            })
            return panel.withBorder(Borders.singleLine())
        }

        val chats = client.findAvailableChats()
        for (chat in chats) {
            chatsView.addComponent(makeMessage(chat))
        }
    }

    override fun setup() {
        window.component = container

        container.statusLine.labelUsername.text = parent.client.self().displayName

        updateChatList()
    }

    override fun run() {
        gui.addWindowAndWait(window)
    }

}