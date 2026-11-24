# Projecte: Desenvolupament de "Diari de Viatges / Crònica de Ruta" a Android Studio

# Objectiu:

Dissenyar i implementar una aplicació Android amb **Jetpack Compose** i arquitectura **MVVM (Model-View-ViewModel)** que permeti registrar rutes o escapades mitjançant un formulari complet, transferir dades entre pantalles i gestionar la persistència tant a l'**emmagatzematge intern** (privat de l'app) com a l'**emmagatzematge extern** (exportable i compartible per l'usuari).

# Fase 1: Arquitectura del Projecte i Model de Dades (MVVM)

L'aplicació s'ha d'estructurar mantenint la separació de responsabilitats (model immutable separat de la lògica de presentació):

1. **Model (**RouteEntry **i** TravelDiaryUiState**):**  
   * Crea una data class immutable (RouteEntry) per representar una ruta amb les propietats: id, routeName (String), destination (String), difficulty (Enum o String: *Fàcil, Moderada, Exigent*), estimatedBudget (Double), date (String), isSolo (Boolean) i notes (String).  
   * Defineix l'estat de la interfície (TravelDiaryUiState) que contingui els camps del formulari actual, els errors de validació i la llista de rutes guardades a l'històric.  
       
2. **ViewModel (**TravelDiaryViewModel**):**  
   * Gestiona l'estat reactiu utilitzant MutableStateFlow privat i StateFlow públic amb .asStateFlow(), actualitzant les propietats de manera immutable mitjançant .copy().  
   * Implementa la lògica de validació del formulari (per exemple, que el nom de la ruta i el destí no estiguin buits i el pressupost sigui vàlid) i els mètodes per desar i exportar les dades.  
     

## Fase 2: Requeriments Funcionals i Interfície (Jetpack Compose)

### 

### **1\. Pantalla 1: Formulari de Nova Ruta (**FormScreen**)**

Ha d'incloure els components següents amb validació visual en temps real:

* **Nom de la ruta** i **Destí:** Camps de text (OutlinedTextField) obligatoris amb missatge d'error si queden buits.  
* **Nivell de dificultat:** Selector d'opció única mitjançant FilterChips (*Fàcil*, *Moderada*, *Exigent*).  
* **Pressupost estimat:** Camp numèric (OutlinedTextField amb KeyboardType.Decimal).  
* **Data de la ruta:** Botó o camp interactiu que obri un DatePickerDialog de Material 3 per seleccionar la data.  
* **Viatge en solitari:** Component Switch acompanyat d'una etiqueta descriptiva (*"Ruta feta en solitari"*).  
* **Notes de l'experiència:** Camp de text multilínia per descriure el relat o punts d'interès de la jornada.  
* **Acció principal:** Botó **"Generar Postal / Crònica"** (desactivat si el formulari no és vàlid) i un accés per consultar l'històric de rutes desades.


##### **2\. Navegació i Pas de Dades (**PostalScreen**)**

* En prémer **"Generar Postal / Crònica"**, l'aplicació ha de navegar cap a la segona pantalla (PostalScreen) utilitzant **Navigation Compose**.  
* Les dades introduïdes al formulari s'han de passar a aquesta pantalla (mitjançant arguments de navegació, serialització JSON o compartint l'objecte seleccionat) i maquetar-se en format de **targeta visual de viatge** (mostrant la data, el destí destacat, una insignia segons la dificultat, el pressupost i el relat).


##### **3\. Emmagatzematge Intern (**filesDir **\- Històric Privat)**

* Cada vegada que es genera una crònica, la ruta s'ha de desar automàticament dins l'emmagatzematge intern privat de l'aplicació (context.filesDir) en un fitxer JSON (ex: diari\_viatges.json).  
* En tornar a obrir l'aplicació, el ViewModel ha de llegir aquest fitxer perquè l'usuari pugui consultar el seu històric personal de rutes en qualsevol moment.


##### **4\. Emmagatzematge Extern (*Storage Access Framework* \- Exportació)**

* A la pantalla de la postal (PostalScreen), afegeix un botó **"Exportar Crònica (.md / .txt)"**.  
* Fes servir el contracte natiu d'Android ActivityResultContracts.CreateDocument("text/plain") per obrir el selector de carpetes del sistema i permetre a l'usuari desar el relat formatat dins la carpeta **Documents** (o *Descàrregues*) del dispositiu.  
* *(Opcional)* Afegeix un botó **"Compartir"** utilitzant Intent.ACTION\_SEND per enviar el text de la crònica directament per WhatsApp o correu electrònic.


## Fase 3: Git, GitHub i Documentació

1. **Repositori:** Crea un repositori públic a GitHub anomenat Android-DiariViatges-Storage amb *commits* descriptius per a cada funcionalitat implementada.  
2. **Fitxer** README.md**:**  
   * Explicació de l'arquitectura **MVVM** aplicada al projecte i captura de pantalla de les dues vistes.  
   * **Comparativa tècnica:** Explica amb les teves paraules la diferència pràctica entre l'emmagatzematge intern (filesDir) i l'extern (*Storage Access Framework*) que has implementat, indicant què passa amb les dades de cada un si es desinstal·la l'app  
   * **Conceptes apresos:** Documenta els components nous utilitzats (FilterChip, DatePicker, CreateDocument, serialització JSON, etc.) i comenta'ls al codi font.  
     

## 

## Criteris d'Avaluació:

| Criteri | Descripció | Percentatge |
| :---- | :---- | :---- |
| **Formulari i Validació** | Ús correcte de tots els components (OutlinedTextField, FilterChip, DatePicker, Switch) i control d'errors. | **25%** |
| **Arquitectura MVVM i Navegació** | Separació correcta entre Model, ViewModel (StateFlow \+ .copy()) i Vistes, i pas de dades funcional entre pantalles.  | **25%** |
| **Storage Intern (**filesDir**)** | Lectura i escriptura persistent de l'històric de rutes en format JSON. | **20%** |
| **Storage Extern (Exportació)** | Creació correcta del fitxer .txt o .md a la carpeta *Documents* del dispositiu mitjançant *Storage Access Framework*. | **20%** |
| **Git, GitHub i README** | Historial de *commits* ordenat, codi comentat i explicació clara al README.md.  | **10%** |

#### 

#### **Lliurament**

Cal lliurar l'**enllaç al repositori de GitHub** a la plataforma virtual abans de la data límit indicada.