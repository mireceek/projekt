package gui;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class BasicMalovani extends JFrame {
    private JPanel kresliciPlocha;
    private ArrayList<Shape> seznamUtvaru;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItem;
    private JPanel panelNastroju;
    private JLabel stavovyRadek;
    private int startX, startY, endX, endY;
    private int currentShapeType = Shape.LINE;

    public BasicMalovani() {
        setTitle("Jednoduché malování");
        setSize(400, 400);
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
            }
        };
        kresliciPlocha.setBackground(Color.WHITE);
        seznamUtvaru = new ArrayList<>();

        kresliciPlocha.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                endX = startX;
                endY = startY;
                repaint();
            }
        });

        kresliciPlocha.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                endX = e.getX();
                endY = e.getY();
                repaint();
            }
        });

        // Nabídka
        menuBar = new JMenuBar();
        menu = new JMenu("Menu");
        menuItem = new JMenuItem("Otevrit");
        menu.add(menuItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        // Panel s základními nástroji
        panelNastroju = new JPanel();
        JButton btnNakreslit = new JButton("Nakreslit");
        panelNastroju.add(btnNakreslit);

        btnNakreslit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Akce po stisknutí tlačítka pro nakreslení
                seznamUtvaru.add(new Shape(currentShapeType, startX, startY, endX, endY, Color.BLACK));
                repaint();
            }
        });

        // Stavový řádek nebo panel (volitelné)
        stavovyRadek = new JLabel("Stavový řádek");

        // Rozložení komponent
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(kresliciPlocha, BorderLayout.CENTER);
        getContentPane().add(panelNastroju, BorderLayout.NORTH);
        getContentPane().add(stavovyRadek, BorderLayout.SOUTH);
    }

    private class Shape {
        public static final int LINE = 0;
        public static final int SQUARE = 1;
        public static final int TRIANGLE = 2;

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
