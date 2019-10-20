package snailmail.client.lanterna

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.*

class MainActivity(parent: LanternaClient) : Activity(parent) {
    val gui = parent.gui
    val client = parent.client

    val window = BasicWindow()

    val statusPanel = Panel().apply {
        layoutManager = LinearLayout(Direction.HORIZONTAL)
    }
    val chatsPanel = Panel().apply {
        layoutManager = LinearLayout(Direction.VERTICAL)
    }
    val currentChatPanel = Panel().apply {
        layoutManager = LinearLayout(Direction.VERTICAL)
    }
    val actionsPanel = Panel().apply {
        layoutManager = LinearLayout(Direction.HORIZONTAL)
    }

    val labelUsername = Label("")

    val buttonSendMessage = Button("Send")
    val buttonChatWith = Button("Chat With")
    val buttonExit = Button("Exit")

    override fun setup() {
        val panel = Panel()
        panel.layoutManager = LinearLayout(Direction.VERTICAL)

        panel.addComponent(statusPanel)

        val mainLayerPanel = Panel()
        mainLayerPanel.layoutManager = LinearLayout(Direction.HORIZONTAL)
        mainLayerPanel.addComponent(chatsPanel.withBorder(Borders.singleLine("Chats")))
        mainLayerPanel.addComponent(currentChatPanel.withBorder(Borders.singleLine("Current Chat")))

        panel.addComponent(mainLayerPanel)

        panel.addComponent(actionsPanel)

        window.component = panel

        chatsPanel.preferredSize = TerminalSize((parent.terminal.terminalSize.columns * (1.0 / 5.0)).toInt(), parent.terminal.terminalSize.rows - 10)
        currentChatPanel.preferredSize = TerminalSize((parent.terminal.terminalSize.columns * (3.5 / 5.0)).toInt(), parent.terminal.terminalSize.rows - 10)
    }

    override fun run() {
        gui.addWindowAndWait(window)
    }

}