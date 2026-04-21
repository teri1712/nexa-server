package com.decade.nexa.users.api;

import com.decade.nexa.users.application.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserInfoTools {

    private static final String USER_ID = "user_id";
    private final UserRepository users;

    public Map<String, Object> prepareContext(UUID userId) {
        return Map.of(USER_ID, userId);
    }


    @Tool(description = "Get the current user information including name and gender", name = "get_current_user_info")
    public UserInfo userTool(ToolContext ctx) {
        UUID id = (UUID) ctx.getContext().get(USER_ID);
        return users.findById(id).map(user -> new UserInfo(user.getName(), user.getGender()))
            .orElseThrow();
    }


    @Tool(description = "Get pronounce of an user", name = "get_pronounce")
    public String pronounceTool(@ToolParam(description = "Gender of an user presented by double value") double gender) {
        if (gender == 1) {
            return "he/him";
        } else if (gender == 2) {
            return "she/her";
        } else {
            return "they/them";
        }
    }
}
