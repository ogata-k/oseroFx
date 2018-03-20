/**
 * Created by Owner on 2017/07/05.
 */

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window

class MsgDialog(val window: Window, val white: Int, val black: Int): Stage(){
    init{
        this.title = "簡易版オセロ"
        this.width = 400.0
        this.height = 300.0
        this.initStyle(javafx.stage.StageStyle.UTILITY)
        this.initOwner(window)
        this.isAlwaysOnTop = true
        this.initModality(Modality.APPLICATION_MODAL)

        val txtLbl = Label("勝負がつきました！！")
        txtLbl.prefWidth(100.0)
        var s = "白：${white}個\n黒：${black}個\nで"
        s += when{white==black -> "引き分け"
                  white > black -> "白の勝ち"
                  else -> "黒の勝ち"}
        val msgLbl = Label(s)
        msgLbl.prefWidth(300.0)

        val endBtn = Button("終了")
        endBtn.prefWidth(80.0)
        endBtn.setOnAction { event -> Platform.exit() }

        val root = VBox()
        root.alignment = Pos.CENTER
        root.padding = Insets(10.0, 10.0, 10.0, 10.0)
        root.spacing = 20.0
        root.children.addAll(txtLbl, msgLbl, endBtn)

        this.scene = Scene(root)
        this.show()
    }
}