import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

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
        stage.setTitle("Lodge Billing Lab");
        showLogin();
        stage.show();
    }

    // ---------- Login screen ----------
    private void showLogin() {
        Label title = new Label("Lodge Billing Lab - Login");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField user = new TextField();
        user.setPromptText("Username");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");
        Label error = new Label();
        error.setStyle("-fx-text-fill: red;");

        Button loginBtn = new Button("Log in");
        loginBtn.setOnAction(e -> {
            if (user.getText().equals("admin") && pass.getText().equals("1234")) {
                showBilling();
            } else {
                error.setText("Wrong username or password.");
            }
        });

        VBox box = new VBox(10, title, user, pass, loginBtn, error);
        box.setPadding(new Insets(30));
        box.setMaxWidth(300);
        StackPane root = new StackPane(box);
        stage.setScene(new Scene(root, 450, 320));
    }

    // ---------- Main billing screen ----------
    private void showBilling() {
        // Menu bar
        MenuItem logout = new MenuItem("Log out");
        logout.setOnAction(e -> { bills.clear(); counter = 0; showLogin(); });
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> stage.close());
        Menu file = new Menu("File", null, logout, exit);

        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION,
                "Lodge Billing Lab - ICT261").showAndWait());
        Menu help = new Menu("Help", null, about);
        MenuBar menuBar = new MenuBar(file, help);

        // Toolbar
        Button newBtn = new Button("New guest");
        newBtn.setOnAction(e -> clearForm());
        Button receiptBtn = new Button("View receipt");
        receiptBtn.setOnAction(e -> showReceipt());
        Button deleteBtn = new Button("Delete selected bill");
        deleteBtn.setOnAction(e -> deleteSelected());
        ToolBar toolBar = new ToolBar(newBtn, receiptBtn, deleteBtn);

        // Form
        guestField = new TextField();
        roomBox = new ComboBox<>(FXCollections.observableArrayList("Standard", "Deluxe", "Family"));
        roomBox.setValue("Standard");
        nightsField = new TextField();
        extrasField = new TextField("0");
        paidField = new TextField("0");
        notesArea = new TextArea();
        notesArea.setPrefRowCount(3);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Guest name:"), guestField);
        form.addRow(1, new Label("Room type:"), roomBox);
        form.addRow(2, new Label("Nights:"), nightsField);
        form.addRow(3, new Label("Extras in K:"), extrasField);
        form.addRow(4, new Label("Amount paid in K:"), paidField);
        form.addRow(5, new Label("Notes:"), notesArea);

        Button calcBtn = new Button("Calculate");
        calcBtn.setOnAction(e -> onCalculate());
        Button saveBtn = new Button("Save bill");
        saveBtn.setOnAction(e -> onSave());
        statusLabel = new Label();

        VBox left = new VBox(10, form, new HBox(10, calcBtn, saveBtn), statusLabel);

        // Price panel
        Label prices = new Label(
                "Practice room prices\n\n"
                        + "Standard: K 350.00 per night\n"
                        + "Deluxe: K 550.00 per night\n"
                        + "Family: K 750.00 per night\n\n"
                        + "5 nights or more: 5% off the room charge.\n"
                        + "Extras get no discount. No tax in this lab.\n"
                        + "Saved bills last only until you close the app.");
        prices.setWrapText(true);
        prices.setPadding(new Insets(15));
        prices.setStyle("-fx-background-color: #e8f0fb;");
        prices.setPrefWidth(280);

        HBox top = new HBox(20, left, prices);
        top.setPadding(new Insets(15));

        // Table
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

        VBox center = new VBox(menuBar, toolBar, top, new Label("  Saved bills"), table);
        stage.setScene(new Scene(center, 900, 650));
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
                "Bill: %s%nGuest: %s%nRoom: %s%nNights: %d%nTotal: K %.2f%nPaid: K %.2f%nBalance: K %.2f",
                b.getId(), b.getGuest(), b.getRoom(), b.getNights(),
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
        notesArea.clear();
        statusLabel.setText("");
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