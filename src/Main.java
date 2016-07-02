import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private static final double WINDOW_PADDING = 10;

    private TextField textField;
    private Label output;
    private Task<Void> task, dotsTask;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Label text = new Label("Which number do you want to factorise?");
        textField = new TextField();
        Button button = new Button("Factorise");
        button.setOnAction(e -> onClickFactorise());
        output = new Label();
        VBox root = new VBox(10, text, textField, button, output);
        root.setPadding(new Insets(WINDOW_PADDING));

        Scene scene = new Scene(root, 500, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Prime Factoriser");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        task.cancel();
        dotsTask.cancel();
        super.stop();
    }

    private void onClickFactorise() {
        try {
            task.cancel();
        } catch (Exception ignored) {}
        try {
            dotsTask.cancel();
        } catch (Exception ignored) {}

        String input = textField.getText();
        final long inputNumber;
        try {
            inputNumber = Long.parseLong(input);
        } catch (NumberFormatException e) {
            showInvalidNumberDialog();
            return;
        }

        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                long number = inputNumber;
                List<Long> factors = new ArrayList<>();
                String text = "Factors: ";
                updateMessage(text);

                long factor = 2;
                while (number != 1) {
                    if (isCancelled()) break;

                    if (!isPrime(factor)) {
                        factor++;
                        continue;
                    }

                    if (number % factor == 0) {
                        factors.add(factor);
                        text += factor + ", ";
                        updateMessage(text);
                        number /= factor;
                    } else {
                        factor++;
                    }
                }

                text = "Factors: ";
                for (int i = 0; i < factors.size(); i++) {
                    text += factors.get(i);
                    if (i != factors.size() - 1) text += ", ";
                }
                updateMessage(text);

                try {
                    dotsTask.cancel();
                } catch (Exception ignored) {}
                return null;
            }
        };

        dotsTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String text = ".";
                updateMessage(text);
                while (true) {
                    if (text.length() == 3)
                        text = ".";
                    else
                        text += ".";
                    updateMessage(text);

                    if (isCancelled()) {
                        updateMessage("");
                        return null;
                    }

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        if (isCancelled()) {
                            updateMessage("");
                            return null;
                        }
                    }
                }
            }
        };

        output.textProperty().bind(task.messageProperty().concat(dotsTask.messageProperty()));
        new Thread(task).start();
        new Thread(dotsTask).start();
    }

    private void showInvalidNumberDialog() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Invalid number");

        Label message = new Label("Please enter a valid number");
        Button button = new Button("OK");
        button.setOnAction(e -> stage.close());
        VBox root = new VBox(10, message, button);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(WINDOW_PADDING));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static boolean isPrime(long number) {
        for (int i = 2; i <= Math.sqrt(number); ++i) {
            if (number % i == 0)
                return false;
        }
        return true;
    }
}
