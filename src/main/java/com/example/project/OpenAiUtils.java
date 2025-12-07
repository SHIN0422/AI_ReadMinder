package com.example.project;


import java.util.List;

public class OpenAiUtils {

    // [요청] 보낼 데이터 구조
    public static class ChatRequest {
        public String model;
        public List<Message> messages;
        public double temperature;

        public ChatRequest(String model, List<Message> messages) {
            this.model = model;
            this.messages = messages;
            this.temperature = 0.7; // 창의성 조절 (0.0 ~ 1.0)
        }
    }

    public static class Message {
        public String role; // "system", "user", "assistant"
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    // [응답] 받을 데이터 구조
    public static class ChatResponse {
        public List<Choice> choices;
    }

    public static class Choice {
        public Message message;
    }
}