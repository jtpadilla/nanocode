# nanocode

Implementación en Java de un agente de codificación CLI minimalista, impulsado por [LangChain4j](https://github.com/langchain4j/langchain4j) y Google AI Gemini. El proyecto ofrece dos modos de operación: un agente simple (`basic`) y un sistema multi-agente con supervisor (`agentic`).

## Creditos

Este proyecto está basado en las ideas y el código de **Guillaume Laforge**:

> **[https://github.com/glaforge/nanocode](https://github.com/glaforge/nanocode)**

Que a su vez se apoya en el trabajo original de:

- [https://github.com/1rgs/nanocode](https://github.com/1rgs/nanocode)
- [https://github.com/maxandersen/nanocode](https://github.com/maxandersen/nanocode)

## Estructura del proyecto

```
nanocode/
├── java/
│   ├── basic/      # Agente único con memoria de conversación
│   ├── agentic/    # Sistema multi-agente con Supervisor
│   ├── config/     # Configuración compartida (API key, modelo)
│   ├── format/     # Utilidades de formato de salida en consola
│   └── result/     # Ejemplo: scraper de noticias UJI generado por el agente
├── MODULE.bazel    # Dependencias Maven gestionadas por Bazel
└── .bazelversion   # Bazel 8.6.0
```

## Módulos

### `//java/basic` — Agente simple

Agente conversacional con memoria de ventana (20 mensajes). Recibe instrucciones por teclado y ejecuta herramientas directamente.

**Herramientas disponibles:**

| Herramienta | Descripción |
|-------------|-------------|
| `read`      | Lee un fichero con números de línea |
| `write`     | Escribe contenido en un fichero |
| `edit`      | Sustituye texto en un fichero |
| `glob`      | Busca ficheros por patrón glob |
| `grep`      | Busca en ficheros por expresión regular |
| `bash`      | Ejecuta un comando de shell |
| `websearch` | Busca en la web via Google Search |
| `webfetch`  | Descarga y resume el contenido de una URL |

### `//java/agentic` — Sistema multi-agente con Supervisor

Implementa el patrón Supervisor + sub-agentes especializados usando `langchain4j-agentic`. El supervisor delega tareas al agente más adecuado según la naturaleza de la petición.

**Agentes especializados:**

| Agente          | Especialidad                                  |
|-----------------|-----------------------------------------------|
| `FileAgent`     | Operaciones de fichero: read, write, edit, glob, grep |
| `SystemAgent`   | Ejecución de comandos de shell (`bash`)       |
| `WebAgent`      | Búsqueda web y descarga de URLs               |

### `//java/result` — Ejemplo de uso

Código Java generado por el propio agente agentico en respuesta al prompt:

> _"Quiero que me generes el código Java para hacer un scraping de las noticias de la página www.uji.es y tienes que entregarme los resultados en una Lista de Records Java"_

El resultado (`Scraper`, `UjiScraper`, `Noticia`, `Main`) requirió únicamente una corrección menor.

## Requisitos

- Java 21+
- [Bazel](https://bazel.build/) 8.6.0 (`bazelisk` recomendado)
- API Key de Google AI Gemini

## Configuración

| Variable de entorno       | Descripción                                    |
|---------------------------|------------------------------------------------|
| `GOOGLE_AI_GEMINI_API_KEY` o `GEMINI_API_KEY` | **Obligatoria.** API key de Google AI Gemini |
| `MODEL`                   | Opcional. Nombre del modelo (por defecto: `gemini-3-flash-preview`) |

```bash
export GOOGLE_AI_GEMINI_API_KEY=your_api_key_here
# Opcional:
export MODEL=gemini-2.5-flash-preview-05-20
```

## Uso

### Modo básico

```bash
bazel run //java/basic
```

### Modo agentico

```bash
bazel run //java/agentic
```

Ambos modos arrancan un REPL interactivo:

```
nanocode | gemini-3-flash-preview (Google AI Gemini) | /ruta/actual

────────────────────────────────────────────────────────────────
❯ 
```

**Comandos especiales:**

| Comando | Acción |
|---------|--------|
| `/q` o `exit` | Salir |
| `/c` | Limpiar la memoria de conversación |

## Stack tecnológico

- **[LangChain4j](https://github.com/langchain4j/langchain4j) 1.13.0** — framework de IA para Java
- **`langchain4j-agentic` 1.13.0-beta23** — sistema multi-agente con supervisor
- **`langchain4j-google-ai-gemini`** — integración con Google AI Gemini
- **[jsoup](https://jsoup.org/) 1.18.3** — parsing de HTML
- **SLF4J 2.0.17** — logging
- **[Bazel](https://bazel.build/) 8.6.0** — sistema de build

## Licencia

[Apache License 2.0](LICENSE)