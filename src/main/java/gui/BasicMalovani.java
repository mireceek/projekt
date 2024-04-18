package gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.*;

public class BasicMalovani extends JFrame {
    private JPanel kresliciPlocha;
    private JList<String> seznamTvaruList;
    private DefaultListModel<String> listModel;
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
        setSize(800, 500);
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

        listModel = new DefaultListModel<>();
        seznamTvaruList = new JList<>(listModel);
        seznamTvaruList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        seznamTvaruList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int selectedIndex = seznamTvaruList.getSelectedIndex();
                if (selectedIndex != -1) {
                    Shape selectedShape = seznamUtvaru.get(selectedIndex); // Získání vybraného tvaru
                    selectedShape.setColor(Color.RED); // Nastavení barvy vybraného tvaru
                    repaint();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(seznamTvaruList);

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
                        listModel.addElement(getShapeName(currentShapeType)); // Přidat název tvaru do seznamu
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
        getContentPane().add(scrollPane, BorderLayout.EAST); // Přidat seznam tvarů na pravou stranu
        getContentPane().add(panelNastroju, BorderLayout.NORTH);
        getContentPane().add(stavovyRadek, BorderLayout.SOUTH);

        JButton changeColorButton = new JButton("Změnit barvu");
        changeColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = seznamTvaruList.getSelectedIndex();
                if (selectedIndex != -1) {
                    Color newColor = JColorChooser.showDialog(BasicMalovani.this, "Vyberte barvu", seznamUtvaru.get(selectedIndex).getColor());
                    if (newColor != null) {
                        seznamUtvaru.get(selectedIndex).setColor(newColor);
                        repaint();
                    }
                }
            }
        });

        JButton changePositionButton = new JButton("Změnit pozici");
        changePositionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = seznamTvaruList.getSelectedIndex();
                if (selectedIndex != -1) {
                    // Zde můžete implementovat logiku pro změnu pozice objektu
                    // Například umožnit uživateli přetáhnout objekt myší na novou pozici
                }
            }
        });

        panelNastroju.add(changeColorButton);
        panelNastroju.add(changePositionButton);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Soubor");
        JMenuItem saveJPGMenuItem = new JMenuItem("Uložit jako JPG");
        JMenuItem savePNGMenuItem = new JMenuItem("Uložit jako PNG");
        JMenuItem saveXMLMenuItem = new JMenuItem("Uložit jako XML");
        JMenuItem openXMLMenuItem = new JMenuItem("Otevřít XML");
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

        saveXMLMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveToXML();
            }
        });

        openXMLMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadFromXML();
            }
        });

        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        });

        fileMenu.add(saveJPGMenuItem);
        fileMenu.add(savePNGMenuItem);
        fileMenu.add(saveXMLMenuItem);
        fileMenu.add(openXMLMenuItem);
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

    private void saveToXML() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element rootElement = doc.createElement("Shapes");
            doc.appendChild(rootElement);

            for (Shape shape : seznamUtvaru) {
                Element shapeElement = doc.createElement("Shape");
                shapeElement.setAttribute("type", Integer.toString(shape.getType()));
                shapeElement.setAttribute("x1", Integer.toString(shape.getX1()));
                shapeElement.setAttribute("y1", Integer.toString(shape.getY1()));
                shapeElement.setAttribute("x2", Integer.toString(shape.getX2()));
                shapeElement.setAttribute("y2", Integer.toString(shape.getY2()));
                shapeElement.setAttribute("color", "#" + Integer.toHexString(shape.getColor().getRGB()).substring(2));
                shapeElement.setAttribute("filled", Boolean.toString(shape.isFilled()));
                rootElement.appendChild(shapeElement);
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Uložit jako XML");
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".xml")) {
                    filePath += ".xml"; // Ujistíme se, že soubor má příponu .xml
                }
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(filePath));
                transformer.transform(source, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromXML() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Otevřít XML soubor");
            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File xmlFile = fileChooser.getSelectedFile();
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlFile);

                doc.getDocumentElement().normalize();

                NodeList shapeList = doc.getElementsByTagName("Shape");

                for (int temp = 0; temp < shapeList.getLength(); temp++) {
                    org.w3c.dom.Node shapeNode = shapeList.item(temp);
                    if (shapeNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        Element shapeElement = (Element) shapeNode;
                        int type = Integer.parseInt(shapeElement.getAttribute("type"));
                        int x1 = Integer.parseInt(shapeElement.getAttribute("x1"));
                        int y1 = Integer.parseInt(shapeElement.getAttribute("y1"));
                        int x2 = Integer.parseInt(shapeElement.getAttribute("x2"));
                        int y2 = Integer.parseInt(shapeElement.getAttribute("y2"));
                        Color color = Color.decode(shapeElement.getAttribute("color"));
                        boolean filled = Boolean.parseBoolean(shapeElement.getAttribute("filled"));
                        seznamUtvaru.add(new Shape(type, x1, y1, x2, y2, color, filled));
                        listModel.addElement(getShapeName(type));
                    }
                }
                kresliciPlocha.repaint(); // Oprava: Repaint se volá na JPanel místo na JFrame
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        private int width = 1; // Šířka tvaru

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

        public void setColor(Color color) {
            this.color = color;
        }

        public boolean isFilled() {
            return filled;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }
    }

    private String getShapeName(int type) {
        switch (type) {
            case Shape.LINE:
                return "Přímka";
            case Shape.RECTANGLE:
                return "Obdélník";
            case Shape.OVAL:
                return "Ovál";
            case Shape.TRIANGLE:
                return "Trojúhelník";
            default:
                return "";
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
