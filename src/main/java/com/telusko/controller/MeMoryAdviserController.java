package com.telusko.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class MeMoryAdviserController {
    private static final String DEFAULT_CONVERSATION_ID = "telusko-chat";

    private OpenAiChatModel chatModel;
    private ChatClient chatClient;
    @Autowired

    private EmbeddingModel embeddingModel;

    ChatMemory chatMemory = MessageWindowChatMemory.builder().build();

    public MeMoryAdviserController(ChatClient.Builder builder){
        this.chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build())
                .build();

    }

    // APi implemenattion

    @GetMapping("/api/{message}")
    public ResponseEntity<String> getChat(
            @PathVariable String message,
            @RequestParam(required = false) String conversationId){

         ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, resolveConversationId(conversationId)))
                .call()
                .chatResponse();

        String response =   chatResponse
                .getResult()
                .getOutput()
                .getText();

        System.out.println(chatResponse.getMetadata().getModel());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommend")
    public String recommend(
            @RequestParam String type,
            @RequestParam String year,
            @RequestParam String lang,
            @RequestParam(required = false) String conversationId){


        String temp = "  I want to watch a {type} movie tonight with good rating,\\s\n" +
                "                               looking  for movies around this year {year}.\\s\n" +
                "                               The  language im looking for is {lang}.\n" +
                "                               Suggest one specific movie and tell me the cast and length of the movie.\n" +
                "                \n" +
                "                \n" +
                "                               response format should be:\n" +
                "                               1. Movie Name\n" +
                "                               2. basic plot\n" +
                "                               3. cast\n" +
                "                               4. length\n" +
                "                               5. IMDB rating\n" +
                "                \"\"\";\n" +
                "        PromptTemplate promptTemplate = new PromptTemplate(temp);\n" +
                "        Prompt prompt=promptTemplate.create(Map.of(\n" +
                "                \"type\", type,\n" +
                "                \"year\", year,\n" +
                "                \"lang\", lang\n" +
                "        ));";
        PromptTemplate promptTemplate = new PromptTemplate(temp);
        Prompt prompt = promptTemplate.create(Map.of(
                "type" , type ,
                "year" ,year ,
                "lang" ,lang
        ));
         String response = chatClient.prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, resolveConversationId(conversationId)))
                .call()
                .content();
         return response;
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return DEFAULT_CONVERSATION_ID;
        }
        return conversationId;
    }


    // Embeding Model  api

    @PostMapping("api/embeddings")
    public float[] embedding(@RequestParam String text){
        return  embeddingModel.embed(text);
    }
}
