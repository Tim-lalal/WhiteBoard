package manager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ManagerLoginWindow extends JFrame{

    public ManagerLoginWindow(){

    }
    public void inputWindow(Server server){
        init(server);

    }
    private void init(Server server) {
        setTitle("Welcome!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        JLabel username = new JLabel("Username");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(username, constraints);

        JTextField usernametext = new JTextField(15);
        constraints.gridx = 1;
        constraints.gridy = 0;
        add(usernametext, constraints);

        JButton loginButton = new JButton("Login");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(loginButton, constraints);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = usernametext.getText();
                System.out.println("Hello: " + name);
                dispose();
                ManagerWindow managerWindow = new ManagerWindow(name, server);
                // repaint the manager canvas in period time!
                int delay = 100; //milliseconds
                ActionListener taskPerformer = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        managerWindow.repaint();
                    }
                };
                new Timer(delay, taskPerformer).start();
            }
        });

        pack();
        setLocationRelativeTo(null); // Center the window on the screen
        setVisible(true);
    }

}

//class main{
//    public static void main(String[] args) {
//        LoginWindow login = new LoginWindow();
//        login.inputWindow();
//
//    }
//}




