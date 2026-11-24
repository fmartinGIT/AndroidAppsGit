package fmdev.trilero.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fmdev.trilero.app.ui.theme.TrileroTheme
import kotlinx.coroutines.delay

// Ponlo a false para ocultar el texto de comprobación (posición de la bola)
private const val MOSTRAR_DEBUG = true

// Tiempos del juego
private const val TIEMPO_REVELADO_MS = 1500L   // cubilete levantado tras elegir
private const val SEGUNDOS_MEZCLA = 2          // espera antes de poder volver a elegir

class MainActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrileroTheme {
                TrileroGame()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrileroGame(
    gameModel: GameModel = remember { GameModel() }
) {
    var cubileteLevantado by remember { mutableStateOf<Int?>(null) }
    var resultadoLevantado by remember { mutableStateOf<GameModel.Result?>(null) }

    // Control de tiempo de espera para poder pulsar
    var sePuedePulsar by remember { mutableStateOf(false) }
    var segundosRestantes by remember { mutableStateOf(0) }
    var rondaId by remember { mutableStateOf(0) }

    // Cambia en cada partida nueva para que los cubiletes vuelvan a "aparecer" con animación
    var partidaId by remember { mutableStateOf(0) }

    // Texto con sombra más nítida para mejorar la lectura sobre el fondo
    val textStyleSombra = TextStyle(
        fontFamily = FontFamily(Font(R.font.hanalei)),
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(2f, 2f),
            blurRadius = 4f
        )
    )

    // Título según los aciertos (3 cubiletes = primer acierto). Con 2 no hay título.
    val tituloHonorifico = when (gameModel.numeroCubiletes) {
        3 -> "¡Buen ojo!"
        4 -> "¡Menuda chiripa!"
        5 -> "¡Buena racha!"
        6 -> "¡La suerte te sonríe!"
        7 -> "¡Imparable!"
        8 -> "¡Formidable!"
        9 -> "¡Última ronda! Todo o nada"
        else -> ""
    }

    // Efecto: el cubilete elegido queda levantado un momento y después viene la espera de "mezcla"
    LaunchedEffect(rondaId) {
        if (rondaId > 0 && gameModel.isPlaying) {
            sePuedePulsar = false
            if (cubileteLevantado != null) {
                delay(TIEMPO_REVELADO_MS)
            }
            cubileteLevantado = null
            resultadoLevantado = null

            for (i in SEGUNDOS_MEZCLA downTo 1) {
                segundosRestantes = i
                delay(1000)
            }
            segundosRestantes = 0
            sePuedePulsar = true
        } else {
            sePuedePulsar = false
            segundosRestantes = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.fondo),
                contentScale = ContentScale.Crop
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            val alturaTotal = maxHeight
            val alturaSuperior = alturaTotal * 0.25f
            val alturaCentral = alturaTotal * 0.58f
            val alturaInferior = alturaTotal * 0.17f

            Column(modifier = Modifier.fillMaxSize()) {

                // ZONA SUPERIOR - Título de bienvenida, o barra de niveles + mensaje
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(alturaSuperior)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = colorResource(R.color.panel_oscuro),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colorResource(R.color.golden).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (gameModel.juegoIniciado) {
                                // Barra de niveles (2 a 9 cubiletes)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (nivel in 2..GameModel.CUBILETES_MAXIMOS) {
                                        val alcanzado = gameModel.numeroCubiletes >= nivel
                                        Box(
                                            modifier = Modifier
                                                .size(width = 22.dp, height = 12.dp)
                                                .background(
                                                    color = if (alcanzado) colorResource(R.color.golden) else Color.White.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (alcanzado) Color.White else Color.White.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Mensaje único: resultado final o título de racha
                                Box(
                                    modifier = Modifier.heightIn(min = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        gameModel.partidaGanada -> Text(
                                            text = "¡Increíble! Le has ganado al trilero",
                                            style = textStyleSombra,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            color = colorResource(R.color.verde_victoria)
                                        )
                                        gameModel.partidaPerdida -> Text(
                                            text = "¡Ay, qué pena! Has perdido",
                                            style = textStyleSombra,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            textAlign = TextAlign.Center,
                                            color = colorResource(R.color.rojo_derrota)
                                        )
                                        gameModel.isPlaying && tituloHonorifico.isNotEmpty() -> Text(
                                            text = tituloHonorifico,
                                            style = textStyleSombra,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            textAlign = TextAlign.Center,
                                            color = colorResource(R.color.golden)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "¡El Trilero!",
                                    style = textStyleSombra,
                                    fontSize = 30.sp,
                                    color = colorResource(R.color.golden)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Acércate, acércate… ¿te atreves?",
                                    style = textStyleSombra,
                                    fontSize = 17.sp,
                                    textAlign = TextAlign.Center,
                                    color = colorResource(R.color.crema)
                                )
                            }
                        }
                    }
                }

                // ZONA CENTRAL - Cubiletes con animación de aparición
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(alturaCentral),
                    contentAlignment = Alignment.Center
                ) {
                    if (gameModel.numeroCubiletes > 0) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            itemVerticalAlignment = Alignment.CenterVertically
                        ) {
                            for (index in 0 until gameModel.numeroCubiletes) {
                                // La clave incluye la partida: al reiniciar, todos vuelven a animarse
                                key(partidaId, index) {
                                    val drawableMostrado = when {
                                        cubileteLevantado == index && resultadoLevantado == GameModel.Result.WIN ->
                                            R.drawable.levantaok
                                        cubileteLevantado == index && resultadoLevantado == GameModel.Result.LOSE ->
                                            R.drawable.levantafail
                                        else -> R.drawable.cubilete
                                    }

                                    // Escala sutil según se pueda pulsar o no
                                    val scaleAnim by animateFloatAsState(
                                        targetValue = if (sePuedePulsar) 1.0f else 0.95f,
                                        animationSpec = tween(durationMillis = 300),
                                        label = "scaleCubilete"
                                    )

                                    // Empieza en "no visible" y pasa a visible, así la animación sí se ejecuta
                                    val visibleState = remember {
                                        MutableTransitionState(false).apply { targetState = true }
                                    }

                                    // Los 2 iniciales entran escalonados; el nuevo espera a que baje el cubilete levantado
                                    val esNuevo = index >= GameModel.CUBILETES_INICIALES
                                    val retraso = if (esNuevo) TIEMPO_REVELADO_MS.toInt() else index * 200

                                    AnimatedVisibility(
                                        visibleState = visibleState,
                                        enter = fadeIn(animationSpec = tween(600, delayMillis = retraso)) +
                                                scaleIn(animationSpec = tween(600, delayMillis = retraso))
                                    ) {
                                        Image(
                                            painter = painterResource(id = drawableMostrado),
                                            contentDescription = stringResource(id = R.string.cubilete_n, index + 1),
                                            modifier = Modifier
                                                .size(90.dp)
                                                .scale(scaleAnim)
                                                .clickable(enabled = gameModel.isPlaying && sePuedePulsar) {
                                                    val res = gameModel.play(index + 1)
                                                    cubileteLevantado = index
                                                    resultadoLevantado = res

                                                    if (res == GameModel.Result.WIN && !gameModel.partidaGanada) {
                                                        rondaId++
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ZONA INFERIOR - Botón y mensaje de estado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(alturaInferior)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = colorResource(R.color.panel_oscuro),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colorResource(R.color.golden).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Button(
                                enabled = !gameModel.isPlaying,
                                onClick = {
                                    gameModel.empezarPartida()
                                    cubileteLevantado = null
                                    resultadoLevantado = null
                                    partidaId++
                                    rondaId++
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.golden),
                                    contentColor = Color.Black,
                                    disabledContainerColor = Color.White.copy(alpha = 0.15f),
                                    disabledContentColor = Color.White.copy(alpha = 0.55f)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 8.dp,
                                    pressedElevation = 2.dp
                                ),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        id = if (gameModel.juegoIniciado) R.string.bRestart else R.string.bStart
                                    ),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily(Font(R.font.hanalei))
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Línea de estado con altura mínima fija: el botón no se mueve al cambiar el texto
                            Box(
                                modifier = Modifier.heightIn(min = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val mensaje = when {
                                    gameModel.isPlaying && !sePuedePulsar && segundosRestantes > 0 ->
                                        "Mira bien, que se mueven… $segundosRestantes"
                                    gameModel.isPlaying && sePuedePulsar ->
                                        "¿Dónde está la bolita?"
                                    else -> ""
                                }
                                if (mensaje.isNotEmpty()) {
                                    Text(
                                        text = mensaje,
                                        style = textStyleSombra,
                                        fontSize = 17.sp,
                                        textAlign = TextAlign.Center,
                                        color = colorResource(R.color.golden)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Texto de comprobación: pequeño, en una esquina y sin el estilo del juego
        if (MOSTRAR_DEBUG && gameModel.isPlaying) {
            Text(
                text = "debug · bola: ${gameModel.position}",
                fontSize = 10.sp,
                color = colorResource(R.color.debug_texto),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrileroGamePreview() {
    TrileroTheme {
        TrileroGame(gameModel = GameModel())
    }
}