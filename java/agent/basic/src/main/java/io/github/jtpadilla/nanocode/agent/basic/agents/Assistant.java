package io.github.jtpadilla.nanocode.agent.basic.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface Assistant {
    @SystemMessage("Concise coding assistant. cwd: {{cwd}}")
    String chat(@V("cwd") String cwd, @UserMessage String userMessage);
}
