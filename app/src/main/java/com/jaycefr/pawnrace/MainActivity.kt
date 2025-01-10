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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        setContent {
            PawnRaceTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    board(
                        boxWidthPx = boxWidth,
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

@Composable
fun board(boxWidthPx : Int, colors : List<Color> = listOf(Color.Black, Color.White)){
    val boxWidth = with(LocalDensity.current) { boxWidthPx.toDp() }
    Column(
        Modifier.border(2.dp, Color(118, 150, 86, 89))
    ){
        for (x in 0..7) {
            Row(
            ){
                for (y in 0..7) {
                    Box(
                        modifier = Modifier
                            .width(boxWidth)
                            .height(boxWidth)
                            .clickable {
                                println("Clicked on $x, $y")
                            }
                            .background(colors[(x + y) % 2]),
                        contentAlignment = Alignment.Center,
                    ){
                        BlackPawn()
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
        contentDescription = "WhitePawn"
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