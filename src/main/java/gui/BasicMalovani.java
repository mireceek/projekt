package gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.*;
import javax.imageio.ImageIO;

public class BasicMalovani extends JFrame {
    private JPanel kresliciPlocha;
    private ArrayList<Shape> seznamUtvaru;
    private JLabel lblLine;
    private JLabel lblRectangle;
    private JLabel lblOval;
    private JLabel lblTriangle;
    private JPanel panelNastroju;
    private JLabel stavovyRadek;
    private int startX, startY, endX, endY;
    private int currentShapeType = Shape.LINE;
    private Color currentColor = Color.BLACK;
    private boolean saved = true; // Indikátor, zda byly provedeny změny

    public BasicMalovani() {
        setTitle("Jednoduché malování");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Změna chování na zavření

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

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
                        case Shape.RECTANGLE:
                            int width = Math.abs(shape.getX2() - shape.getX1());
                            int height = Math.abs(shape.getY2() - shape.getY1());
                            int startX = Math.min(shape.getX1(), shape.getX2());
                            int startY = Math.min(shape.getY1(), shape.getY2());
                            if (shape.isFilled()) {
                                g.fillRect(startX, startY, width, height);
                            } else {
                                g.drawRect(startX, startY, width, height);
                            }
                            break;
                        case Shape.OVAL:
                            width = Math.abs(shape.getX2() - shape.getX1());
                            height = Math.abs(shape.getY2() - shape.getY1());
                            startX = Math.min(shape.getX1(), shape.getX2());
                            startY = Math.min(shape.getY1(), shape.getY2());
                            if (shape.isFilled()) {
                                g.fillOval(startX, startY, width, height);
                            } else {
                                g.drawOval(startX, startY, width, height);
                            }
                            break;
                        case Shape.TRIANGLE:
                            int[] xPoints = {shape.getX1(), shape.getX2(), (shape.getX1() + shape.getX2()) / 2};
                            int[] yPoints = {shape.getY2(), shape.getY2(), shape.getY1()};
                            if (shape.isFilled()) {
                                g.fillPolygon(xPoints, yPoints, 3);
                            } else {
                                g.drawPolygon(xPoints, yPoints, 3);
                            }
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
                switch (currentShapeType) {
                    case Shape.LINE:
                    case Shape.RECTANGLE:
                    case Shape.OVAL:
                    case Shape.TRIANGLE:
                        seznamUtvaru.add(new Shape(currentShapeType, startX, startY, endX, endY, currentColor, false));
                        saved = false; // Nastavit, že provedené změny nebyly uloženy
                        break;
                    default:
                        break;
                }
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                // Neprovádíme žádnou akci při puštění tlačítka myši
            }
        });

        kresliciPlocha.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                endX = e.getX();
                endY = e.getY();
                seznamUtvaru.get(seznamUtvaru.size() - 1).setX2(endX);
                seznamUtvaru.get(seznamUtvaru.size() - 1).setY2(endY);
                repaint();
            }
        });

        lblLine = createShapeLabel("Přímka");
        lblRectangle = createShapeLabel("Obdélník");
        lblOval = createShapeLabel("Ovál");
        lblTriangle = createShapeLabel("Trojúhelník");

        panelNastroju = new JPanel();
        panelNastroju.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panelNastroju.add(lblLine);
        panelNastroju.add(lblRectangle);
        panelNastroju.add(lblOval);
        panelNastroju.add(lblTriangle);

        stavovyRadek = new JLabel("Stavový řádek");

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(kresliciPlocha, BorderLayout.CENTER);
        getContentPane().add(panelNastroju, BorderLayout.NORTH);
        getContentPane().add(stavovyRadek, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Soubor");
        JMenuItem saveJPGMenuItem = new JMenuItem("Uložit jako JPG");
        JMenuItem savePNGMenuItem = new JMenuItem("Uložit jako PNG");
        JMenuItem exitMenuItem = new JMenuItem("Ukončit aplikaci");

        saveJPGMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage("jpg");
            }
        });

        savePNGMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage("png");
            }
        });

        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose(); // Při kliknutí na "Ukončit aplikaci" zavoláme metodu onClose()
            }
        });

        fileMenu.add(saveJPGMenuItem);
        fileMenu.add(savePNGMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private JLabel createShapeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                label.setBackground(Color.LIGHT_GRAY);
            }

            public void mouseExited(MouseEvent e) {
                label.setBackground(null);
            }

            public void mouseClicked(MouseEvent e) {
                switch (text) {
                    case "Přímka":
                        currentShapeType = Shape.LINE;
                        break;
                    case "Obdélník":
                        currentShapeType = Shape.RECTANGLE;
                        break;
                    case "Ovál":
                        currentShapeType = Shape.OVAL;
                        break;
                    case "Trojúhelník":
                        currentShapeType = Shape.TRIANGLE;
                        break;
                    default:
                        break;
                }
            }
        });
        return label;
    }

    private void saveImage(String format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Uložit jako");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.endsWith("." + format)) {
                    filePath += "." + format;
                }
                File imageFile = new File(filePath);
                ImageIO.write(createImage(), format, imageFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private BufferedImage createImage() {
        BufferedImage image = new BufferedImage(kresliciPlocha.getWidth(), kresliciPlocha.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        kresliciPlocha.printAll(g2d);
        g2d.dispose();
        return image;
    }

    private class Shape {
        public static final int LINE = 0;
        public static final int RECTANGLE = 1;
        public static final int OVAL = 2;
        public static final int TRIANGLE = 3;

        private int type;
        private int x1, y1, x2, y2;
        private Color color;
        private boolean filled;

        public Shape(int type, int x1, int y1, int x2, int y2, Color color, boolean filled) {
            this.type = type;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
            this.filled = filled;
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

        public boolean isFilled() {
            return filled;
        }
    }

    private void onClose() {
        if (!saved) {
            int result = JOptionPane.showConfirmDialog(this, "Chcete uložit změny?", "Upozornění", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                saveImage("jpg"); // Uložit jako JPG, můžete změnit na požadovaný formát
                dispose(); // Ukončit aplikaci po uložení
            } else if (result == JOptionPane.NO_OPTION) {
                dispose(); // Ukončit aplikaci bez uložení
            }
            // Pokud uživatel vybere "Cancel", zůstane aplikace otevřená
        } else {
            dispose(); // Ukončit aplikaci, pokud nebyly provedeny žádné změny
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
