package finder.bot;

import finder.model.User;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import finder.service.UserService;

import java.io.InputStream;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class);


    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private final UserService userService;

    public ChatBot(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    //метод который описывает данные работы с кешем
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        final String text = update.getMessage().getText();
        final long chatId = update.getMessage().getChatId();


        User user = userService.findByChatId(chatId); //ищем пользователя по чат айди

        BotContext context;
        BotState state = BotState.getInitialState();
        ;

        if (user == null) {//если пользователь зашел впервые
            user = new User(chatId, state.ordinal()); // создаем нового пользователя
            userService.addUser(user);//сохраняем его
        }

        context = BotContext.of(this, user, text);//создаем контекст
        state = BotState.byId(user.getStateId());//создаем позицию

        state.handleInput(context);

        do {
            state = state.nextState(context);
            try {
                state.enter(context);
            } catch (NullPointerException n) {
                System.out.println("Its just finish!");
            }
        } while (!state.isInputNeeded());//цикл для перехода по позиям

        user.setStateId(state.ordinal());//изменяем позицию пользователя
        userService.updateUser(user);//обновляем пользователя
    }


}
