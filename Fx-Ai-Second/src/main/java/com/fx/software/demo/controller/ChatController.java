package com.fx.software.demo.controller;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChatController {

    @Autowired
    private OpenAiChatModel openAiChatModel;

//    public ChatController(OpenAiChatModel openAiChat){
//        this.openAiChatModel = openAiChat;
//    }
//
//    @GetMapping("/ai/generate")
//    public Map<String,String> generate(@RequestParam(value = "message",defaultValue = "背一首古诗") String massge){
//        return Map.of("generation",this.openAiChatModel.call(massge));
//    }


}
