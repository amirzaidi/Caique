package v6.caique;

/**
 * Created by Kaan on 28/09/2016.
 */

public final class ChatManager
{
    public static String CheckChat() {
        ChatActivity CurrentChat = new ChatActivity();

        if (CurrentChat.InChat == true) {
            return CurrentChat.ChatID;
        }

        return null;
    }
}
