package com.jaycefr.pawnrace

import java.io.PrintWriter
import java.io.InputStreamReader
import java.io.BufferedReader
import kotlin.math.abs
import kotlin.math.max

// You should not add any more member values or member functions to this class
// (or change its name!). The autorunner will load it in via reflection and it
// will be safer for you to just call your code from within the playGame member
// function, without any unexpected surprises!
class PawnRace {
    // Don't edit the type or the name of this method
    // The colour can take one of two values: 'W' or 'B', this indicates your player colour
    fun playGame(colour: Char, output: PrintWriter, input: BufferedReader) {
        if (colour == 'B')
            output.println("aa")

        val gaps = input.readLine()

        val board = Board(File(gaps[0].toLowerCase() - 'a'), File(gaps[1].toLowerCase() - 'a'))
        val whitePlayer = Player(Piece.WHITE)
        val blackPlayer = Player(Piece.BLACK, whitePlayer)
        whitePlayer.opponent = blackPlayer
        var game = Game(board, whitePlayer)

        if (colour == 'W') {
            output.println(game.player.makeMove(game))
        }

        while (!game.over()) {
            println()
            val inputH = input.readLine()
            println("input provided $inputH")
            game.applyMove(game.parseMove(inputH)!!)
            if (!game.over()) {
                output.println(game.player.makeMove(game))
            }
        }

        println(game.winner())
    }
}

// When running the command, provide an argument either W or B, this indicates your player colour
//fun main(args: Array<String>) {
//    PawnRace().playGame(args[0][0], PrintWriter(System.out, true), BufferedReader(InputStreamReader(System.`in`)))
//}


enum class Piece {
    BLACK, WHITE;

    override fun toString(): String =
        when (this) {
            BLACK -> "B"
            WHITE -> "W"
        }

    fun getOps(): Piece =
        when (this) {
            BLACK -> WHITE
            WHITE -> BLACK
        }

    fun copy(): Piece = when (this) {
        BLACK -> BLACK
        WHITE -> WHITE
    }

}

class File(
    val file: Int
) {

    override fun toString(): String {
        return ('a' + file).toString()
    }
}

class Rank(
    val rank: Int
) {

    override fun toString(): String {
        return (rank + 1).toString()
    }

}


class Position(val pos: String) { // a..h, 1..8
    val file = File(pos[0].toLowerCase() - 'a')
    val rank = Rank(pos[1] - '1')

    val column = file.file // 0..7
    val row = rank.rank // 0..7

    override fun toString(): String {
        return file.toString() + rank.toString()
    }

    fun addDelta(x: Int = 0, y: Int = 0): Position {
        return Position("${'a' + (column + x)}${row + y + 1}")
    }

    override fun equals(other: Any?): Boolean {
        if (other is Position) {
            return column == other.column
                    && row == other.row
        }
        return false
    }

    override fun hashCode(): Int {
        var result = pos.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + rank.hashCode()
        result = 31 * result + column
        result = 31 * result + row
        return result
    }

}

enum class MoveType {
    PEACEFUL, CAPTURE, EN_PASSANT
}

data class Move(
    val piece: Piece,
    val from: Position,
    val to: Position,
    val type: MoveType
) {

    override fun toString(): String =
        when (type) {
            MoveType.PEACEFUL -> "${from.file}${to.rank}"
            MoveType.CAPTURE, MoveType.EN_PASSANT -> "${from.file}x$to"
        }

}

class Board(
    val whiteGap: File,
    val blackGap: File
) {

    val board = Array(8) { index ->
        when (index) {
            1 -> genArray(Piece.WHITE, whiteGap.file)
            6 -> genArray(Piece.BLACK, blackGap.file)
            else -> Array<Piece?>(8) { null }
        }
    }

    fun copy(): Board {
        val newBoard = Board(whiteGap, blackGap)
        board.copyInto(newBoard.board)
        return Board(whiteGap, blackGap)
    }

    override fun toString(): String {
        // the board is flipped
        var output = "   A B C D E F G H\n\n"
        for (x in 7 downTo 0) {
            output += "${x + 1}  "
            for (y in 0..7) {
                output += (board[x][y]?.toString() ?: ".") + " "
            }
            output += "  ${x + 1}\n"
        }
        return "$output\n   A B C D E F G H"
    }

    fun pieceAt(pos: Position): Piece? {
        if (pos.row < 0 || pos.row > 7 || pos.column < 0 || pos.column > 7)
            return null
        return board[pos.row][pos.column]
    }

    fun isPiece(row: Int, column: Int, piece: Piece): Boolean {
        if (row < 0 || row > 7 || column < 0 || column > 7)
            return false
        return board[row][column] == piece
    }

    fun isEmptyAt(row : Int, column : Int) : Boolean{
        if (row < 0 || row > 7 || column < 0 || column > 7)
            return false
        return board[row][column] == null
    }

    fun positionOf(piece: Piece): List<Position> {
        val positionList = emptyList<Position>().toMutableList()
        for (row in 0..7) {
            for (column in 0..7) {
                if (board[row][column] == piece) {
                    positionList.add(Position("${'a' + column}${row + 1}"))
                }
            }
        }
        return positionList.toList()
    }

    fun isValidMove(move: Move, lastMove: Move? = null): Boolean {
        if (move.to.row < 0 || move.to.row > 7 || move.to.column < 0 || move.to.column > 7) {
            return false
        }
        val delta = if (move.piece == Piece.WHITE) 1 else -1
        return when (move.type) {
            MoveType.PEACEFUL ->
                (move.to.column == move.from.column) && // same column
                        (when (abs(move.to.row - move.from.row)) {
                            1 -> pieceAt(move.to) == null
                            2 -> ((move.piece == Piece.WHITE && move.from.row == 1) || (move.piece == Piece.BLACK && move.from.row == 6))
                                    && pieceAt(move.to) == null
                                    && pieceAt(move.from.addDelta(0, delta)) == null

                            else -> false
                        })

            MoveType.CAPTURE -> (pieceAt(move.to) == move.piece.getOps()) &&
                    (abs(move.to.row - move.from.row) == 1) &&
                    (abs(move.to.column - move.from.column) == 1)

            MoveType.EN_PASSANT -> {
                val targetPos = Position("${'a' + move.to.column}${move.from.row + 1}")
                (pieceAt(targetPos) == move.piece.getOps())
                        && (lastMove?.to == targetPos)
                        && (abs(lastMove.to.row - lastMove.from.row) == 2)
            }
        }
    }

    fun move(
        m: Move,
        lastMove: Move? = null,
        reverting: Boolean = false
    ): Array<Array<Piece?>> { // spec says this should be of type Board
        if (!reverting) {
            if (isValidMove(m, lastMove)) {
                board[m.from.row][m.from.column] = null
                board[m.to.row][m.to.column] = m.piece.copy()
                if (m.type == MoveType.EN_PASSANT) {
                    board[m.from.row][m.to.column] = null
                }
            }
        } else {
            // need to do something special for en passant and capture
            // we know move is valid
            board[m.from.row][m.from.column] = m.piece.copy()
            when (m.type) {
                MoveType.PEACEFUL ->
                    board[m.to.row][m.to.column] = null

                MoveType.CAPTURE ->
                    board[m.to.row][m.to.column] = m.piece.getOps() // rebirth and replace
                MoveType.EN_PASSANT -> {
                    board[m.from.row][m.to.column] = m.piece.getOps() // rebirth
                    board[m.to.row][m.to.column] = null // deletes the piece
                }

            }
        }
        return board
    }

    private fun genArray(piece: Piece, pos: Int): Array<Piece?> {
        return Array<Piece?>(8) { i ->
            when (i) {
                pos -> null
                else -> piece
            }
        }
    }

}

class Game(
    val board: Board,
    var player: Player,
    val moves: MutableList<Move> = mutableListOf()
) {

    val transpositionTable = mutableMapOf<Long, Int>()
    val zobristHasher = ZobristHasher()

    fun applyMove(move: Move) {
        if (moves.isEmpty())
            board.move(move, null)
        else
            board.move(move, moves.last().copy())
        moves.add(move)
        player = player.opponent!!
//        return Game(board, player, move)
    }

    fun unapplyMove() {
        if (moves.isNotEmpty()) {
            val move = moves.removeAt(moves.size - 1)
            board.move(move, null, true)
            player = player.opponent!!
        }
    }

    fun reset() : Game{
        val nboard = Board(File(7), File(0))
        val nwhitePlayer = Player(Piece.WHITE)
        val nblackPlayer = Player(Piece.BLACK, nwhitePlayer)
        nwhitePlayer.opponent = nblackPlayer
        return Game(nboard, nwhitePlayer)
    }

//    fun copy() : Game {
//        return Game(board.copy(), player, moves)
//    }

    private fun moveForwardBy(pos: Position, step: Int, piece: Piece): Move? {
        // pre - step is +ve for white and -ve for black
        val move: Move = Move(
            piece,
            pos,
            pos.addDelta(0, step),
            MoveType.PEACEFUL
        )
        if (board.isValidMove(move)) {
            return move
        }
        return null
    }

    fun moveDiagonalBy(pos: Position, isLeft: Boolean, piece: Piece, type: MoveType): Move? {
        val y = if (piece == Piece.BLACK) -1 else 1 // white
        val targetPos = if (isLeft) pos.addDelta(-1, y) else pos.addDelta(1, y)
        val move: Move = Move(
            piece,
            pos,
            targetPos,
            type
        )
        if (moves.isNotEmpty() && board.isValidMove(move, moves.last().copy())) {
            return move
        }
        return null
    }

    fun validPawnAtPosMoves(pos: Position, piece: Piece) : List<Move>{
        val delta = if (piece == Piece.BLACK) -1 else 1
        val moveList = mutableListOf<Move>()
        listOf(
            moveForwardBy(pos, 1 * delta, piece),
            moveForwardBy(pos, 2 * delta, piece)
        ).forEach { move ->
            move?.let { moveList.add(it) }
        }
        // Add diagonal moves for capture and en passant
        listOf(
            moveDiagonalBy(pos, false, piece, MoveType.CAPTURE),
            moveDiagonalBy(pos, true, piece, MoveType.CAPTURE),
            moveDiagonalBy(pos, false, piece, MoveType.EN_PASSANT),
            moveDiagonalBy(pos, true, piece, MoveType.EN_PASSANT)
        ).forEach { move ->
            move?.let { moveList.add(it) }
        }
        return moveList.toList()
    }

    fun moves(piece: Piece): List<Move> {
        val moveList = mutableListOf<Move>()
        val allPos: List<Position> = board.positionOf(piece)
        val delta = if (piece == Piece.BLACK) -1 else 1

        for (pos in allPos) {
            // Add forward moves
            listOf(
                moveForwardBy(pos, 1 * delta, piece),
                moveForwardBy(pos, 2 * delta, piece)
            ).forEach { move ->
                move?.let { moveList.add(it) }
            }

            // Add diagonal moves for capture and en passant
            listOf(
                moveDiagonalBy(pos, false, piece, MoveType.CAPTURE),
                moveDiagonalBy(pos, true, piece, MoveType.CAPTURE),
                moveDiagonalBy(pos, false, piece, MoveType.EN_PASSANT),
                moveDiagonalBy(pos, true, piece, MoveType.EN_PASSANT)
            ).forEach { move ->
                move?.let { moveList.add(it) }
            }
        }
        return moveList.toList()
    }

    fun over(): Boolean {
        // Check if the current player has won
        val winRow = if (player.piece == Piece.WHITE) 7 else 0
        val playerPos = player.getAllPawns(this)
        for (pos in playerPos) {
            if (pos.row == winRow) return true // Player wins by promotion
        }

        // Check if the opponent has won
        val oppWinRow = if (player.piece == Piece.BLACK) 7 else 0
        val oppoPos = board.positionOf(player.piece.getOps())
        for (pos in oppoPos) {
            if (pos.row == oppWinRow) return true // Opponent wins by promotion
        }

        // Check for stalemate (current player has no valid moves)
        return player.getAllValidMoves(this).isEmpty()
    }


    fun winner(): Piece? {
        for (pos in board.positionOf(Piece.WHITE)){
            if (pos.row == 7)
                return Piece.WHITE
        }
        for (pos in board.positionOf(Piece.BLACK)){
            if (pos.row == 0)
                return Piece.BLACK
        }
        return null
    }

    fun getRankAtFile(file: Char, targetRank: Int? = null, type: MoveType? = null): Int? {
        val fileInt = file.toLowerCase() - 'a'
        val listOfPos = mutableListOf<Int>()
        val positions = board.positionOf(player.piece)
        val delta = if (player.piece == Piece.WHITE) 1 else -1
        val basePos = if (player.piece == Piece.WHITE) 2 else 7
        for (position in positions) {
            if (position.column == fileInt) {
                listOfPos.add(position.row + 1)
            }
        }
        if (targetRank != null) {
            if (type == null) {
                // either an en passant or a capture
                for (pos in listOfPos) {
                    if (targetRank - pos == delta)
                        return pos
                }
                return null
            }
            // peaceful
            for (pos in listOfPos) {
                if ( (targetRank - pos == delta && board.isEmptyAt(targetRank-1, fileInt) ||
                            (targetRank - pos == 2 * delta) && (pos == basePos) && board.isEmptyAt(targetRank-1, fileInt) && board.isEmptyAt(targetRank-2, fileInt)) )
                    return pos
            }
        }
        if (listOfPos.isNotEmpty())
            return listOfPos[0]
        return null
    }

    fun parseMove(san: String): Move? {
        val fromTo = san.split("x")
        var move: Move? = null
        if (fromTo.size == 1) {
            // Peaceful Move
            val rankAtFIle = getRankAtFile(fromTo[0][0], fromTo[0][1] - '0', MoveType.PEACEFUL) ?: return null
            move = Move(
                player.piece,
                Position("${fromTo[0][0]}$rankAtFIle"),
                Position(fromTo[0]),
                MoveType.PEACEFUL
            )
        } else {
            // either a CAPTURE or EN-PASSANT
            if (fromTo[0].length == 1) {
                val rank = getRankAtFile(fromTo[0][0], fromTo[1][1] - '0') ?: return null
                move = Move(
                    player.piece,
                    Position("${fromTo[0]}$rank"),
                    Position(fromTo[1]),
                    MoveType.EN_PASSANT
                )
            } else {
                move = Move(
                    player.piece,
                    Position(fromTo[0]),
                    Position(fromTo[1]),
                    MoveType.EN_PASSANT
                )
            }

            if (moves.isNotEmpty() && !board.isValidMove(move, moves.last().copy()))
                if (fromTo[0].length == 1) {
                    val rank = getRankAtFile(fromTo[0][0], fromTo[1][1] - '0') ?: return null
                    move = Move(
                        player.piece,
                        Position("${fromTo[0]}$rank"),
                        Position(fromTo[1]),
                        MoveType.CAPTURE
                    )
                } else {
                    move = Move(
                        player.piece,
                        Position(fromTo[0]),
                        Position(fromTo[1]),
                        MoveType.CAPTURE
                    )
                }
            else {
                return move
            }
        }

        if (board.isValidMove(move))
            return move

        return null
    }

}

class Player(val piece: Piece, var opponent: Player? = null) {

    fun getAllPawns(game: Game): List<Position> {
        return game.board.positionOf(piece)
    }

    fun getAllValidMoves(game: Game): List<Move> {
        return game.moves(piece)
    }

    fun isPassedPawn(pos: Position, game: Game, piece: Piece): Boolean {
        val oppoPawnPositions = game.board.positionOf(piece.getOps())
        val delta = if (piece == Piece.WHITE) 1 else -1
        for (opos in oppoPawnPositions) {
            if (delta * (opos.row - pos.row) >= 1) {
                if (abs(opos.column - pos.column) == 1) {
                    return false
                }
            }
        }
        return true
    }

    fun isPawnUnderAttack(game: Game, pos: Position, oppoPoss: List<Position>, piece: Piece): Boolean {
        for (oppos in oppoPoss) {
            // Diagonal captures (left and right)
            if (game.moveDiagonalBy(pos, true, piece, MoveType.CAPTURE) != null &&
                oppos.row == pos.row - 1 && oppos.column == pos.column - 1
            ) {
                return true
            }
            if (game.moveDiagonalBy(pos, false, piece, MoveType.CAPTURE) != null &&
                oppos.row == pos.row - 1 && oppos.column == pos.column + 1
            ) {
                return true
            }

            // En Passant checks (left and right)
            if (game.moveDiagonalBy(pos, true, piece, MoveType.EN_PASSANT) != null &&
                oppos.row == pos.row - 1 && oppos.column == pos.column - 1
            ) {
                return true
            }
            if (game.moveDiagonalBy(pos, false, piece, MoveType.EN_PASSANT) != null &&
                oppos.row == pos.row - 1 && oppos.column == pos.column + 1
            ) {
                return true
            }
        }
        return false
    }

    fun makeMove(game: Game): Move? {
        val availableMoves = game.moves(piece)
        if (availableMoves.isEmpty())
            return null

        var bestMove: Move? = null
        val depth = if (game.board.positionOf(Piece.WHITE).size <= 4) 6 else 4 // Dynamic depth adjustment

        val startTime = System.currentTimeMillis()
        val timeLimit = 5000L // 5 seconds
        var bestEval = if (piece == Piece.WHITE) Int.MIN_VALUE else Int.MAX_VALUE

        for (depthIter in 1..10) { // Iterative deepening
            if (System.currentTimeMillis() - startTime > timeLimit) break

            for (move in availableMoves) {
                game.applyMove(move)
                val eval = minimax2(game, depthIter, Int.MIN_VALUE, Int.MAX_VALUE, piece != Piece.WHITE)
                game.unapplyMove()

                if ((piece == Piece.WHITE && eval > bestEval) || (piece == Piece.BLACK && eval < bestEval)) {
                    bestMove = move
                    bestEval = eval
                }
            }
        }

        if (bestMove != null) game.applyMove(bestMove)
        return bestMove
    }

    private val mapOfScore = mapOf(
        0 to 0,
        1 to 10,
        2 to 50,
        3 to 100,
        4 to 200,
        5 to 400,
        6 to 800,
        7 to 100000
    )

    fun evaluateBoard(game: Game): Int {
        var score = 0

        val whitePieces = game.board.positionOf(Piece.WHITE)
        val blackPieces = game.board.positionOf(Piece.BLACK)

        // Piece count
        score += whitePieces.size * 500
        score -= blackPieces.size * 500

        // Pawn-related evaluation
        for (pos in whitePieces) {
            // Proximity to promotion
            score += mapOfScore[pos.row] ?: 0

            // Passed pawn
            if (isPassedPawn(pos, game, Piece.WHITE)) {
                val distanceToPromotion = 7 - pos.row
                score += 10000 / (distanceToPromotion + 1) // Reward closer passed pawns more
            }

            // Penalize doubled pawns
            if (isDoubledPawn(pos, game, Piece.WHITE)) {
                score -= 500
            }

            // Penalize isolated pawns
            if (isIsolatedPawn(pos, game, Piece.WHITE)) {
                score -= 1000
            }

            // Pawn chains
            if (isChained(game, pos, Piece.WHITE)) {
                score += 800
            }

            // Central control
            if (pos.row in 3..4) {
                score += 30 // Increase reward for central pawns
            }

            // Penalize pawns under attack
            if (isPawnUnderAttack(game, pos, blackPieces, Piece.BLACK)) {
                score -= 2000
            }
        }

        for (pos in blackPieces) {
            // Proximity to promotion
            score -= mapOfScore[7 - pos.row] ?: 0

            // Passed pawn
            if (isPassedPawn(pos, game, Piece.BLACK)) {
                val distanceToPromotion = pos.row
                score -= 10000 / (distanceToPromotion + 1) // Reward closer passed pawns more
            }

            // Penalize doubled pawns
            if (isDoubledPawn(pos, game, Piece.BLACK)) {
                score += 500
            }

            // Penalize isolated pawns
            if (isIsolatedPawn(pos, game, Piece.BLACK)) {
                score += 1000
            }

            // Pawn chains
            if (isChained(game, pos, Piece.BLACK)) {
                score -= 800
            }

            // Central control
            if (pos.row in 3..4) {
                score -= 30 // Increase penalty for opponent's central pawns
            }

            // Penalize pawns under attack
            if (isPawnUnderAttack(game, pos, whitePieces, Piece.WHITE)) {
                score += 2000
            }
        }

        return score
    }

    fun isDoubledPawn(pos: Position, game: Game, piece: Piece): Boolean {
        val columnPawns = game.board.positionOf(piece).filter { it.column == pos.column }
        return columnPawns.size > 1
    }

    // Helper to check if a pawn is isolated
    fun isIsolatedPawn(pos: Position, game: Game, piece: Piece): Boolean {
        val adjacentColumns = listOf(pos.column - 1, pos.column + 1)
        return game.board.positionOf(piece).none { it.column in adjacentColumns }
    }

    fun isChained(game: Game, pos: Position, piece: Piece): Boolean {
        // which piece pawn
        val y = if (piece == Piece.WHITE) -1 else 1
        return (game.board.isPiece(pos.row + y, pos.column + 1, piece)) ||
                (game.board.isPiece(pos.row + y, pos.column - 1, piece))
    }

    // Quiescence Search
    fun quiescenceSearch(game: Game, alpha: Int, beta: Int, maximizing: Boolean): Int {
        val standPat = evaluateBoard(game)
        if (maximizing) {
            if (standPat >= beta) return beta
            var currAlpha = maxOf(alpha, standPat)
            val captures = game.moves(if (maximizing) Piece.WHITE else Piece.BLACK).filter { it.type == MoveType.CAPTURE }
            for (move in captures) {
                game.applyMove(move)
                val eval = -quiescenceSearch(game, -currAlpha, -beta, !maximizing)
                game.unapplyMove()
                if (eval >= beta) return beta
                currAlpha = maxOf(currAlpha, eval)
            }
            return currAlpha
        } else {
            if (standPat <= alpha) return alpha
            var currBeta = minOf(beta, standPat)
            val captures = game.moves(if (maximizing) Piece.WHITE else Piece.BLACK).filter { it.type == MoveType.CAPTURE }
            for (move in captures) {
                game.applyMove(move)
                val eval = -quiescenceSearch(game, -beta, -currBeta, !maximizing)
                game.unapplyMove()
                if (eval <= alpha) return alpha
                currBeta = minOf(currBeta, eval)
            }
            return currBeta
        }
    }

    // Minimax with Alpha-Beta Pruning
    fun minimax2(game: Game, depth: Int, alpha: Int, beta: Int, maximizing: Boolean): Int {
        val currPiece = if (maximizing) Piece.WHITE else Piece.BLACK
        val hash = game.zobristHasher.computeHash(game, currPiece)

        // Skip reevaluating already visited positions
        game.transpositionTable[hash]?.let { return it }

        if (depth == 0 || game.over()) {
            val eval = evaluateBoard(game)
            game.transpositionTable[hash] = eval
            return eval
        }

        var currAlpha = alpha
        var currBeta = beta

        if (maximizing) {
            val availableMoves = game.moves(Piece.WHITE)
            val sortedMoves = availableMoves.sortedByDescending { move ->
                when {
                    move.to.row == if (currPiece == Piece.WHITE) 7 else 0 -> 10000
                    isPassedPawn(move.to, game, currPiece) -> 3000
                    move.type == MoveType.CAPTURE || move.type == MoveType.EN_PASSANT -> 2000
                    isPawnUnderAttack(game, move.to, game.board.positionOf(currPiece.getOps()), currPiece.getOps()) -> -500
                    else -> 100
                }
            }

            var maxEval = Int.MIN_VALUE
            for (move in sortedMoves) {
                game.applyMove(move)
                val eval = minimax2(game, depth - 1, currAlpha, currBeta, false)
                game.unapplyMove()
                if (eval > maxEval)
                    maxEval = eval
                currAlpha = maxOf(currAlpha, eval)
                if (currBeta <= currAlpha)
                    break
            }
            game.transpositionTable[hash] = maxEval
            return maxEval
        } else {
            val availableMoves = game.moves(Piece.BLACK)
            val sortedMoves = availableMoves.sortedByDescending { move ->
                when {
                    move.to.row == if (currPiece == Piece.WHITE) 7 else 0 -> 10000
                    isPassedPawn(move.to, game, currPiece) -> 3000
                    move.type == MoveType.CAPTURE || move.type == MoveType.EN_PASSANT -> 2000
                    isPawnUnderAttack(game, move.to, game.board.positionOf(currPiece.getOps()), currPiece.getOps()) -> -500
                    else -> 100
                }
            }

            var minEval = Int.MAX_VALUE
            for (move in sortedMoves) {
                game.applyMove(move)
                val eval = minimax2(game, depth - 1, currAlpha, currBeta, true)
                game.unapplyMove()
                if (eval < minEval)
                    minEval = eval
                currBeta = minOf(currBeta, eval)
                if (currBeta <= currAlpha)
                    break
            }
            game.transpositionTable[hash] = minEval
            return minEval
        }
    }
}

class ZobristHasher {
    private val zobristTable: Array<Array<LongArray>> = Array(8) { Array(8) { LongArray(2) } }
    private val random = java.util.Random()
    private val turnHash: Long = random.nextLong() // Random value for turn tracking

    init {
        // Initialize Zobrist table with random values for each position and piece type
        for (row in zobristTable.indices) {
            for (col in zobristTable[row].indices) {
                zobristTable[row][col][0] = random.nextLong() // White pawn
                zobristTable[row][col][1] = random.nextLong() // Black pawn
            }
        }
    }

    fun computeHash(game: Game, currPiece: Piece): Long {
        var hash = 0L
        var colorIndex = 0
        // Include each pawn's position in the hash
        for (pawn in game.board.positionOf(Piece.WHITE)) {
            val row = pawn.row
            val col = pawn.column
            hash = hash xor zobristTable[row][col][colorIndex] // Accessing a single Long value
        }

        colorIndex = 1
        for (pawn in game.board.positionOf(Piece.BLACK)) {
            val row = pawn.row
            val column = pawn.column
            hash = hash xor zobristTable[row][column][colorIndex]
        }

        // Include the turn (White or Black) in the hash
        if (currPiece == Piece.BLACK) {
            hash = hash xor turnHash
        }

        return hash
    }
}


//class Player(val piece: Piece, var opponent: Player? = null) {
//    fun getAllPawns(game: Game): List<Position> {
//        return game.board.positionOf(piece)
//    }
//
//    fun getAllValidMoves(game: Game): List<Move> {
//        return game.moves(piece)
//    }
//
//    fun isPassedPawn(pos: Position, game: Game, piece: Piece): Boolean {
//        val oppoPawnPositions = game.board.positionOf(piece.getOps())
//        val delta = if (piece == Piece.WHITE) 1 else -1
//        for (opos in oppoPawnPositions) {
//            if (delta * (opos.row - pos.row) >= 1) {
//                if (abs(opos.column - pos.column) == 1) {
//                    return false
//                }
//            }
//        }
//        return true
//    }
//
//    fun isPawnUnderAttack(game: Game, pos: Position, oppoPoss: List<Position>, piece: Piece): Boolean {
//        for (oppos in oppoPoss) {
//            // Diagonal captures (left and right)
//            if (game.moveDiagonalBy(pos, true, piece, MoveType.CAPTURE) != null &&
//                oppos.row == pos.row - 1 && oppos.column == pos.column - 1
//            ) {
//                return true
//            }
//            if (game.moveDiagonalBy(pos, false, piece, MoveType.CAPTURE) != null &&
//                oppos.row == pos.row - 1 && oppos.column == pos.column + 1
//            ) {
//                return true
//            }
//
//            // En Passant checks (left and right)
//            if (game.moveDiagonalBy(pos, true, piece, MoveType.EN_PASSANT) != null &&
//                oppos.row == pos.row - 1 && oppos.column == pos.column - 1
//            ) {
//                return true
//            }
//            if (game.moveDiagonalBy(pos, false, piece, MoveType.EN_PASSANT) != null &&
//                oppos.row == pos.row - 1 && oppos.column == pos.column + 1
//            ) {
//                return true
//            }
//        }
//        return false
//    }
//
//
//    fun makeMove(game: Game): Move? {
//
//        val availableMoves = game.moves(piece)
//        if (availableMoves.isEmpty())
//            return null
//
//        var bestMove: Move? = null
//        if (piece == Piece.WHITE) {
//            // maximising
//            var maxEval = Int.MIN_VALUE
//            for (move in availableMoves) {
//                game.applyMove(move)
//                val eval = minimax2(game, 2, Int.MIN_VALUE, Int.MAX_VALUE, false)
//                println("move : $move eval : $eval")
//                println("______________________")
//                if (eval >= maxEval) {
//                    bestMove = move
//                    maxEval = eval
//                }
//                game.unapplyMove()
//            }
//        } else {
//            var minEval = Int.MAX_VALUE
//            for (move in availableMoves) {
//                game.applyMove(move)
//                val eval = minimax2(game, 2, Int.MIN_VALUE, Int.MAX_VALUE, true)
//                println("move : $move eval : $eval")
//                println("______________________")
//                if (eval <= minEval) {
//                    bestMove = move
//                    minEval = eval
//                }
//                game.unapplyMove()
//            }
//        }
//
//        if (bestMove != null)
//            game.applyMove(bestMove)
//
//        println("bestmove$bestMove")
//        println(game.over())
//
//        return bestMove
//
//    }
//
//    private val mapOfScore = mapOf(
//        0 to 0,
//        1 to 10,
//        2 to 50,
//        3 to 100,
//        4 to 200,
//        5 to 400,
//        6 to 800,
//        7 to 100000
//    )
//
//
//    fun evaluateBoard(game: Game): Int {
//        var score = 0
//
//        val whitePieces = game.board.positionOf(Piece.WHITE)
//        val blackPieces = game.board.positionOf(Piece.BLACK)
//
//        // Piece count
//        score += whitePieces.size * 500
//        score -= blackPieces.size * 500
//
//        // Pawn-related evaluation
//        for (pos in whitePieces) {
//            // Proximity to promotion
//            score += mapOfScore[pos.row] ?: 0
//
//            // Passed pawn
//            if (isPassedPawn(pos, game, Piece.WHITE)) {
//                val distanceToPromotion = 7 - pos.row
//                score += 10000 / (distanceToPromotion + 1) // Reward closer passed pawns more
//            }
//
//            // Penalize doubled pawns
//            if (isDoubledPawn(pos, game, Piece.WHITE)) {
//                score -= 500
//            }
//
//            // Penalize isolated pawns
//            if (isIsolatedPawn(pos, game, Piece.WHITE)) {
//                score -= 1000
//            }
//
//            // Pawn chains
//            if (isChained(game, pos, Piece.WHITE)) {
//                score += 2000
//            }
//
//            // Central control
//            if (pos.row in 3..4) {
//                score += 30 // Increase reward for central pawns
//            }
//
//            // Penalize pawns under attack
//            if (isPawnUnderAttack(game, pos, blackPieces, Piece.BLACK)) {
//                score -= 2000
//            }
//        }
//
//        for (pos in blackPieces) {
//            // Proximity to promotion
//            score -= mapOfScore[7 - pos.row] ?: 0
//
//            // Passed pawn
//            if (isPassedPawn(pos, game, Piece.BLACK)) {
//                val distanceToPromotion = pos.row
//                score -= 10000 / (distanceToPromotion + 1) // Reward closer passed pawns more
//            }
//
//            // Penalize doubled pawns
//            if (isDoubledPawn(pos, game, Piece.BLACK)) {
//                score += 500
//            }
//
//            // Penalize isolated pawns
//            if (isIsolatedPawn(pos, game, Piece.BLACK)) {
//                score += 1000
//            }
//
//            // Pawn chains
//            if (isChained(game, pos, Piece.BLACK)) {
//                score -= 2000
//            }
//
//            // Central control
//            if (pos.row in 3..4) {
//                score -= 30 // Increase penalty for opponent's central pawns
//            }
//
//            // Penalize pawns under attack
//            if (isPawnUnderAttack(game, pos, whitePieces, Piece.WHITE)) {
//                score += 2000
//            }
//        }
//
//        return score
//    }
//
//    // Helper to check if a pawn is doubled
//    fun isDoubledPawn(pos: Position, game: Game, piece: Piece): Boolean {
//        val columnPawns = game.board.positionOf(piece).filter { it.column == pos.column }
//        return columnPawns.size > 1
//    }
//
//    // Helper to check if a pawn is isolated
//    fun isIsolatedPawn(pos: Position, game: Game, piece: Piece): Boolean {
//        val adjacentColumns = listOf(pos.column - 1, pos.column + 1)
//        return game.board.positionOf(piece).none { it.column in adjacentColumns }
//    }
//
//
//    fun isChained(game: Game, pos: Position, piece: Piece): Boolean {
//        // which piece pawn
//        val y = if (piece == Piece.WHITE) -1 else 1
//        return (game.board.isPiece(pos.row + y, pos.column + 1, piece)) ||
//                (game.board.isPiece(pos.row + y, pos.column - 1, piece))
//    }
//
//
//    fun minimax2(game: Game, depth: Int, alpha: Int, beta: Int, maximizing: Boolean): Int {
//
//        val currPiece = if (maximizing) Piece.WHITE else Piece.BLACK
//
//        val hash = game.zobristHasher.computeHash(game, currPiece)
//        // skip reevaluating
//        game.transpositionTable[hash]?.let { return it }
//
//        if (depth == 0 || game.over()){
//            val eval =  evaluateBoard(game)
//            game.transpositionTable[hash] = eval
//            return eval
//        }
//
//
//        var currAlpha = alpha
//        var currBeta = beta
//
//
//        if (maximizing) {
//            // white piece turn
//            val availableMoves = game.moves(Piece.WHITE)
//            val sortedMoves = availableMoves.sortedByDescending { move ->
//                when {
//                    move.to.row == if (currPiece == Piece.WHITE) 7 else 0 -> 10000
//                    isPassedPawn(move.to, game, currPiece) -> 3000
//                    move.type == MoveType.CAPTURE || move.type == MoveType.EN_PASSANT -> 2000
//                    isPawnUnderAttack(game, move.to, game.board.positionOf(currPiece.getOps()), currPiece.getOps()) -> -500
//                    else -> 100
//                }
//            }
//            var maxEval = Int.MIN_VALUE
//            for (move in sortedMoves) {
//                game.applyMove(move)
//                val eval = minimax2(game, depth - 1, currAlpha, currBeta, false)
//                println(" depth $depth  maximise move : $move eval : $eval")
//                game.unapplyMove()
//                if (eval > maxEval)
//                    maxEval = eval
//                currAlpha = maxOf(currAlpha, eval)
//                if (currBeta <= currAlpha)
//                    break
//            }
//            game.transpositionTable[hash] = maxEval
//            return maxEval
//        } else {
//            val availableMoves = game.moves(Piece.BLACK)
//            val sortedMoves = availableMoves.sortedByDescending { move ->
//                when {
//                    move.to.row == if (currPiece == Piece.WHITE) 7 else 0 -> 10000
//                    isPassedPawn(move.to, game, currPiece) -> 3000
//                    move.type == MoveType.CAPTURE || move.type == MoveType.EN_PASSANT -> 2000
//                    isPawnUnderAttack(game, move.to, game.board.positionOf(currPiece.getOps()), currPiece.getOps()) -> -500
//                    else -> 100
//                }
//            }
//            // black piece turn
//            var minEval = Int.MAX_VALUE
//            for (move in sortedMoves) {
//                game.applyMove(move)
//                val eval = minimax2(game, depth - 1, currAlpha, currBeta, true)
//                println(" depth $depth  minimise move : $move eval : $eval")
//                game.unapplyMove()
//                if (eval < minEval)
//                    minEval = eval
//                currBeta = minOf(currBeta, eval)
//                if (currBeta <= currAlpha)
//                    break
//            }
//            game.transpositionTable[hash] = minEval
//            return minEval
//        }
//    }
//
//}
//
//class ZobristHasher {
//    private val zobristTable: Array<Array<LongArray>> = Array(8) { Array(8) { LongArray(2) } }
//    private val random = java.util.Random()
//    private val turnHash: Long = random.nextLong() // Random value for turn tracking
//
//    init {
//        // Initialize Zobrist table with random values for each position and piece type
//        for (row in zobristTable.indices) {
//            for (col in zobristTable[row].indices) {
//                zobristTable[row][col][0] = random.nextLong() // White pawn
//                zobristTable[row][col][1] = random.nextLong() // Black pawn
//            }
//        }
//    }
//
//    fun computeHash(game: Game, currPiece : Piece): Long {
//        var hash = 0L
//        var colorIndex = 0
//        // Include each pawn's position in the hash
//        for (pawn in game.board.positionOf(Piece.WHITE)) {
//            val row = pawn.row
//            val col = pawn.column
//            hash = hash xor zobristTable[row][col][colorIndex] // Accessing a single Long value
//        }
//
//        colorIndex = 1
//        for (pawn in game.board.positionOf(Piece.BLACK)){
//            val row = pawn.row
//            val column = pawn.column
//            hash = hash xor zobristTable[row][column][colorIndex]
//        }
//
//        // Include the turn (White or Black) in the hash
//        if (currPiece == Piece.BLACK) {
//            hash = hash xor turnHash
//        }
//
//        return hash
//    }
//}


