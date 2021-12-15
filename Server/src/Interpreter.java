import Answers.Answer;
import Answers.ErrorAnswer;
import Answers.OkAnswer;
import Answers.Request;
import Commands.*;
import Data.SpaceMarine;
import Data.SpaceMarines;
import Data.User;
import Exceptions.CommandAlreadyExistsException;
import Exceptions.NotFoundCommandException;
import Exceptions.RightException;
import Exceptions.SameIdException;


import javax.sound.midi.Receiver;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Interpreter extends Thread {
    public static final Logger logger = Logger.getLogger(Receiver.class.getName());
    private FileHandler fileTxt;
    private Request request;
    private boolean hasNewPacket=false;
    private Sender sender;
    private DatagramSocket socket;
    private DatagramPacket da;

    public Interpreter(Sender sender,DatagramSocket socket) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, CommandAlreadyExistsException, SameIdException, RightException, IOException, SameIdException, ClassNotFoundException {
//        this.fileTxt=fileTxt;
//        logger.addHandler(fileTxt);

        CommandManager manager = CommandManager.getInstance("login", "pass");
        manager.initCommand(ExitCommand.class, "exit", "Выход, без сохранения");
        manager.initCommand(InfoCommand.class, "info", "Выводит информацию о коллекции");
        manager.initCommand(HelpCommand.class, "help", "Выводит справку по коммандам");
        manager.initCommand(ShowCommand.class, "show", "Выводит все элементы коллекции");
        manager.initCommand(AddCommand.class, "add", "Добавляет элемент в коллекцию", SpaceMarine.class);
        manager.initCommand(ClearCommand.class, "clear", "Очищает коллекцию");
        manager.initCommand(UpdateCommand.class, "update", "Обновляет значение элемента по id", String.class, SpaceMarine.class);
        manager.initCommand(InsertAtIndexCommand.class, "insert_at_index", "Обновляет значение элемента по индексу", String.class, SpaceMarine.class);
        manager.initCommand(RemoveByIdCommand.class, "remove_by_id", "Удаляет элемент с заданным id", String.class);
        manager.initCommand(RemoveAtIndexCommand.class, "remove_at_index", "Удаляет элемент с заданным id", String.class);
        manager.initCommand(SaveCommand.class, "save", "Сохраняет информацию в файл, использловать можно только на сервере");
        manager.initCommand(RemoveGreaterCommand.class, "remove_greater", "Удаляет из коллекции все элементы, превыщающий заданный", SpaceMarine.class);
        manager.initCommand(ExecuteScriptCommand.class, "execute_script", "Считывает и испольняет скрипт из файла", String.class);
        manager.initCommand(PrintFieldHeightCommand.class, "print_field_descending_height", "Выводит все значения поля height всех элементов в порядке убывания");
        manager.initCommand(FilterContainsNameCommand.class,"filter_contains_name","Выводит элементы, значение поля name которых содержит заданную подстроку",String.class);
        manager.initCommand(FilterLessThanWeaponTypeCommand.class,"filter_less_than_weapon_type","Выводит элементы, значение поля weaponType которых меньше заданного",String.class);
        manager.initCommand(AuthCommand.class, "auth", "Авторизует пользователя", User.class);
        manager.initCommand(RegisterCommand.class, "register", "Региструет пользователя", User.class);

        CommandManager.updateCollection();
        this.sender=sender;
        this.socket=socket;
    }

    public void putCommand(Request request,DatagramPacket da){
        this.request=request;
        this.hasNewPacket=true;
        this.da=da;
    }

    @Override
    public void run() {
        while (!isInterrupted()){
            if (hasNewPacket){
                interpret(request,da);
            }
        }
    }

    public void interpret(Request request, DatagramPacket da) {
        hasNewPacket=false;
        User user = request.getUser();
        Command cmd = request.getCommand();
        Object[] args = request.getArgs();
        try{
            sender.send(new OkAnswer(CommandManager.execute(user,cmd, args)),da.getAddress(),da.getPort());

        } catch (NullPointerException e){
            sender.send(new ErrorAnswer("Сервер не смог выполнить команду"), socket.getInetAddress(), socket.getPort());
            e.printStackTrace();
        }
    }

    public void askCommand(Scanner scanner) throws IOException {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                String name = CommandManager.parseName(line);

                Object[] args = CommandManager.parseArgs(line);
                Command command = CommandManager.getCommand(name);
                Object[] fillableArg = CommandManager.getFillableArgs(command, scanner);
                args = CommandManager.concatArgs(args, fillableArg);

                CommandManager.validate(command, args);
                logger.info(CommandManager.execute(new User("server", "server"), command, args));
                logger.info("Команда успешна провалидирована");
            } catch (NotFoundCommandException | IllegalArgumentException e) {
                logger.log(Level.SEVERE, "Error message", e);
            }
        }
    }
}
