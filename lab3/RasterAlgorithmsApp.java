import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class RasterAlgorithmsApp extends JFrame {
    private DrawingPanel drawingPanel;
    private JComboBox<String> algorithmComboBox;
    private JComboBox<String> shapeComboBox;
    private JButton drawButton;
    private JButton clearButton;
    private JTextField x1Field, y1Field, x2Field, y2Field, radiusField;
    private JTextArea logArea;
    private JCheckBox showGridCheckBox;
    private JCheckBox showAxesCheckBox;
    private JCheckBox showCoordinatesCheckBox;

    public RasterAlgorithmsApp() {
        setTitle("Базовые растровые алгоритмы - Лабораторная работа 3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        drawingPanel = new DrawingPanel();

        String[] algorithms = {
                "Пошаговый алгоритм",
                "Алгоритм ЦДА",
                "Алгоритм Брезенхема (линия)",
                "Алгоритм Брезенхема (окружность)"
        };

        String[] shapes = {"Отрезок", "Окружность"};

        algorithmComboBox = new JComboBox<>(algorithms);
        shapeComboBox = new JComboBox<>(shapes);

        drawButton = new JButton("Нарисовать");
        clearButton = new JButton("Очистить");

        x1Field = new JTextField("100", 5);
        y1Field = new JTextField("100", 5);
        x2Field = new JTextField("200", 5);
        y2Field = new JTextField("150", 5);
        radiusField = new JTextField("50", 5);

        logArea = new JTextArea(10, 30);
        logArea.setEditable(false);

        showGridCheckBox = new JCheckBox("Сетка", true);
        showAxesCheckBox = new JCheckBox("Оси", true);
        showCoordinatesCheckBox = new JCheckBox("Координаты", true);

        updateInputFields();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Панель управления
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Алгоритм:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        controlPanel.add(algorithmComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("Фигура:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        controlPanel.add(shapeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        controlPanel.add(new JLabel("X1:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        controlPanel.add(x1Field, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        controlPanel.add(new JLabel("Y1:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        controlPanel.add(y1Field, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        controlPanel.add(new JLabel("X2:"), gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        controlPanel.add(x2Field, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        controlPanel.add(new JLabel("Y2:"), gbc);

        gbc.gridx = 1; gbc.gridy = 5;
        controlPanel.add(y2Field, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        controlPanel.add(new JLabel("Радиус:"), gbc);

        gbc.gridx = 1; gbc.gridy = 6;
        controlPanel.add(radiusField, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        controlPanel.add(drawButton, gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        controlPanel.add(clearButton, gbc);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        controlPanel.add(showGridCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        controlPanel.add(showAxesCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 2;
        controlPanel.add(showCoordinatesCheckBox, gbc);

        // Панель лога
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Лог алгоритма"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Правая панель
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(controlPanel, BorderLayout.NORTH);
        rightPanel.add(logPanel, BorderLayout.CENTER);

        add(drawingPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void setupEventListeners() {
        drawButton.addActionListener(e -> drawShape());
        clearButton.addActionListener(e -> clearCanvas());

        shapeComboBox.addActionListener(e -> updateInputFields());

        showGridCheckBox.addActionListener(e -> drawingPanel.repaint());
        showAxesCheckBox.addActionListener(e -> drawingPanel.repaint());
        showCoordinatesCheckBox.addActionListener(e -> drawingPanel.repaint());
    }

    private void updateInputFields() {
        String shape = (String) shapeComboBox.getSelectedItem();
        boolean isCircle = "Окружность".equals(shape);

        x2Field.setEnabled(!isCircle);
        y2Field.setEnabled(!isCircle);
        radiusField.setEnabled(isCircle);
    }

    private void drawShape() {
        try {
            int x1 = Integer.parseInt(x1Field.getText());
            int y1 = Integer.parseInt(y1Field.getText());
            String algorithm = (String) algorithmComboBox.getSelectedItem();
            String shape = (String) shapeComboBox.getSelectedItem();

            long startTime = System.nanoTime();

            if ("Окружность".equals(shape)) {
                int radius = Integer.parseInt(radiusField.getText());
                drawingPanel.drawCircle(x1, y1, radius, algorithm);
                logArea.append(String.format("Окружность: центр(%d,%d), радиус=%d, алгоритм: %s\n",
                        x1, y1, radius, algorithm));
            } else {
                int x2 = Integer.parseInt(x2Field.getText());
                int y2 = Integer.parseInt(y2Field.getText());
                drawingPanel.drawLine(x1, y1, x2, y2, algorithm);
                logArea.append(String.format("Отрезок: (%d,%d)-(%d,%d), алгоритм: %s\n",
                        x1, y1, x2, y2, algorithm));
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            logArea.append(String.format("Время выполнения: %d наносекунд\n\n", duration));

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите корректные числовые значения",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearCanvas() {
        drawingPanel.clear();
        logArea.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RasterAlgorithmsApp().setVisible(true);
        });
    }

    class DrawingPanel extends JPanel {
        private BufferedImage canvas;
        private List<Point> points;
        private Color drawColor = Color.RED;
        private static final int GRID_SIZE = 20;
        private static final int ORIGIN_X = 400;
        private static final int ORIGIN_Y = 300;

        public DrawingPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(800, 600));
            points = new ArrayList<>();
            clear();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        // Правый клик - вторая точка
                        if (points.size() == 1) {
                            points.add(new Point(e.getX() - ORIGIN_X, e.getY() - ORIGIN_Y));
                            repaint();
                        }
                    } else {
                        // Левый клик - первая точка
                        points.clear();
                        points.add(new Point(e.getX() - ORIGIN_X, e.getY() - ORIGIN_Y));
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Антиалиасинг для лучшего качества
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Рисуем сетку
            if (showGridCheckBox.isSelected()) {
                drawGrid(g2d);
            }

            // Рисуем оси
            if (showAxesCheckBox.isSelected()) {
                drawAxes(g2d);
            }

            // Рисуем координаты
            if (showCoordinatesCheckBox.isSelected()) {
                drawCoordinates(g2d);
            }

            // Рисуем точки
            drawPoints(g2d);

            // Рисуем канвас
            if (canvas != null) {
                g2d.drawImage(canvas, ORIGIN_X, ORIGIN_Y, null);
            }
        }

        private void drawGrid(Graphics2D g2d) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(1));

            // Вертикальные линии
            for (int x = ORIGIN_X % GRID_SIZE; x < getWidth(); x += GRID_SIZE) {
                g2d.drawLine(x, 0, x, getHeight());
            }

            // Горизонтальные линии
            for (int y = ORIGIN_Y % GRID_SIZE; y < getHeight(); y += GRID_SIZE) {
                g2d.drawLine(0, y, getWidth(), y);
            }
        }

        private void drawAxes(Graphics2D g2d) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));

            // Ось X
            g2d.drawLine(0, ORIGIN_Y, getWidth(), ORIGIN_Y);
            // Ось Y
            g2d.drawLine(ORIGIN_X, 0, ORIGIN_X, getHeight());

            // Стрелки
            g2d.fillPolygon(new int[] {getWidth() - 10, getWidth() - 10, getWidth()},
                    new int[] {ORIGIN_Y - 5, ORIGIN_Y + 5, ORIGIN_Y}, 3);
            g2d.fillPolygon(new int[] {ORIGIN_X - 5, ORIGIN_X + 5, ORIGIN_X},
                    new int[] {10, 10, 0}, 3);

            // Подписи осей
            g2d.drawString("X", getWidth() - 20, ORIGIN_Y - 10);
            g2d.drawString("Y", ORIGIN_X + 10, 20);
        }

        private void drawCoordinates(Graphics2D g2d) {
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));

            // Подписи по оси X
            for (int x = ORIGIN_X + GRID_SIZE; x < getWidth(); x += GRID_SIZE) {
                int value = (x - ORIGIN_X) / GRID_SIZE;
                g2d.drawString(String.valueOf(value * GRID_SIZE), x - 10, ORIGIN_Y + 15);
            }
            for (int x = ORIGIN_X - GRID_SIZE; x > 0; x -= GRID_SIZE) {
                int value = (x - ORIGIN_X) / GRID_SIZE;
                g2d.drawString(String.valueOf(value * GRID_SIZE), x - 10, ORIGIN_Y + 15);
            }

            // Подписи по оси Y
            for (int y = ORIGIN_Y + GRID_SIZE; y < getHeight(); y += GRID_SIZE) {
                int value = (ORIGIN_Y - y) / GRID_SIZE;
                g2d.drawString(String.valueOf(value * GRID_SIZE), ORIGIN_X + 5, y + 5);
            }
            for (int y = ORIGIN_Y - GRID_SIZE; y > 0; y -= GRID_SIZE) {
                int value = (ORIGIN_Y - y) / GRID_SIZE;
                g2d.drawString(String.valueOf(value * GRID_SIZE), ORIGIN_X + 5, y + 5);
            }
        }

        private void drawPoints(Graphics2D g2d) {
            g2d.setColor(Color.BLUE);
            for (Point p : points) {
                int x = ORIGIN_X + p.x;
                int y = ORIGIN_Y + p.y;
                g2d.fillOval(x - 3, y - 3, 6, 6);
                g2d.drawString(String.format("(%d,%d)", p.x, p.y), x + 5, y - 5);
            }
        }

        public void clear() {
            canvas = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = canvas.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 800, 600);
            g2d.dispose();
            points.clear();
            repaint();
        }

        public void drawLine(int x1, int y1, int x2, int y2, String algorithm) {
            Graphics2D g2d = canvas.createGraphics();
            g2d.setColor(drawColor);

            List<Point> linePoints = new ArrayList<>();

            switch (algorithm) {
                case "Пошаговый алгоритм":
                    linePoints = stepByStepLine(x1, y1, x2, y2);
                    break;
                case "Алгоритм ЦДА":
                    linePoints = ddaLine(x1, y1, x2, y2);
                    break;
                case "Алгоритм Брезенхема (линия)":
                    linePoints = bresenhamLine(x1, y1, x2, y2);
                    break;
            }

            // Рисуем пиксели
            for (Point p : linePoints) {
                int canvasX = p.x + 400; // Смещение к центру канваса
                int canvasY = 300 - p.y; // Инвертируем Y для правильной системы координат

                if (canvasX >= 0 && canvasX < 800 && canvasY >= 0 && canvasY < 600) {
                    g2d.fillRect(canvasX, canvasY, 1, 1);
                }
            }

            g2d.dispose();
            repaint();
        }

        public void drawCircle(int xc, int yc, int radius, String algorithm) {
            if ("Алгоритм Брезенхема (окружность)".equals(algorithm)) {
                Graphics2D g2d = canvas.createGraphics();
                g2d.setColor(drawColor);

                List<Point> circlePoints = bresenhamCircle(xc, yc, radius);

                for (Point p : circlePoints) {
                    int canvasX = p.x + 400;
                    int canvasY = 300 - p.y;

                    if (canvasX >= 0 && canvasX < 800 && canvasY >= 0 && canvasY < 600) {
                        g2d.fillRect(canvasX, canvasY, 1, 1);
                    }
                }

                g2d.dispose();
                repaint();
            }
        }

        // Пошаговый алгоритм
        private List<Point> stepByStepLine(int x1, int y1, int x2, int y2) {
            List<Point> points = new ArrayList<>();

            if (x1 == x2) {
                // Вертикальная линия
                int startY = Math.min(y1, y2);
                int endY = Math.max(y1, y2);
                for (int y = startY; y <= endY; y++) {
                    points.add(new Point(x1, y));
                }
            } else {
                float m = (float)(y2 - y1) / (x2 - x1);
                float b = y1 - m * x1;

                if (Math.abs(m) <= 1) {
                    int startX = Math.min(x1, x2);
                    int endX = Math.max(x1, x2);
                    for (int x = startX; x <= endX; x++) {
                        int y = Math.round(m * x + b);
                        points.add(new Point(x, y));
                    }
                } else {
                    int startY = Math.min(y1, y2);
                    int endY = Math.max(y1, y2);
                    for (int y = startY; y <= endY; y++) {
                        int x = Math.round((y - b) / m);
                        points.add(new Point(x, y));
                    }
                }
            }

            return points;
        }

        // Алгоритм ЦДА (Digital Differential Analyzer)
        private List<Point> ddaLine(int x1, int y1, int x2, int y2) {
            List<Point> points = new ArrayList<>();

            int dx = x2 - x1;
            int dy = y2 - y1;
            int steps = Math.max(Math.abs(dx), Math.abs(dy));

            if (steps == 0) {
                points.add(new Point(x1, y1));
                return points;
            }

            float xIncrement = (float) dx / steps;
            float yIncrement = (float) dy / steps;

            float x = x1;
            float y = y1;

            for (int i = 0; i <= steps; i++) {
                points.add(new Point(Math.round(x), Math.round(y)));
                x += xIncrement;
                y += yIncrement;
            }

            return points;
        }

        // Алгоритм Брезенхема для линии
        private List<Point> bresenhamLine(int x1, int y1, int x2, int y2) {
            List<Point> points = new ArrayList<>();

            int dx = Math.abs(x2 - x1);
            int dy = Math.abs(y2 - y1);
            int sx = x1 < x2 ? 1 : -1;
            int sy = y1 < y2 ? 1 : -1;
            int err = dx - dy;

            int x = x1;
            int y = y1;

            while (true) {
                points.add(new Point(x, y));

                if (x == x2 && y == y2) break;

                int err2 = 2 * err;

                if (err2 > -dy) {
                    err -= dy;
                    x += sx;
                }

                if (err2 < dx) {
                    err += dx;
                    y += sy;
                }
            }

            return points;
        }

        // Алгоритм Брезенхема для окружности
        private List<Point> bresenhamCircle(int xc, int yc, int radius) {
            List<Point> points = new ArrayList<>();

            int x = 0;
            int y = radius;
            int d = 3 - 2 * radius;

            // Рисуем начальные точки
            drawCirclePoints(xc, yc, x, y, points);

            while (y >= x) {
                x++;

                if (d > 0) {
                    y--;
                    d = d + 4 * (x - y) + 10;
                } else {
                    d = d + 4 * x + 6;
                }

                drawCirclePoints(xc, yc, x, y, points);
            }

            return points;
        }

        private void drawCirclePoints(int xc, int yc, int x, int y, List<Point> points) {
            points.add(new Point(xc + x, yc + y));
            points.add(new Point(xc - x, yc + y));
            points.add(new Point(xc + x, yc - y));
            points.add(new Point(xc - x, yc - y));
            points.add(new Point(xc + y, yc + x));
            points.add(new Point(xc - y, yc + x));
            points.add(new Point(xc + y, yc - x));
            points.add(new Point(xc - y, yc - x));
        }
    }
}