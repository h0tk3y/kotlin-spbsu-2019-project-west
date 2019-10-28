package snailmail.client.lanterna

import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import snailmail.core.UserCredentials
import java.util.regex.Pattern

class LoginActivity(parent: LanternaClient) : Activity(parent) {
    val gui = parent.gui
    val client = parent.client

    val panel = Panel()
    val window = BasicWindow()

    val labelUsername = Label("Username")
    val inputUsername = TextBox()

    val labelPassword = Label("Password")
    val inputPassword = TextBox()

    val buttonAuthenticate = Button("Authenticate")
    val buttonRegister = Button("Register")

    val passwordRenderer = object : TextBox.DefaultTextBoxRenderer() {
        override fun drawComponent(graphics: TextGUIGraphics?, component: TextBox?) {
            if (component != null) {
                val password = component.text
                component.text = StringBuilder("*").repeat(password.length)
                super.drawComponent(graphics, component)
                component.text = password
            }
        }
    }

    fun onAuth() {
        MessageDialog.showMessageDialog(gui, "Success!", client.self().username + " " + client.self().id)

        gui.removeWindow(window)
        parent.transition(MainActivity(parent))
    }

    override fun setup() {
        inputUsername.setValidationPattern(Pattern.compile("[0-9a-zA-Z._]{1,16}"))
        inputPassword.renderer = passwordRenderer

        buttonAuthenticate.addListener { button ->
            if (button == null) throw IllegalArgumentException()
            try {
                if (client.authenticate(UserCredentials(inputUsername.text, inputPassword.text))) {
                    onAuth()
                } else {
                    MessageDialog.showMessageDialog(gui, "Authentication error!", "Incorrect credentials!")
                }
            } catch (e: Exception) {
                MessageDialog.showMessageDialog(gui, "Authentication error!", e.message)
            }
        }

        buttonRegister.addListener { button ->
            if (button == null) throw IllegalArgumentException()
            try {
                if (client.register(UserCredentials(inputUsername.text, inputPassword.text))) {
                    onAuth()
                } else {
                    MessageDialog.showMessageDialog(gui, "Register error!", "Username is already taken!")
                }
            } catch (e: Exception) {
                MessageDialog.showMessageDialog(gui, "Register error!", e.message)
            }
        }

        panel.layoutManager = GridLayout(2)

        panel.addComponent(labelUsername)
        panel.addComponent(inputUsername)

        panel.addComponent(labelPassword)
        panel.addComponent(inputPassword)

        panel.addComponent(buttonAuthenticate)
        panel.addComponent(buttonRegister)

        window.setHints(listOf(Window.Hint.CENTERED))
        window.component = panel
    }

    override fun run() {
        gui.addWindowAndWait(window)
    }
}