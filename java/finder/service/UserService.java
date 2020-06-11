package finder.service;

import finder.model.User;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {


    List<User> allUsers = new ArrayList<>();


    //метод для сохранения пользователя
    public void addUser(User user)
    {
        this.allUsers.add(user);
    }

    //метод для поиска пользователя по чат айди
    public User findByChatId(Long chatId){

        User user = null;

        for (User us:allUsers
             ) {

            if (us.getChatId().equals(chatId)){
                user=us;
            }
        }
        return user;
    }


    //метод который обновляет пользователя (текущую позицию)
    public void updateUser(User user) {
        this.allUsers.add(user);

        for (User u: allUsers
             ) {
            if (u.getChatId().equals(user.getChatId())){
                u.setStateId(user.getStateId());
            }
        }

    }
}

