package sam;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.sound.sampled.*;
import java.util.Stack;
import java.net.URL;

public class AdvancedCalculator extends JFrame implements ActionListener {

    private JTextField textField;
    private JButton[] buttons;
    private String[] buttonLabels = {
            "7", "8", "9", "=",
            "4", "5", "6", "/",
            "1", "2", "3", "*",
            "(", ")", "0", "-",
            "sin", "cos", "tan", "+",
            ".", "C", "←", "%"
    };

    public AdvancedCalculator() {
        setTitle("Advanced Calculator");
        setSize(300, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        textField = new JTextField();
        textField.setHorizontalAlignment(JTextField.RIGHT);
        add(textField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 4));

        buttons = new JButton[buttonLabels.length];
        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = new JButton(buttonLabels[i]);
            buttons[i].addActionListener(this);
            buttonPanel.add(buttons[i]);
        }

        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        new AdvancedCalculator();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("=")) {
            calculate();
        } else if (command.equals("C")) {
        	playSound(command);
            textField.setText("");
        } else if (command.equals("←")) {
        	playSound("back");
            if (textField.getText().length() > 0) {
                textField.setText(textField.getText().substring(0, textField.getText().length() - 1));
            }
        } else if (command.equals("%")) {
        	playSound("perc");
            calculatePercentage();
        } else {
            if (command.matches("\\d")) {
                playSound(command);
            }
            else if (command.equals("+")) {
                playSound("plus");
            } else if (command.equals("-")) {
                playSound("minus");
            } else if (command.equals("*")) {
                playSound("multiply");
            } else if (command.equals("/")) {
                playSound("divide");
                
            }
            
            textField.setText(textField.getText() + command);
        }
    }

    private void calculate() {
        String input = textField.getText();
        input = input.replaceAll("sin", "s");
        input = input.replaceAll("cos", "c");
        input = input.replaceAll("tan", "t");
        String[] tokens = tokenize(input);

        try {
            double result = evaluate(tokens);
            textField.setText(Double.toString(result));
        } catch (Exception ex) {
            textField.setText("Error");
        }
    }

    private void calculatePercentage() {
        String input = textField.getText();
        try {
            double value = Double.parseDouble(input);
            double result = value / 100;
            textField.setText(Double.toString(result));
        } catch (Exception ex) {
            textField.setText("Error");
        }
    }

    private String[] tokenize(String input) {
        input = input.replaceAll("\\s+", ""); // Remove all white spaces

        // Separate tokens around operators and keep operators as separate tokens
        input = input.replaceAll("(?<=[-+*/()])|(?=[-+*/()])", " ");
        return input.split("\\s+");
    }

    private double evaluate(String[] tokens) throws Exception {
        Stack<Double> values = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (token.matches("[+-]?\\d+(\\.\\d+)?")) { // Number
                values.push(Double.parseDouble(token));
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.peek().equals("(")) {
                    applyOperation(values, operators.pop());
                }
                operators.pop(); // Pop the '('
            } else if (isOperator(token)) {
                while (!operators.empty() && precedence(operators.peek()) >= precedence(token)) {
                    applyOperation(values, operators.pop());
                }
                operators.push(token);
            } else if (token.equals("s") || token.equals("c") || token.equals("t")) { // Function
                operators.push(token);
            } else {
                throw new Exception("Invalid input");
            }
        }

        while (!operators.empty()) {
            applyOperation(values, operators.pop());
        }

        return values.pop();
    }

    private void applyOperation(Stack<Double> values, String operator) {
        if (operator.equals("s") || operator.equals("c") || operator.equals("t")) {
            double rightOperand = values.pop();
            double result = 0;

            switch (operator) {
                case "s":
                    result = Math.sin(Math.toRadians(rightOperand));
                    break;
                case "c":
                    result = Math.cos(Math.toRadians(rightOperand));
                    break;
                case "t":
                    result = Math.tan(Math.toRadians(rightOperand));
                    break;
            }

            values.push(result);
        } else {
            double rightOperand = values.pop();
            double leftOperand = values.pop();
            double result = 0;

            switch (operator) {
                case "+":
                    result = leftOperand + rightOperand;
                    break;
                case "-":
                    result = leftOperand - rightOperand;
                    break;
                case "*":
                    result = leftOperand * rightOperand;
                    break;
                case "/":
                    if (rightOperand == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    result = leftOperand / rightOperand;
                    break;
            }

            values.push(result);
        }
    }

    private int precedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "s":
            case "c":
            case "t":
                return 3;
            default:
                return 0;
        }
    }

    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }

    private void playSound(String command) {
        try {
            String soundFileName = "/sam/sounds/sound" + command + ".wav";
            URL soundURL = getClass().getResource(soundFileName);
            if (soundURL == null) {
                System.err.println("Sound file not found: " + command);
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }
    
}







