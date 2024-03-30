package gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class BasicMalovani extends JFrame {
    private JPanel kresliciPlocha;
    private ArrayList<Shape> seznamUtvaru;
    private JLabel lblLine;
    private JLabel lblSquare;
    private JLabel lblTriangle;
    private JLabel lblPen; // Nový nástroj - pero
    private JPanel panelNastroju;
    private JLabel stavovyRadek;
    private int startX, startY, endX, endY;
    private int currentShapeType = Shape.LINE;
    private Shape currentShape;

    public BasicMalovani() {
        setTitle("Jednoduché malování");
        setSize(600, 500); // Zvětšení velikosti okna
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Kreslici plocha
        kresliciPlocha = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (Shape shape : seznamUtvaru) {
                    g.setColor(shape.getColor());
                    switch (shape.getType()) {
                        case Shape.LINE:
                            g.drawLine(shape.getX1(), shape.getY1(), shape.getX2(), shape.getY2());
                            break;
                        case Shape.SQUARE:
                            int width = Math.abs(shape.getX2() - shape.getX1());
                            int height = Math.abs(shape.getY2() - shape.getY1());
                            int startX = Math.min(shape.getX1(), shape.getX2());
                            int startY = Math.min(shape.getY1(), shape.getY2());
                            g.drawRect(startX, startY, width, height);
                            break;
                        case Shape.TRIANGLE:
                            int[] xPoints = {shape.getX1(), (shape.getX1() + shape.getX2()) / 2, shape.getX2()};
                            int[] yPoints = {shape.getY2(), shape.getY1(), shape.getY2()};
                            g.drawPolygon(xPoints, yPoints, 3);
                            break;
                        default:
                            break;
                    }
                }

                if (currentShape != null) {
                    g.setColor(currentShape.getColor());
                    switch (currentShape.getType()) {
                        case Shape.LINE:
                            g.drawLine(currentShape.getX1(), currentShape.getY1(), currentShape.getX2(), currentShape.getY2());
                            break;
                        case Shape.SQUARE:
                            int width = Math.abs(currentShape.getX2() - currentShape.getX1());
                            int height = Math.abs(currentShape.getY2() - currentShape.getY1());
                            int startX = Math.min(currentShape.getX1(), currentShape.getX2());
                            int startY = Math.min(currentShape.getY1(), currentShape.getY2());
                            g.drawRect(startX, startY, width, height);
                            break;
                        case Shape.TRIANGLE:
                            int[] xPoints = {currentShape.getX1(), (currentShape.getX1() + currentShape.getX2()) / 2, currentShape.getX2()};
                            int[] yPoints = {currentShape.getY2(), currentShape.getY1(), currentShape.getY2()};
                            g.drawPolygon(xPoints, yPoints, 3);
                            break;
                        case Shape.PEN:
                            g.drawLine(currentShape.getX1(), currentShape.getY1(), currentShape.getX2(), currentShape.getY2());
                            break;
                        default:
                            break;
                    }
                }
            }
        };
        kresliciPlocha.setBackground(Color.WHITE);
        seznamUtvaru = new ArrayList<>();

        kresliciPlocha.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (currentShapeType == Shape.PEN) {
                    startX = e.getX();
                    startY = e.getY();
                    currentShape = new Shape(currentShapeType, startX, startY, startX, startY, Color.BLACK);
                    seznamUtvaru.add(currentShape); // Přidáváme aktuální čáru do seznamu
                } else {
                    startX = e.getX();
                    startY = e.getY();
                    endX = startX;
                    endY = startY;
                    currentShape = new Shape(currentShapeType, startX, startY, endX, endY, Color.BLACK);
                }
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                if (currentShapeType != Shape.PEN) {
                    seznamUtvaru.add(currentShape);
                    currentShape = null; // Uvolníme aktuální tvar
                }
            }
        });

        kresliciPlocha.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (currentShapeType == Shape.PEN) {
                    endX = e.getX();
                    endY = e.getY();
                    currentShape.setX2(endX);
                    currentShape.setY2(endY);
                    startX = endX;
                    startY = endY;
                    repaint();
                } else {
                    endX = e.getX();
                    endY = e.getY();
                    if (currentShape != null) {
                        currentShape.setX2(endX);
                        currentShape.setY2(endY);
                    }
                    repaint();
                }
            }
        });

        // Nabídka tvarů v textovém popisu s efektem hover
        lblLine = createShapeLabel("Přímka");
        lblSquare = createShapeLabel("Čtverec");
        lblTriangle = createShapeLabel("Trojúhelník");
        lblPen = createShapeLabel("Pero"); // Přidání popisu pro pero

        // Panel s nástroji
        panelNastroju = new JPanel();
        panelNastroju.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panelNastroju.add(lblLine);
        panelNastroju.add(lblSquare);
        panelNastroju.add(lblTriangle);
        panelNastroju.add(lblPen); // Přidání popisu pro pero

        // Stavový řádek
        stavovyRadek = new JLabel("Stavový řádek");

        // Rozložení komponent
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(kresliciPlocha, BorderLayout.CENTER);
        getContentPane().add(panelNastroju, BorderLayout.NORTH);
        getContentPane().add(stavovyRadek, BorderLayout.SOUTH);
    }

    private JLabel createShapeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Přidání odsazení
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Změna kurzoru na ruku
        label.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                label.setBackground(Color.LIGHT_GRAY); // Změna barvy pozadí při najetí myší
            }

            public void mouseExited(MouseEvent e) {
                label.setBackground(null); // Vrácení původní barvy pozadí po opuštění myší
            }

            public void mouseClicked(MouseEvent e) {
                seznamUtvaru.clear(); // Vymažeme seznam tvarů při změně nástroje
                switch (text) {
                    case "Přímka":
                        currentShapeType = Shape.LINE;
                        break;
                    case "Čtverec":
                        currentShapeType = Shape.SQUARE;
                        break;
                    case "Trojúhelník":
                        currentShapeType = Shape.TRIANGLE;
                        break;
                    case "Pero":
                        currentShapeType = Shape.PEN;
                        break;
                    default:
                        break;
                }
            }
        });
        return label;
    }

    private class Shape {
        public static final int LINE = 0;
        public static final int SQUARE = 1;
        public static final int TRIANGLE = 2;
        public static final int PEN = 3; // Konstanta pro pero

        private int type;
        private int x1, y1, x2, y2;
        private Color color;

        public Shape(int type, int x1, int y1, int x2, int y2, Color color) {
            this.type = type;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
        }

        public int getType() {
            return type;
        }

        public int getX1() {
            return x1;
        }

        public int getY1() {
            return y1;
        }

        public int getX2() {
            return x2;
        }

        public int getY2() {
            return y2;
        }

        public void setX2(int x2) {
            this.x2 = x2;
        }

        public void setY2(int y2) {
            this.y2 = y2;
        }

        public Color getColor() {
            return color;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BasicMalovani malovani = new BasicMalovani();
                malovani.setVisible(true);
            }
        });
    }
}



