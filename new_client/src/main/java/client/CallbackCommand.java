package client;

/**
 * Интерфейс для получения ответных сообщений с сервера
 */
public interface CallbackCommand {
    void call(Object str);
}
