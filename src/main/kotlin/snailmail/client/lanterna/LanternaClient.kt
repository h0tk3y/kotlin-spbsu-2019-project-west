package snailmail.client.lanterna

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.DefaultWindowManager
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import snailmail.client.Client
import snailmail.core.api.API

class LanternaClient(api: API) {
    val client = Client(api)
    // Setup terminal and screen layers
    val terminal: Terminal = DefaultTerminalFactory().createTerminal()
    val screen = TerminalScreen(terminal)
    val gui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLACK))

    fun run() {
        screen.startScreen()

        val loginActivity = LoginActivity(this)
        loginActivity.setup()
        loginActivity.run()
    }

    fun transition(activity: Activity) {
        activity.setup()
        activity.run()
    }
}