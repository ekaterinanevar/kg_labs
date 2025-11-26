import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class LocalThresholdingLab2 extends JFrame {
    private BufferedImage originalImage;
    private JLabel imageLabel;

    public LocalThresholdingLab2() {
        super("Лабораторная №2 — Локальная пороговая обработка (Ниблэк / Саувола)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        // Кнопки
        JPanel topPanel = new JPanel();
        JButton openButton = new JButton("Открыть изображение");
        JButton niblackButton = new JButton("Метод Ниблэка");
        JButton sauvolaButton = new JButton("Метод Сауволы");
        topPanel.add(openButton);
        topPanel.add(niblackButton);
        topPanel.add(sauvolaButton);
        add(topPanel, BorderLayout.NORTH);

        // Панель для изображения
        imageLabel = new JLabel("", SwingConstants.CENTER);
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        // Обработчики
        openButton.addActionListener(this::openImage);
        niblackButton.addActionListener(e -> applyThreshold("niblack"));
        sauvolaButton.addActionListener(e -> applyThreshold("sauvola"));
    }

    private void openImage(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                originalImage = ImageIO.read(file);
                imageLabel.setIcon(new ImageIcon(originalImage));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки изображения: " + ex.getMessage());
            }
        }
    }

    private void applyThreshold(String method) {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала откройте изображение.");
            return;
        }

        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);

        int windowSize = 15; // локальное окно
        double k = method.equals("niblack") ? -0.2 : 0.5; // параметры методов
        double R = 128; // для Сауволы

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // вычисляем локальное среднее и σ
                double mean = 0, variance = 0;
                int count = 0;
                for (int dy = -windowSize / 2; dy <= windowSize / 2; dy++) {
                    for (int dx = -windowSize / 2; dx <= windowSize / 2; dx++) {
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                            int rgb = originalImage.getRGB(nx, ny);
                            int gray = (int) ((rgb >> 16 & 0xFF) * 0.3 + (rgb >> 8 & 0xFF) * 0.59 + (rgb & 0xFF) * 0.11);
                            mean += gray;
                            count++;
                        }
                    }
                }
                mean /= count;

                for (int dy = -windowSize / 2; dy <= windowSize / 2; dy++) {
                    for (int dx = -windowSize / 2; dx <= windowSize / 2; dx++) {
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                            int rgb = originalImage.getRGB(nx, ny);
                            int gray = (int) ((rgb >> 16 & 0xFF) * 0.3 + (rgb >> 8 & 0xFF) * 0.59 + (rgb & 0xFF) * 0.11);
                            variance += Math.pow(gray - mean, 2);
                        }
                    }
                }
                double std = Math.sqrt(variance / count);

                double threshold;
                if (method.equals("niblack"))
                    threshold = mean + k * std;
                else
                    threshold = mean * (1 + k * ((std / R) - 1));

                int rgb = originalImage.getRGB(x, y);
                int gray = (int) ((rgb >> 16 & 0xFF) * 0.3 + (rgb >> 8 & 0xFF) * 0.59 + (rgb & 0xFF) * 0.11);
                int newColor = gray > threshold ? 0xFFFFFF : 0x000000;
                result.setRGB(x, y, newColor);
            }
        }

        imageLabel.setIcon(new ImageIcon(result));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LocalThresholdingLab2().setVisible(true));
    }
}
