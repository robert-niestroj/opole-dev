package dev.opole.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ChatController {

  private final ChatClient chatClient;

  public ChatController(ChatClient.Builder builder) {
    this.chatClient = builder.defaultAdvisors(new SimpleLoggerAdvisor(1)).build();
  }

  @GetMapping("hello")
  public String hello() {
    return "Hello World!";
  }

  @GetMapping("chat")
  public String chat(@RequestParam String prompt) {
    return chatClient.prompt(prompt).call().content();
  }

  @GetMapping("chat-stream")
  public Flux<String> chatStream(@RequestParam String prompt) {
    return chatClient.prompt(prompt).stream().content();
  }

  @GetMapping("chat-response")
  public ChatResponse chatResponse(@RequestParam String prompt) {
    return chatClient.prompt(prompt).call().chatResponse();
  }

  @GetMapping("chat-system-prompt")
  public String chatSystemPrompt(@RequestParam String prompt) {
    return chatClient
        .prompt(prompt)
        .system(
            "You are a friendly worker at a travel agency. You answer only question related to travel.")
        .call()
        .content();
  }

  @GetMapping("chat-with-template")
  public String chatWithTemplate(@RequestParam String city, @RequestParam Integer days) {
    PromptTemplate promptTemplate = new PromptTemplate("Plan me a {days}-day trip to {city}");
    Prompt prompt = promptTemplate.create(Map.of("days", days, "city", city));
    return chatClient.prompt(prompt).call().content();
  }

  @GetMapping("chat-structured-output")
  public TripItinerary chatStructuredOutput(@RequestParam String city, @RequestParam Integer days) {
    PromptTemplate promptTemplate = new PromptTemplate("Plan me a {days}-day trip to {city}");
    Prompt prompt = promptTemplate.create(Map.of("days", days, "city", city));
    TripItinerary entity =
        chatClient.prompt(prompt).call().responseEntity(TripItinerary.class).entity();
    return entity;
  }

  public record TripItinerary(String city, String currency, String nearestAirport, Integer days, List<ItineraryDetails> details) {
    public record ItineraryDetails(Integer day, LocalTime hour, String place, String description) {}
  }
}
