package on_the_wire;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

public class Wired {
    Image pole, bubble;
    private final JFrame frame;
    private Connection myTalker;
    private boolean connected = false;
    
    private JRadioButton serverButton;
    private JRadioButton clientButton;
    private JTextField serverAddress;
    private JButton startButton;
    
    private StringBuilder sb;
    private JTextField toSend;
    private JTextArea dialog;
    
    public static void main(String args[]){
        new Wired();
    }
    
    public Wired(){
        frame = new JFrame("On The Wire!  - A LAN chat, behind of its age...");
        frame.setBounds(100, 100, 500, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.DARK_GRAY);
        frame.setLayout(null);
        frame.setResizable(false);
        
        setServerOrClientButtons();
        setStartButton();
        setSender();
        setDialogBox();
        
        URL u = this.getClass().getResource("pic/wire.png");  //for pics inside package directory!  (jar and more...)
        pole = Toolkit.getDefaultToolkit().createImage(u);
        //URL u2 = this.getClass().getResource("pic/bubble.png");
        //bubble = Toolkit.getDefaultToolkit().createImage(u2);
        Decoration deco = new Decoration();
        deco.setBounds(270, 20, 190, 208);
        frame.add(deco);
        frame.setVisible(true);
    }
    
    private class Decoration extends JPanel{
        
        Decoration(){
        this.setBackground(Color.WHITE);
        }
        
        @Override
        public void paintComponent(Graphics g) {
           super.paintComponent(g);
           g.drawImage(pole, 5, 5, this.getWidth()-10, this.getHeight()-10, this);
           //g.drawImage(bubble, 25, 25, this.getWidth()-50, this.getHeight()-50, this);
        }
    }
    
    private void setServerOrClientButtons(){
        ButtonGroup selector = new ButtonGroup();

        serverButton = new JRadioButton("SERVER");
        serverButton.setBounds(20, 30, 80, 30);
        serverButton.setBackground(new Color(160, 200, 100));
        serverButton.setSelected(true);
        selector.add(serverButton);
        frame.add(serverButton);

        clientButton = new JRadioButton("CLIENT");
        clientButton.setBounds(20, 100, 80, 30);
        clientButton.setBackground(new Color(230, 190, 50));
        selector.add(clientButton);
        frame.add(clientButton);

        JLabel addressLabel = new JLabel("This computer's address:");
        addressLabel.setForeground(Color.RED);
        addressLabel.setBounds(125, 20, 180, 30);
        addressLabel.setFont(new Font("SansSerif",1,10));
        frame.add(addressLabel);
        
        JLabel orLabel = new JLabel("OR");
        orLabel.setForeground(Color.RED);
        orLabel.setBounds(45, 65, 80, 30);
        orLabel.setFont(new Font("SansSerif",10,18));
        frame.add(orLabel);
        
        JLabel fillLabel = new JLabel("Your chat partner's address:");
        fillLabel.setForeground(Color.RED);
        fillLabel.setBounds(115, 85, 180, 30);
        fillLabel.setFont(new Font("SansSerif",1,10));
        frame.add(fillLabel);
        
        JLabel mLabel = new JLabel("Enter messages here:");
        mLabel.setForeground(Color.RED);
        mLabel.setBounds(200, 585, 180, 30);
        mLabel.setFont(new Font("SansSerif",1,10));
        frame.add(mLabel);
        
        String s;
        try {
            s = InetAddress.getLocalHost().getHostAddress();
        } catch (IOException e) {
            s = e.getMessage();
            e.printStackTrace();
        }
        JLabel myAddress = new JLabel(s);
        myAddress.setForeground(Color.RED);
        myAddress.setBounds(120, 40, 180, 30);
        myAddress.setFont(new Font("SansSerif",5,20));
        frame.add(myAddress);
        
        serverAddress = new JTextField();
        serverAddress.setBounds(120, 110, 125, 25);
        serverAddress.setFont(new Font("SansSerif",5,16));
        frame.add(serverAddress);
    }
    
    private void setStartButton() {
        startButton = new JButton("Start connection");
        startButton.setBounds(20, 170, 220, 50);
        startButton.setBackground(new Color(240, 100, 90));
        frame.add(startButton);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (connected) {
                    sb.append("Connection closed.\n");
                    connected = false;
                    afterClose();
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            beforeOpen();
                            try {
                                if (serverButton.isSelected()) {
                                    myTalker = new ServerConnection();
                                } else if (clientButton.isSelected()) {
                                    myTalker = new ClientConnection(serverAddress.getText());
                                }
                            }catch (BindException be){
                                be.printStackTrace();
                                clientButton.setSelected(true);
                                afterUnconnected();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (myTalker != null && myTalker.isConnected()) {
                                afterOpen();
                            } else {
                                afterUnconnected();
                            }
                        }
                    }.start();
                }
            }
        });
    }
    
    private void setSender(){
        sb = new StringBuilder("");
        toSend = new JTextField();
        toSend.setBounds(20, 610, 440, 30);
        toSend.setFont(new Font ("SansSerif", 20, 14));
        toSend.setEnabled(false);
        frame.add(toSend);
        
        toSend.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
                try {
                    myTalker.sendMessage(toSend.getText());
                    sb.append("Me: ").append(toSend.getText()).append("\n");
                    dialog.setText(sb.toString());
                    dialog.setCaretPosition(dialog.getDocument().getLength());
                    toSend.setText(null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    
    private void setDialogBox(){
        dialog = new JTextArea(sb.toString());
        dialog.setEditable(false);
        dialog.setFont(new Font ("SansSerif", 20, 14));
        dialog.setLineWrap(true);
        dialog.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(dialog);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBounds(20, 240, 440, 350);
        frame.add(scroll);
    }
    
    private void beforeOpen() {
        startButton.setBackground(Color.ORANGE);
        startButton.setText("Connecting...");
        serverButton.setEnabled(false);
        clientButton.setEnabled(false);
        serverAddress.setEnabled(false);
        if (serverButton.isSelected()) startButton.setEnabled(false);
        connected = true;
    }
    
    private void afterOpen(){
        //connected = true;
        startButton.setEnabled(true);
        startButton.setBackground(new Color(160, 200, 100));
        startButton.setText("Close connection");
        sb.append("Connection is OK. Start chatting!\n");
        dialog.setText(sb.toString());
        dialog.setCaretPosition(dialog.getDocument().getLength());
        toSend.setEnabled(true);
        toSend.requestFocus();
        Thread reciever = new RecievingThread();
        reciever.start();
    }
    
    private void afterUnconnected(){
        myTalker = null;
        startButton.setEnabled(true);
        startButton.setBackground(new Color(240, 100, 90));
        startButton.setText("Start connection");
        serverButton.setEnabled(true);
        clientButton.setEnabled(true);
        serverAddress.setEnabled(true);
        connected = false;
    }
    
    private void afterClose() {
        try{
            //new Socket("localhost", 2008).close();
            if (myTalker != null){
                myTalker.closeConnection();
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            myTalker = null;
            dialog.setText(sb.toString());
            dialog.setCaretPosition(dialog.getDocument().getLength());
            startButton.setBackground(new Color(240, 100, 90));
            startButton.setText("Start connection");
            serverButton.setEnabled(true);
            clientButton.setEnabled(true);
            serverAddress.setEnabled(true);
            toSend.setEnabled(false);
            connected = false;
        }
    }
        
    private class RecievingThread extends Thread{
    
        @Override
        public void run(){
            String s;
            while(connected){
                try{
                    s = myTalker.getMessage();
                    sb.append("Chat partner: ").append(s).append("\n");
                    dialog.setText(sb.toString());
                    dialog.setCaretPosition(dialog.getDocument().getLength());
                } catch (SocketException | EOFException se){
                    se.printStackTrace();
                    if (connected){
                        sb.append("Connection lost. Try again!\n");
                        afterClose();
                    }            
                } catch (IOException e){
                    e.printStackTrace();
                } 
            }
        }
    } 
}