/**
 * Created by Owner on 2017/07/03.
 */
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window

class Osero: Application(){
    //宣言部分--------------------------------------------------------------------------
    val w: Double = 800.0
    val h: Double = 950.0

    val playArea: Canvas = Canvas(this.w,this.w)
    val gc: GraphicsContext = playArea.graphicsContext2D

    val boardLen: Int = this.getLen(8)

    val board = List(this.boardLen, { MutableList(this.boardLen, {0})})

    //0は石がない状態、1は白色、2は黒色
    var whiteNum: Int = this.count(1)
    var blackNum: Int = this.count(2)

    //石の個数についてのラベル
    val whiteLbl = Label("白：${this.whiteNum}個")
    val blackLbl = Label("黒：${this.blackNum}個")

    //手番用の番号
    var player: Int = 1  //最初は白番
    val playerColor = mapOf<Int, Color>(1 to Color.WHITE, 2 to Color.BLACK)
    val playerLbl = Label()

    //ステージ
    lateinit var baseStage: Stage

    //初期化部分------------------------------------------------------------------------------------
    override fun start(stage: Stage?) {
        if (stage == null) return
        //アプリの設定
        this.checkSize()
        stage.title = "簡易版オセロ"
        stage.width = this.w
        stage.height = this.h
        stage.isResizable = false
        this.baseStage = stage
        //石の設置->ラベルの書き換え
        this.board[this.boardLen/2-1][this.boardLen/2-1] = 1
        this.board[this.boardLen/2][this.boardLen/2] = 1
        this.board[this.boardLen/2-1][this.boardLen/2] = 2
        this.board[this.boardLen/2][this.boardLen/2-1] = 2
        this.updateStoneLbl()

        val root = VBox()
        //キャンバス関係
        this.canvasInitPaint()
        this.playArea.setOnMouseClicked { event -> this.putStone(event) }
        root.children.add(playArea)

        //ラベルの設定
        this.whiteLbl.font = Font(50.0)
        this.blackLbl.font = Font(50.0)
        this.playerLbl.font = Font(50.0)
        //手番用のラベルの初期化
        this.setPlayer()
        //下に用意するパネルの設定
        val underPane = AnchorPane()
        AnchorPane.setTopAnchor(this.whiteLbl, 10.0)
        AnchorPane.setLeftAnchor(this.whiteLbl, 10.0)
        AnchorPane.setTopAnchor(this.playerLbl, 10.0)
        AnchorPane.setLeftAnchor(this.playerLbl, this.w/2 - 50)
        AnchorPane.setTopAnchor(this.blackLbl, 10.0)
        AnchorPane.setRightAnchor(this.blackLbl, 10.0)
        underPane.children.addAll(this.whiteLbl, this.playerLbl, this.blackLbl)
        //パネルを親に設置
        root.children.add(underPane)

        stage.scene = Scene(root)
        stage.show()
        InfoDialog(this.baseStage)
    }
    //アプリ関係---------------------------------------------------------------------------------------
    private fun checkSize() {
        require( this.w < this.h, {"縦幅の方が長くなければいけません"})
    }
    private fun getLen(size: Int): Int{
        require(size > 0 && size%2==0, {"サイズは非負の偶数でなければなりません"})
        return size
    }
    private fun getSize(): Double = this.getBoardSize() / this.boardLen
    private fun getBoardSize(): Double = arrayOf(this.w, this.h).min() ?: 0.0

    //マウス関係--------------------------------------------------------------------------------------
    private fun putStone(event: MouseEvent){
        println("${this.playerLbl.text}です。")
        if (this.checkAllPutable(this.player))
            println("石を置く場所が存在します。")
        else{
            println("石を置く場所が存在しません。相手と交代します。")
            this.changePlayer()
            this.setPlayer()
            return
        }
        val (i,j) = this.getIJ(event)
        print("${i} ")
        print("${j} ")
        val targets = this.searchRecerseableTarget(i, j, this.player)
        val count = this.searchReverseableNum(i,j, this.player)
        println("${targets} ${count}")
        if (!this.isPutable(i, j, this.player)) return

        targets.forEach{ (i, j) ->
            this.circlePaint(i, j, this.playerColor[this.player]!!)
            this.board[i][j] = this.player
            }
        this.updateStoneLbl()
        println("無事に石を置けました。手番を交代します。")
        this.changePlayer()
        this.setPlayer()
    }

    private fun getXY(event: MouseEvent): Pair<Double, Double>{
        val x = event.x
        val y = event.y
        return Pair(x, y)
    }

    private fun getIJ(event: MouseEvent): Pair<Int, Int> {
        val (x, y) = getXY(event)
        val i = (y / 100).toInt()
        val j = (x / 100).toInt()
        return Pair(i, j)
    }
    //キャンバス関係-----------------------------------------------------------------------------------
    private fun canvasInitPaint() {
        this.gc.fill = Color.GREEN
        this.gc.fillRect(0.0, 0.0, this.getBoardSize(), this.getBoardSize())
        this.gc.fill = Color.BLACK

        gc.lineWidth = 4.0
        for (i in 0..this.boardLen+1){
            this.gc.strokeLine(i*this.getSize(), 0.0, i*this.getSize(), this.getBoardSize())
            this.gc.strokeLine(0.0, i*this.getSize(), this.getBoardSize(), i*this.getSize())
        }
        gc.lineWidth = 1.0
        this.circlePaint(this.boardLen/2-1, this.boardLen/2-1, Color.WHITE)
        this.circlePaint(this.boardLen/2, this.boardLen/2, Color.WHITE)
        this.circlePaint(this.boardLen/2-1, this.boardLen/2, Color.BLACK)
        this.circlePaint(this.boardLen/2, this.boardLen/2-1, Color.BLACK)
    }

    private fun circlePaint(i: Int, j:Int, color: Color){
        gc.fill = color
        gc.fillOval(this.getSize()*(j+0.1), this.getSize()*(i+0.1), this.getSize()*0.8, this.getSize()*0.8)
    }

    //手番関係-----------------------------------------------------------------------------------------
    private fun setPlayer(){
        if (this.player==1) {
            this.playerLbl.text = "白 番"
        } else if (this.player==2) {
            this.playerLbl.text = "黒 番"
        }
        this.checkFinish()
    }
    private fun checkFinish(){
        this.updateCount()
        if (!this.checkAllPutable(1) && !this.checkAllPutable(2)){
            println("決着がつきました。")
            MsgDialog(this.baseStage, this.whiteNum, this.blackNum)
        }
    }
    private fun changePlayer(){
        this.player = 3 - this.player
    }
    //石関係--------------------------------------------------------------------------------------------
    private fun updateStoneLbl(){
        this.updateCount()
        this.whiteLbl.text = "白：${this.whiteNum}個"
        this.blackLbl.text = "黒：${this.blackNum}個"
    }
    private fun updateCount(){
        this.whiteNum = this.count(1)
        this.blackNum = this.count(2)
    }
    //石を置くことができるかのチェック置けたらtrue
    private fun isPutable(i: Int, j: Int, target: Int): Boolean{
        if (this.board[i][j] != 0) return false
        val count: Int = this.searchReverseableNum(i, j, target)
        if (count == 0)
            return false
        return true
    }
    private fun checkAllPutable(target: Int) : Boolean =
            (0..this.boardLen-1).any{ i -> (0..this.boardLen-1).any{j -> this.isPutable(i, j, target)} }
    //反転可能な石の個数を探す関数
    private fun searchRight(i: Int, j: Int, target: Int): Int{
        var count: Int = 0
        var flg = false  // 問題なくひっくり返すことができるかのフラグ
        if (j == this.boardLen-1) return 0 //壁際なら探さない
        if (3-target != this.board[i][j+1]) return 0  // すぐ横が反転の対象色出なければ空か自身と同じ色なので0を返す
        for (k in 1 .. this.boardLen-j-1){
            if (3-target == this.board[i][j+k])
                count++
            else if (target == this.board[i][j+k]){
                flg = true
                break
            } else break
        }
        if (! flg) count=0
        return count
    }
    private fun searchLeft(i: Int, j: Int, target: Int): Int{
        var count: Int = 0
        var flg = false  // 問題なくひっくり返すことができるかのフラグ
        if (j == 0) return 0 //壁際なら探さない
        if (3-target != this.board[i][j-1]) return 0  // すぐ横が反転の対象色出なければ空か自身と同じ色なので0を返す
        for (k in 1 .. j){
            if (3-target == this.board[i][j-k])
                count++
            else if (target == this.board[i][j-k]){
                flg = true
                break
            } else break
        }
        if (! flg) count=0
        return count
    }
    private fun searchUp(i: Int, j: Int, target: Int): Int{
        var count: Int = 0
        var flg = false  // 問題なくひっくり返すことができるかのフラグ
        if (i == 0) return 0 //壁際なら探さない
        if (3-target != this.board[i-1][j]) return 0  // すぐ隣が反転の対象色出なければ空か自身と同じ色なので0を返す
        for (k in 1 .. i){
            if (3-target == this.board[i-k][j])
                count++
            else if (target == this.board[i-k][j]){
                flg = true
                break
            } else break
        }
        if (! flg) count=0
        return count
    }
    private fun searchDown(i: Int, j: Int, target: Int): Int{
        var count: Int = 0
        var flg = false  // 問題なくひっくり返すことができるかのフラグ
        if (i == this.boardLen-1) return 0 //壁際なら探さない
        if (3-target != this.board[i+1][j]) return 0  // すぐ隣が反転の対象色出なければ空か自身と同じ色なので0を返す
        for (k in 1 .. this.boardLen-i-1){
            if (3-target == this.board[i+k][j])
                count++
            else if (target == this.board[i+k][j]){
                flg = true
                break
            } else break
        }
        if (! flg) count=0
        return count
    }
    private fun searchRightUp(i: Int, j: Int, target: Int): Int{
        var count: Int = 0
        var flg = false  // 問題なくひっくり返すことができるかのフラグ
        if (i == 0 || j == this.boardLen-1) return 0 //壁際なら探さない
        if (3-target != this.board[i-1][j+1]) return 0  // すぐ隣が反転の対象色出なければ空か自身と同じ色なので0を返す
        for (k in 1 .. minOf(i, this.boardLen-j-1)){
            if (3-target == this.board[i-k][j+k])
                count++
            else if (target == this.board[i-k][j+k]){
                flg = true
                break
            } else break
        }
        if (! flg) count=0
        return count
    }
    private fun searchRightDown(i: Int, j: Int, target: Int): Int{
        var count: Int = 0
        var flg = false  // 問題なくひっくり返すことができるかのフラグ
        if (i == this.boardLen-1 || j == this.boardLen-1) return 0 //壁際なら探さない
        if (3-target != this.board[i+1][j+1]) return 0  // すぐ隣が反転の対象色出なければ空か自身と同じ色なので0を返す
        for (k in 1 .. minOf(this.boardLen-i-1, this.boardLen-j-1)){
            if (3-target == this.board[i+k][j+k])
                count++
            else if (target == this.board[i+k][j+k]){
                flg = true
                break
            } else break
        }
        if (! flg) count=0
        return count
    }
    private fun searchLeftUp(i: Int, j: Int, target: Int): Int{
        var count: Int = 0
        var flg = false  // 問題なくひっくり返すことができるかのフラグ
        if (i == 0 || j == 0) return 0 //壁際なら探さない
        if (3-target != this.board[i-1][j-1]) return 0  // すぐ隣が反転の対象色出なければ空か自身と同じ色なので0を返す
        for (k in 1 .. minOf(i, j)){
            if (3-target == this.board[i-k][j-k])
                count++
            else if (target == this.board[i-k][j-k]){
                flg = true
                break
            } else break
        }
        if (! flg) count=0
        return count
    }
    private fun searchLeftDown(i: Int, j: Int, target: Int): Int{
        var count: Int = 0
        var flg = false  // 問題なくひっくり返すことができるかのフラグ
        if (i == this.boardLen-1 || j == 0) return 0 //壁際なら探さない
        if (3-target != this.board[i+1][j-1]) return 0  // すぐ隣が反転の対象色出なければ空か自身と同じ色なので0を返す
        for (k in 1 .. minOf(this.boardLen-i-1, j)){
            if (3-target == this.board[i+k][j-k])
                count++
            else if (target == this.board[i+k][j-k]){
                flg = true
                break
            } else break
        }
        if (! flg) count=0
        return count
    }
    private fun searchReverseableNum(i: Int, j:Int, target: Int): Int{
        var count = 0  //反転可能な石の個数
        count += this.searchRight(i, j, target)
        count += this.searchLeft(i, j, target)
        count += this.searchUp(i, j, target)
        count += this.searchDown(i, j, target)
        count += this.searchRightUp(i, j, target)
        count += this.searchRightDown(i, j, target)
        count += this.searchLeftUp(i, j, target)
        count += this.searchLeftDown(i, j, target)
        return count
    }

    //置く場所と反転可能な石の場所からなるリストを返す
    private fun searchTargetRight(i: Int, j: Int, target: Int): List<Pair<Int, Int>>{
        var result= mutableListOf<Pair<Int, Int>>()
        val count = this.searchRight(i, j, target)
        for (k in 0 .. count)
            result.add(Pair(i, j+k))
        return result
    }
    private fun searchTargetLeft(i: Int, j: Int, target: Int): List<Pair<Int, Int>>{
        var result= mutableListOf<Pair<Int, Int>>()
        val count = this.searchLeft(i, j, target)
        for (k in 0 .. count)
            result.add(Pair(i, j-k))
        return result
    }
    private fun searchTargetUp(i: Int, j: Int, target: Int): List<Pair<Int, Int>>{
        var result= mutableListOf<Pair<Int, Int>>()
        val count = this.searchUp(i, j, target)
        for (k in 0 .. count)
            result.add(Pair(i-k, j))
        return result
    }
    private fun searchTargetDown(i: Int, j: Int, target: Int): List<Pair<Int, Int>>{
        var result= mutableListOf<Pair<Int, Int>>()
        val count = this.searchDown(i, j, target)
        for (k in 0 .. count)
            result.add(Pair(i+k, j))
        return result
    }
    private fun searchTargetRightUp(i: Int, j: Int, target: Int): List<Pair<Int, Int>>{
    var result= mutableListOf<Pair<Int, Int>>()
    val count = this.searchRightUp(i, j, target)
    for (k in 0 .. count)
    result.add(Pair(i-k, j+k))
    return result
    }
    private fun searchTargetRightDown(i: Int, j: Int, target: Int): List<Pair<Int, Int>>{
        var result= mutableListOf<Pair<Int, Int>>()
        val count = this.searchRightDown(i, j, target)
        for (k in 0 .. count)
            result.add(Pair(i+k, j+k))
        return result
    }
    private fun searchTargetLeftUp(i: Int, j: Int, target: Int): List<Pair<Int, Int>>{
        var result= mutableListOf<Pair<Int, Int>>()
        val count = this.searchLeftUp(i, j, target)
        for (k in 0 .. count)
            result.add(Pair(i-k, j-k))
        return result
    }
    private fun searchTargetLeftDown(i: Int, j: Int, target: Int): List<Pair<Int, Int>>{
        var result= mutableListOf<Pair<Int, Int>>()
        val count = this.searchLeftDown(i, j, target)
        for (k in 0 .. count)
            result.add(Pair(i+k, j-k))
        return result
    }
    private fun searchRecerseableTarget(i: Int, j: Int, target: Int): List<Pair<Int, Int>>{
        val result: MutableList<Pair<Int, Int>> = mutableListOf()
        this.searchTargetRight(i, j, target).forEach{result.add(it)}
        this.searchTargetLeft(i, j, target).forEach{result.add(it)}
        this.searchTargetUp(i, j, target).forEach{result.add(it)}
        this.searchTargetDown(i, j, target).forEach{result.add(it)}
        this.searchTargetRightUp(i, j, target).forEach{result.add(it)}
        this.searchTargetRightDown(i, j, target).forEach{result.add(it)}
        this.searchTargetLeftUp(i, j, target).forEach{result.add(it)}
        this.searchTargetLeftDown(i, j, target).forEach{result.add(it)}
        return result.toSet().toList()
    }

    //targetの盤上にある個数を返す
    private fun count(target: Int): Int = this.board.map{ it.count{ it==target } }.sum()
    //this.board.map{ it.count{ it==target } }.reduce({a:Int, b:Int -> a+b})でも可
}
