package projekt;


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontProvider;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * FXML Controller class
 *
 * @author Kinga
 */
public class MainWindowController implements Initializable {

    @FXML
    private TextField wzor;

    @FXML
    private WebView podglad;
    @FXML
    private LineChart<Number, Number> chart;
    NumberAxis xAxis;
    NumberAxis yAxis;

    WebEngine webEngine;
    WebEngine webEngine2;
    boolean isUp = false;
    @FXML
    private TextField lSide;
    @FXML
    private TextField rSide;
    @FXML
    private WebView rezultat;
    StringBuilder line;
    StringBuilder html;
    @FXML
    private JFXHamburger hamburger;
    @FXML
    private JFXDrawer drawer;
    @FXML
    private AnchorPane box;

    @FXML
    void wcisniecieKlawisza(KeyEvent event) {
        StringBuilder line = new StringBuilder();
        isUp = false;
        for (int i = 0; i < wzor.getText().length(); i++) {
            if (wzor.getText().charAt(i) == '^') {
                line.append("<sup>");
                isUp = true;
            }
            if (wzor.getText().charAt(i) == '<') {
                if (isUp) {
                    line.append("</sup>").append("&lt;");
                } else {
                    line.append("&lt;");
                }
                isUp = false;
            }
            if (wzor.getText().charAt(i) == '>') {
                if (isUp) {
                    line.append("</sup>").append("&gt;");
                } else {
                    line.append("&gt;");
                }
                isUp = false;
            }
            if (wzor.getText().charAt(i) == '+' || wzor.getText().charAt(i) == '-' || wzor.getText().charAt(i) == '/' || wzor.getText().charAt(i) == '*' || wzor.getText().charAt(i) == '=') {
                if (isUp) {
                    line.append("</sup>").append(wzor.getText().charAt(i));
                } else {
                    line.append(wzor.getText().charAt(i));
                }
                isUp = false;
            }
            if (wzor.getText().charAt(i) != '^' && wzor.getText().charAt(i) != '+' && wzor.getText().charAt(i) != '-' && wzor.getText().charAt(i) != '/' && wzor.getText().charAt(i) != '*' && wzor.getText().charAt(i) != '=' && wzor.getText().charAt(i) != '>' && wzor.getText().charAt(i) != '<') {
                line.append(wzor.getText().charAt(i));
            }
        }
        if (isUp) {
            line.append("</sup>");
        }
        webEngine.loadContent(line.toString());
    }

    @FXML
    public void wykonajOperacje(ActionEvent event) {
        List<String> obliczenia = new ArrayList<>();
        List<Double> wyniki = new ArrayList<>();
        double delta;
        double x1, x2;
        double sqrDelta;
        TRownanie result = null;
        chart.getData().clear();
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        chart.getXAxis().setLabel(wzor.getText());
        double degree = 0;// stopien wielomiany
        //defining a series
        XYChart.Series series = new XYChart.Series<Number, Number>();
        XYChart.Series series2 = new XYChart.Series<Number, Number>();
        XYChart.Series series3 = new XYChart.Series<Number, Number>();
        XYChart.Series series4 = new XYChart.Series<Number, Number>();

        series.setName("Wykres funkcji");
        //populating the series with data]
        StringReader tekstReader = new StringReader(wzor.getText());
        parser parser_obj = new parser(new MyLexer(tekstReader));
        try {
            //PARSOWANIE
            result = (TRownanie) parser_obj.parse().value;
            if (result != null) {
                result.sort();
            }
            //UZYCIE DRZEWA OBLICZEN
            double min = Double.valueOf(lSide.getText());
            double max = Double.valueOf(rSide.getText());
            degree = result.getMaxSt();
            double i = 0.1;
            if (degree > 3) {
                for (double a = min; a < max; a += 0.0001) {
                    double tmp = result.lewaStrona.oblicz(a);
                    if (result.znak.equals("=")) {
                        tmp *= 100;
                        tmp = Math.round(tmp);
                        tmp /= 100;

                        if (tmp == result.wartosc) {
                            if (!wyniki.isEmpty()) {
                                if (Math.abs(wyniki.get(wyniki.size() - 1) - a) > 0.2) {
                                    wyniki.add(a);
                                    series2.getData().add(new XYChart.Data(min, tmp));
                                }
                            } else {
                                wyniki.add(a);
                            }
                        }
                    }
                }
            }
            while (min <= max) {
                double tmp = result.lewaStrona.oblicz(min);
                series.getData().add(new XYChart.Data(min, tmp));
                min += 0.05;
            }

        } catch (Exception e) {
            //System.out.println("Tekst wyjatku po parsowaniu: "+e.getMessage());
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Math Analysis");
            alert.setHeaderText("Tekst wyjątku po parsowaniu:");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        // chart.setAnimated(false);
//        chart.setCreateSymbols(true);
        chart.getData().addAll(series, series2, series3, series4);
        xAxis.setLabel(wzor.toString());
        line = new StringBuilder();
        html = new StringBuilder();
        line.append("<html><head></head><body style=\"font-size:12.0pt; font-family:Times New Roman\">").append("\n");
        isUp = false;

        for (int i = 0; i < wzor.getText().length(); i++) {
            if (wzor.getText().charAt(i) == '^') {
                line.append("<sup>").append("\n");
                isUp = true;
            }
            if (wzor.getText().charAt(i) == '<') {
                if (isUp) {
                    line.append("</sup>").append("&lt;").append("\n");
                } else {
                    line.append("&lt;").append("\n");
                }
                isUp = false;
            }
            if (wzor.getText().charAt(i) == '>') {
                if (isUp) {
                    line.append("</sup>").append("&gt;").append("\n");
                } else {
                    line.append("&gt;").append("\n");
                }
                isUp = false;
            }
            if (wzor.getText().charAt(i) == '+' || wzor.getText().charAt(i) == '-' || wzor.getText().charAt(i) == '/' || wzor.getText().charAt(i) == '*' || wzor.getText().charAt(i) == '=') {
                if (isUp) {
                    line.append("</sup>").append(wzor.getText().charAt(i));
                } else {
                    line.append(wzor.getText().charAt(i));
                }
                isUp = false;
            }
            if (wzor.getText().charAt(i) != '^' && wzor.getText().charAt(i) != '+' && wzor.getText().charAt(i) != '-' && wzor.getText().charAt(i) != '/' && wzor.getText().charAt(i) != '*' && wzor.getText().charAt(i) != '=' && wzor.getText().charAt(i) != '>' && wzor.getText().charAt(i) != '<') {
                line.append(wzor.getText().charAt(i));
            }
        }
        if (isUp) {
            line.append("</sup>").append("\n");
        }
        html.append(line);
        if (result.lewaStrona.lista.get(0).getPotega() > 3) {
            line.append(" <br/><h1>Rozwiązania:</h1>").append("\n");
            html.append(" <br/><h1>Rozwiązania:</h1>").append("\n");
            if (result.znak.equals("=")) {
                for (int i = 0; i < wyniki.size(); i++) {
                    html.append(" <br/>x<sub>").append((i + 1)).append("</sub> = ").append(wyniki.get(i)).append("\n");
                    line.append(" <br/>x<sub>").append((i + 1)).append("</sub> = ").append(wyniki.get(i)).append("\n");
                }
                if (wyniki.size() == 0) {
                    line.append(" <br/><b>Brak rozwiązań dla podanego przedziału.</b>").append("\n");
                    html.append(" <br/><b>Brak rozwiązań dla podanego przedziału.</b>").append("\n");
                }
            } else {
                line.append(" <br/> <b>Brak rozwiązań dla nierówności dowolnego stopnia:</b>").append("\n");
                html.append(" <br/> <b>Brak rozwiązań dla nierówności dowolnego stopnia:</b>").append("\n");
            }

        }
        if (result.lewaStrona.lista.get(0).getPotega() == 3) {
            if (result.znak.equals("=")) {
                double a = 0, b = 0, c = 0, d = 0, f, g, h, i, j, k, m, n, p, xx1, xx2, xx3;
                if (result.lewaStrona.lista.size() == 4) {
                    a = result.lewaStrona.lista.get(0).getLiczba();
                    b = result.lewaStrona.lista.get(1).getLiczba();
                    c = result.lewaStrona.lista.get(2).getLiczba();
                    d = result.lewaStrona.lista.get(3).getLiczba() - result.wartosc;
                }
                if (result.lewaStrona.lista.size() == 3) {
                    a = result.lewaStrona.lista.get(0).getLiczba();
                    if (result.lewaStrona.lista.get(1).getPotega() == 2) {
                        b = result.lewaStrona.lista.get(1).getLiczba();
                    } else {
                        b = 0;
                    }
                    if (result.lewaStrona.lista.get(1).getPotega() == 1 || result.lewaStrona.lista.get(2).getPotega() == 1) {
                        if (result.lewaStrona.lista.get(1).getPotega() == 1) {
                            c = result.lewaStrona.lista.get(1).getLiczba();
                        } else {
                            c = result.lewaStrona.lista.get(2).getLiczba();
                        }
                    } else {
                        c = 0;
                    }
                    if (result.lewaStrona.lista.get(1).getPotega() == 0 || result.lewaStrona.lista.get(2).getPotega() == 0) {
                        if (result.lewaStrona.lista.get(1).getPotega() == 0) {
                            d = result.lewaStrona.lista.get(1).getLiczba() - result.wartosc;
                        } else {
                            d = result.lewaStrona.lista.get(2).getLiczba() - result.wartosc;
                        }
                    } else {
                        d = -1 * result.wartosc;
                    }
                }
                if (result.lewaStrona.lista.size() == 2) {
                    if (result.lewaStrona.lista.get(1).getPotega() == 2) {
                        b = result.lewaStrona.lista.get(1).getLiczba();
                        c = 0;
                        d = -1 * result.wartosc;
                    }
                    if (result.lewaStrona.lista.get(1).getPotega() == 1) {
                        c = result.lewaStrona.lista.get(1).getLiczba();
                        b = 0;
                        d = -1 * result.wartosc;
                    }
                    if (result.lewaStrona.lista.get(1).getPotega() == 0) {
                        c = 0;
                        b = 0;
                        d = result.lewaStrona.lista.get(1).getLiczba() * result.wartosc;
                    }
                }
                line.append(" <br/>a =").append(a).append("\n");
                line.append(" <br/>b =").append(b).append("\n");
                line.append(" <br/>c =").append(c).append("\n");
                line.append(" <br/>d =").append(d).append("\n");
                html.append(" <br/>a =").append(a).append("\n");
                html.append(" <br/>b =").append(b).append("\n");
                html.append(" <br/>c =").append(c).append("\n");
                html.append(" <br/>d =").append(d).append("\n");
                f = (c / a) - ((b * b) / (3 * a * a));
                line.append(" <br/>Zmienna pomocnicza f=").append(f).append("\n");
                URL url = this.getClass().getResource("/projekt/HTML/f.gif");
                //System.out.println(s.substring(6));
                try {
                    line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                } catch (URISyntaxException ex) {
                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }
                html.append(" <br/>Zmienna pomocnicza f=").append(f).append("\n");
                html.append(" <br/><img src=\"src/projekt/HTML/f.gif\"> </img>").append("\n");

                g = ((2 * (b * b * b)) / (27 * (a * a * a))) - ((b * c) / (3 * a * a)) + (d / a);
                line.append(" <br/>Zmienna pomocnicza g=").append(g).append("\n");
                url = this.getClass().getResource("/projekt/HTML/g.gif");
                //System.out.println(s.substring(6));
                try {
                    line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                } catch (URISyntaxException ex) {
                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }
                html.append(" <br/>Zmienna pomocnicza g=").append(g).append("\n");
                html.append(" <br/><img src=\"src/projekt/HTML/g.gif\"> </img>").append("\n");
                h = ((g * g) / 4) + ((f * f * f) / 27);
                line.append(" <br/>Zmienna pomocnicza h=").append(h).append("\n");
                url = this.getClass().getResource("/projekt/HTML/h.gif");
                //System.out.println(s.substring(6));
                try {
                    line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                } catch (URISyntaxException ex) {
                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }
                html.append(" <br/>Zmienna pomocnicza h=").append(h).append("\n");
                html.append(" <br/><img src=\"src/projekt/HTML/h.gif\"> </img>").append("\n");
                if ((h > 0) || (Math.abs(f) < 0.0001 && Math.abs(g) < 0.0001)) {
                    if (h > 0) {
                        line.append(" <br/>h>0 tylko jeden pierwiastek rzeczywisty.").append(h);
                        double tmp = (-1 * g / 2) + Math.sqrt(h);
                        double tmp2 = (-1 * g / 2) - Math.sqrt(h);
                        double tmp3 = ((b) / (3 * a));
                        xx1 = Math.cbrt(tmp) + Math.cbrt(tmp2) - tmp3;
                        series2.getData().add(new XYChart.Data(xx1, -20));
                        series2.getData().add(new XYChart.Data(xx1, 20));
                        line.append(" <br/>x<sub>1</sub>=").append(xx1).append("\n");
                        url = this.getClass().getResource("/projekt/HTML/x111.gif");
                        //System.out.println(s.substring(6));
                        try {
                            line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        html.append(" <br/><br/>x<sub>1</sub>=").append(xx1).append("\n");
                        html.append(" <br/><img src=\"src/projekt/HTML/x111.gif\"> </img>").append("\n");
                    }
                    if (Math.abs(f) < 0.0001 && Math.abs(g) < 0.0001) {
                        line.append(" <br/>g ≈0 i f ≈ 0. Jeden potrójny pierwiatsek <br/>");
                        xx1 = -1 * Math.cbrt(d / a);
                        series2.getData().add(new XYChart.Data(xx1, -20));
                        series2.getData().add(new XYChart.Data(xx1, 20));

                        line.append(" <br/>x<sub>1</sub>=x<sub>2</sub>=x<sub>3</sub>=").append(xx1).append("\n");
                        url = this.getClass().getResource("/projekt/HTML/x123.gif");
                        //System.out.println(s.substring(6));
                        try {
                            line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        html.append(" <br/>x<sub>1</sub>=x<sub>2</sub>=x<sub>3</sub>=").append(xx1).append("\n");
                        html.append(" <br/><img src=\"src/projekt/HTML/x123.gif\"> </img>").append("\n");

                    }
                } else {
                    i = Math.sqrt(((g * g) / 4) - h);
                    j = Math.cbrt(i);
                    k = Math.acos(-(g / (2 * i)));
                    m = Math.cos(k / 3);
                    n = Math.sqrt(3) * Math.sin(k / 3);
                    p = -(b / (3 * a));
                    xx1 = (2 * j * m) + p;
                    xx2 = -j * (m + n) + p;
                    xx3 = -j * (m - n) + p;
                    series2.getData().add(new XYChart.Data(xx1, -20));
                    series2.getData().add(new XYChart.Data(xx1, 20));
                    series3.getData().add(new XYChart.Data(xx2, -20));
                    series3.getData().add(new XYChart.Data(xx2, 20));
                    series4.getData().add(new XYChart.Data(xx3, -20));
                    series4.getData().add(new XYChart.Data(xx3, 20));
                    line.append("<br/> Przypadek 3, gdy pozostałe dwa warunki nie są spełnione 3 pierwiastki").append("\n");
                    line.append(" <br/>Zmienna pomocnicza i = ").append(i).append("\n");
                    url = this.getClass().getResource("/projekt/HTML/i.gif");
                    //System.out.println(s.substring(6));
                    try {
                        line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    html.append(" <br/>Zmienna pomocnicza i = ").append(i).append("\n");
                    html.append(" <br/><img src=\"src/projekt/HTML/i.gif\"> </img>").append("\n");
                    line.append(" <br/>Zmienna pomocnicza j = ").append(j).append("\n");
                    url = this.getClass().getResource("/projekt/HTML/j.gif");
                    //System.out.println(s.substring(6));
                    try {
                        line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    html.append(" <br/>Zmienna pomocnicza j = ").append(j).append("\n");
                    html.append(" <br/><img src=\"src/projekt/HTML/j.gif\"> </img>").append("\n");
                    line.append(" <br/>Zmienna pomocnicza k = ").append(k).append("\n");
                    url = this.getClass().getResource("/projekt/HTML/k.gif");
                    //System.out.println(s.substring(6));
                    try {
                        line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    html.append(" <br/>Zmienna pomocnicza k = ").append(k).append("\n");
                    html.append(" <br/><img src=\"src/projekt/HTML/k.gif\"> </img>").append("\n");
                    line.append(" <br/>Zmienna pomocnicza m = ").append(m).append("\n");
                    url = this.getClass().getResource("/projekt/HTML/m.gif");
                    //System.out.println(s.substring(6));
                    try {
                        line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    html.append(" <br/>Zmienna pomocnicza m = ").append(m).append("\n");
                    html.append(" <br/><img src=\"src/projekt/HTML/m.gif\"> </img>").append("\n");
                    line.append(" <br/>Zmienna pomocnicza n = ").append(n).append("\n");
                    url = this.getClass().getResource("/projekt/HTML/n.gif");
                    //System.out.println(s.substring(6));
                    try {
                        line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    html.append(" <br/>Zmienna pomocnicza n = ").append(n).append("\n");
                    html.append(" <br/><img src=\"src/projekt/HTML/n.gif\"> </img>").append("\n");
                    line.append(" <br/>Zmienna pomocnicza p = ").append(p).append("\n");
                    url = this.getClass().getResource("/projekt/HTML/p.gif");
                    //System.out.println(s.substring(6));
                    try {
                        line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    html.append(" <br/>Zmienna pomocnicza p = ").append(p).append("\n");
                    html.append(" <br/><img src=\"src/projekt/HTML/p.gif\"> </img>").append("\n");
                    line.append(" <br/>x<sub>1</sub> = (2×j×m)+p").append(xx1).append("\n");
                    line.append(" <br/>x<sub>2</sub> = -j×(m+n+p)").append(xx2).append("\n");
                    line.append(" <br/>x<sub>3</sub> = -j×(m-n+p)").append(xx3).append("\n");
                    html.append(" <br/>x<sub>1</sub> = (2×j×m)+p").append(xx1).append("\n");
                    html.append(" <br/>x<sub>2</sub> = -j×(m+n+p)").append(xx2).append("\n");
                    html.append(" <br/>x<sub>3</sub> = -j×(m-n+p)").append(xx3).append("\n");
                }
            } else {
                line.append("<br /> Program nie rozwiązuje nierówności zadanego stopnia").append("\n");
            }
        }
        if (result.lewaStrona.lista.get(0).getPotega() == 2.0) {
            line.append(" <br/>a =").append(result.lewaStrona.lista.get(0).getLiczba()).append("\n");
            html.append(" <br/>a =").append(result.lewaStrona.lista.get(0).getLiczba()).append("\n");
            if (result.lewaStrona.lista.size() == 3) {
                line.append("<br />b = ").append(result.lewaStrona.lista.get(1).getLiczba()).append("\n");
                html.append("<br />b = ").append(result.lewaStrona.lista.get(1).getLiczba()).append("\n");
                result.lewaStrona.lista.get(2).setLiczba(result.lewaStrona.lista.get(2).getLiczba() - result.wartosc);
                line.append("<br />c = ").append(String.valueOf(result.lewaStrona.lista.get(2).getLiczba())).append("\n");
                URL url = this.getClass().getResource("/projekt/HTML/delta.gif");
                try {
                    line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                } catch (URISyntaxException ex) {
                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }
                html.append(" <br/><img src=\"src/projekt/HTML/delta.gif\"> </img>").append("\n");

                if (result.znak.equals("=")) {
                    delta = (result.lewaStrona.lista.get(1).getLiczba() * result.lewaStrona.lista.get(1).getLiczba()) - (4 * result.lewaStrona.lista.get(0).getLiczba() * result.lewaStrona.lista.get(2).getLiczba());
                    if (delta >= 0) {
                        sqrDelta = Math.sqrt(delta);
                        line.append("<br />√Δ =").append(sqrDelta).append("\n");
                        html.append("<br />√Δ =").append(sqrDelta).append("\n");
                        x1 = ((-1 * result.lewaStrona.lista.get(1).getLiczba()) - sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        x2 = (-1 * result.lewaStrona.lista.get(1).getLiczba() + sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        url = this.getClass().getResource("/projekt/HTML/x1d.gif");
                        try {
                            line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        html.append(" <br/><img src=\"src/projekt/HTML/x1d.gif\"> </img>").append("\n");
                        line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        line.append("<br />x<sub>2</sub> =").append(x2).append("\n");
                        html.append("<br />x<sub>2</sub> =").append(x2).append("\n");
                        series2.getData().add(new XYChart.Data(x1, -20));
                        series2.getData().add(new XYChart.Data(x1, 20));
                        series3.getData().add(new XYChart.Data(x2, -20));
                        series3.getData().add(new XYChart.Data(x2, 20));
                    } else {
                        line.append("<br />Δ &lt; 0<br/> Brak rozwiązania");
                    }
                }
                if (result.znak.equals(">") || result.znak.equals(">=")) {
                    delta = (result.lewaStrona.lista.get(1).getLiczba() * result.lewaStrona.lista.get(1).getLiczba()) - (4 * result.lewaStrona.lista.get(0).getLiczba() * result.lewaStrona.lista.get(2).getLiczba());
                    if (delta >= 0) {
                        sqrDelta = Math.sqrt(delta);
                        x1 = ((-1 * result.lewaStrona.lista.get(1).getLiczba()) - sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        x2 = (-1 * result.lewaStrona.lista.get(1).getLiczba() + sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        line.append("<br />√Δ =").append(sqrDelta).append("\n");
                        html.append("<br />√Δ =").append(sqrDelta).append("\n");
                        url = this.getClass().getResource("/projekt/HTML/x1d.gif");
                        try {
                            line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        html.append(" <br/><img src=\"src/projekt/HTML/x1d.gif\"> </img>").append("\n");
                        line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        line.append("<br />x<sub>2</sub>=").append(x2).append("\n");
                        html.append("<br />x<sub>2</sub>=").append(x2).append("\n");
                        double min = Double.min(x2, x1);
                        double max = Double.max(x2, x1);
                        min *= 100;
                        min = Math.round(min);
                        min /= 100;
                        max *= 100;
                        max = Math.round(max);
                        max /= 100;
                        series2.getData().add(new XYChart.Data(x1, -20));
                        series2.getData().add(new XYChart.Data(x1, 20));
                        series3.getData().add(new XYChart.Data(x2, -20));
                        series3.getData().add(new XYChart.Data(x2, 20));

                        if (result.lewaStrona.lista.get(0).getLiczba() > 0) {
                            if (result.znak.equals(">")) {
                                line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)").append("\n");
                                html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)").append("\n");
                            } else {
                                line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)").append("\n");
                                html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)").append("\n");
                            }
                        } else if (result.znak.equals(">")) {
                            line.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")").append("\n");
                            html.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")").append("\n");
                        } else {
                            line.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;").append("\n");
                            html.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;").append("\n");
                        }

                    } else {
                        line.append("<br />Δ &lt;<br/> Rozwiązanie :<br/>  (-∞;+∞)").append("\n");
                        html.append("<br />Δ &lt;<br/> Rozwiązanie :<br/>  (-∞;+∞)").append("\n");
                    }
                }
                // 
                if (result.znak.equals("<") || result.znak.equals("=<")) {
                    delta = (result.lewaStrona.lista.get(1).getLiczba() * result.lewaStrona.lista.get(1).getLiczba()) - (4 * result.lewaStrona.lista.get(0).getLiczba() * result.lewaStrona.lista.get(2).getLiczba());
                    if (delta >= 0) {
                        sqrDelta = Math.sqrt(delta);
                        x1 = ((-1 * result.lewaStrona.lista.get(1).getLiczba()) - sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        x2 = (-1 * result.lewaStrona.lista.get(1).getLiczba() + sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        line.append("<br />√Δ =").append(sqrDelta).append("\n");
                        html.append("<br />√Δ =").append(sqrDelta).append("\n");
                        url = this.getClass().getResource("/projekt/HTML/x1d.gif");
                        try {
                            line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        html.append(" <br/><img src=\"src/projekt/HTML/x1d.gif\"> </img>").append("\n");
                        line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        line.append("<br />x<sub>2</sub>=").append(x2).append("\n");
                        html.append("<br />x<sub>2</sub>=").append(x2).append("\n");
                        double min = Double.min(x2, x1);
                        double max = Double.max(x2, x1);
                        min *= 100;
                        min = Math.round(min);
                        min /= 100;
                        max *= 100;
                        max = Math.round(max);
                        max /= 100;
                        series2.getData().add(new XYChart.Data(x1, -20));
                        series2.getData().add(new XYChart.Data(x1, 20));
                        series3.getData().add(new XYChart.Data(x2, -20));
                        series3.getData().add(new XYChart.Data(x2, 20));

                        if (result.lewaStrona.lista.get(0).getLiczba() > 0) {
                            if (result.znak.equals("<")) {
                                line.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")").append("\n");
                                html.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")").append("\n");
                            } else {
                                line.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;").append("\n");
                                html.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;").append("\n");
                            }
                        } else if (result.znak.equals("<")) {
                            line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)").append("\n");
                            html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)").append("\n");
                        } else {
                            line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)").append("\n");
                            html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)").append("\n");
                        }
                    } else {
                        line.append("<br />Δ &lt; 0 <br/>Brak rozwiązania").append("\n");
                        html.append("<br />Δ &lt; 0 <br/>Brak rozwiązania").append("\n");
                    }
                }
            }
            if (result.lewaStrona.lista.size() == 2) {
                if (result.lewaStrona.lista.get(1).getPotega() == 1.0) {
                    line.append("<br />b = ").append(result.lewaStrona.lista.get(1).getLiczba());
                    line.append("<br />c = ").append(String.valueOf(-result.wartosc));
                    html.append("<br />b = ").append(result.lewaStrona.lista.get(1).getLiczba());
                    html.append("<br />c = ").append(String.valueOf(-result.wartosc));
                    double c = -result.wartosc;
                    URL url = this.getClass().getResource("/projekt/HTML/delta.gif");
                    try {
                        line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    html.append(" <br/><img src=\"src/projekt/HTML/delta.gif\"> </img>").append("\n");

                    if (result.znak.equals("=")) {
                        delta = (result.lewaStrona.lista.get(1).getLiczba() * result.lewaStrona.lista.get(1).getLiczba()) - (4 * result.lewaStrona.lista.get(0).getLiczba() * c);
                        if (delta >= 0) {
                            sqrDelta = Math.sqrt(delta);
                            line.append("<br />√Δ =").append(sqrDelta).append("\n");
                            html.append("<br />√Δ =").append(sqrDelta).append("\n");
                            x1 = ((-1 * result.lewaStrona.lista.get(1).getLiczba()) - sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            x2 = (-1 * result.lewaStrona.lista.get(1).getLiczba() + sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            url = this.getClass().getResource("/projekt/HTML/x1d.gif");
                            try {
                                line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                            } catch (URISyntaxException ex) {
                                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            html.append(" <br/><img src=\"src/projekt/HTML/x1d.gif\"> </img>").append("\n");
                            line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            html.append("<br />x<sub>2</sub> =").append(x2).append("\n");
                            line.append("<br />x<sub>2</sub> =").append(x2).append("\n");
                            series2.getData().add(new XYChart.Data(x1, -20));
                            series2.getData().add(new XYChart.Data(x1, 20));
                            series3.getData().add(new XYChart.Data(x2, -20));
                            series3.getData().add(new XYChart.Data(x2, 20));

                        } else {
                            line.append("<br />Δ &lt; 0 <br/>Brak rozwiązania").append("\n");
                            html.append("<br />Δ &lt; 0 <br/>Brak rozwiązania").append("\n");
                        }
                    }
                    if (result.znak.equals(">") || result.znak.equals(">=")) {
                        delta = (result.lewaStrona.lista.get(1).getLiczba() * result.lewaStrona.lista.get(1).getLiczba()) - (4 * result.lewaStrona.lista.get(0).getLiczba() * 0);
                        if (delta >= 0) {
                            sqrDelta = Math.sqrt(delta);
                            x1 = ((-1 * result.lewaStrona.lista.get(1).getLiczba()) - sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            x2 = (-1 * result.lewaStrona.lista.get(1).getLiczba() + sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            line.append("<br />√Δ =").append(sqrDelta).append("\n");
                            html.append("<br />√Δ =").append(sqrDelta).append("\n");
                            url = this.getClass().getResource("/projekt/HTML/x1d.gif");
                            try {
                                line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                            } catch (URISyntaxException ex) {
                                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            html.append(" <br/><img src=\"src/projekt/HTML/x1d.gif\"> </img>").append("\n");
                            line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            html.append("<br />x<sub>2</sub> =").append(x2).append("\n");
                            line.append("<br />x<sub>2</sub> =").append(x2).append("\n");
                            series2.getData().add(new XYChart.Data(x1, -20));
                            series2.getData().add(new XYChart.Data(x1, 20));
                            series3.getData().add(new XYChart.Data(x2, -20));
                            series3.getData().add(new XYChart.Data(x2, 20));
                            double min = Double.min(x2, x1);
                            double max = Double.max(x2, x1);
                            min *= 100;
                            min = Math.round(min);
                            min /= 100;
                            max *= 100;
                            max = Math.round(max);
                            max /= 100;
                            if (result.lewaStrona.lista.get(0).getLiczba() > 0) {
                                if (result.znak.equals(">")) {
                                    line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)").append("\n");
                                    html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)").append("\n");
                                } else {
                                    line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)").append("\n");
                                    html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)").append("\n");
                                }
                            } else if (result.znak.equals(">")) {
                                line.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")").append("\n");
                                html.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")").append("\n");
                            } else {
                                line.append("<br />Rozwiązanie : <br/> &lt;").append(min).append(",").append(max).append("&gt;").append("\n");
                                html.append("<br />Rozwiązanie : <br/> &lt;").append(min).append(",").append(max).append("&gt;").append("\n");
                            }
                        } else {
                            line.append("<br />Δ &lt; 0<br/> Rozwiązanie :<br/> (-∞;+∞)").append("\n");
                            html.append("<br />Δ &lt; 0<br/> Rozwiązanie :<br/> (-∞;+∞)").append("\n");
                        }
                    }
                    if (result.znak.equals("<") || result.znak.equals("=<")) {
                        delta = (result.lewaStrona.lista.get(1).getLiczba() * result.lewaStrona.lista.get(1).getLiczba()) - (4 * result.lewaStrona.lista.get(0).getLiczba() * 0);
                        if (delta >= 0) {
                            sqrDelta = Math.sqrt(delta);
                            x1 = ((-1 * result.lewaStrona.lista.get(1).getLiczba()) - sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            x2 = (-1 * result.lewaStrona.lista.get(1).getLiczba() + sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());

                            line.append("<br />√Δ =").append(sqrDelta).append("\n");
                            html.append("<br />√Δ =").append(sqrDelta).append("\n");
                            url = this.getClass().getResource("/projekt/HTML/x1d.gif");
                            try {
                                line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                            } catch (URISyntaxException ex) {
                                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            html.append(" <br/><img src=\"src/projekt/HTML/x1d.gif\"> </img>").append("\n");
                            line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            line.append("<br />x<sub>2</sub> =").append(x2).append("\n");
                            html.append("<br />x<sub>2</sub> =").append(x2).append("\n");
                            series2.getData().add(new XYChart.Data(x1, -20));
                            series2.getData().add(new XYChart.Data(x1, 20));
                            series3.getData().add(new XYChart.Data(x2, -20));
                            series3.getData().add(new XYChart.Data(x2, 20));
                            double min = Double.min(x2, x1);
                            double max = Double.max(x2, x1);
                            min *= 100;
                            min = Math.round(min);
                            min /= 100;
                            max *= 100;
                            max = Math.round(max);
                            max /= 100;
                            if (result.lewaStrona.lista.get(0).getLiczba() > 0) {
                                if (result.znak.equals("<")) {
                                    line.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")").append("\n");
                                    html.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")").append("\n");
                                } else {
                                    line.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;").append("\n");
                                    html.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;").append("\n");
                                }
                            } else if (result.znak.equals("<")) {
                                line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)").append("\n");
                                html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)").append("\n");
                            } else {
                                line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)").append("\n");
                                html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)").append("\n");
                            }
                        } else {
                            line.append("<br />Δ &lt; 0 <br/>Brak rozwiązania").append("\n");
                        }
                        html.append("<br />Δ &lt; 0 <br/>Brak rozwiązania").append("\n");
                    }
                }
                if (result.lewaStrona.lista.get(1).getPotega() == 0.0) {
                    line.append("<br />b = ").append("0").append("\n");
                    html.append("<br />b = ").append("0").append("\n");
                    result.lewaStrona.lista.get(1).setLiczba(result.lewaStrona.lista.get(1).getLiczba() - result.wartosc);
                    line.append("<br />c = ").append(String.valueOf(result.lewaStrona.lista.get(1).getLiczba())).append("\n");
                    html.append("<br />c = ").append(String.valueOf(result.lewaStrona.lista.get(1).getLiczba())).append("\n");
                    delta = (0) - (4 * result.lewaStrona.lista.get(0).getLiczba() * result.lewaStrona.lista.get(1).getLiczba());
                    URL url = this.getClass().getResource("/projekt/HTML/delta.gif");
                    try {
                        line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    html.append(" <br/><img src=\"src/projekt/HTML/delta.gif\"> </img>").append("\n");

                    if (result.znak.equals("=")) {
                        if (delta >= 0) {
                            sqrDelta = Math.sqrt(delta);
                            x1 = ((0) - sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            x2 = (0 + sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            line.append("<br />√Δ =").append(sqrDelta).append("\n");
                            html.append("<br />√Δ =").append(sqrDelta).append("\n");
                            url = this.getClass().getResource("/projekt/HTML/x1d.gif");
                            try {
                                line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                            } catch (URISyntaxException ex) {
                                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            html.append(" <br/><img src=\"src/projekt/HTML/x1d.gif\"> </img>").append("\n");
                            line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            line.append("<br />x<sub>2</sub> =").append(x2);
                            html.append("<br />x<sub>2</sub> =").append(x2);
                            series2.getData().add(new XYChart.Data(x1, -20));
                            series2.getData().add(new XYChart.Data(x1, 20));
                            series3.getData().add(new XYChart.Data(x2, -20));
                            series3.getData().add(new XYChart.Data(x2, 20));

                        } else {
                            line.append("<br />Δ &lt; 0<br/> Brak rozwiązania");
                            html.append("<br />Δ &lt; 0<br/> Brak rozwiązania");
                        }
                    }
                    if (result.znak.equals(">") || result.znak.equals(">=")) {
                        delta = -(4 * result.lewaStrona.lista.get(0).getLiczba() * result.lewaStrona.lista.get(1).getLiczba());
                        line.append("<br />Δ =").append(delta);
                        if (delta >= 0) {
                            sqrDelta = Math.sqrt(delta);
                            x1 = (-sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            line.append("<br />√Δ =").append(sqrDelta).append("\n");
                            html.append("<br />√Δ =").append(sqrDelta).append("\n");
                            line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            html.append("<br />x<sub>1</sub> =").append(x1).append("\n");

                            x2 = (sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            line.append("<br />x<sub>2</sub> =").append(x2);
                            html.append("<br />x<sub>2</sub> =").append(x2);
                            double min = Double.min(x2, x1);
                            double max = Double.max(x2, x1);
                            min *= 100;
                            min = Math.round(min);
                            min /= 100;
                            max *= 100;
                            max = Math.round(max);
                            max /= 100;
                            if (result.lewaStrona.lista.get(0).getLiczba() > 0) {
                                if (result.znak.equals(">")) {
                                    line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)");
                                    html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)");
                                } else {
                                    line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)");
                                    html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)");
                                }
                            } else if (result.znak.equals(">")) {
                                line.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")");
                                html.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")");
                            } else {
                                line.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;");
                                html.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;");
                            }
                        } else {
                            line.append("<br />Δ &lt; 0 <br/> Rozwiązanie :<br/> (-∞;+∞)");
                            html.append("<br />Δ &lt; 0 <br/> Rozwiązanie :<br/> (-∞;+∞)");
                        }
                    }
                    if (result.znak.equals(">") || result.znak.equals(">=")) {
                        delta = -(4 * result.lewaStrona.lista.get(0).getLiczba() * result.lewaStrona.lista.get(1).getLiczba());
                        line.append("<br />Δ =").append(delta);
                        if (delta >= 0) {
                            sqrDelta = Math.sqrt(delta);
                            x1 = (-sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            line.append("<br />√Δ =").append(sqrDelta).append("\n");
                            html.append("<br />√Δ =").append(sqrDelta).append("\n");
                            line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            x2 = (sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            series2.getData().add(new XYChart.Data(x1, -20));
                            series2.getData().add(new XYChart.Data(x1, 20));
                            series3.getData().add(new XYChart.Data(x2, -20));
                            series3.getData().add(new XYChart.Data(x2, 20));
                            line.append("<br />x<sub>2</sub> =").append(x2);
                            html.append("<br />x<sub>2</sub> =").append(x2);
                            double min = Double.min(x2, x1);
                            double max = Double.max(x2, x1);
                            min *= 100;
                            min = Math.round(min);
                            min /= 100;
                            max *= 100;
                            max = Math.round(max);
                            max /= 100;
                            if (result.lewaStrona.lista.get(0).getLiczba() > 0) {
                                if (result.znak.equals(">")) {
                                    line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)");
                                    html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)");
                                } else {
                                    line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)");
                                    html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)");
                                }
                            } else if (result.znak.equals(">")) {
                                line.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")");
                                html.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")");
                            } else {
                                line.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;");
                                html.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;");
                            }
                        } else {
                            line.append("<br />Δ &lt; 0<br/>Rozwiaznanie :<br/> (-∞;+∞)");
                            html.append("<br />Δ &lt; 0<br/>Rozwiaznanie :<br/> (-∞;+∞)");
                        }
                    }
                    if (result.znak.equals("<") || result.znak.equals("=<")) {
                        delta = -(4 * result.lewaStrona.lista.get(0).getLiczba() * result.lewaStrona.lista.get(1).getLiczba());
                        line.append("<br />Δ =").append(delta);
                        html.append("<br />Δ =").append(delta);
                        if (delta >= 0) {
                            sqrDelta = Math.sqrt(delta);
                            x1 = (-sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            line.append("<br />√Δ =").append(sqrDelta).append("\n");
                            html.append("<br />√Δ =").append(sqrDelta).append("\n");

                            line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                            html.append("<br />x<sub>1</sub> =").append(x1).append("\n");

                            x2 = (sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                            series2.getData().add(new XYChart.Data(x1, -20));
                            series2.getData().add(new XYChart.Data(x1, 20));
                            series3.getData().add(new XYChart.Data(x2, -20));
                            series3.getData().add(new XYChart.Data(x2, 20));
                            line.append("<br />x<sub>2</sub> =").append(x2);
                            html.append("<br />x<sub>2</sub> =").append(x2);
                            double min = Double.min(x2, x1);
                            double max = Double.max(x2, x1);
                            min *= 100;
                            min = Math.round(min);
                            min /= 100;
                            max *= 100;
                            max = Math.round(max);
                            max /= 100;
                            if (result.lewaStrona.lista.get(0).getLiczba() > 0) {
                                if (result.znak.equals("<")) {
                                    line.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")");
                                    html.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")");
                                } else {
                                    line.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;");
                                    html.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;");
                                }

                            } else if (result.znak.equals("<")) {
                                line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)");
                                html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)");
                            } else {
                                line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)");
                                html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)");
                            }
                        } else {
                            line.append("<br />Δ &lt; 0<br/> Brak rozwiązania");
                            html.append("<br />Δ &lt; 0<br/> Brak rozwiązania");
                        }
                    }

                }
            }
            if (result.lewaStrona.lista.size() == 1) {
                line.append("<br />b = ").append("0");
                html.append("<br />b = ").append("0");
                double b = 0;
                double c = -result.wartosc;
                line.append("<br />c = ").append(c);
                html.append("<br />c = ").append(c);
                URL url = this.getClass().getResource("/projekt/HTML/delta.gif");
                try {
                    line.append(" <br/><img src=\"").append(url.toURI()).append("\"> </img>").append("\n");
                } catch (URISyntaxException ex) {
                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }
                html.append(" <br/><img src=\"src/projekt/HTML/delta.gif\"> </img>").append("\n");
                if (result.znak.equals("=")) {
                    delta = (0) - (4 * result.lewaStrona.lista.get(0).getLiczba() * c);
                    line.append("<br />Δ =").append(delta);
                    html.append("<br />Δ =").append(delta);
                    if (delta >= 0) {
                        sqrDelta = Math.sqrt(delta);
                        x1 = ((0) - sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        line.append("<br />√Δ =").append(sqrDelta).append("\n");
                        html.append("<br />√Δ =").append(sqrDelta).append("\n");
                        line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        x2 = (0 + sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        series2.getData().add(new XYChart.Data(x1, -20));
                        series2.getData().add(new XYChart.Data(x1, 20));
                        series3.getData().add(new XYChart.Data(x2, -20));
                        series3.getData().add(new XYChart.Data(x2, 20));
                        line.append("<br />x<sub>2</sub>=").append(x2);
                        html.append("<br />x<sub>2</sub>=").append(x2);
                    } else {
                        line.append("<br />Δ &lt; 0<br/> Brak rozwiązania");
                        html.append("<br />Δ &lt; 0<br/> Brak rozwiązania");
                    }
                }
                if (result.znak.equals(">") || result.znak.equals(">=")) {
                    delta = (0) - (4 * result.lewaStrona.lista.get(0).getLiczba() * c);
                    line.append("<br />Δ =").append(delta);
                    html.append("<br />Δ =").append(delta);
                    if (delta >= 0) {
                        sqrDelta = Math.sqrt(delta);
                        x1 = (-sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        line.append("<br />√Δ =").append(sqrDelta).append("\n");
                        html.append("<br />√Δ =").append(sqrDelta).append("\n");
                        line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        x2 = (sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        series2.getData().add(new XYChart.Data(x1, -20));
                        series2.getData().add(new XYChart.Data(x1, 20));
                        series3.getData().add(new XYChart.Data(x2, -20));
                        series3.getData().add(new XYChart.Data(x2, 20));
                        line.append("<br />x<sub>2</sub> =").append(x2);
                        html.append("<br />x<sub>2</sub> =").append(x2);
                        double min = Double.min(x2, x1);
                        double max = Double.max(x2, x1);
                        min *= 100;
                        min = Math.round(min);
                        min /= 100;
                        max *= 100;
                        max = Math.round(max);
                        max /= 100;
                        if (result.lewaStrona.lista.get(0).getLiczba() > 0) {
                            if (result.znak.equals(">")) {
                                line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)");
                                html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)");
                            } else {
                                line.append("<br />Rozwiązaneie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)");
                                html.append("<br />Rozwiązaneie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)");
                            }
                        } else if (result.znak.equals(">")) {
                            line.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")");
                            html.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")");
                        } else {
                            line.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;");
                            html.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;");
                        }
                    } else {
                        line.append("<br />Δ &lt; 0<br/> Rozwiązanie : <br/> (-∞;+∞)");
                        html.append("<br />Δ &lt; 0<br/> Rozwiązanie : <br/> (-∞;+∞)");
                    }
                }
                if (result.znak.equals("<") || result.znak.equals("=<")) {
                    delta = (0) - (4 * result.lewaStrona.lista.get(0).getLiczba() * c);
                    line.append("<br />Δ =").append(delta);
                    html.append("<br />Δ =").append(delta);
                    if (delta >= 0) {
                        sqrDelta = Math.sqrt(delta);
                        x1 = (-sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        line.append("<br />√Δ =").append(sqrDelta).append("\n");
                        html.append("<br />√Δ =").append(sqrDelta).append("\n");
                        line.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        html.append("<br />x<sub>1</sub> =").append(x1).append("\n");
                        x2 = (sqrDelta) / (2 * result.lewaStrona.lista.get(0).getLiczba());
                        series2.getData().add(new XYChart.Data(x1, -20));
                        series2.getData().add(new XYChart.Data(x1, 20));
                        series3.getData().add(new XYChart.Data(x2, -20));
                        series3.getData().add(new XYChart.Data(x2, 20));
                        line.append("<br />x<sub>2</sub> =").append(x2);
                        html.append("<br />x<sub>2</sub> =").append(x2);
                        double min = Double.min(x2, x1);
                        double max = Double.max(x2, x1);
                        min *= 100;
                        min = Math.round(min);
                        min /= 100;
                        max *= 100;
                        max = Math.round(max);
                        max /= 100;
                        if (result.lewaStrona.lista.get(0).getLiczba() > 0) {
                            if (result.znak.equals("<")) {
                                line.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")");
                                html.append("<br />Rozwiązanie :<br/> (").append(min).append(",").append(max).append(")");
                            } else {
                                line.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;");
                                html.append("<br />Rozwiązanie :<br/>  &lt;").append(min).append(",").append(max).append("&gt;");
                            }

                        } else if (result.znak.equals("<")) {
                            line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)");
                            html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append(") ∪ (").append(max).append(",+∞)");
                        } else {
                            line.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)");
                            html.append("<br />Rozwiązanie :<br/> (-∞,").append(min).append("&gt; ∪ &lt;").append(max).append(",+∞)");
                        }
                    } else {
                        line.append("<br />Δ &lt;<br/>Rozwiązanie 0 (-∞;+∞)");
                        html.append("<br />Δ &lt;<br/>Rozwiązanie 0 (-∞;+∞)");
                    }
                }

            }
        }
        if (result.lewaStrona.lista.get(0).getPotega() == 1.0 && result.lewaStrona.znaki.size() > 0) {
            if (result.znak.equals("=<")) {
                result.znak = "&le;";
            }
            if (result.znak.equals(">=")) {
                result.znak = "&ge;";
            }
            if (result.znak.equals("<")) {
                result.znak = "&lt;";
            }
            if (result.znak.equals(">")) {
                result.znak = "&gt;";
            }
            line.append(" <br/>a =").append(result.lewaStrona.lista.get(0).getLiczba()).append("<br/>b =");
            html.append(" <br/>a =").append(result.lewaStrona.lista.get(0).getLiczba()).append("<br/>b =");

            if (result.lewaStrona.znaki.get(0).equals("-")) {
                result.lewaStrona.lista.get(1).setLiczba(-result.lewaStrona.lista.get(1).getLiczba());
            }
            line.append(result.lewaStrona.lista.get(1).getLiczba());
            line.append("<br/> y=").append(result.wartosc);
            // tutaj
            line.append("<br/> x").append(result.znak).append("(y-b)/a");
            html.append(result.lewaStrona.lista.get(1).getLiczba());
            html.append("<br/> y=").append(result.wartosc);
            // tutaj
            html.append("<br/> x").append(result.znak).append("(y-b)/a");

            if (result.lewaStrona.znaki.get(0).equals("-")) {
                line.append(result.znak).append(" (").append(result.wartosc).append("-(").append(result.lewaStrona.lista.get(1).getLiczba()).append("))/").append(result.lewaStrona.lista.get(0).getLiczba()).append(result.znak);
                html.append(result.znak).append(" (").append(result.wartosc).append("-(").append(result.lewaStrona.lista.get(1).getLiczba()).append("))/").append(result.lewaStrona.lista.get(0).getLiczba()).append(result.znak);
            } else {
                line.append(result.znak).append(" (").append(result.wartosc).append("-").append(result.lewaStrona.lista.get(1).getLiczba()).append(")/").append(result.lewaStrona.lista.get(0).getLiczba()).append(result.znak);
                html.append(result.znak).append(" (").append(result.wartosc).append("-").append(result.lewaStrona.lista.get(1).getLiczba()).append(")/").append(result.lewaStrona.lista.get(0).getLiczba()).append(result.znak);
            }
            double wynik = (result.wartosc - result.lewaStrona.lista.get(1).getLiczba()) / result.lewaStrona.lista.get(0).getLiczba();
            line.append(wynik);
            html.append(wynik);
            double t = result.lewaStrona.oblicz(wynik);
            if (t > 0) {
                t += 2;
                series2.getData().add(new XYChart.Data(wynik, t));
                series2.getData().add(new XYChart.Data(wynik, -2));
            }
            if (t < 0) {
                t -= 2;
                series2.getData().add(new XYChart.Data(wynik, t));
                series2.getData().add(new XYChart.Data(wynik, 2));
            }
            if (t == 0) {
                series2.getData().add(new XYChart.Data(wynik, -4));
                series2.getData().add(new XYChart.Data(wynik, 4));
            }
        }
        if (result.lewaStrona.lista.size() == 1 && result.lewaStrona.lista.get(0).getPotega() == 1) {
            if (result.lewaStrona.lista.get(0).getPotega() == 1) {
                line.append(" <br/>a =").append(result.lewaStrona.lista.get(0).getLiczba()).append("<br/>b =0");
                line.append("<br/> y=").append(result.wartosc);
                //tutaj
                line.append("<br/> x").append(result.znak).append("y-/a");

                html.append(result.znak).append(" ").append(result.wartosc).append("/").append(result.lewaStrona.lista.get(0).getLiczba()).append(result.znak).append(" ");
                html.append(" <br/>a =").append(result.lewaStrona.lista.get(0).getLiczba()).append("<br/>b =0");
                html.append("<br/> y=").append(result.wartosc);
                //tutaj
                html.append("<br/> x").append(result.znak).append("y-/a");
                html.append(result.znak).append(" ").append(result.wartosc).append("/").append(result.lewaStrona.lista.get(0).getLiczba()).append(result.znak).append(" ");

                double wynik = (result.wartosc) / result.lewaStrona.lista.get(0).getLiczba();
                line.append(wynik);
                html.append(wynik);
                double t = result.lewaStrona.oblicz(wynik);
                if (t > 0) {
                    t += 2;
                    series2.getData().add(new XYChart.Data(wynik, t));
                    series2.getData().add(new XYChart.Data(wynik, -2));
                }
                if (t < 0) {
                    t -= 2;
                    series2.getData().add(new XYChart.Data(wynik, t));
                    series2.getData().add(new XYChart.Data(wynik, 2));
                }
                if (t == 0) {
                    series2.getData().add(new XYChart.Data(wynik, -4));
                    series2.getData().add(new XYChart.Data(wynik, 4));
                }

            } else {
                line.append("Brak rozwiązania");
                html.append("Brak rozwiązania");
            }
            html = new StringBuilder(line);
        }
        line.append("</ body>"
                + "</ html>");
        html.append("</ body>"
                + "</ html>");

        webEngine2.loadContent(line.toString());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        webEngine = podglad.getEngine();
        webEngine2 = rezultat.getEngine();
        drawer.setSidePane(box);
        drawer.close();
        html = new StringBuilder();
        HamburgerBackArrowBasicTransition transition = new HamburgerBackArrowBasicTransition(hamburger);
        transition.setRate(-1);
        hamburger.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            transition.setRate(transition.getRate() * -1);
            transition.play();

            if (drawer.isShown()) {
                drawer.close();
            } else {
                drawer.open();
            }
        });
        xAxis = new NumberAxis();

    }

    @FXML
    private void zapDoPdf(ActionEvent event) throws DocumentException, FileNotFoundException, IOException {
        // step 1
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Pliki PDF", "*.pdf"));

        File fileSel = fileChooser.showSaveDialog(new Stage());

        if (fileSel != null) {
            Document document = new Document();
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileSel));
            // step 3
            document.open();
            // step 4
            javafx.scene.image.Image image = chart.snapshot(null, null);
            File file = new File("chart.png");
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            Image img;
            img = Image.getInstance("chart.png");
            img.scaleAbsolute(550f, 525f);
            document.add(img);
            XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
            System.out.println(html.toString());
            InputStream is = new ByteArrayInputStream(html.toString().getBytes(StandardCharsets.UTF_8));
            worker.parseXHtml(writer, document, is, Charset.forName("UTF-8"));
            document.close();
        }
    }

    @FXML
    private void zamknij(ActionEvent event) {
        Platform.exit();
    }
}
