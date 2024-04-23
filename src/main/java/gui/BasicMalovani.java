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
                Graphics2D g2 = (Graphics2D) g;
                for (int i = 0; i < seznamUtvaru.size(); i++) {
                    Shape shape = seznamUtvaru.get(i);
                    g2.setColor(shape.getColor());
                    g2.setStroke(new BasicStroke(shape.getWidth())); // Nastavit tloušťku čáry
                    switch (shape.getType()) {
                        case Shape.LINE:
                            g2.drawLine(shape.getX1(), shape.getY1(), shape.getX2(), shape.getY2());
                            break;
                        case Shape.RECTANGLE:
                            int width = Math.abs(shape.getX2() - shape.getX1());
                            int height = Math.abs(shape.getY2() - shape.getY1());
                            int startX = Math.min(shape.getX1(), shape.getX2());
                            int startY = Math.min(shape.getY1(), shape.getY2());
                            if (shape.isFilled()) {
                                g2.fillRect(startX, startY, width, height);
                            } else {
                                g2.drawRect(startX, startY, width, height);
                            }
                            break;
                        case Shape.OVAL:
                            width = Math.abs(shape.getX2() - shape.getX1());
                            height = Math.abs(shape.getY2() - shape.getY1());
                            startX = Math.min(shape.getX1(), shape.getX2());
                            startY = Math.min(shape.getY1(), shape.getY2());
                            if (shape.isFilled()) {
                                g2.fillOval(startX, startY, width, height);
                            } else {
                                g2.drawOval(startX, startY, width, height);
                            }
                            break;
                        case Shape.TRIANGLE:
                            int[] xPoints = {shape.getX1(), shape.getX2(), (shape.getX1() + shape.getX2()) / 2};
                            int[] yPoints = {shape.getY2(), shape.getY2(), shape.getY1()};
                            if (shape.isFilled()) {
                                g2.fillPolygon(xPoints, yPoints, 3);
                            } else {
                                g2.drawPolygon(xPoints, yPoints, 3);
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
                        listModel.addElement(getShapeName(currentShapeType) + " " + (seznamUtvaru.size() + 1)); // Přidat název tvaru do seznamu
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
                    // Zde umožníte uživateli zadat nové souřadnice pro změnu pozice objektu
                    String xCoord = JOptionPane.showInputDialog(BasicMalovani.this, "Zadej novou X souřadnici:");
                    String yCoord = JOptionPane.showInputDialog(BasicMalovani.this, "Zadej novou Y souřadnici:");
                    try {
                        int newX = Integer.parseInt(xCoord);
                        int newY = Integer.parseInt(yCoord);
                        seznamUtvaru.get(selectedIndex).setX1(newX);
                        seznamUtvaru.get(selectedIndex).setY1(newY);
                        repaint();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(BasicMalovani.this, "Neplatné souřadnice!", "Chyba", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JButton deleteShapeButton = new JButton("Smazat tvar");
        deleteShapeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = seznamTvaruList.getSelectedIndex();
                if (selectedIndex != -1) {
                    seznamUtvaru.remove(selectedIndex);
                    listModel.remove(selectedIndex);
                    repaint();
                }
            }
        });

        JButton changeThicknessButton = new JButton("Změnit tloušťku čáry");
        changeThicknessButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = seznamTvaruList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String thicknessInput = JOptionPane.showInputDialog(BasicMalovani.this, "Zadej novou tloušťku čáry:");
                    try {
                        int newThickness = Integer.parseInt(thicknessInput);
                        if (newThickness > 0) {
                            seznamUtvaru.get(selectedIndex).setWidth(newThickness);
                            repaint();
                        } else {
                            JOptionPane.showMessageDialog(BasicMalovani.this, "Tloušťka čáry musí být kladné číslo!", "Chyba", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(BasicMalovani.this, "Neplatná tloušťka čáry!", "Chyba", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        panelNastroju.add(changeColorButton);
        panelNastroju.add(changePositionButton);
        panelNastroju.add(deleteShapeButton);
        panelNastroju.add(changeThicknessButton);

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

            for (int i = 0; i < seznamUtvaru.size(); i++) {
                Shape shape = seznamUtvaru.get(i);
                Element shapeElement = doc.createElement("Shape");
                shapeElement.setAttribute("type", Integer.toString(shape.getType()));
                shapeElement.setAttribute("x1", Integer.toString(shape.getX1()));
                shapeElement.setAttribute("y1", Integer.toString(shape.getY1()));
                shapeElement.setAttribute("x2", Integer.toString(shape.getX2()));
                shapeElement.setAttribute("y2", Integer.toString(shape.getY2()));
                shapeElement.setAttribute("color", "#" + Integer.toHexString(shape.getColor().getRGB()).substring(2));
                shapeElement.setAttribute("filled", Boolean.toString(shape.isFilled()));
                shapeElement.setAttribute("width", Integer.toString(shape.getWidth())); // Uložit tloušťku čáry
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
                saved = true; // Nastavit, že změny byly uloženy
                stavovyRadek.setText("Uloženo do " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromXML() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Otevřít XML soubor");
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(selectedFile);
                doc.getDocumentElement().normalize();

                NodeList shapeList = doc.getElementsByTagName("Shape");
                seznamUtvaru.clear();
                listModel.clear();

                for (int temp = 0; temp < shapeList.getLength(); temp++) {
                    Node shapeNode = shapeList.item(temp);
                    if (shapeNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element shapeElement = (Element) shapeNode;
                        int type = Integer.parseInt(shapeElement.getAttribute("type"));
                        int x1 = Integer.parseInt(shapeElement.getAttribute("x1"));
                        int y1 = Integer.parseInt(shapeElement.getAttribute("y1"));
                        int x2 = Integer.parseInt(shapeElement.getAttribute("x2"));
                        int y2 = Integer.parseInt(shapeElement.getAttribute("y2"));
                        Color color = Color.decode(shapeElement.getAttribute("color"));
                        boolean filled = Boolean.parseBoolean(shapeElement.getAttribute("filled"));
                        int width = Integer.parseInt(shapeElement.getAttribute("width")); // Načíst tloušťku čáry

                        seznamUtvaru.add(new Shape(type, x1, y1, x2, y2, color, filled));
                        seznamUtvaru.get(seznamUtvaru.size() - 1).setWidth(width); // Nastavit tloušťku čáry
                        listModel.addElement(getShapeName(type)); // Přidat název tvaru do seznamu
                    }
                }
                saved = true; // Nastavit, že změny byly uloženy
                stavovyRadek.setText("Načteno ze " + selectedFile.getAbsolutePath());
                repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImage(String format) {
        BufferedImage image = new BufferedImage(kresliciPlocha.getWidth(), kresliciPlocha.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        kresliciPlocha.print(g2);
        g2.dispose();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Uložit jako " + format.toUpperCase());
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith("." + format)) {
                filePath += "." + format; // Ujistíme se, že soubor má správnou příponu
            }
            try {
                ImageIO.write(image, format, new File(filePath));
                saved = true; // Nastavit, že změny byly uloženy
                stavovyRadek.setText("Uloženo do " + filePath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void onClose() {
        if (!saved) {
            int option = JOptionPane.showConfirmDialog(BasicMalovani.this,
                    "Chcete uložit provedené změny?",
                    "Uložit změny",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                saveToXML();
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }
        dispose();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BasicMalovani app = new BasicMalovani();
                app.setVisible(true);
            }
        });
    }
}


