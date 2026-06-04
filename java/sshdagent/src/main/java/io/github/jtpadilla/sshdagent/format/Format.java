/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.jtpadilla.sshdagent.format;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Colores ANSI y un renderer mínimo de markdown a ANSI, usados para pintar la
 * salida del shell SSH. Copia adaptada del {@code Format} de los agentes: el
 * separador toma la anchura por parámetro ({@link #sep(int)}) en lugar de
 * consultar un tty local con {@code tput}, porque en una sesión SSH la anchura
 * la da el PTY remoto, no el proceso del servidor.
 */
public class Format {

    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String DIM = "\033[2m";
    public static final String ITALIC = "\033[3m";
    public static final String BLUE = "\033[34m";
    public static final String CYAN = "\033[36m";
    public static final String GREEN = "\033[32m";
    public static final String RED = "\033[31m";
    public static final String YELLOW = "\033[93m";
    public static final String MAGENTA = "\033[35m";
    public static final String UNDERLINE = "\033[4m";
    public static final String STRIKE = "\033[9m";
    public static final String CODE_BG = "\033[37;40m";

    /**
     * Separador del ancho indicado (acotado a 80). La anchura se inyecta desde
     * fuera (p. ej. {@code terminal.getSize().getColumns()}) porque aquí no hay
     * un tty local que interrogar.
     */
    public static String sep(int cols) {
        return DIM + "─".repeat(Math.max(1, Math.min(cols, 80))) + RESET;
    }

    static public String preview(String s, int max) {
        if (s == null || s.isEmpty()) return "";
        var lines = s.split("\n");
        var p = lines[0].substring(0, Math.min(lines[0].length(), max));
        return lines.length > 1 ? p + " ... +" + (lines.length - 1) + " lines" : (lines[0].length() > max ? p + "..." : p);
    }

    public static String markdown(String md) {
        if (md == null) return "";
        var blocks = new ArrayList<String>();
        var m = Pattern.compile("(?s)```(\\w+)?\\n(.*?)\\n```").matcher(md);
        var sb = new StringBuilder();
        while (m.find()) {
            var lang = m.group(1) == null ? "" : m.group(1);
            var content = m.group(2);
            var formatted = (lang.isEmpty() ? "" : ITALIC + BOLD + lang + RESET + "\n") +
                            content.replaceAll("(?m)^", CODE_BG) + RESET + "\n";
            m.appendReplacement(sb, Matcher.quoteReplacement("%%BLOCK_CODE_" + blocks.size() + "%%"));
            blocks.add(formatted);
        }
        m.appendTail(sb);
        var res = sb.toString()
            .replaceAll("\\*\\*(.*?)\\*\\*", BOLD + "$1" + RESET)
            .replaceAll("\\*(.*?)\\*", ITALIC + "$1" + RESET)
            .replaceAll("__(.*?)__", UNDERLINE + "$1" + RESET)
            .replaceAll("~~(.*?)~~", STRIKE + "$1" + RESET)
            .replaceAll("(?m)^> ?(.*)", ITALIC + BLUE + BOLD + "> $1" + RESET)
            .replaceAll("(?m)^([\\d]+\\.|-|\\*) (.*)", MAGENTA + BOLD + "$1" + RESET + " $2")
            .replaceAll("(?m)^(#{1,6}) (.*)", CYAN + BOLD + "$1 $2" + RESET)
            .replaceAll("(?m)^(.*?\n={2,}\n)", CYAN + BOLD + "$1" + RESET)
            .replaceAll("(?m)^(.*?\n-{2,}\n)", CYAN + BOLD + "$1" + RESET)
            .replaceAll("!?\\[(.*?)]\\((.*?)\\)", BLUE + "$1" + RESET + " (" + BLUE + UNDERLINE + "$2" + RESET + ")");
        for (int i = 0; i < blocks.size(); i++)
            res = res.replace("%%BLOCK_CODE_" + i + "%%", blocks.get(i));
        return res;
    }
}
