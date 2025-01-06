package camp.coconut.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;

    @Autowired
    public ChatService(@Qualifier("bedrockProxyChatModel") ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * Send a user-provided prompt to the AI model via {@link ChatClient},
     * and return a reactive stream of valid response chunks.
     */
    public Flux<String> generateResponse(String prompt) {
        return chatClient.prompt(prompt).stream().chatResponse()
                .filter(chatResponse -> chatResponse.getResult() != null)
                .map(response -> response.getResult().getOutput().getContent());
    }
}
