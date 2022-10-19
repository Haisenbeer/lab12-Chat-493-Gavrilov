package com.example.lab12chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
    DatagramSocket socket;
    DatagramPacket send_packet;

    EditText txt_ip;
    EditText txt_portSend;
    EditText txt_portRecieve;
    EditText txt_message;
    EditText txt_name;

    TextView txt_chat;

    String send_s;
    String receive_s;

    ArrayList<String> lst = new ArrayList<String>();

    //Обновление окна чата
    void update_tv()
    {
        txt_chat.setText("");
        g.messages.getAllMessages(lst);

        String allReception = "";
        for(int i = 0; i < lst.size(); i++)
        {
            allReception += lst.get(i);
        }
        txt_chat.setText(allReception);
    }

    //Обновление информации о настройках
    private void update_info()
    {
        String[] newInfo = new String[4];
        g.messages.getInfo(newInfo);

        if (newInfo[0].isEmpty())
        {
            txt_name.setText("");
            txt_ip.setText("");
            txt_portSend.setText("");
            txt_portRecieve.setText("");
        }
        else
        {
            txt_name.setText(newInfo[0]);
            txt_ip.setText(newInfo[1]);
            txt_portSend.setText(newInfo[2]);
            txt_portRecieve.setText(newInfo[3]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_ip = findViewById(R.id.txt_IP);
        txt_portRecieve = findViewById(R.id.txt_PortRecieve);
        txt_portSend = findViewById(R.id.txt_PortSend);
        txt_message = findViewById(R.id.txt_Message);
        txt_name = findViewById(R.id.txt_Name);
        txt_chat = findViewById(R.id.txt_Chat);
        txt_chat.setMovementMethod(new ScrollingMovementMethod());

        g.messages = new DB(this, "Messages.db", null, 1);
        update_tv();
        update_info();

        //Порт приема
        int portRecieve;
        try
        {
            portRecieve = Integer.parseInt(txt_portRecieve.getText().toString());
        } catch (Exception e)
        {
            Toast.makeText(this, "Incorrect IP format", Toast.LENGTH_SHORT).show();
            return;
        }

        try
        {
            InetAddress local_network = InetAddress.getByName("0.0.0.0");
            SocketAddress local_address = new InetSocketAddress(local_network, portRecieve);
            socket = new DatagramSocket(local_address);
        } catch (UnknownHostException | SocketException e)
        {
            e.printStackTrace();
        }

        Runnable receiver = () -> {
            byte[] receive_buffer = new byte[500];
            DatagramPacket received_packet = new DatagramPacket(receive_buffer, receive_buffer.length);

            while (true)
            {
                try
                {
                    socket.receive(received_packet);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                receive_s = new String(receive_buffer, 0 , received_packet.getLength());

                String name;
                String message;
                String[] s = receive_s.split("%", 2);

                name = s[0];
                message = s[1];

                runOnUiThread(() ->
                {
                    String allReception = txt_chat.getText().toString();

                    int countS = allReception.split("\n").length + 1;

                    if (allReception.isEmpty())
                    {
                        countS = 1;
                    }

                    Date date = new Date();
                    SimpleDateFormat formatForDateNow = new SimpleDateFormat("yy.MM.dd hh:mm");
                    String sDate = formatForDateNow.format(date);

                    String newS = countS + " "  + sDate + " " + name + ": " + message + "\n";

                    txt_chat.setText(allReception + newS);
                    g.messages.addMessage(countS, sDate, name, message);
                });
                Log.e("MESSAGE",receive_s);
            }
        };

        Thread receiving_thread = new Thread(receiver);
        receiving_thread.start();
    }

    public void onSendClick(View v)
    {
        //Строки с именем пользвателя, сообщение и IP адресата
        String name = txt_name.getText().toString();
        String message = txt_message.getText().toString();
        String ip = txt_ip.getText().toString();

        if (name.isEmpty())
        {
            Toast.makeText(this, "Enter your nickname", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.equals(""))
        {
            message = " ";
        }

        int portSend;
        int portRecieve;
        try
        {
            portSend = Integer.parseInt(txt_portSend.getText().toString());
            portRecieve = Integer.parseInt(txt_portRecieve.getText().toString());
        } catch (Exception e)
        {
            Toast.makeText(this, "Ports incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        g.messages.saveInfo(name, ip, portSend, portRecieve);

        //Отправляемая строка, где имя отделяется от сообщения знаком процента
        send_s = name + '%' + message;

        String allReception = txt_chat.getText().toString();

        int countS = allReception.split("\n").length + 1;

        if (allReception.isEmpty())
        {
            countS = 1;
        }

        Date date = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yy.MM.dd hh:mm");
        String sDate = formatForDateNow.format(date);

        String newS = countS + " "  + sDate + " " + name + ": " + message + "\n";

        txt_chat.setText(allReception + newS);
        g.messages.addMessage(countS, sDate, name, message);

        //Превращение отправляемой строки в массив байт
        byte[] send_buffer = send_s.getBytes(StandardCharsets.UTF_8);

        try
        {
            InetAddress remote_address = InetAddress.getByName(ip);
            send_packet = new DatagramPacket(send_buffer, send_buffer.length, remote_address, portSend);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }

        send_packet.setLength(send_s.length());

        Runnable r = () -> {
            try
            {
                socket.send(send_packet);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        };

        Thread sending_thread = new Thread(r);
        sending_thread.start();
    }

    //Очистка окна чата и удаление всех сообщений из БД
    public void onClear_Click(View v)
    {
        txt_chat.setText("");
        g.messages.deleteAllMessages();
    }
}