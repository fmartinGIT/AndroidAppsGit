# Treballar amb Git, GitHub, Android i varis projectes. IA Ollama OpenCode

# 1- Crear projecte VSCode Git i GitHub

Crear Projecte amb VSCode i afegir remote a GitHub (públic per a que puguin veure els profes)

# 2- Crear projecte Android Studio

Crear projecte des de Android Studio  sense Git a una carpeta del projecte VSCode

# 3- .gitignore

Incloure directoris que no volem al VCS

\*/.gradle/  
\*/app/build/  
\*/.idea/  
\*/gradle/  
\*/build/

# 4- Commits i sync

Sempre des de VSCode

# 5- OpenCode / Ollama

Instal·lar Ollama  
Instal·lar OpenCode

Instal·lar Model per programar lleuger: 

 \> ollama pull qwen2.5-coder:7b

Instal·lar model ollama per diàlegs  
   
 \> ollama pull llama3.2

Arrencar OpenCode desde terminal:

## 5.1 OpenCode gemma4, qwen2.5-coder

### 

### **1\. Crea la variant de qwen2.5-coder:7b amb 16K de context i temperatura baixa**

Executa aquestes tres línies a PowerShell (es crea a l'instant a partir del model que ja tens baixat):

PowerShell

```sh
[System.IO.File]::WriteAllLines("$PWD\Modelfile", @("FROM qwen2.5-coder:7b", "PARAMETER num_ctx 16384", "PARAMETER temperature 0.1"))
ollama create qwen25-opencode -f Modelfile
Remove-Item Modelfile
```

Aquestes tres ordres de PowerShell serveixen per **crear una versió personalitzada i optimitzada per a programació del model `qwen2.5-coder:7b` dins d'Ollama**, anomenada `qwen25-opencode`, sense deixar fitxers temporals al teu projecte.

A continuació tens el detall de què fa cada línia exactament:

1. **`[System.IO.File]::WriteAllLines("$PWD\Modelfile", @("FROM qwen2.5-coder:7b", "PARAMETER num_ctx 16384", "PARAMETER temperature 0.1"))`**  
   * Crea un fitxer de text anomenat `Modelfile` a la carpeta actual (`$PWD`) fent servir una funció nativa de .NET (que evita problemes de codificació BOM que de vegades dona `Out-File` a Windows).  
   * Dins hi escriu tres instruccions de configuració per a Ollama:  
     * `FROM qwen2.5-coder:7b`: Pren com a base el model original de 7.000 milions de paràmetres.  
     * `PARAMETER num_ctx 16384`: Amplia la finestra de memòria (context) dels 2048/4096 tokens per defecte fins a **16.384 tokens**, permetent-li llegir fitxers de codi llargs i el *system prompt* d'OpenCode sense perdre el fil.  
     * `PARAMETER temperature 0.1`: Baixa la creativitat al mínim (de 0.8 per defecte a 0.1) perquè el model sigui molt més determinista, precís i no s'inventi noms de funcions o sintaxi quan genera codi o JSON d'eines.  
2. **`ollama create qwen25-opencode -f Modelfile`**  
   * Llegeix la recepta del `Modelfile` que s'acaba de generar i registra el nou model personalitzat a la teva instància local d'Ollama amb el nom `qwen25-opencode` (això no duplica els 4,7 GB al disc, només crea un nou punter amb aquests paràmetres).  
3. **`Remove-Item Modelfile`**  
   * Esborra el fitxer temporal `Modelfile` de la carpeta del teu projecte un cop Ollama ja ha desat la configuració internament, deixant el teu directori de treball net.

### **2\. Afegeix-lo a opencode.jsonc (i deixa'l com a model per defecte)**

Obre el teu fitxer C:\\Users\\AlbertMallada\\.config\\opencode\\opencode.jsonc i substitueix el seu contingut per aquest, que manté gemma4-opencode i afegeix **qwen25-opencode** amb tools: true i els límits de context configurats:

Exemple amb 2 models: `qwen2.5-coder:7b i gemma4-opencode`

```json
{
  "$schema": "https://opencode.ai/config.json",
  "provider": {
    "ollama": {
      "npm": "@ai-sdk/openai-compatible",
      "name": "Ollama (Local)",
      "options": {
        "baseURL": "http://127.0.0.1:11434/v1"
      },
      "models": {
        "qwen2.5-coder:7b": {
          "name": "Qwen 2.5 Coder 7B (16K)",
          "tools": true,
          "limit": {
            "context": 16384,
            "output": 4096
          }
        },
        "gemma4-opencode": {
          "name": "Gemma 4 OpenCode (16K)",
          "tools": true,
          "limit": {
            "context": 16384,
            "output": 4096
          }
        }
      }
    }
  },
  "model": "ollama/qwen2.5-coder:7b"
}
```

### 

### **Com comprovar que ja escriu fitxers:**

1. Guarda opencode.jsonc i arrenca **opencode** des de la carpeta del teu projecte (D:\\python\\Android-IA\\RutesDeViatge).  
2. Verifica que a la part inferior apareix seleccionat **Qwen 2.5 Coder 7B (16K)** (també pots canviar entre Qwen i Gemma en qualsevol moment escrivint /models).  
3. Demana-li directament que et creï un fitxer (per exemple: *"Crea el fitxer Product.kt amb el model de dades per a Open Food Facts"*) i veuràs com executa l'eina write i guarda l'arxiu al disc.

