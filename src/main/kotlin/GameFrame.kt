import javafx.application.Platform
import javafx.scene.web.WebView
import java.awt.BorderLayout
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFrame
import javafx.scene.Scene
import javafx.embed.swing.JFXPanel
import javafx.scene.input.KeyEvent

class GameFrame: JFrame() {
    lateinit var browser: WebView
    private val browserPane = JFXPanel()
    init {
        title = "Dvornik Fortress"
        iconImage = ImageIO.read(File("src/resources/dvornik_fortress.png"))

        defaultCloseOperation = EXIT_ON_CLOSE
        background = Color.BLACK
        setSize(700, 450)
        setLocationRelativeTo(null)
        isVisible = true

        contentPane.add(browserPane, BorderLayout.CENTER)

        Platform.runLater {
            browser = WebView()
            browserPane.scene = Scene(browser)
            browser.setOnKeyPressed { e -> handleKeyPress(e) }
        }
    }

    private fun handleKeyPress(e: KeyEvent) {
        val buttonCode = e.code.code
        buttonMap[buttonCode]?.invoke()

        println("button pressed - $buttonCode")
        println("pos - (${gameMap.cursor.posX}, ${gameMap.cursor.posY}, ${gameMap.cursor.posZ})\n")
    }
}
