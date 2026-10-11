import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    // ---------- Name and colours (change these to restyle the app) ----------
    private static final String LODGE_NAME = "Elite Night Inn Lodge";
    private static final String BLUE = "#1e5aa8";
    private static final String DARK_BLUE = "#0f3d7a";
    private static final String MIDNIGHT = "#0b1f4a";
    private static final String LIGHT_BLUE = "#e8f0fb";
    private static final String ACCENT = "#2f7de1";
    private static final String GOLD = "#f5c542";
    private static final String GREEN = "#2e9e4f";    // colour while a button is clicked

    // ---------- Data class: one saved bill ----------
    public static class Bill {
        private final String id, guest, room;
        private final int nights;
        private final double total, paid, balance;

        public Bill(String id, String guest, String room, int nights,
                    double total, double paid, double balance) {
            this.id = id; this.guest = guest; this.room = room;
            this.nights = nights; this.total = total;
            this.paid = paid; this.balance = balance;
        }
        public String getId() { return id; }
        public String getGuest() { return guest; }
        public String getRoom() { return room; }
        public int getNights() { return nights; }
        public double getTotal() { return total; }
        public double getPaid() { return paid; }
        public double getBalance() { return balance; }
    }

    // ---------- Fields ----------
    private Stage stage;
    private final ObservableList<Bill> bills = FXCollections.observableArrayList();
    private int counter = 0;

    private TextField guestField, nightsField, extrasField, paidField;
    private ComboBox<String> roomBox;
    private TextArea notesArea;
    private Label statusLabel;
    private TableView<Bill> table;

    // ---------- Start ----------
    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle(LODGE_NAME + " - Lodge Billing Lab");
        showLogin();
        stage.show();
    }

    // ---------- Logo: crescent moon and stars over a small inn ----------
    private Pane createLogo(double s) {
        Pane logo = new Pane();
        logo.setMinSize(s, s);
        logo.setPrefSize(s, s);
        logo.setMaxSize(s, s);

        // round night-sky badge with a gold ring
        Circle badge = new Circle(s / 2, s / 2, s / 2 - 1);
        badge.setFill(Color.web(MIDNIGHT));
        badge.setStroke(Color.web(GOLD));
        badge.setStrokeWidth(s * 0.05);

        // crescent moon = big circle minus a smaller circle
        Circle moon = new Circle(s * 0.45, s * 0.40, s * 0.22);
        Circle cut = new Circle(s * 0.55, s * 0.34, s * 0.19);
        Shape crescent = Shape.subtract(moon, cut);
        crescent.setFill(Color.web(GOLD));

        // stars
        Circle star1 = new Circle(s * 0.74, s * 0.26, s * 0.035);
        Circle star2 = new Circle(s * 0.80, s * 0.42, s * 0.025);
        Circle star3 = new Circle(s * 0.62, s * 0.17, s * 0.022);
        star1.setFill(Color.WHITE);
        star2.setFill(Color.WHITE);
        star3.setFill(Color.WHITE);

        // the inn: roof, walls and two lit windows
        Rectangle walls = new Rectangle(s * 0.30, s * 0.68, s * 0.40, s * 0.14);
        walls.setFill(Color.web(BLUE));
        Polygon roof = new Polygon(s * 0.24, s * 0.68, s * 0.50, s * 0.55, s * 0.76, s * 0.68);
        roof.setFill(Color.web(BLUE));
        Rectangle window1 = new Rectangle(s * 0.36, s * 0.72, s * 0.07, s * 0.06);
        Rectangle window2 = new Rectangle(s * 0.57, s * 0.72, s * 0.07, s * 0.06);
        window1.setFill(Color.web(GOLD));
        window2.setFill(Color.web(GOLD));

        logo.getChildren().addAll(badge, crescent, star1, star2, star3,
                walls, roof, window1, window2);
        return logo;
    }

    // ---------- Buttons: normal colour, turn green when clicked ----------
    private Button makeButton(String text, String bg, String fg, String extra) {
        Button b = new Button(text);
        String base = "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; " + extra;
        String normal = "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " + base;
        String green = "-fx-background-color: " + GREEN + "; -fx-text-fill: white; " + base;
        b.setStyle(normal);
        b.pressedProperty().addListener((obs, wasPressed, isPressed) -> {
            if (isPressed) {
                b.setStyle(green);
            } else {
                PauseTransition pause = new PauseTransition(Duration.millis(400));
                pause.setOnFinished(ev -> {
                    if (!b.isPressed()) b.setStyle(normal);
                });
                pause.play();
            }
        });
        return b;
    }

    private Button accentButton(String text) {
        return makeButton(text, ACCENT, "white", "-fx-padding: 8 18 8 18;");
    }

    private Button lightButton(String text) {
        return makeButton(text, LIGHT_BLUE, DARK_BLUE, "-fx-padding: 8 18 8 18;");
    }

    private Button sidebarButton(String text) {
        Button b = makeButton(text, "white", DARK_BLUE, "-fx-padding: 8 12 8 12;");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private Button headerButton(String text) {
        return makeButton(text, GOLD, MIDNIGHT, "-fx-padding: 6 14 6 14;");
    }

    private Label formLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + DARK_BLUE + "; -fx-font-weight: bold;");
        return l;
    }

    // ---------- Login screen ----------
    private void showLogin() {
        // starry background
        Pane sky = new Pane();
        sky.setMouseTransparent(true);
        double[][] stars = {
                {40, 60, 2.2}, {110, 30, 1.4}, {200, 90, 1.8}, {300, 40, 1.2},
                {380, 80, 2.0}, {470, 35, 1.5}, {60, 170, 1.3}, {450, 160, 1.8},
                {30, 300, 1.6}, {480, 320, 1.3}, {90, 430, 2.0}, {410, 440, 1.4},
                {250, 520, 1.7}, {160, 540, 1.2}, {360, 530, 1.9}};
        for (double[] st : stars) {
            Circle c = new Circle(st[0], st[1], st[2]);
            c.setFill(Color.WHITE);
            c.setOpacity(0.75);
            sky.getChildren().add(c);
        }

        Pane logo = createLogo(90);

        Label title = new Label(LODGE_NAME);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + MIDNIGHT + ";");
        Label sub = new Label("Lodge Billing Lab - please log in");
        sub.setStyle("-fx-text-fill: gray;");

        TextField user = new TextField();
        user.setPromptText("Username");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");
        Label error = new Label();
        error.setStyle("-fx-text-fill: red;");

        Button loginBtn = accentButton("Log in");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            if (user.getText().equals("Admin") && pass.getText().equals("1234")) {
                showBilling();
            } else {
                error.setText("Wrong username or password.");
            }
        });
        pass.setOnAction(e -> loginBtn.fire());   // pressing Enter also logs in

        VBox card = new VBox(12, logo, title, sub, user, pass, loginBtn, error);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(340);
        card.setMaxHeight(VBox.USE_PREF_SIZE);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; "
                + "-fx-border-color: " + GOLD + "; -fx-border-width: 2; -fx-border-radius: 14;");

        StackPane root = new StackPane(sky, card);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, " + MIDNIGHT + ", " + BLUE + ");");
        stage.setScene(new Scene(root, 520, 580));
    }

    private void logout() {
        bills.clear();
        counter = 0;
        showLogin();
    }

    // ---------- Main billing screen ----------
    private void showBilling() {
        // ----- Menu bar -----
        MenuItem logoutItem = new MenuItem("Log out");
        logoutItem.setOnAction(e -> logout());
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> stage.close());
        Menu file = new Menu("File", null, logoutItem, exit);

        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION,
                LODGE_NAME + " - Lodge Billing Lab (ICT261)").showAndWait());
        Menu help = new Menu("Help", null, about);
        MenuBar menuBar = new MenuBar(file, help);

        // ----- Header: logo, lodge name, log out -----
        Pane headerLogo = createLogo(64);

        Label lodgeTitle = new Label(LODGE_NAME);
        lodgeTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label course = new Label("Lodge Billing Lab  |  ICT261 Lecture 3  |  Mr E Nyirenda  |  Mulungushi University");
        course.setStyle("-fx-text-fill: #cfe0f7;");
        VBox titles = new VBox(2, lodgeTitle, course);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button headerLogout = headerButton("Log out");
        headerLogout.setOnAction(e -> logout());

        HBox header = new HBox(15, headerLogo, titles, spacer, headerLogout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, " + MIDNIGHT + ", " + BLUE + "); "
                + "-fx-border-color: transparent transparent " + GOLD + " transparent; "
                + "-fx-border-width: 0 0 3 0;");

        VBox top = new VBox(menuBar, header);

        // ----- Left sidebar: actions + prices -----
        Label actionsTitle = new Label("Actions");
        actionsTitle.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        Button newBtn = sidebarButton("New guest");
        newBtn.setOnAction(e -> clearForm());
        Button receiptBtn = sidebarButton("View receipt");
        receiptBtn.setOnAction(e -> showReceipt());
        Button deleteBtn = sidebarButton("Delete selected bill");
        deleteBtn.setOnAction(e -> deleteSelected());

        Label pricesTitle = new Label("Practice room prices");
        pricesTitle.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label prices = new Label(
                "Standard: K 350.00 per night\n"
                        + "Deluxe: K 550.00 per night\n"
                        + "Family: K 750.00 per night");
        prices.setStyle("-fx-text-fill: white;");
        Label rules = new Label(
                "5 nights or more: 5% off the room charge.\n\n"
                        + "Extras get no discount. No tax in this lab.\n\n"
                        + "Saved bills last only until you close the app.");
        rules.setWrapText(true);
        rules.setStyle("-fx-text-fill: #cfe0f7;");

        VBox sidebar = new VBox(10, actionsTitle, newBtn, receiptBtn, deleteBtn,
                new Separator(), pricesTitle, prices, rules);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: " + MIDNIGHT + ";");

        // ----- Form card -----
        guestField = new TextField();
        roomBox = new ComboBox<>(FXCollections.observableArrayList("Standard", "Deluxe", "Family"));
        roomBox.setValue("Standard");
        nightsField = new TextField();
        extrasField = new TextField("0");
        paidField = new TextField("0");
        notesArea = new TextArea();
        notesArea.setPrefRowCount(3);

        guestField.setMaxWidth(Double.MAX_VALUE);
        roomBox.setMaxWidth(Double.MAX_VALUE);
        nightsField.setMaxWidth(Double.MAX_VALUE);
        extrasField.setMaxWidth(Double.MAX_VALUE);
        paidField.setMaxWidth(Double.MAX_VALUE);
        notesArea.setMaxWidth(Double.MAX_VALUE);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(130);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelCol, fieldCol);

        form.addRow(0, formLabel("Guest name:"), guestField);
        form.addRow(1, formLabel("Room type:"), roomBox);
        form.addRow(2, formLabel("Nights:"), nightsField);
        form.addRow(3, formLabel("Extras in K:"), extrasField);
        form.addRow(4, formLabel("Amount paid in K:"), paidField);
        form.addRow(5, formLabel("Notes:"), notesArea);

        Button calcBtn = lightButton("Calculate");
        calcBtn.setOnAction(e -> onCalculate());
        Button saveBtn = accentButton("Save bill");
        saveBtn.setOnAction(e -> onSave());
        statusLabel = new Label();

        HBox buttons = new HBox(10, calcBtn, saveBtn, statusLabel);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox formCard = new VBox(15, form, buttons);
        formCard.setPadding(new Insets(20));
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-border-color: " + GOLD + " #c9d9f0 #c9d9f0 #c9d9f0; "
                + "-fx-border-width: 4 1 1 1; -fx-border-radius: 10;");

        // ----- Saved bills table -----
        Label billsTitle = new Label("Saved bills");
        billsTitle.setStyle("-fx-text-fill: " + BLUE + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        table = new TableView<>(bills);
        addColumn("Bill", "id");
        addColumn("Guest", "guest");
        addColumn("Room", "room");
        addColumn("Nights", "nights");
        addColumn("Total", "total");
        addColumn("Paid", "paid");
        addColumn("Balance", "balance");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(200);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox center = new VBox(12, formCard, billsTitle, table);
        center.setPadding(new Insets(20));
        center.setStyle("-fx-background-color: " + LIGHT_BLUE + ";");

        // ----- Put it all together -----
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setLeft(sidebar);
        root.setCenter(center);

        stage.setScene(new Scene(root, 1000, 720));
    }

    private void addColumn(String title, String property) {
        TableColumn<Bill, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        table.getColumns().add(col);
    }

    // ---------- Calculation (the core logic) ----------
    // returns {nights, total, paid, balance}; throws if input is invalid
    private double[] calculate() {
        if (guestField.getText().trim().isEmpty())
            throw new IllegalArgumentException("Enter the guest name.");

        int nights;
        try {
            nights = Integer.parseInt(nightsField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Nights must be a whole number.");
        }
        if (nights < 1) throw new IllegalArgumentException("Nights must be at least 1.");

        double extras = parseMoney(extrasField, "Extras");
        double paid = parseMoney(paidField, "Amount paid");

        double rate;
        switch (roomBox.getValue()) {
            case "Deluxe": rate = 550; break;
            case "Family": rate = 750; break;
            default: rate = 350;
        }

        double roomCharge = rate * nights;
        if (nights >= 5) roomCharge = roomCharge * 0.95;   // 5% discount, room only

        double total = roomCharge + extras;
        return new double[]{nights, total, paid, total - paid};
    }

    private double parseMoney(TextField field, String name) {
        String text = field.getText().trim();
        if (text.isEmpty()) return 0;
        try {
            double value = Double.parseDouble(text);
            if (value < 0) throw new IllegalArgumentException(name + " cannot be negative.");
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a number.");
        }
    }

    // ---------- Button actions ----------
    private void onCalculate() {
        try {
            double[] r = calculate();
            showStatus(String.format("Total: K %.2f   Balance: K %.2f", r[1], r[3]), false);
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), true);
        }
    }

    private void onSave() {
        try {
            double[] r = calculate();
            String id = String.format("B%03d", ++counter);
            bills.add(new Bill(id, guestField.getText().trim(), roomBox.getValue(),
                    (int) r[0], r[1], r[2], r[3]));
            showStatus(String.format("Saved %s: Balance: K %.2f", id, r[3]), false);
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), true);
        }
    }

    private void showReceipt() {
        Bill b = table.getSelectionModel().getSelectedItem();
        if (b == null) { showStatus("Select a bill in the table first.", true); return; }
        String text = String.format(
                "%s%n------------------------------%nBill: %s%nGuest: %s%nRoom: %s%nNights: %d%nTotal: K %.2f%nPaid: K %.2f%nBalance: K %.2f",
                LODGE_NAME, b.getId(), b.getGuest(), b.getRoom(), b.getNights(),
                b.getTotal(), b.getPaid(), b.getBalance());
        new Alert(Alert.AlertType.INFORMATION, text).showAndWait();
    }

    private void deleteSelected() {
        Bill b = table.getSelectionModel().getSelectedItem();
        if (b == null) { showStatus("Select a bill to delete.", true); return; }
        bills.remove(b);
        showStatus("Deleted " + b.getId(), false);
    }

    private void clearForm() {
        guestField.clear();
        roomBox.setValue("Standard");
        nightsField.clear();
        extrasField.setText("0");
        paidField.setText("0");
        notesArea.clear();statusLabel.setText("");
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError
                ? "-fx-text-fill: red; -fx-font-weight: bold;"
                : "-fx-text-fill: green; -fx-font-weight: bold;");
    }

    public static void main(String[] args) {
        launch(args);
    }
}