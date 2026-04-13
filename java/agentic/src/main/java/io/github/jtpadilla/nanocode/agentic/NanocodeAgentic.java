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
package io.github.jtpadilla.nanocode.agentic;

import java.util.NoSuchElementException;
import java.util.Scanner;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import io.github.jtpadilla.nanocode.agentic.agents.FileAgent;
import io.github.jtpadilla.nanocode.agentic.agents.SystemAgent;
import io.github.jtpadilla.nanocode.agentic.agents.WebAgent;
import io.github.jtpadilla.nanocode.agentic.tools.FileTools;
import io.github.jtpadilla.nanocode.agentic.tools.SystemTools;
import io.github.jtpadilla.nanocode.agentic.tools.WebTools;
import io.github.jtpadilla.nanocode.format.Format;


import static io.github.jtpadilla.nanocode.config.Config.GEMINI_KEY;
import static io.github.jtpadilla.nanocode.config.Config.MODEL_NAME;

/**
 * nanocode_agentic - multi-agent coding assistant, powered by LangChain4j.
 * Original: https://github.com/glaforge/nanocode
 */
public class NanocodeAgentic {

    public static void main(String[] args) {

        if (GEMINI_KEY == null || GEMINI_KEY.isBlank()) {
            System.out.println(Format.RED + "Error: GOOGLE_AI_GEMINI_API_KEY or GEMINI_API_KEY environment variable is not set." + Format.RESET);
            return;
        }

        var cwd = System.getProperty("user.dir");
        System.out.println(Format.BOLD + "nanocode" + Format.RESET + " | " + Format.DIM + MODEL_NAME + " (Agentic Supervisor) | " + cwd + Format.RESET + "\n");

        var model = GoogleAiGeminiChatModel.builder()
                .apiKey(GEMINI_KEY)
                .modelName(MODEL_NAME)
                .sendThinking(true)
                .returnThinking(true)
                .build();

        // Build specialized AI agents
        var fileAgent = AgenticServices.agentBuilder(FileAgent.class)
                .chatModel(model)
                .tools(new FileTools(Format::preview))
                .build();

        var systemAgent = AgenticServices.agentBuilder(SystemAgent.class)
                .chatModel(model)
                .tools(new SystemTools(Format::preview))
                .build();

        var webAgent = AgenticServices.agentBuilder(WebAgent.class)
                .chatModel(model)
                .tools(new WebTools())
                .build();

        // Orchestrate with Supervisor
        SupervisorAgent supervisor = AgenticServices.supervisorBuilder()
                .chatModel(model)
                .subAgents(fileAgent, systemAgent, webAgent)
                .responseStrategy(SupervisorResponseStrategy.SUMMARY)
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
                    ((AgenticScopeAccess) supervisor).evictAgenticScope("default");
                    System.out.println(Format.GREEN + "⏺ Cleared" + Format.RESET);
                    continue;
                }

                var response = supervisor.invoke(input);
                if (response != null && !response.isBlank()) {
                    System.out.println("\n" + Format.CYAN + "⏺" + Format.RESET + " " + Format.markdown(response));
                }
                System.out.println();
            } catch (Exception e) {
                System.out.println(Format.RED + "⏺ Error: " + e.getMessage() + Format.RESET);
            }
        }

    }

}
