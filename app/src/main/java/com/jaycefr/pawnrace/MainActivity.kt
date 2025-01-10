package com.jaycefr.pawnrace

import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jaycefr.pawnrace.ui.theme.PawnRaceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val displayMatrix = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMatrix)
        val boxWidth : Int = (displayMatrix.widthPixels - 100) / 8
        val board = Board(File(7), File(0))
        val whitePlayer = Player(Piece.WHITE)
        val blackPlayer = Player(Piece.BLACK, whitePlayer)
        whitePlayer.opponent = blackPlayer
        val game = Game(board, whitePlayer)
        val choice = mutableIntStateOf(-1)
        setContent {
            PawnRaceTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (choice.intValue == -1){
                        Text(
                            text = "Pawn Race",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { choice.intValue = 1}
                        ){
                            Text(text = "One Player")
                        }
                        Button(
                            onClick = { choice.intValue = 0 }
                        ){
                            Text(text = "Two Player")
                        }
                    }
                    else {
                        if (choice.intValue == 1)
                            OnePlayer(
                                boxWidthPx = boxWidth,
                                game = game,
                                colors = listOf(
                                    Color(118,150,86),
                                    Color(238,238,210)
                                )
                            )
                        else
                            TwoPlayer(
                                boxWidthPx = boxWidth,
                                game = game,
                                colors = listOf(
                                    Color(118,150,86),
                                    Color(238,238,210)
                                )
                            )
                    }
                }
            }
        }
    }
}


@Composable
fun BlackPawn(){
    Image(
        painter = painterResource(id = R.drawable.pawnblack),
        contentDescription = "BlackPawn",
        Modifier.scale(4f)
    )
}

@Composable
fun WhitePawn(){
    Image(
        painter = painterResource(id = R.drawable.pawnwhite),
        contentDescription = "WhitePawn",
        Modifier.scale(4f)
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PawnRaceTheme {
        Greeting("Android")
    }
}