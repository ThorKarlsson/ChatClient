//Thorgeir Karlsson 
//Simple chat client
//Computer Networking 12.02.2012

import java.io.*;
import java.net.*;

class Client {
    static Socket socket;
    static String username;
    //boolean variables used to determine in what state the client is in
    static boolean connection = false;
    static boolean loggedIn = false;

    static DataOutputStream inputToSever; //Stream used to send messages TO the server
    static DataInputStream inputFromServer; //Stream used to receive message FROM the server

    static socketMonitor sm;

    public static void main(String[] args) {
        System.out.println("Welcome to simple chat client");
        help();
        input();//this will run until user quits program
    }

	public enum command{
        CONNECT, USER, HELP, DISCONNECT, QUIT, NULL
    }
	
    //Lists available commands along with description
    public static void help() {
        System.out.println("\nCONNECT TO <address | url>   connect user to server at address with port number");
        System.out.println("USER <name>                  logs the user in with the specified name");
        System.out.println("HELP                         outputs this list of commands");
        System.out.println("DISCONNECT                   disconnect from the server");
        System.out.println("QUIT                         exit program\n");
    }

    //reads in input from user
    public static void input() {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        try {
            String message = input.readLine();
            command C = command.NULL;
            String tokens[] = message.split(" ");

            /*Converts input to command enum*/
            try{
                C = command.valueOf(tokens[0]);
            }
            catch(IllegalArgumentException e){
                C = command.NULL;
            }
            catch (ArrayIndexOutOfBoundsException e){
                C = command.NULL;
            }

            switch (C) {//Must switch on an enum however in java 7 you may switch on a string
                case DISCONNECT:
                    disconnect();
                    break;
                case QUIT:
                    if(connection){
                        disconnect();
                    }
                    quit();
                    break;
                case USER:
                    if (tokens.length == 2) {
                        if (connection == false) {
                            System.out.println("Must be connected to server before logging in");
                            break;
                        }
                        else if(loggedIn == true){
                            System.out.println("Already logged in");
                        }
                        else{
                            user(tokens[1]);
                        }
                    }
                    else{
                        System.out.println("Incorrect syntax");
                        System.out.println("USER <name>\n");
                    }
                    break;
                case HELP:
                    help();
                    break;
                case CONNECT:
                    if (tokens.length == 3 && tokens[1].equals("TO")) {
                        connect(tokens[2]);
                    } else {
                        System.out.println("Incorrect syntax");
                        System.out.println("CONNECT TO <ADDRESS | URL>\n");
                    }
                    break;
                default:
                    if (connection && loggedIn) {//if user is connected and logged in then whatever is sent is a chat
                        chat(message);
                    }
                    else if(connection){//if user is connected but not logged in then he/she must log in
                        System.out.println("You must log in before sending messages");
                    }
                    else{//If user is not typing in a command when they aren't connected then its an invalid command rather than chat error
                        System.out.println("Invalid command\n");
                    }
                    break;
            }
            input();
        } catch (IOException e) {
            System.out.println("input error\n");
            System.exit(1);
        }
    }

    //Attempts to connect user to the provided host
    public static void connect(String host) {
        try {
            if(!connection)
            {
                //establishes connection and receives message from server
                socket = new Socket(host, 8080);
                connection = true;
                //Message data structures for sending messages to and from server:
                inputFromServer = new DataInputStream(socket.getInputStream());
                inputToSever = new DataOutputStream(socket.getOutputStream());

                String fromServer;
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
                        inputFromServer));//inFromServer receives messages from the server into its stream
                inputToSever.writeBytes(" ");
                fromServer = inFromServer.readLine();
                System.out.println(fromServer);

                //start thread for monitoring output from server
                sm = new socketMonitor(inFromServer);//sends in buffered reader which is bound to the socket to monitor
                sm.start();

            }
            else
            {
                System.out.println("Connection already established");
            }

        } catch (IOException ex) {
            System.out.println("Error connecting to server");
        }
    }

    //Closes socket if it is open
    public static void disconnect() {
        if (connection) {
            try {
                socket.close();//terminates connection
                connection = false;
                loggedIn = false;
                System.out.println("Disconnected from host\n");
            } catch (IOException ex) {
                System.out.println("error terminating connection\n");
            }
        } else {
            System.out.println("No open connection to terminate\n");
        }
    }

    //Attempts to log user in
    public static void user(String name) {
        if (connection) {
            username = name;
            loggedIn = true;

            try {//sends command USER <username> into server
                inputToSever.writeBytes("USER " + username + '\n');

            } catch (IOException e) {
                System.out.println("An unknown error occurred\n");
                System.exit(-1);
            }
        }
        else {
            System.out.println("Must be connected to server in order to log in\n");
        }
    }

    //User chats within this function until one of the commands is called
    public static void chat(String message) {
        try {//Sends whatever user is typed into server
            inputToSever.writeBytes(message + '\n');

        } catch (IOException e) {
            System.out.println("unknown connection error\n");
        }
    }

    //Disconnects from server then exits
    public static void quit() {
        System.exit(1);
    }
}