package org.example;

import java.io.*;
import java.util.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс, представляющий собой телефонный номер.
 * Содержит два параметра:
 * type - тип номера(мобильный, домашний, факс и т.д.)
 * number - сам цифровой номер телефона
 */
class PhoneNumber {
    String type;
    String number;

    public PhoneNumber(String type, String number) {
        this.type = type;
        this.number = number;
    }
}

/**
 * Класс, представляющий собой контакт.
 * Содержит два параметра:
 * - fullName - ФИО контакта
 * - phoneNumbers - список телефонных номеров типа PhoneNumber контакта
 */
class Contact {
    String fullName;
    ArrayList<PhoneNumber> phoneNumbers;

    /**
     * Инициализация экземпляра контакта.
     * @param fullName ФИО контакта
     */
    public Contact(String fullName) {
        this.fullName = fullName;
        this.phoneNumbers = new ArrayList<>();
    }

    /**
     * Метод для добавления телефонного номера типа PhoneNumber.
     * @param type Тип номера
     * @param number Сам цифровой номер телефона
     */
    public void addPhoneNumber(String type, String number) {
        PhoneNumber phoneNumber = new PhoneNumber(type, number);
        phoneNumbers.add(phoneNumber);
    }
}

/**
 * Класс, представляющий собой базу данных.
 * Содержит параметр:
 * - fileName - адрес базы данных
 */
class Database {
    String fileName;
    ArrayList<Contact> database = new ArrayList<>();

    /**
     * Инициализация экземпляра базы данных: адрес базы данных, чтение базы данных, сортировка контактов.
     * @param fileName Адрес базы данных
     * @throws CorruptFileException Исключение, возникающее при ошибке чтения базы данных
     */
    public Database(String fileName) throws CorruptFileException {
        this.fileName = fileName;
        database = readRecordsFromFile();
        sortContacts();
    }

    /**
     * Метод для добавления в базу данных нового контакта.
     * @param contact Новый контакт
     * @return Введённые данные контакта корректны(true) или нет(false)
     */
    public boolean addContact(Contact contact) {
        Contact correctContact = new Contact(contact.fullName);
        for (PhoneNumber phoneNumber: contact.phoneNumbers) {
            if (
                    phoneNumber.number.chars().anyMatch(Character::isDigit)
                            && phoneNumber.number.chars().noneMatch(Character::isLetter)
            ) {
                correctContact.addPhoneNumber(phoneNumber.type, phoneNumber.number);
            }
            else {
                return false;
            }
        }
        database.add(correctContact);
        sortContacts();
        return true;
    }

    /**
     * Метод для записи отредактированного списка контактов в файл базы данных.
     */
    public void writeRecords() {
        try (DataOutputStream outStream = new DataOutputStream(new FileOutputStream(fileName))){
            outStream.writeInt(database.size());
            for (Contact contact: database) {
                outStream.writeUTF(contact.fullName);
                outStream.writeInt(contact.phoneNumbers.size());
                for (PhoneNumber pn: contact.phoneNumbers) {
                    outStream.writeUTF(pn.type);
                    outStream.writeUTF(pn.number);
                }
            }
        }
        catch (IOException e) {
            // Ошибка в записи файла
            e.printStackTrace();
        }
    }

    /**
     * Метод для считывания файла базы данных.
     * @return Список контактов
     * @throws CorruptFileException Исключение, возникающее при ошибке чтения базы данных
     */
    public ArrayList<Contact> readRecordsFromFile() throws CorruptFileException {
        ArrayList<Contact> databaseInFile = new ArrayList<>();

        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                // Ошибка при создании файла
                e.printStackTrace();
            }
        }
        else if (file.length() != 0) {
            try (DataInputStream inStream = new DataInputStream(new FileInputStream(fileName))){
                int contactsAmount = inStream.readInt();
                for(int c = 0; c < contactsAmount; c++) {
                    String name = inStream.readUTF();
                    Contact contact = new Contact(name);
                    int am = inStream.readInt();
                    for (int i = 0; i < am; i++) {
                        String type = inStream.readUTF();
                        String num = inStream.readUTF();
                        contact.addPhoneNumber(type, num);
                    }
                    databaseInFile.add(contact);
                }
            } catch (IOException e) {
                // Файл существует, но не был прочитан
                e.printStackTrace();
                throw new CorruptFileException(
                        "Возникла ошибка при чтении файла базы данных. Необходимо переименовать файл "
                                + fileName +
                                " или переместить его в другую директорию. " +
                                "При выполнении этих действий во время запуска программы " +
                                "будет создан новый файл базы данных " + fileName + "."
                );
            }
        }

        return databaseInFile;
    }

    /**
     * Метод для считывания списка контактов из памяти устройства.
     * @return Список контактов.
     */
    public ArrayList<Contact> readTemporaryRecords() {
        return database;
    }

    /**
     * Метод для сортировки списка контактов по имени.
     */
    public void sortContacts() {
        Collections.sort(database, Comparator.comparing(contact -> contact.fullName.toLowerCase()));
    }

    /**
     * Метод для преобразования списка контактов(всего или по поиску) в текст для вывода контактов в интерфейсе.
     * @param contacts Контакты, которые нужно вывести
     * @return Текстовый список контактов для вывода в интерфейсе
     */
    public StringBuilder selectedContactsToTextList(ArrayList<Contact> contacts) {
        StringBuilder text = new StringBuilder();
        for (Contact contact: contacts) {
            text.append("--------------------------------\n");

            if (!Objects.equals(contact.fullName.replace(" ", ""), "")) {
                text.append(contact.fullName);
            }
            else {
                text.append("Неизвестное название контакта");
            }

            text.append("\n");

            for (PhoneNumber phoneNumber: contact.phoneNumbers) {
                if (!Objects.equals(phoneNumber.type.replace(" ", ""), "")) {
                    text.append(phoneNumber.type);
                }
                else {
                    text.append("Неизвестный тип номера");
                }

                text.append(": ");

                if (!Objects.equals(phoneNumber.number.replace(" ", ""), "")) {
                    text.append(phoneNumber.number);
                }
                else {
                    text.append("Неизвестный номер");
                }

                text.append("\n");
            }
        }
        text.append("--------------------------------");
        return text;
    }

    /**
     * Метод для поиска контактов по ключу и возвращения найденных контактов в текстовом виде.
     * @param searchKey Ключ(часть имени контакта или номера)
     * @return Текстовый список найденных контактов для вывода в интерфейсе
     */
    public String searchContactsToTextList(String searchKey) {
        searchKey = searchKey.toLowerCase();
        ArrayList<Contact> foundContacts = new ArrayList<Contact>();

        for (Contact contact: database) {
            if (contact.fullName.toLowerCase().contains(searchKey)) {
                foundContacts.add(contact);
            }
            else {
                for (PhoneNumber phoneNumber: contact.phoneNumbers) {
                    if (phoneNumber.number.contains(searchKey)) {
                        foundContacts.add(contact);
                        break;
                    }
                }
            }
        }
        Collections.sort(foundContacts, Comparator.comparing(contact -> contact.fullName.toLowerCase()));

        StringBuilder text = new StringBuilder();
        text.append("Результат поиска: (для отображения всех контактов сотрите строку поиска)\n");
        text.append(selectedContactsToTextList(foundContacts));
        return text.toString();
    }

    // Возвращение списка контактов в виде текста

    /**
     * Метод для возвращения списка всех контактов в текстовом виде.
     * @return Текстовый список всех контактов для вывода в интерфейсе
     */
    public String databaseToTextList() {
        StringBuilder text = new StringBuilder();
        text.append("Все контакты:\n");
        text.append(selectedContactsToTextList(database));
        return text.toString();
    }
}

/**
 * Исключение, возникающее при чтении повреждённого или некорректного файла базы данных.
 */
class CorruptFileException extends Exception {
    public CorruptFileException(String message) {
        super(message);
    }
}

/**
 * Класс для обработки файла базы данных и представления графического интерфейса.
 */
public class App extends Application {
    // Экземпляр логгера
    private static final Logger logger = LogManager.getLogger(App.class);
    Database db;
    // Текстовое поле со списком контактов
    TextArea contactsTextArea;

    {
        try {
            db = new Database("db.bin");
            contactsTextArea = new TextArea(db.databaseToTextList());
            logger.info("Database db.bin was successfully read");
        } catch (CorruptFileException e) {
            showAlert("Ошибка", "Ошибка чтения базы данных", e.getMessage(), Alert.AlertType.ERROR);
            contactsTextArea = new TextArea("Возникла ошибка при чтении файла базы данных. " +
                    "Необходимо переименовать файл db.bin или переместить его в другую директорию. " +
                    "При выполнении этих действий во время следующего запуска программы будет создан новый файл " +
                    "базы данных db.bin."
            );
            logger.warn("Error reading database file db.bin");
        }
    }

    int fontSize = 12;
    CheckBox largerFontCheckBox = new CheckBox("Увеличить размер шрифта");

    /**
     * Метод для отображения уведомлений.
     * @param title Заголовок окна
     * @param headerText Заголовок уведомления
     * @param contentText Основной текст уведомления
     * @param alertType Тип уведомления
     */
    private void showAlert(String title, String headerText, String contentText, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    /**
     * Метод для отображения основного окна программы.
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages. (сгенерировано автоматически)
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Телефонный справочник");

        VBox vBox = new VBox();

        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));

        // Устанавливаем, чтобы VBox занимал всю доступную площадь окна
        VBox.setVgrow(contactsTextArea, Priority.ALWAYS);

        Label infoLabel = new Label("Поиск по названиям контактов и номерам: ");

        TextField searchField = new TextField();
        Button addButton = new Button("Добавить контакт");

        contactsTextArea.setEditable(false);
        contactsTextArea.setWrapText(true);

        vBox.getChildren().addAll(
                infoLabel,
                searchField,
                addButton,
                contactsTextArea,
                largerFontCheckBox
        );

        addButton.setOnAction(event -> {
            showAddContactWindow();
        });

        Scene scene = new Scene(vBox, 300, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Слушатель для отслеживания изменений в поисковом запросе и изменении выдачи контактов
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    contactsTextArea.setText(db.databaseToTextList());
                } else {
                    contactsTextArea.setText(db.searchContactsToTextList(newValue));
                }
            }
        });

        // Слушатель для отслеживания изменений в галочке размера шрифта
        largerFontCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == true)
                fontSize = 24;
            else
                fontSize = 12;
            vBox.setStyle("-fx-font: " + fontSize + " arial;");
            logger.info(String.format("Font size was set to %d", fontSize));
        });

        logger.info("Phone book main window opened");
    }

    /**
     * Метод для отображения окна создания нового контакта.
     */
    private void showAddContactWindow() {
        Stage addContactStage = new Stage();
        addContactStage.setTitle("Добавить контакт");

        // Окно для создания нового контакта
        GridPane root = new GridPane();
        root.setStyle("-fx-font: " + fontSize + " arial;");
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(10));

        ColumnConstraints column1 = new ColumnConstraints(fontSize == 12? 120: 250);
        root.getColumnConstraints().add(column1);
        ColumnConstraints column2 = new ColumnConstraints(150,150,Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().add(column2);

        TextField contactNameTextField = new TextField();
        ArrayList <TextField> contactNumberTextFields = new ArrayList<>();
        ArrayList <TextField> contactNumberTypeTextFields = new ArrayList<>();
        contactNumberTextFields.add(new TextField());
        contactNumberTypeTextFields.add(new TextField());

        // Счётчик контактов
        final int[] orderCounter = {1};

        // Кнопка для добавления ещё одного номера
        Button addNumberButton = new Button("Добавить номер");

        // Кнопка для сохранения контакта
        Button saveContactButton = new Button("Сохранить");

        // Обработчик события для кнопки сохранения контакта
        saveContactButton.setOnAction(event -> {
            // Новый контакт
            Contact newContact = new Contact("");

            newContact.fullName = contactNameTextField.getText();
            for (int i = 0; i < orderCounter[0]; i++) {
                newContact.addPhoneNumber(
                        contactNumberTypeTextFields.get(i).getText(),
                        contactNumberTextFields.get(i).getText()
                );
            }
            // Проверка на корректность ввода номеров
            boolean valid = db.addContact(newContact);
            if (!valid) {
                showAlert(
                        "Ошибка",
                        "Ошибка сохранения контакта",
                        "Среди введённых номеров содержатся номера с недопустимыми символами(буквами) или пустые номера.",
                        Alert.AlertType.ERROR
                );
                logger.warn("Invalid character input in numbers in add contact window");
                return;
            }

            db.writeRecords();
            addContactStage.close();
            contactsTextArea.setText(db.databaseToTextList());

            logger.info("A new contact was added");
        });

        root.addRow(0, addNumberButton, saveContactButton);
        root.addRow(1, new Label("ФИО"), contactNameTextField);
        root.addRow(2, new Label("Тип номера 1"), contactNumberTypeTextFields.get(0));
        root.addRow(3, new Label("Номер 1"), contactNumberTextFields.get(0));

        // Обработчик события для кнопки добавления номера
        addNumberButton.setOnAction(
                event -> {
                    orderCounter[0]++;
                    contactNumberTypeTextFields.add(new TextField());
                    contactNumberTextFields.add(new TextField());
                    root.addRow(
                            4 + (orderCounter[0] - 2) * 2,
                            new Label("Тип номера " + orderCounter[0]),
                            contactNumberTypeTextFields.get(orderCounter[0] - 1)
                    );
                    root.addRow(
                            5 + (orderCounter[0] - 2) * 2,
                            new Label("Номер " + orderCounter[0]),
                            contactNumberTextFields.get(orderCounter[0] - 1)
                    );
                });

        // Создаем ScrollPane для окна создания контакта
        ScrollPane addContactScrollPane = new ScrollPane(root);
        addContactScrollPane.setFitToWidth(true);

        Scene scene = new Scene(addContactScrollPane, 310, 300);
        addContactStage.setScene(scene);
        addContactStage.show();

        // Слушатель для отслеживания изменений в галочке размера шрифта
        largerFontCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            root.setStyle("-fx-font: " + fontSize + " arial;");
            if (newValue == true)
                column1.setPrefWidth(250);
            else
                column1.setPrefWidth(120);
        });

        logger.info("Add contact window opened");
    }

    /**
     * Метод для запуска программы.
     * @param args Список аргументов
     */
    public static void main(String[] args) {
        launch(args);
    }
}