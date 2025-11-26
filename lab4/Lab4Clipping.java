import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class Lab4Clipping extends JFrame {
    private DrawingPanel drawingPanel;
    private JButton loadDataButton;
    private JButton executeButton;
    private JComboBox<String> algorithmCombo;
    private JTextArea dataInputArea;

    private List<LineSegment> segments;
    private List<Polygon> polygons;
    private Rectangle2D.Double clippingWindow;
    private List<LineSegment> clippedSegments;
    private List<Polygon> clippedPolygons;

    private boolean dataLoaded = false;

    public Lab4Clipping() {
        setTitle("Лабораторная работа 4 - Отсечение отрезков и многоугольников (Вариант 9)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();

        segments = new ArrayList<>();
        polygons = new ArrayList<>();
        clippedSegments = new ArrayList<>();
        clippedPolygons = new ArrayList<>();

        // Загружаем пример данных по умолчанию
        loadDefaultData();
    }

    private void initializeComponents() {
        drawingPanel = new DrawingPanel();
        loadDataButton = new JButton("Загрузить данные");
        executeButton = new JButton("Выполнить отсечение");

        String[] algorithms = {"Алгоритм средней точки", "Отсечение выпуклого многоугольника"};
        algorithmCombo = new JComboBox<>(algorithms);

        dataInputArea = new JTextArea(10, 30);
        dataInputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        loadDataButton.addActionListener(e -> loadDataFromText());
        executeButton.addActionListener(e -> executeClipping());

        // Кнопка для загрузки тестовых данных
        JButton testDataButton = new JButton("Тестовые данные");
        testDataButton.addActionListener(e -> loadTestData());
    }

    private void setupLayout() {
        // Панель управления
        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("Алгоритм:"));
        controlPanel.add(algorithmCombo);
        controlPanel.add(loadDataButton);
        controlPanel.add(executeButton);

        // Панель ввода данных
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Ввод данных"));
        inputPanel.add(new JScrollPane(dataInputArea), BorderLayout.CENTER);

        JPanel inputButtonsPanel = new JPanel();
        JButton testDataButton = new JButton("Тестовые данные");
        JButton clearButton = new JButton("Очистить");
        testDataButton.addActionListener(e -> loadTestData());
        clearButton.addActionListener(e -> dataInputArea.setText(""));
        inputButtonsPanel.add(testDataButton);
        inputButtonsPanel.add(clearButton);
        inputPanel.add(inputButtonsPanel, BorderLayout.SOUTH);

        // Основной layout
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(drawingPanel), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.EAST);

        // Инструкция по формату данных
        showDataFormatHelp();
    }

    private void showDataFormatHelp() {
        String helpText =
                "Формат данных:\n" +
                        "1 строка: количество отрезков\n" +
                        "Следующие n строк: координаты отрезков (x1 y1 x2 y2)\n" +
                        "Последняя строка: отсекающее окно (xmin ymin xmax ymax)\n\n" +
                        "Пример:\n" +
                        "5\n" +
                        "50 50 200 200\n" +
                        "100 100 300 300\n" +
                        "150 50 150 250\n" +
                        "80 180 280 80\n" +
                        "120 120 250 250\n" +
                        "100 100 400 400";

        dataInputArea.setText(helpText);
    }

    private void loadDefaultData() {
        String defaultData =
                "5\n" +
                        "50 50 200 200\n" +
                        "100 100 300 300\n" +
                        "150 50 150 250\n" +
                        "80 180 280 80\n" +
                        "120 120 250 250\n" +
                        "100 100 400 400";

        dataInputArea.setText(defaultData);
        loadDataFromText();
    }

    private void loadTestData() {
        String testData =
                "8\n" +
                        "50 50 200 200\n" +
                        "100 100 300 300\n" +
                        "150 50 150 250\n" +
                        "80 180 280 80\n" +
                        "120 120 250 250\n" +
                        "350 150 450 250\n" +
                        "50 300 150 400\n" +
                        "300 50 400 150\n" +
                        "100 100 400 400";

        dataInputArea.setText(testData);
        loadDataFromText();
    }

    private void loadDataFromText() {
        try {
            String text = dataInputArea.getText();
            String[] lines = text.split("\n");

            segments.clear();
            polygons.clear();
            clippedSegments.clear();
            clippedPolygons.clear();

            if (lines.length < 2) {
                JOptionPane.showMessageDialog(this, "Недостаточно данных!");
                return;
            }

            // Чтение количества отрезков
            int n = Integer.parseInt(lines[0].trim());

            // Чтение отрезков
            for (int i = 1; i <= n; i++) {
                if (i >= lines.length) {
                    JOptionPane.showMessageDialog(this, "Недостаточно строк с отрезками!");
                    return;
                }
                String[] coords = lines[i].trim().split("\\s+");
                if (coords.length < 4) {
                    JOptionPane.showMessageDialog(this, "Ошибка в строке " + (i+1) + ": недостаточно координат");
                    return;
                }
                double x1 = Double.parseDouble(coords[0]);
                double y1 = Double.parseDouble(coords[1]);
                double x2 = Double.parseDouble(coords[2]);
                double y2 = Double.parseDouble(coords[3]);
                segments.add(new LineSegment(x1, y1, x2, y2));
            }

            // Чтение отсекающего окна
            int windowLine = n + 1;
            if (windowLine >= lines.length) {
                JOptionPane.showMessageDialog(this, "Отсутствуют координаты отсекающего окна!");
                return;
            }

            String[] windowCoords = lines[windowLine].trim().split("\\s+");
            if (windowCoords.length < 4) {
                JOptionPane.showMessageDialog(this, "Ошибка в координатах окна: недостаточно значений");
                return;
            }

            double xmin = Double.parseDouble(windowCoords[0]);
            double ymin = Double.parseDouble(windowCoords[1]);
            double xmax = Double.parseDouble(windowCoords[2]);
            double ymax = Double.parseDouble(windowCoords[3]);
            clippingWindow = new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin);

            dataLoaded = true;
            drawingPanel.repaint();

            JOptionPane.showMessageDialog(this,
                    "Данные успешно загружены!\n" +
                            "Отрезков: " + n + "\n" +
                            "Окно: (" + xmin + "," + ymin + ") - (" + xmax + "," + ymax + ")");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки данных: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void executeClipping() {
        if (!dataLoaded) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите данные!");
            return;
        }

        int selectedAlgorithm = algorithmCombo.getSelectedIndex();

        clippedSegments.clear();
        clippedPolygons.clear();

        if (selectedAlgorithm == 0) {
            // Алгоритм средней точки для отрезков
            for (LineSegment segment : segments) {
                LineSegment clipped = midpointClip(segment, clippingWindow);
                if (clipped != null) {
                    clippedSegments.add(clipped);
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Отсечение завершено!\n" +
                            "Исходных отрезков: " + segments.size() + "\n" +
                            "Видимых частей: " + clippedSegments.size());
        } else {
            // Отсечение выпуклого многоугольника
            Polygon testPolygon = createTestPolygon();
            Polygon clipped = sutherlandHodgmanPolygonClip(testPolygon, clippingWindow);
            if (clipped != null) {
                clippedPolygons.add(clipped);
            }
            JOptionPane.showMessageDialog(this, "Отсечение многоугольника завершено!");
        }

        drawingPanel.repaint();
    }

    // Остальные методы остаются без изменений (midpointClip, computeCode, cohenSutherlandClip, etc.)
    // Алгоритм средней точки для отсечения отрезков
    private LineSegment midpointClip(LineSegment segment, Rectangle2D.Double clipWindow) {
        double x1 = segment.x1, y1 = segment.y1;
        double x2 = segment.x2, y2 = segment.y2;

        int code1 = computeCode(x1, y1, clipWindow);
        int code2 = computeCode(x2, y2, clipWindow);

        while (true) {
            if ((code1 | code2) == 0) {
                // Полностью видимый отрезок
                return new LineSegment(x1, y1, x2, y2);
            } else if ((code1 & code2) != 0) {
                // Полностью невидимый отрезок
                return null;
            } else {
                // Частично видимый - используем алгоритм Коэна-Сазерленда
                return cohenSutherlandClip(segment, clipWindow);
            }
        }
    }

    // Коды областей для алгоритма Коэна-Сазерленда
    private int computeCode(double x, double y, Rectangle2D.Double clipWindow) {
        int code = 0;
        if (x < clipWindow.getX()) code |= 1; // слева
        if (x > clipWindow.getX() + clipWindow.getWidth()) code |= 2; // справа
        if (y < clipWindow.getY()) code |= 4; // снизу
        if (y > clipWindow.getY() + clipWindow.getHeight()) code |= 8; // сверху
        return code;
    }

    // Алгоритм Коэна-Сазерленда для отсечения отрезков
    private LineSegment cohenSutherlandClip(LineSegment segment, Rectangle2D.Double clipWindow) {
        double x1 = segment.x1, y1 = segment.y1;
        double x2 = segment.x2, y2 = segment.y2;

        int code1 = computeCode(x1, y1, clipWindow);
        int code2 = computeCode(x2, y2, clipWindow);

        while (true) {
            if ((code1 | code2) == 0) {
                return new LineSegment(x1, y1, x2, y2);
            } else if ((code1 & code2) != 0) {
                return null;
            } else {
                int codeOut = code1 != 0 ? code1 : code2;
                double x = 0, y = 0;

                if ((codeOut & 8) != 0) { // сверху
                    x = x1 + (x2 - x1) * (clipWindow.getY() + clipWindow.getHeight() - y1) / (y2 - y1);
                    y = clipWindow.getY() + clipWindow.getHeight();
                } else if ((codeOut & 4) != 0) { // снизу
                    x = x1 + (x2 - x1) * (clipWindow.getY() - y1) / (y2 - y1);
                    y = clipWindow.getY();
                } else if ((codeOut & 2) != 0) { // справа
                    y = y1 + (y2 - y1) * (clipWindow.getX() + clipWindow.getWidth() - x1) / (x2 - x1);
                    x = clipWindow.getX() + clipWindow.getWidth();
                } else if ((codeOut & 1) != 0) { // слева
                    y = y1 + (y2 - y1) * (clipWindow.getX() - x1) / (x2 - x1);
                    x = clipWindow.getX();
                }

                if (codeOut == code1) {
                    x1 = x;
                    y1 = y;
                    code1 = computeCode(x1, y1, clipWindow);
                } else {
                    x2 = x;
                    y2 = y;
                    code2 = computeCode(x2, y2, clipWindow);
                }
            }
        }
    }

    // Алгоритм Сазерленда-Ходжмана для отсечения многоугольника
    private Polygon sutherlandHodgmanPolygonClip(Polygon subjectPolygon, Rectangle2D.Double clipWindow) {
        List<Point2D.Double> outputList = new ArrayList<>();

        // Преобразуем полигон в список точек
        for (int i = 0; i < subjectPolygon.npoints; i++) {
            outputList.add(new Point2D.Double(subjectPolygon.xpoints[i], subjectPolygon.ypoints[i]));
        }

        // Отсекаем по каждой границе окна
        outputList = clipAgainstEdge(outputList, clipWindow.getX(), clipWindow.getY(),
                clipWindow.getX() + clipWindow.getWidth(), clipWindow.getY(), true); // нижняя
        outputList = clipAgainstEdge(outputList, clipWindow.getX() + clipWindow.getWidth(), clipWindow.getY(),
                clipWindow.getX() + clipWindow.getWidth(), clipWindow.getY() + clipWindow.getHeight(), false); // правая
        outputList = clipAgainstEdge(outputList, clipWindow.getX() + clipWindow.getWidth(), clipWindow.getY() + clipWindow.getHeight(),
                clipWindow.getX(), clipWindow.getY() + clipWindow.getHeight(), true); // верхняя
        outputList = clipAgainstEdge(outputList, clipWindow.getX(), clipWindow.getY() + clipWindow.getHeight(),
                clipWindow.getX(), clipWindow.getY(), false); // левая

        if (outputList.size() < 3) return null;

        // Преобразуем обратно в Polygon
        int[] xpoints = new int[outputList.size()];
        int[] ypoints = new int[outputList.size()];
        for (int i = 0; i < outputList.size(); i++) {
            xpoints[i] = (int) Math.round(outputList.get(i).x);
            ypoints[i] = (int) Math.round(outputList.get(i).y);
        }

        return new Polygon(xpoints, ypoints, outputList.size());
    }

    private List<Point2D.Double> clipAgainstEdge(List<Point2D.Double> inputList,
                                                 double edgeX1, double edgeY1,
                                                 double edgeX2, double edgeY2,
                                                 boolean isBottomOrTop) {
        List<Point2D.Double> outputList = new ArrayList<>();

        if (inputList.isEmpty()) return outputList;

        Point2D.Double s = inputList.get(inputList.size() - 1);

        for (Point2D.Double e : inputList) {
            if (isInside(e, edgeX1, edgeY1, edgeX2, edgeY2, isBottomOrTop)) {
                if (!isInside(s, edgeX1, edgeY1, edgeX2, edgeY2, isBottomOrTop)) {
                    outputList.add(computeIntersection(s, e, edgeX1, edgeY1, edgeX2, edgeY2));
                }
                outputList.add(e);
            } else if (isInside(s, edgeX1, edgeY1, edgeX2, edgeY2, isBottomOrTop)) {
                outputList.add(computeIntersection(s, e, edgeX1, edgeY1, edgeX2, edgeY2));
            }
            s = e;
        }

        return outputList;
    }

    private boolean isInside(Point2D.Double p, double edgeX1, double edgeY1,
                             double edgeX2, double edgeY2, boolean isBottomOrTop) {
        if (isBottomOrTop) {
            // Для нижней и верхней границ - проверяем по Y
            return p.y >= edgeY1;
        } else {
            // Для левой и правой границ - проверяем по X
            return p.x >= edgeX1;
        }
    }

    private Point2D.Double computeIntersection(Point2D.Double s, Point2D.Double e,
                                               double edgeX1, double edgeY1,
                                               double edgeX2, double edgeY2) {
        double x1 = s.x, y1 = s.y;
        double x2 = e.x, y2 = e.y;

        if (edgeX1 == edgeX2) { // вертикальная граница
            double x = edgeX1;
            double y = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
            return new Point2D.Double(x, y);
        } else { // горизонтальная граница
            double y = edgeY1;
            double x = x1 + (x2 - x1) * (y - y1) / (y2 - y1);
            return new Point2D.Double(x, y);
        }
    }

    // Создание тестового многоугольника для демонстрации
    private Polygon createTestPolygon() {
        int[] xpoints = {150, 250, 300, 200, 100};
        int[] ypoints = {100, 80, 200, 250, 180};
        return new Polygon(xpoints, ypoints, 5);
    }

    // Класс для представления отрезка
    private static class LineSegment {
        double x1, y1, x2, y2;

        LineSegment(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    // Панель для рисования
    private class DrawingPanel extends JPanel {
        private final int MARGIN = 50;
        private final double SCALE = 2.0;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Рисуем систему координат
            drawCoordinateSystem(g2d);

            if (clippingWindow != null) {
                // Рисуем отсекающее окно
                g2d.setColor(new Color(0, 100, 0));
                g2d.setStroke(new BasicStroke(2));
                drawRectangle(g2d, clippingWindow);

                // Рисуем исходные отрезки
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(1));
                for (LineSegment segment : segments) {
                    drawLine(g2d, segment);
                }

                // Рисуем отсеченные отрезки
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(3));
                for (LineSegment segment : clippedSegments) {
                    drawLine(g2d, segment);
                }

                // Рисуем отсеченные многоугольники
                g2d.setColor(new Color(0, 0, 200));
                g2d.setStroke(new BasicStroke(2));
                for (Polygon poly : clippedPolygons) {
                    g2d.drawPolygon(poly);
                }

                // Рисуем тестовый многоугольник (для демонстрации)
                if (algorithmCombo.getSelectedIndex() == 1 && clippedPolygons.isEmpty()) {
                    Polygon testPoly = createTestPolygon();
                    g2d.setColor(new Color(200, 0, 200, 100));
                    g2d.fillPolygon(testPoly);
                }
            }

            // Легенда
            drawLegend(g2d);
        }

        private void drawCoordinateSystem(Graphics2D g2d) {
            int width = getWidth();
            int height = getHeight();

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));

            // Оси
            g2d.drawLine(MARGIN, height - MARGIN, width - MARGIN, height - MARGIN); // X-axis
            g2d.drawLine(MARGIN, MARGIN, MARGIN, height - MARGIN); // Y-axis

            // Стрелки
            g2d.drawLine(width - MARGIN, height - MARGIN, width - MARGIN - 10, height - MARGIN - 5);
            g2d.drawLine(width - MARGIN, height - MARGIN, width - MARGIN - 10, height - MARGIN + 5);
            g2d.drawLine(MARGIN, MARGIN, MARGIN - 5, MARGIN + 10);
            g2d.drawLine(MARGIN, MARGIN, MARGIN + 5, MARGIN + 10);

            // Подписи
            g2d.drawString("X", width - MARGIN + 5, height - MARGIN);
            g2d.drawString("Y", MARGIN, MARGIN - 10);
        }

        private void drawLine(Graphics2D g2d, LineSegment segment) {
            int x1 = transformX(segment.x1);
            int y1 = transformY(segment.y1);
            int x2 = transformX(segment.x2);
            int y2 = transformY(segment.y2);
            g2d.drawLine(x1, y1, x2, y2);
        }

        private void drawRectangle(Graphics2D g2d, Rectangle2D.Double rect) {
            int x = transformX(rect.getX());
            int y = transformY(rect.getY() + rect.getHeight());
            int width = (int)(rect.getWidth() * SCALE);
            int height = (int)(rect.getHeight() * SCALE);
            g2d.drawRect(x, y, width, height);
        }

        private int transformX(double x) {
            return (int)(MARGIN + x * SCALE);
        }

        private int transformY(double y) {
            return (int)(getHeight() - MARGIN - y * SCALE);
        }

        private void drawLegend(Graphics2D g2d) {
            int legendX = getWidth() - 200;
            int legendY = 50;

            g2d.setColor(Color.BLACK);
            g2d.drawString("Легенда:", legendX, legendY);

            g2d.setColor(new Color(0, 100, 0));
            g2d.drawString("□ Отсекающее окно", legendX, legendY + 20);

            g2d.setColor(Color.RED);
            g2d.drawString("--- Исходные отрезки", legendX, legendY + 40);

            g2d.setColor(Color.BLUE);
            g2d.drawString("--- Отсеченные отрезки", legendX, legendY + 60);

            g2d.setColor(new Color(0, 0, 200));
            g2d.drawString("▣ Отсеченный многоугольник", legendX, legendY + 80);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Lab4Clipping().setVisible(true);
        });
    }
}