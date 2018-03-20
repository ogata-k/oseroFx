/**
 * Created by Owner on 2017/07/07.
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

class InfoDialog(val win: Window): Stage(){
    init{
        this.title = "簡易版オセロ"
        this.width = 400.0
        this.height = 300.0
        this.initStyle(javafx.stage.StageStyle.UNDECORATED)
        this.initOwner(win)
        initModality(Modality.APPLICATION_MODAL)

        val txtLbl = Label("ルールについて説明します。")
        txtLbl.prefWidth(100.0)
        var s = "基本は普通のオセロと同じルールです。\n白色から開始です。\n" +
                "置くところがないときに置こうとすると自動的にパスとなります。\n" +
                "開始ボタンを押したらゲームが始まります。"
        val msgLbl = Label(s)
        msgLbl.prefWidth(300.0)

        val endBtn = Button("開始")
        endBtn.prefWidth(80.0)
        endBtn.setOnAction { event -> this.close() }

        val root = VBox()
        root.alignment = Pos.CENTER
        root.padding = Insets(10.0, 10.0, 10.0, 10.0)
        root.spacing = 20.0
        root.children.addAll(txtLbl, msgLbl, endBtn)

        this.scene = Scene(root)
        this.show()
    }

}