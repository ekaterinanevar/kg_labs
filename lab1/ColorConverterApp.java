import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ColorConverterApp extends JFrame {
    private JSlider rSlider, gSlider, bSlider;
    private JSlider hSlider, sSlider, lSlider;
    private JSlider cSlider, mSlider, ySlider, kSlider;
    private JTextField rField, gField, bField;
    private JTextField hField, sField, lField;
    private JTextField cField, mField, yField, kField;
    private JPanel colorPanel;
    private boolean updating = false;

    public ColorConverterApp() {
        super("Цветовые модели: CMYK ↔ RGB ↔ HSL");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel slidersPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        slidersPanel.add(createRGBPanel());
        slidersPanel.add(createHSLPanel());
        slidersPanel.add(createCMYKPanel());

        colorPanel = new JPanel();
        colorPanel.setPreferredSize(new Dimension(200, 200));
        colorPanel.setBorder(BorderFactory.createTitledBorder("Цвет"));

        add(slidersPanel, BorderLayout.CENTER);
        add(colorPanel, BorderLayout.EAST);

        updateColorFromRGB();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }



    private JPanel createRGBPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 3, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("RGB"));
        rSlider = createSlider(panel, "R", 0, 255);
        gSlider = createSlider(panel, "G", 0, 255);
        bSlider = createSlider(panel, "B", 0, 255);

        rField = createField(panel, rSlider);
        gField = createField(panel, gSlider);
        bField = createField(panel, bSlider);

        ChangeListener listener = e -> { if (!updating) updateColorFromRGB(); };
        rSlider.addChangeListener(listener);
        gSlider.addChangeListener(listener);
        bSlider.addChangeListener(listener);
        return panel;
    }

    private JPanel createHSLPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 3, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("HSL"));
        hSlider = createSlider(panel, "H", 0, 360);
        sSlider = createSlider(panel, "S", 0, 100);
        lSlider = createSlider(panel, "L", 0, 100);

        hField = createField(panel, hSlider);
        sField = createField(panel, sSlider);
        lField = createField(panel, lSlider);

        ChangeListener listener = e -> { if (!updating) updateColorFromHSL(); };
        hSlider.addChangeListener(listener);
        sSlider.addChangeListener(listener);
        lSlider.addChangeListener(listener);
        return panel;
    }

    private JPanel createCMYKPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 3, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("CMYK"));
        cSlider = createSlider(panel, "C", 0, 100);
        mSlider = createSlider(panel, "M", 0, 100);
        ySlider = createSlider(panel, "Y", 0, 100);
        kSlider = createSlider(panel, "K", 0, 100);

        cField = createField(panel, cSlider);
        mField = createField(panel, mSlider);
        yField = createField(panel, ySlider);
        kField = createField(panel, kSlider);

        ChangeListener listener = e -> { if (!updating) updateColorFromCMYK(); };
        cSlider.addChangeListener(listener);
        mSlider.addChangeListener(listener);
        ySlider.addChangeListener(listener);
        kSlider.addChangeListener(listener);
        return panel;
    }



    private JSlider createSlider(JPanel panel, String label, int min, int max) {
        JLabel lbl = new JLabel(label);
        JSlider slider = new JSlider(min, max, (min + max) / 2);
        panel.add(lbl);
        panel.add(slider);
        return slider;
    }

    private JTextField createField(JPanel panel, JSlider slider) {
        JTextField field = new JTextField(String.valueOf(slider.getValue()));
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int val = Integer.parseInt(field.getText());
                    val = Math.max(slider.getMinimum(), Math.min(slider.getMaximum(), val));
                    slider.setValue(val);
                    field.setText(String.valueOf(val));
                } catch (NumberFormatException ex) {
                    field.setText(String.valueOf(slider.getValue()));
                }
            }
        });
        slider.addChangeListener(e -> field.setText(String.valueOf(slider.getValue())));
        panel.add(field);
        return field;
    }



    private void updateColorFromRGB() {
        updating = true;
        int r = rSlider.getValue();
        int g = gSlider.getValue();
        int b = bSlider.getValue();

        colorPanel.setBackground(new Color(r, g, b));

        float[] hsl = rgbToHsl(r, g, b);
        hSlider.setValue(Math.round(hsl[0]));
        sSlider.setValue(Math.round(hsl[1] * 100));
        lSlider.setValue(Math.round(hsl[2] * 100));

        float[] cmyk = rgbToCmyk(r, g, b);
        cSlider.setValue(Math.round(cmyk[0] * 100));
        mSlider.setValue(Math.round(cmyk[1] * 100));
        ySlider.setValue(Math.round(cmyk[2] * 100));
        kSlider.setValue(Math.round(cmyk[3] * 100));

        updating = false;
    }

    private void updateColorFromHSL() {
        updating = true;
        float h = hSlider.getValue();
        float s = sSlider.getValue() / 100f;
        float l = lSlider.getValue() / 100f;

        int[] rgb = hslToRgb(h, s, l);
        rSlider.setValue(rgb[0]);
        gSlider.setValue(rgb[1]);
        bSlider.setValue(rgb[2]);

        updateColorFromRGB();
        updating = false;
    }

    private void updateColorFromCMYK() {
        updating = true;
        float c = cSlider.getValue() / 100f;
        float m = mSlider.getValue() / 100f;
        float y = ySlider.getValue() / 100f;
        float k = kSlider.getValue() / 100f;

        int[] rgb = cmykToRgb(c, m, y, k);
        rSlider.setValue(rgb[0]);
        gSlider.setValue(rgb[1]);
        bSlider.setValue(rgb[2]);
        updateColorFromRGB();
        updating = false;
    }



    private float[] rgbToCmyk(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float k = 1 - Math.max(rf, Math.max(gf, bf));
        if (k >= 1.0f) return new float[]{0, 0, 0, 1};
        float c = (1 - rf - k) / (1 - k);
        float m = (1 - gf - k) / (1 - k);
        float y = (1 - bf - k) / (1 - k);
        return new float[]{c, m, y, k};
    }

    private int[] cmykToRgb(float c, float m, float y, float k) {
        int r = Math.round(255 * (1 - c) * (1 - k));
        int g = Math.round(255 * (1 - m) * (1 - k));
        int b = Math.round(255 * (1 - y) * (1 - k));
        return new int[]{r, g, b};
    }


    private float[] rgbToHsl(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float h, s, l;
        l = (max + min) / 2f;

        if (max == min) {
            h = s = 0;
        } else {
            float d = max - min;
            s = d / (1 - Math.abs(2 * l - 1));
            if (max == rf)
                h = ((gf - bf) / d + (gf < bf ? 6 : 0)) * 60f;
            else if (max == gf)
                h = ((bf - rf) / d + 2) * 60f;
            else
                h = ((rf - gf) / d + 4) * 60f;
        }
        return new float[]{h % 360, s, l};
    }

    private int[] hslToRgb(float h, float s, float l) {
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = l - c / 2f;

        float rf = 0, gf = 0, bf = 0;
        if (h < 60) {rf = c; gf = x;}
        else if (h < 120) {rf = x; gf = c;}
        else if (h < 180) {gf = c; bf = x;}
        else if (h < 240) {gf = x; bf = c;}
        else if (h < 300) {rf = x; bf = c;}
        else {rf = c; bf = x;}

        int r = Math.round((rf + m) * 255);
        int g = Math.round((gf + m) * 255);
        int b = Math.round((bf + m) * 255);
        return new int[]{r, g, b};
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColorConverterApp::new);
    }
}
