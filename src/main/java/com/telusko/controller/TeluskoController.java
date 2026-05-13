package com.telusko.controller;


import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TeluskoController {

    private OpenAiChatModel chatModel;
    private ChatClient chatClient;

   public TeluskoController(OpenAiChatModel chatModel){
//       this.chatModel = chatModel;

    this.chatClient = ChatClient.create(chatModel);

   }

   @GetMapping("api/answer/{message}")
   public ResponseEntity<String> getChat(@PathVariable  String message){
//        String response = chatClient.prompt(message)
//                .call()
//                .content();

        ChatResponse chatResponse = chatClient.prompt(message)
               .call()
               .chatResponse();

          String response =   chatResponse.getResult().getOutput().getText();

       System.out.println(chatResponse.getMetadata().getModel());
        return ResponseEntity.ok(response);
   }
}
