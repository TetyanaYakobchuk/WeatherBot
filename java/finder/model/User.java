package finder.model;
import java.util.*;


//класс который описывает пользователя, который пользуется ботом
public class User {

    private Long chatId;
    private Integer stateId;


    public User(Long chatId, Integer state) {
        this.chatId = chatId;
        this.stateId = state;

    }

    public Long getChatId() {
        return chatId;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getChatId(), user.getChatId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getChatId());
    }


}
