package camp.coconut.demo.controller;

import camp.coconut.demo.model.ImageGenerationRequest;
import camp.coconut.demo.model.ImageGenerationResponse;
import camp.coconut.demo.service.ChatService;
import camp.coconut.demo.service.ImageGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ApiController {

    private final ChatService chatService;
    private final ImageGenerationService imageGenerationService;

    /**
     * This endpoint takes a prompt, and streams a response from the AI model and processes it into
     * server-sent events (SSE) for real-time updates.
     */
    @GetMapping("/prompt")
    public ResponseEntity<?> generateResponse(@RequestParam String prompt) {
        Flux<String> feedbackFlux = chatService.generateResponse(prompt);
        return ResponseEntity.ok(feedbackFlux
                .map(this::processFeedbackChunk)
                .concatWith(Flux.just(
                        ServerSentEvent.builder("[END_OF_STREAM]").event("end").build()))
                .doOnError(e -> log.error("Error streaming AI response", e))
                .doOnComplete(() -> log.info("AI response stream completed")));
    }

    /**
     * This endpoint takes a description, generates an image, and returns the resulting image URL.
     */
    @PostMapping("/generate-image")
    public ResponseEntity<?> generateImage(@RequestBody ImageGenerationRequest imageGenerationRequest) {
        Mono<String> imageUrl = imageGenerationService.generateImage(imageGenerationRequest.getDescription());
        return ResponseEntity.ok(imageUrl.map(ImageGenerationResponse::new));
    }

    /**
     * Processes a chunk of feedback text from the AI response, replacing leading spaces
     * and hyphens for better handling on the frontend.
     */
    private ServerSentEvent<String> processFeedbackChunk(String feedbackChunk) {
        // replace leading space with non-breaking space and hyphens with non-breaking hyphens
        if (feedbackChunk.startsWith(" ")) {
            feedbackChunk = "\u00A0" + feedbackChunk.substring(1);
        }
        feedbackChunk = feedbackChunk.replaceAll("-", "\u2011");
        return ServerSentEvent.builder(feedbackChunk).build();
    }
}
