import sqldb.FireWallBlockListForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * Created by v1ar on 13.01.15.
 */
public class FireWallMainForm extends JFrame{
    private JButton blackList;
    private JPanel MainPanel;
    private JButton start;
    private JButton stop;
    FireWallBlockListForm form;

    public static TrayIcon trayIcon;

    public static final String APPLICATION_NAME = "Фаерволл";
    public static final String FW_WORK = "/images/work.png";
    public static final String FW_STAY = "/images/stay.png";

    public FireWallMainForm() {
        super();
        setResizable(false);
        setTitle("Фаерволл v0.1");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(MainPanel);
        pack();
        setLocationRelativeTo(null);

        blackList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.dbform.setVisible(true);
            }
        });
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Main.fireWall.isstarted) {
                    Main.fireWall.StartFilter();
                }
            }
        });
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Main.fireWall.isstarted) {
                    Main.fireWall.StopFilter();
                }
            }
        });

        if (setTrayIcon() != 0) {
            setVisible(true);
        }
    }

    public static int setTrayIcon() {
        if(! SystemTray.isSupported() ) {
            return 1;
        }

        PopupMenu trayMenu = new PopupMenu();

        MenuItem blitem = new MenuItem("Черный список");
        trayMenu.add(blitem);

        MenuItem startitem = new MenuItem("Старт");
        trayMenu.add(startitem);

        MenuItem stopitem = new MenuItem("Стоп");
        trayMenu.add(stopitem);

        MenuItem item = new MenuItem("Закрыть");
        trayMenu.add(item);


        final URL imageStay = Main.class.getResource(FW_STAY);
        final URL imageWork = Main.class.getResource(FW_WORK);

        Image icon = Toolkit.getDefaultToolkit().getImage(imageStay);
        trayIcon = new TrayIcon(icon, APPLICATION_NAME, trayMenu);
        trayIcon.setImageAutoSize(true);

        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.journalFrame.setVisible(true);
            }
        });

        SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        trayIcon.displayMessage(APPLICATION_NAME, "Приложение готово к работе",
                TrayIcon.MessageType.INFO);

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        stopitem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Main.fireWall.isstarted) {
                    Main.fireWall.StopFilter();

                    trayIcon.displayMessage(APPLICATION_NAME, "Приложение остановлено",
                            TrayIcon.MessageType.INFO);
                    trayIcon.setImage(Toolkit.getDefaultToolkit().getImage(imageStay));
                }
            }
        });

        startitem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Main.fireWall.isstarted) {
                    Main.fireWall.StartFilter();

                    trayIcon.displayMessage(APPLICATION_NAME, "Приложение запущено",
                            TrayIcon.MessageType.INFO);
                    trayIcon.setImage(Toolkit.getDefaultToolkit().getImage(imageWork));
                }
            }
        });

        blitem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.dbform.setVisible(true);
            }
        });



        return 0;
    }

}
