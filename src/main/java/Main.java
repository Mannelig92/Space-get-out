import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Thread server = new Thread(Main::server, "Сервер");
        Thread client = new Thread(Main::client, "Клиент");

        server.start();
        client.start();
    }

    public static void server() {
        // Занимаем порт, определяя серверный сокет
        try {
            final ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress("localhost", 23334));
            while (true) {
                // Ждем подключения клиента и получаем потоки для дальнейшей работы
                try (SocketChannel socketChannel = serverChannel.accept()) {
                    // Определяем буфер для получения данных
                    final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);
                    while (socketChannel.isConnected()) {
                        // читаем данные из канала в буфер
                        int bytesCount = socketChannel.read(inputBuffer);
                        // если из потока читать нельзя, перестаем работать с этим клиентом
                        if (bytesCount == -1) break;
                        // получаем переданную от клиента строку в нужной кодировке и очищаем буфер
                        final String msg = new String(inputBuffer.array(), 0, bytesCount,
                                StandardCharsets.UTF_8);
                        inputBuffer.clear();
                        if (msg.equals("end")) return;
                        socketChannel.write(ByteBuffer.wrap(("Ваше сообщение без пробелов: \n" +
                                msg.replace(" ", "")).getBytes(StandardCharsets.UTF_8)));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void client() {
        // Определяем сокет сервера
        try {
            InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 23334);
            final SocketChannel socketChannel = SocketChannel.open();
            // подключаемся к серверу
            socketChannel.connect(socketAddress);
            // Получаем входящий и исходящий потоки информации
            Scanner scanner = new Scanner(System.in);
            // Определяем буфер для получения данных
            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);
            String msg;
            while (true) {
                System.out.println("Вводи слова для удаления пробелов или \"end\" для выхода");
                msg = scanner.nextLine();
                socketChannel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
                if ("end".equals(msg)) break;
                int bytesCount = socketChannel.read(inputBuffer);
                System.out.println(new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8).trim());
                inputBuffer.clear();
            }
            scanner.close();
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
