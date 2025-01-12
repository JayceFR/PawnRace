package com.jaycefr.pawnrace

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun OnePlayer(
    boxWidthPx: Int,
    game: Game,
    colors: List<Color> = listOf(Color.Black, Color.White),
    highlightColor: Color = Color.Yellow,
    flipBoard: Boolean = true
) {
    val boxWidth = with(LocalDensity.current) { boxWidthPx.toDp() }
    val gameState = remember { mutableStateOf(game) }
    val validPositions = remember { mutableStateOf<List<Position>>(emptyList()) }
    val validMoves = remember { mutableStateOf<List<Move>>(emptyList()) }
    val isBotThinking = remember { mutableStateOf(false) }

    // Handle bot move when it's the bot's turn
    LaunchedEffect(isBotThinking.value) {
        if (isBotThinking.value) {
            withContext(Dispatchers.Default) {
                gameState.value.player.makeMove(gameState.value)
            }
            isBotThinking.value = false
        }
    }

    if (gameState.value.over()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Game OVER!! ${
                    when {
                        gameState.value.winner() == Piece.BLACK -> "BLACK"
                        else -> "WHITE"
                    }
                } WON",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                // Reset game state
                gameState.value = game.reset()
                validMoves.value = emptyList()
                validPositions.value = emptyList()
                isBotThinking.value = false
            }) {
                Text("Restart")
            }
        }
    } else {
        Box(
            Modifier
                .border(2.dp, Color(118, 150, 86, 89))
        ) {
            // Board UI
            Column {
                val rowRange = if (flipBoard) (7 downTo 0) else (0..7)
                val colRange = if (flipBoard) (7 downTo 0) else (0..7)

                for (x in rowRange) {
                    Row {
                        for (y in colRange) {
                            val position = Position("${'a' + y}${x + 1}")
                            Box(
                                modifier = Modifier
                                    .width(boxWidth)
                                    .height(boxWidth)
                                    .clickable(enabled = !isBotThinking.value) {
                                        println("Clicked on $x, $y")
                                        val currPiece = gameState.value.board.pieceAt(position)
                                        if (position in validPositions.value) {
                                            // Player makes their move
                                            val pos = validPositions.value.indexOf(position)
                                            gameState.value.applyMove(validMoves.value[pos])
                                            validPositions.value = emptyList()
                                            validMoves.value = emptyList()

                                            // Trigger bot's move after the player's move
                                            if (!gameState.value.over()) {
                                                isBotThinking.value = true
                                            }
                                        } else {
                                            // Highlight valid moves for the selected piece
                                            if (currPiece == gameState.value.player.piece) {
                                                val moves = gameState.value.validPawnAtPosMoves(
                                                    position,
                                                    gameState.value.player.piece
                                                )
                                                validMoves.value = moves
                                                validPositions.value = moves.map { it.to }
                                            } else {
                                                validPositions.value = emptyList()
                                                validMoves.value = emptyList()
                                            }
                                        }
                                    }
                                    .background(
                                        when {
                                            position in validPositions.value -> highlightColor
                                            else -> colors[((x + y) % 2)]
                                        }
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (gameState.value.board.isPiece(x, y, Piece.WHITE))
                                    WhitePawn(boxWidthPx)
                                if (gameState.value.board.isPiece(x, y, Piece.BLACK))
                                    BlackPawn(boxWidthPx)
                            }
                        }
                    }
                }
            }

            // "Bot Thinking" indicator in the top-right corner
            if (isBotThinking.value) {
                Box(
                    Modifier
                        .padding(16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    CircularProgressIndicator(color = highlightColor)
                }
            }
        }
    }
}






