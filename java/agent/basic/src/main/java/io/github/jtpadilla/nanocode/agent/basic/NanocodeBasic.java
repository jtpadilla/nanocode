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
package io.github.jtpadilla.nanocode.agent.basic;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import io.github.jtpadilla.nanocode.agent.basic.agents.Assistant;
import io.github.jtpadilla.nanocode.agent.basic.tools.Tools;
import io.github.jtpadilla.nanocode.lib.config.Config;
import io.github.jtpadilla.nanocode.lib.format.Format;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * nanocode_basic - minimal CLI coding agent, powered by LangChain4j.
 * Original: https://github.com/1rgs/nanocode and https://github.com/maxandersen/nanocode and https://github.com/glaforge/nanocode
 */
public class NanocodeBasic {

    public static void main(String[] args) {

        if (Config.GEMINI_KEY == null || Config.GEMINI_KEY.isBlank()) {
            System.out.println(Format.RED + "Error: GOOGLE_AI_GEMINI_API_KEY or GEMINI_API_KEY environment variable is not set." + Format.RESET);
            return;
        }

        var cwd = System.getProperty("user.dir");
        System.out.println(Format.BOLD + "nanocode" + Format.RESET + " | " + Format.DIM + Config.MODEL_NAME + " (Google AI Gemini) | " + cwd + Format.RESET + "\n");

        var model = GoogleAiGeminiChatModel.builder()
                .apiKey(Config.GEMINI_KEY)
                .modelName(Config.MODEL_NAME)
                .sendThinking(true)
                .returnThinking(true)
                .build();

        var memory = MessageWindowChatMemory.withMaxMessages(20);
        var tools = new Tools(Format::preview);

        var assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(memory)
                .tools(tools)
                .build();

        var scanner = new Scanner(System.in);

        while (true) {
            try {
                System.out.println(Format.sep());
                System.out.print(Format.BOLD + Format.BLUE + "❯ " + Format.RESET + Format.YELLOW);
                System.out.flush();
                String input;
                try {
                    input = scanner.nextLine();
                } catch (NoSuchElementException e) {
                    break;
                }
                System.out.print(Format.RESET);
                System.out.flush();
                input = input.strip();
                System.out.println(Format.sep());

                if (input.isEmpty()) continue;
                if (input.equals("/q") || input.equals("exit")) break;
                if (input.equals("/c")) {
                    memory.clear();
                    System.out.println(Format.GREEN + "⏺ Cleared" + Format.RESET);
                    continue;
                }

                var response = assistant.chat(cwd, input);
                System.out.println("\n" + Format.CYAN + "⏺" + Format.RESET + " " + Format.markdown(response));
                System.out.println();
            } catch (Exception e) {
                System.out.println(Format.RED + "⏺ Error: " + e.getMessage() + Format.RESET);
            }

        }
    }
}
