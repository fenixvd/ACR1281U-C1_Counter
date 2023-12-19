import javax.smartcardio.*;
import java.util.List;

public class CardReader {
    public static void main(String[] args) {
        byte[] responseData;
        StringBuilder sb;
        try {
            // Получить фабрику терминалов по умолчанию
            TerminalFactory factory = TerminalFactory.getDefault();

            // Получить все терминалы чтения карт
            List<CardTerminal> terminals = factory.terminals().list();

            // Проверить, есть ли терминалы
            if (terminals.isEmpty()) {
                System.out.println("Нет доступных терминалов.");
            } else {
                // Использовать первый терминал
                CardTerminal terminal = terminals.get(0);
                System.out.println("Используется терминал: " + terminal.getName());

                // Подключиться к устройству
                Card card = terminal.connect("*");
                CardChannel channel = card.getBasicChannel();

                // Отправить команду инициализации счетчика
                byte[] INIT_COMMAND = new byte[]{(byte)0xE1, 0x00, 0x00, 0x00, 0x00};
                CommandAPDU initCommand = new CommandAPDU(INIT_COMMAND);
                ResponseAPDU initResponse = channel.transmit(initCommand);

                // Проверить статус ошибки
                int sw = initResponse.getSW();
                if (sw != 0x9000) {
                    System.out.println("Ошибка инициализации: номер статуса = " + Integer.toHexString(sw));
                } else {
                    // Получить данные ответа
                    responseData = initResponse.getData();

                    // Преобразовать данные ответа в строку
                    sb = new StringBuilder();
                    for (byte b : responseData) {
                        sb.append(String.format("%02X ", b));
                    }

                    System.out.println("Ответ инициализации: " + sb.toString());
                }

                // Отправить команду чтения счетчика вставки карты
                byte[] READ_COMMAND = new byte[]{(byte)0xE0, 0x00, 0x00, 0x09, 0x00};
                CommandAPDU readCommand = new CommandAPDU(READ_COMMAND);
                ResponseAPDU readResponse = channel.transmit(readCommand);

                // Проверить статус
                sw = readResponse.getSW();
                if (sw != 0x9000) {
                    System.out.println("Ошибка чтения: номер статуса = " + Integer.toHexString(sw));
                } else {
                    // Получить данные ответа
                    responseData = readResponse.getData();

                    // Преобразовать данные ответа в строку
                    sb = new StringBuilder();
                    for (byte b : responseData) {
                        sb.append(String.format("%02X ", b));
                    }

                    System.out.println("Ответ чтения: " + sb.toString());
                }
            }
        } catch (CardException e) {
            System.out.println("Ошибка карты: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
