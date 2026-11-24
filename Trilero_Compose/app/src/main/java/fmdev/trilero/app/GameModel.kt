package fmdev.trilero.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

class GameModel {

    enum class Result { NOT_PLAYING, WIN, LOSE }

    companion object {
        const val CUBILETES_INICIALES = 2
        const val CUBILETES_MAXIMOS = 9
    }

    // Número de cubiletes en la ronda actual (0 si no ha empezado el juego)
    var numeroCubiletes: Int by mutableStateOf(0)
        private set

    // Posición donde está la bola (1..numeroCubiletes)
    var position: Int by mutableStateOf(0)
        private set

    // Si hay una partida en curso
    var isPlaying: Boolean by mutableStateOf(false)
        private set

    // Si la partida ha sido ganada (llegó a 9 cubiletes y acertó)
    var partidaGanada: Boolean by mutableStateOf(false)
        private set

    // Si la partida ha terminado en derrota
    var partidaPerdida: Boolean by mutableStateOf(false)
        private set

    // Indica si el juego ya fue iniciado al menos una vez
    var juegoIniciado: Boolean by mutableStateOf(false)
        private set

    /**
     * Inicia una nueva partida desde cero:
     * - 2 cubiletes
     * - Bola en posición aleatoria entre 1 y 2
     */
    fun empezarPartida() {
        juegoIniciado = true
        numeroCubiletes = CUBILETES_INICIALES
        partidaGanada = false
        partidaPerdida = false
        position = Random.nextInt(1, numeroCubiletes + 1)
        isPlaying = true
    }

    /**
     * Procesa una jugada:
     * - Si acierta:
     *   - Si ya estamos en 9 cubiletes: ¡Partida ganada!
     *   - Si estamos en < 9: añade un cubilete y genera nueva posición.
     * - Si falla:
     *   - Partida perdida.
     */
    fun play(selected: Int): Result {
        if (!isPlaying) return Result.NOT_PLAYING

        return if (selected == position) {
            if (numeroCubiletes >= CUBILETES_MAXIMOS) {
                isPlaying = false
                partidaGanada = true
                Result.WIN
            } else {
                numeroCubiletes++
                position = Random.nextInt(1, numeroCubiletes + 1)
                Result.WIN
            }
        } else {
            isPlaying = false
            partidaPerdida = true
            Result.LOSE
        }
    }
}
