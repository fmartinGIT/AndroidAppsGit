# Storage Intern Extern

Aquest manual tècnic detalla el funcionament, les diferències i la implementació pràctica de l'**emmagatzematge intern** i l'**emmagatzematge extern** a Android modern (API 29+ / Scoped Storage) utilitzant Kotlin i Jetpack Compose.

## **1\. Visió General i Diferències Clau**

Android divideix l'espai d'emmagatzematge del dispositiu en dues grans àrees per garantir la seguretat i la privadesa de les dades de l'usuari:

| Característica | Emmagatzematge Intern (Internal Storage) | Emmagatzematge Extern (External Storage) |
| :---- | :---- | :---- |
| **Privadesa** | **100% Privat.** Només la teva aplicació hi pot accedir. Ni l'usuari (sense root) ni altres apps poden veure els fitxers. | **Compartit / Públic.** Accessible per l'usuari (via app *Files*) i per altres aplicacions si tenen permís. |
| **Ruta habitual** | /data/data/\<nom.del.paquet\>/files/ | /storage/emulated/0/Documents/ (o *Downloads*, *Pictures*, etc.) |
| **Permisos necessaris** | **Cap.** No cal declarar res al AndroidManifest.xml. | **Cap** si s'utilitza *Storage Access Framework* (SAF) o *MediaStore* per a fitxers propis. |
| **Desinstal·lació** | Els fitxers **s'esborren automàticament** quan l'usuari desinstal·la l'app. | Els fitxers **persisteixen** al telèfon encara que l'aplicació s'esborri. |
| **Ús recomanat** | Configuracions internes, esborranys, memòria cau (*cache*), bases de dades locals, JSONs privats de l'app. | Exportació d'informes (.txt, .csv, .pdf), fotografies de l'usuari, còpies de seguretat manuals. |

## 

## **2\. Emmagatzematge Intern (*Internal Storage*)**

L'emmagatzematge intern està sempre disponible i és el lloc més segur per desar l'estat o l'històric privat de l'aplicació (per exemple, el fitxer diari\_viatges.json).

### **Directoris principals:**

* context.filesDir: Per a fitxers persistents que l'app necessita mantenir.  
* context.cacheDir: Per a fitxers temporals. Si el sistema es queda sense espai, Android pot esborrar aquest directori automàticament.


### **Implementació en Kotlin (Escriure i Llegir)**

Kotlin  
import android.content.Context  
import java.io.File

class InternalStorageManager(private val context: Context) {

    // 1\. Escriure text o JSON a l'emmagatzematge intern  
    fun savePrivateFile(fileName: String, content: String) {  
        // Es crea o sobreescriu el fitxer dins de /data/data/\<paquet\>/files/  
        val file \= File(context.filesDir, fileName)  
        file.writeText(content)  
    }

    // 2\. Llegir el contingut del fitxer  
    fun readPrivateFile(fileName: String): String? {  
        val file \= File(context.filesDir, fileName)  
        return if (file.exists()) {  
            file.readText()  
        } else {  
            null // El fitxer encara no s'ha creat  
        }  
    }

    // 3\. Esborrar el fitxer  
    fun deletePrivateFile(fileName: String): Boolean {  
        val file \= File(context.filesDir, fileName)  
        return if (file.exists()) file.delete() else false  
    }  
}

**Com inspeccionar-ho a Android Studio:** Obre la pestanya **Device Explorer** (a la barra lateral dreta d'Android Studio), navega a data $\rightarrow$ data $\rightarrow$ el.teu.paquet $\rightarrow$ files per veure els arxius creats en temps real.

## 

## **3\. Emmagatzematge Extern (*External Storage* i *Scoped Storage*)**

A partir d'Android 10 (API 29), Google va introduir el **Scoped Storage** (emmagatzematge delimitat). Ja no es permet escriure lliurement a qualsevol carpeta de la memòria del telèfon fent servir rutes absolutes (/sdcard/...), ni es fan servir els permisos antics WRITE\_EXTERNAL\_STORAGE.

Per exportar documents (.txt, .md, .csv, .pdf) a carpetes públiques com **Documents** o **Descàrregues**, la manera estàndard i recomanada és utilitzar el **Storage Access Framework (SAF)**.

### **Com funciona el *Storage Access Framework* (SAF)?**

En lloc de demanar permisos invasius, la teva app llança un selector natiu d'Android. L'usuari tria la carpeta i el nom del fitxer, i el sistema et retorna una Uri (un identificador segur) on la teva app pot escriure les dades.

### **Implementació en Jetpack Compose (**CreateDocument**)**

Kotlin  
import android.net.Uri  
import androidx.activity.compose.rememberLauncherForActivityResult  
import androidx.activity.result.contract.ActivityResultContracts  
import androidx.compose.material3.Button  
import androidx.compose.material3.Text  
import androidx.compose.runtime.Composable  
import androidx.compose.ui.platform.LocalContext

@Composable  
fun ExportDocumentButton(  
    defaultFileName: String \= "cronica\_viatge.txt",  
    contentToExport: String,  
    onExportSuccess: () \-\> Unit  
) {  
    val context \= LocalContext.current

    // 1\. Registrem el llançador del selector de fitxers d'Android  
    val createDocumentLauncher \= rememberLauncherForActivityResult(  
        contract \= ActivityResultContracts.CreateDocument("text/plain")  
    ) { uri: Uri? \-\>  
        // Aquest bloc s'executa quan l'usuari prem "Desar" al selector del sistema  
        uri?.let { destinationUri \-\>  
            try {  
                // 2\. Obrim un flux de sortida mitjançant el ContentResolver  
                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream \-\>  
                    outputStream.write(contentToExport.toByteArray())  
                }  
                onExportSuccess()  
            } catch (e: Exception) {  
                e.printStackTrace()  
            }  
        }  
    }

    // 3\. Botó a la UI que dispara el procés  
    Button(onClick \= {  
        // Obre el gestor de fitxers d'Android suggerint el nom per defecte  
        createDocumentLauncher.launch(defaultFileName)  
    }) {  
        Text("Exportar a Documents (.txt)")  
    }  
}

### 

### **Lectura de fitxers externs (**OpenDocument**)**

Si en algun moment l'usuari vol importar un fitxer des del telèfon cap a l'app, s'utilitza el contracte invers:

Kotlin  
val openDocumentLauncher \= rememberLauncherForActivityResult(  
    contract \= ActivityResultContracts.OpenDocument()  
) { uri: Uri? \-\>  
    uri?.let { sourceUri \-\>  
        val textLlegit \= context.contentResolver.openInputStream(sourceUri)?.bufferedReader()?.use {  
            it.readText()  
        }  
    }  
}  
// Per llançar-lo: openDocumentLauncher.launch(arrayOf("text/plain", "application/json"))

## 

## **4\. Integració amb l'Arquitectura MVVM**

Per mantenir una bona separació de responsabilitats (evitant posar lògica d'entrada/sortida de fitxers directament dins dels *Composables* o del ViewModel sense control):

1. **Capa de Dades (**StorageRepository**):** Crea una classe repositori que rebi el Context (o Application) i contingui els mètodes saveToInternal(...) i writeToUri(uri: Uri, content: String).  
2. **ViewModel (**TravelDiaryViewModel**):** Crida als mètodes del repositori dins d'una corrutina (viewModelScope.launch(Dispatchers.IO)) per no bloquejar el fil principal de la interfície d'usuari (*UI Thread*) mentre s'escriu al disc.  
3. **Vista (**Composable**):** Únicament s'encarrega de mantenir el rememberLauncherForActivityResult (ja que requereix el cicle de vida de l'Activity) i passar la Uri resultant al ViewModel perquè aquest executi l'escriptura.

## **5\. Resum de Bones Pràctiques**

* **Operacions al fil secundari:** Llegir o escriure fitxers grans pot alentir la pantalla. Fes servir sempre withContext(Dispatchers.IO) quan treballis amb fitxers des de corrutines.  
* **Tipus MIME correctes:** A CreateDocument("..."), indica el tipus de fitxer adequat ("text/plain" per a .txt/.md, "text/csv" per a fulls de càlcul, "application/json" per a dades estructurades).  
* **Tancament de fluxos:** Utilitza sempre la funció .use { ... } de Kotlin en obrir un OutputStream o InputStream; això garanteix que el fitxer es tanqui automàticament encara que es produeixi un error d'escriptura.

