package userInterface.utils;

import org.jdatepicker.JDatePicker;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class InputCheck {
    public static void checkInputs(JComponent... fields) {
        ArrayList<JComponent> components = new ArrayList<>(Arrays.asList(fields));

        components.forEach((component) -> {
            if ((component instanceof JDatePicker && ((JDatePicker)component).getFormattedTextField().getText().isEmpty())
                    || (component instanceof JComboBox<?> && Objects.equals(((JComboBox<?>) component).getSelectedItem(), ""))
                    || (component instanceof JTextField && ((JTextField)component).getText().isEmpty())
                    || (component instanceof JSpinner && (Integer)((JSpinner)component).getValue() == 0)) {
                component.setBorder(BorderFactory.createLineBorder(Color.RED));
            } else if (component != null && component.getBorder() != null && !component.getBorder().equals(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"))) {
                component.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
            }
        });
    }
}
