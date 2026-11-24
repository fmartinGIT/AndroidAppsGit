# Formularis Compose

Crear formularis en Android Studio és un dels patrons més habituals en el desenvolupament d'aplicacions. Utilitzant l'arquitectura recomanada (**MVVM \+ Jetpack Compose \+** StateFlow), aconseguim un formulari reactiu on la interfície actualitza l'estat automàticament i valida les dades en temps real.

### **1\. El Model d'Estat del Formulari (**LoginFormState**)**

Definim una data class immutable que conté els camps del formulari, les seves vistes, missatges d'error i si el formulari és vàlid.

Kotlin  
// LoginFormState.kt  
data class LoginFormState(  
    val email: String \= "",  
    val emailError: String? \= null,  
    val password: String \= "",  
    val passwordError: String? \= null,  
    val isFormValid: Boolean \= false,  
    val isLoading: Boolean \= false  
)

### 

### **2\. El** ViewModel **amb Lògica de Validació**

El ViewModel gestiona els canvis dels textfields, executa les comprovacions i actualitza l'estat exposat mitjançant StateFlow.

Kotlin  
// LoginViewModel.kt  
import androidx.lifecycle.ViewModel  
import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.StateFlow  
import kotlinx.coroutines.flow.asStateFlow  
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val \_uiState \= MutableStateFlow(LoginFormState())  
    val uiState: StateFlow\<LoginFormState\> \= \_uiState.asStateFlow()

    fun onEmailChanged(email: String) {  
        \_uiState.update { currentState \-\>  
            val error \= validateEmail(email)  
            val updatedState \= currentState.copy(email \= email, emailError \= error)  
            updatedState.copy(isFormValid \= isFormValid(updatedState))  
        }  
    }

    fun onPasswordChanged(password: String) {  
        \_uiState.update { currentState \-\>  
            val error \= validatePassword(password)  
            val updatedState \= currentState.copy(password \= password, passwordError \= error)  
            updatedState.copy(isFormValid \= isFormValid(updatedState))  
        }  
    }

    fun onSubmit() {  
        if (\_uiState.value.isFormValid) {  
            \_uiState.update { it.copy(isLoading \= true) }  
            // Aquí s'executaria la crida a l'API o base de dades  
        }  
    }

    // \--- REGLES DE VALIDACIÓ \---  
    private fun validateEmail(email: String): String? {  
        return when {  
            email.isEmpty() \-\> "El correu no pot estar buit"  
            \!android.util.Patterns.EMAIL\_ADDRESS.matcher(email).matches() \-\> "Format de correu no vàlid"  
            else \-\> null  
        }  
    }

    private fun validatePassword(password: String): String? {  
        return when {  
            password.isEmpty() \-\> "La contrasenya no pot estar buida"  
            password.length \< 6 \-\> "La contrasenya ha de tenir almenys 6 caràcters"  
            else \-\> null  
        }  
    }

    private fun isFormValid(state: LoginFormState): Boolean {  
        return state.emailError \== null &&   
               state.passwordError \== null &&   
               state.email.isNotEmpty() &&   
               state.password.isNotEmpty()  
    }  
}

### 

### **3\. La Interfície amb Jetpack Compose (**OutlinedTextField**)**

Creem la pantalla observant l'estat amb collectAsStateWithLifecycle(). Utilitzem OutlinedTextField per als camps d'entrada i gestionem els missatges d'error visuals.

Kotlin  
// LoginScreen.kt  
import androidx.compose.foundation.layout.\*  
import androidx.compose.foundation.text.KeyboardOptions  
import androidx.compose.material3.\*  
import androidx.compose.runtime.Composable  
import androidx.compose.runtime.getValue  
import androidx.compose.ui.Alignment  
import androidx.compose.ui.Modifier  
import androidx.compose.ui.text.input.KeyboardType  
import androidx.compose.ui.text.input.PasswordVisualTransformation  
import androidx.compose.ui.unit.dp  
import androidx.lifecycle.compose.collectAsStateWithLifecycle  
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable  
fun LoginScreen(  
    viewModel: LoginViewModel \= viewModel()  
) {  
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(  
        modifier \= Modifier  
            .fillMaxSize()  
            .padding(24\.dp),  
        verticalArrangement \= Arrangement.Center,  
        horizontalAlignment \= Alignment.CenterHorizontally  
    ) {  
        Text(  
            text \= "Inici de Sessió",  
            style \= MaterialTheme.typography.headlineMedium  
        )

        Spacer(modifier \= Modifier.height(24\.dp))

        // \--- CAMP EMAIL \---  
        OutlinedTextField(  
            value \= state.email,  
            onValueChange \= { viewModel.onEmailChanged(it) },  
            label \= { Text("Correu electrònic") },  
            isError \= state.emailError \!= null,  
            supportingText \= {  
                state.emailError?.let { Text(it, color \= MaterialTheme.colorScheme.error) }  
            },  
            keyboardOptions \= KeyboardOptions(keyboardType \= KeyboardType.Email),  
            singleLine \= true,  
            modifier \= Modifier.fillMaxWidth()  
        )

        Spacer(modifier \= Modifier.height(12\.dp))

        // \--- CAMP CONTRASENYA \---  
        OutlinedTextField(  
            value \= state.password,  
            onValueChange \= { viewModel.onPasswordChanged(it) },  
            label \= { Text("Contrasenya") },  
            isError \= state.passwordError \!= null,  
            supportingText \= {  
                state.passwordError?.let { Text(it, color \= MaterialTheme.colorScheme.error) }  
            },  
            visualTransformation \= PasswordVisualTransformation(),  
            keyboardOptions \= KeyboardOptions(keyboardType \= KeyboardType.Password),  
            singleLine \= true,  
            modifier \= Modifier.fillMaxWidth()  
        )

        Spacer(modifier \= Modifier.height(24\.dp))

        // \--- BOTÓ D'ENVIAMENT \---  
        Button(  
            onClick \= { viewModel.onSubmit() },  
            enabled \= state.isFormValid && \!state.isLoading,  
            modifier \= Modifier  
                .fillMaxWidth()  
                .height(50\.dp)  
        ) {  
            if (state.isLoading) {  
                CircularProgressIndicator(  
                    color \= MaterialTheme.colorScheme.onPrimary,  
                    modifier \= Modifier.size(24\.dp)  
                )  
            } else {  
                Text("Entrar")  
            }  
        }  
    }  
}

### 

### **Punts Clau d'Aquest Patró**

1. **Estat Únic de la UI (**LoginFormState**):** La vista reacciona a un sol objecte d'estat, evitant problemes de dessincronització entre la pantalla i la lògica.  
2. isError **i** supportingText**:** Propietats natives de OutlinedTextField que adapten el color del vora i mostren el text d'error automàticament quan el valor no és null.  
3. **Botó condicionat (**enabled**):** Es manté deshabilitat fins que totes les validacions són correctes, millorant l'experiència d'usuari (UX).

