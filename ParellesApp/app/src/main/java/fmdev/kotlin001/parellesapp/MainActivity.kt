package fmdev.kotlin001.parellesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import fmdev.kotlin001.parellesapp.ui.theme.ParellesAppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Modelo de datos para cada carta
data class CardModel(
    val id: Int,
    val faceDrawableRes: Int,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

// Estado general del juego
data class GameUiState(
    val cards: List<CardModel> = emptyList(),
    val backgroundRes: Int = R.drawable.fondo_azul,
    val selectedIndices: List<Int> = emptyList(),
    val pairsFound: Int = 0,
    val isProcessing: Boolean = false,
    val isGameOver: Boolean = false
)

// ViewModel que gestiona la lógica del juego de las parejas
class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var processJob: Job? = null

    // Las 4 caras disponibles (4 parejas = 8 cartas)
    private val faces = listOf(
        R.drawable.chiquito,
        R.drawable.eugenio,
        R.drawable.rubianes,
        R.drawable.faemino
    )

    init {
        resetGame()
    }

    fun resetGame() {
        processJob?.cancel()
        processJob = null

        // Crear las 8 cartas (2 de cada cara) y barajarlas
        val cardList = (faces + faces).shuffled().mapIndexed { index, drawableRes ->
            CardModel(id = index, faceDrawableRes = drawableRes)
        }

        _uiState.value = GameUiState(
            cards = cardList,
            backgroundRes = R.drawable.fondo_azul,
            selectedIndices = emptyList(),
            pairsFound = 0,
            isProcessing = false,
            isGameOver = false
        )
    }

    fun onCardClick(cardId: Int) {
        val currentState = _uiState.value
        if (currentState.isProcessing) return

        val clickedIndex = currentState.cards.indexOfFirst { it.id == cardId }
        if (clickedIndex == -1) return

        val clickedCard = currentState.cards[clickedIndex]
        if (clickedCard.isFaceUp || clickedCard.isMatched) return

        // Girar la carta seleccionada
        val updatedCards = currentState.cards.toMutableList()
        updatedCards[clickedIndex] = clickedCard.copy(isFaceUp = true)

        val newSelected = currentState.selectedIndices + clickedIndex

        if (newSelected.size == 1) {
            _uiState.value = currentState.copy(
                cards = updatedCards,
                selectedIndices = newSelected
            )
        } else if (newSelected.size == 2) {
            val firstIndex = newSelected[0]
            val secondIndex = newSelected[1]

            _uiState.value = currentState.copy(
                cards = updatedCards,
                selectedIndices = newSelected,
                isProcessing = true
            )

            val firstCard = updatedCards[firstIndex]
            val secondCard = updatedCards[secondIndex]

            processJob = viewModelScope.launch {
                if (firstCard.faceDrawableRes == secondCard.faceDrawableRes) {
                    // ¡Acierto!
                    val matchedCards = _uiState.value.cards.toMutableList()
                    matchedCards[firstIndex] = firstCard.copy(isMatched = true)
                    matchedCards[secondIndex] = secondCard.copy(isMatched = true)

                    val newPairsFound = _uiState.value.pairsFound + 1
                    val isVictory = newPairsFound == 4

                    if (isVictory) {
                        _uiState.value = _uiState.value.copy(
                            cards = matchedCards,
                            backgroundRes = R.drawable.fondo_verde,
                            selectedIndices = emptyList(),
                            pairsFound = newPairsFound,
                            isProcessing = false,
                            isGameOver = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            cards = matchedCards,
                            backgroundRes = R.drawable.fondo_amarillo,
                            selectedIndices = emptyList(),
                            pairsFound = newPairsFound
                        )
                        delay(1000)
                        _uiState.value = _uiState.value.copy(
                            backgroundRes = R.drawable.fondo_azul,
                            isProcessing = false
                        )
                    }
                } else {
                    // ¡Fallo!
                    _uiState.value = _uiState.value.copy(
                        backgroundRes = R.drawable.fondo_rojo
                    )
                    delay(1000)
                    val resetCards = _uiState.value.cards.toMutableList()
                    resetCards[firstIndex] = resetCards[firstIndex].copy(isFaceUp = false)
                    resetCards[secondIndex] = resetCards[secondIndex].copy(isFaceUp = false)

                    _uiState.value = _uiState.value.copy(
                        cards = resetCards,
                        backgroundRes = R.drawable.fondo_azul,
                        selectedIndices = emptyList(),
                        isProcessing = false
                    )
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParellesAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Imagen de fondo dinámica
        Image(
            painter = painterResource(id = uiState.backgroundRes),
            contentDescription = "Fondo de pantalla",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera superior con título, marcador y botón de reinicio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Juego de las Parejas",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parejas: ${uiState.pairsFound} / 4",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Button(
                            onClick = { viewModel.resetGame() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E88E5),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Reiniciar")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rejilla de cartas (2 columnas x 4 filas = 8 cartas)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(uiState.cards, key = { it.id }) { card ->
                    CardItem(
                        card = card,
                        isProcessing = uiState.isProcessing,
                        onCardClick = { viewModel.onCardClick(card.id) }
                    )
                }
            }
        }

        // Diálogo de victoria al encontrar las 4 parejas
        if (uiState.isGameOver) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(text = "¡Enhorabuena!", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(text = "¡Has encontrado las 4 parejas con éxito!")
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetGame() }) {
                        Text(text = "Jugar de nuevo")
                    }
                }
            )
        }
    }
}

// Componente para representar cada carta con animación de giro
@Composable
fun CardItem(
    card: CardModel,
    isProcessing: Boolean,
    onCardClick: () -> Unit
) {
    val isFaceUpOrMatched = card.isFaceUp || card.isMatched

    // Animación de rotación 3D en el eje Y
    val rotationY by animateFloatAsState(
        targetValue = if (isFaceUpOrMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlipAnimation"
    )

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 12f * density
            }
            .clickable(enabled = !isFaceUpOrMatched && !isProcessing) {
                onCardClick()
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (rotationY <= 90f) {
                // Reverso de la carta
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Reverso de la carta",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Cara de la carta (se gira 180° para evitar efecto espejo)
                Image(
                    painter = painterResource(id = card.faceDrawableRes),
                    contentDescription = "Cara de la carta",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            this.rotationY = 180f
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}